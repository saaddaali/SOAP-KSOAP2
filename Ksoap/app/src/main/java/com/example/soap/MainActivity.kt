package com.example.soap

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorest.R
import com.example.soap.modele.Compte
import com.example.soap.modele.TypeCompte
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.util.Vector
import java.util.concurrent.Executors
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind


class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var compteAdapter: CompteAdapter

    private val METHOD_GET_COMPTES = "getAllComptes"

    private val NAMESPACE = "http://controllers.tprestcontroller.example.com/" // Web service namespace

    private val URL = "http://10.0.2.2:8085/services/ws" // URL for your backend endpoint (adjust if needed)

    private val SOAP_ACTION_GET_COMPTES = ""

    private val METHOD_CREATE = "createCompte" // Method for creating a compte

    private val METHOD_GET = "getCompteById" // Method for getting a compte by ID

    private val SOAP_ACTION_CREATE = "$NAMESPACE$METHOD_CREATE" // SOAP action for createCompte method

    private val SOAP_ACTION_GET = "$NAMESPACE$METHOD_GET" // SOAP action for getCompteById method



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Using regular Button
        val btnCreateCompte = findViewById<Button>(R.id.btn_create_compte)
        btnCreateCompte.setOnClickListener {
            compteAdapter.showCreateDialog(this)
        }

        // Initialize adapter
        compteAdapter = CompteAdapter(this, mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = compteAdapter

        // Load comptes using SOAP
        loadComptes()
    }


    private fun loadComptes() {
        // Perform SOAP request in a background thread
        Executors.newSingleThreadExecutor().execute {
            try {
                val comptes = getAllComptesFromSoap()
                runOnUiThread {
                    if (comptes.isNotEmpty()) {
                         compteAdapter.updateData(comptes)  // Update the adapter with new data
                    } else {
                        Toast.makeText(this@MainActivity, "No comptes found.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun getAllComptesFromSoap(): List<Compte> {
        val request = SoapObject(NAMESPACE, METHOD_GET_COMPTES)
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            setOutputSoapObject(request)
        }

        val httpTransport = HttpTransportSE(URL)

        return try {
            // Call the SOAP web service
            httpTransport.call(SOAP_ACTION_GET_COMPTES, envelope)

            // Check if the response is a Vector or a single SoapObject
            val response = envelope.response

            val comptes = mutableListOf<Compte>()

            when (response) {
                is Vector<*> -> {
                    // Iterate through the collection of objects
                    for (item in response) {
                        if (item is SoapObject) {
                            comptes.add(parseCompte(item))
                        }
                    }
                }

                is SoapObject -> {
                    // Single response case
                    comptes.add(parseCompte(response))
                }

                else -> {
                    Log.e("SOAP", "Unexpected response type: ${response::class.java.name}")
                }
            }

            comptes
        } catch (e: Exception) {
            Log.e("SOAP", "Error calling SOAP web service: ${e.message}", e)
            emptyList() // Return empty list on failure
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun parseCompte(soapObject: SoapObject): Compte {
        val id = try {
            soapObject.getProperty("id")?.toString()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            Log.e("SOAP", "Error fetching id", e)
            0
        }

        val solde = try {
            soapObject.getProperty("solde")?.toString()?.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            Log.e("SOAP", "Error fetching solde", e)
            0.0
        }

        val dateCreation = try {
            soapObject.getProperty("dateCreation")?.toString()
        } catch (e: Exception) {
            Log.e("SOAP", "Error fetching dateCreation", e)
            null
        }

        val type = try {
            val typeString = soapObject.getProperty("typeCompte")?.toString()
            TypeCompte.valueOf(typeString ?: "UNKNOWN")
        } catch (e: IllegalArgumentException) {
            Log.e("SOAP", "Invalid TypeCompte value", e)
            InvocationKind.UNKNOWN // Ensure a fallback for the enum exists
        }

        // Assuming your Compte class includes these fields
        return Compte( solde , type)
    }




}
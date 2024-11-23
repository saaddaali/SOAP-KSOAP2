package com.example.soap

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorest.R
import com.example.soap.modele.Compte
import com.example.soap.modele.TypeCompte
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.PropertyInfo
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import kotlin.math.log

class CompteAdapter(private val context: Context, private var comptes: MutableList<Compte>) :
    RecyclerView.Adapter<CompteAdapter.CompteViewHolder>() {

    // Define ViewHolder inside the adapter
    class CompteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val solde: TextView = itemView.findViewById(R.id.solde)
        val dateCreation: TextView = itemView.findViewById(R.id.dateCreation)
        val typeCompte: TextView = itemView.findViewById(R.id.typeCompte)
        val btnUpdate: Button = itemView.findViewById(R.id.updateButton)
        val btnDelete: Button = itemView.findViewById(R.id.deleteButton)
    }
    private val NAMESPACE = "http://controllers.tprestcontroller.example.com/"
    private val URL = "http://10.0.2.2:8085/services/ws"
    private val SOAP_ACTION_GET_COMPTES = "http://controllers.tprestcontroller.example.com/getComptes"
    private val SOAP_ACTION_CREATE_COMPTES = ""
    private val SOAP_ACTION = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compte, parent, false)
        return CompteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        try {
            val compte = comptes[position]
            with(holder) {
                solde.text = compte.solde.toString()
                typeCompte.text = compte.typeCompte.name

                // Setup Update button click listener
                btnUpdate.setOnClickListener {
                    // showUpdateDialog(compte, holder.itemView)
                }

                // Setup Delete button click listener
                btnDelete.setOnClickListener {
                     showDeleteDialog(compte, holder.itemView, position)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error in onBindViewHolder: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    override fun getItemCount(): Int = comptes.size
    suspend fun deleteCompteSoap(id: Long, position: Int) {
        try {
            val soapRequest = SoapObject(NAMESPACE, "deleteCompte")

            // Add ID as a property
            val idProp = PropertyInfo().apply {
                name = "id"
                type = Long::class.java // Match the expected type
                setValue(id)
            }
            soapRequest.addProperty(idProp)

            // Prepare the envelope and transport
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.setOutputSoapObject(soapRequest)
            val transport = HttpTransportSE(URL)

            // Perform the SOAP call
            transport.call(SOAP_ACTION, envelope)

            val response = envelope.response
            Log.d("DeleteCompte", "Server Response: $response")

            // Validate response
            withContext(Dispatchers.Main) {
                if (response == true || response.toString() == "true") { // Check if response indicates success
                    comptes.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, comptes.size) // Refresh remaining items

                    Toast.makeText(context, "Compte deleted successfully (SOAP)", Toast.LENGTH_SHORT).show()
                } else {
                    throw Exception("Failed to delete compte. Server response: $response")
                }
            }
        } catch (e: Exception) {
            Log.e("DeleteCompte", "Error during SOAP request", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error deleting compte: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    suspend fun createCompteSoap(compte: Compte) {
        val soapRequest = SoapObject(NAMESPACE, "createCompte")

        // Solde property
        val soldeProp = PropertyInfo()
        soldeProp.name = "solde"
        soldeProp.type = Double::class.java // Corrected type
        soldeProp.setValue(compte.solde.toString()) // Converts the Double to String

        soapRequest.addProperty(soldeProp)

        // TypeCompte property
        val typeProp = PropertyInfo()
        typeProp.name = "typeCompte"
        typeProp.type = String::class.java
        typeProp.setValue(compte.typeCompte.name)
        soapRequest.addProperty(typeProp)

        // Create envelope and set request
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.setOutputSoapObject(soapRequest)

        // Set up transport
        val transport = HttpTransportSE(URL)
        try {
            transport.call(SOAP_ACTION_CREATE_COMPTES, envelope)
            val response = envelope.response

            withContext(Dispatchers.Main) {
                if (response != null) {
                    if (response is SoapObject) {
                        val createdCompte = Compte(
                            solde = response.getProperty("solde").toString().toDouble(),
                            typeCompte = TypeCompte.valueOf(response.getProperty("typeCompte").toString())
                        )
                        comptes.add(createdCompte)
                    }
                    notifyItemInserted(comptes.size - 1)
                    Toast.makeText(context, "Compte created successfully (SOAP)", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error creating compte: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    // Show a dialog for creating a new compte
    fun showCreateDialog(context: Context) {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_compte, null)
        var dialog: AlertDialog? = null

        // Initialize fields
        val etSolde = dialogView.findViewById<EditText>(R.id.create_solde)
        // Create dialog
        dialog = AlertDialog.Builder(context)
            .setTitle("Create New Compte")
            .setView(dialogView)
            .setPositiveButton("Create", null) // Set to null initially
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Show dialog and setup button after
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val solde = etSolde.text.toString().toDoubleOrNull()
            if (solde != null) {
                val newCompte = Compte( solde, TypeCompte.COURANT)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        createCompteSoap(newCompte)

                        // Update UI on the main thread
                        withContext(Dispatchers.Main) {
                            dialog.dismiss()
                            Toast.makeText(context, "Compte created successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Handle error on the main thread
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error creating compte: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }else {
                Toast.makeText(context, "Invalid solde value", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showDeleteDialog(compte: Compte, itemView: View, position: Int) {
        AlertDialog.Builder(itemView.context)
            .setTitle("Delete Compte")
            .setMessage("Are you sure you want to delete this compte?")
            .setPositiveButton("Delete") { dialog, _ ->
                compte.id=3L
                Log.d("DeleteCompte", "Compte before dialog: $compte")
                compte.id?.let { id ->
                    Log.d("DeleteCompte", "Deleting compte with ID: $id")
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("DeleteCompte", "Deleting compte in background")
                        try {
                            deleteCompteSoap(id.toLong(), position) // Ensure correct ID type
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(itemView.context, "Error deleting compte: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } ?: run {
                    Log.e("DeleteCompte", "Invalid compte ID")
                    Toast.makeText(itemView.context, "Invalid compte ID. Cannot delete.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }




    // Method to update data in the adapter
    fun updateData(newComptes: List<Compte>) {
        comptes.clear() // Clear the existing data
        comptes.addAll(newComptes) // Add the new data
        notifyDataSetChanged() // Notify the adapter to refresh the views
    }
}

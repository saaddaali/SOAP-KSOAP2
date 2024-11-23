package com.example.soap.modele

import org.ksoap2.serialization.KvmSerializable
import org.ksoap2.serialization.PropertyInfo
import java.util.Hashtable

class Compte : KvmSerializable {

    var id: Long? = null
    var solde: Double = 0.0
    var typeCompte: TypeCompte = TypeCompte.COURANT


    constructor()

    constructor(solde: Double, typeCompte: Enum<*>) {
        this.solde = solde
        this.typeCompte = typeCompte as TypeCompte
    }

    override fun getProperty(index: Int): Any? {
        return when (index) {
            0 -> id
            1 -> solde

            3 -> typeCompte.toString() // Convert Enum to String for SOAP
            else -> null
        }
    }

    override fun getPropertyCount(): Int = 4

    override fun setProperty(index: Int, value: Any?) {
        when (index) {
            0 -> id = value as? Long
            1 -> solde = value as Double

            3 -> typeCompte = TypeCompte.valueOf(value as String) // Convert String to Enum
        }
    }

    override fun getPropertyInfo(index: Int, properties: Hashtable<*, *>?, info: PropertyInfo?) {
        when (index) {
            0 -> {
                if (info != null) {
                    info.name = "id"
                }
                if (info != null) {
                    info.type = Long::class.java
                }
            }
            1 -> {
                if (info != null) {
                    info.name = "solde"
                }
                if (info != null) {
                    info.type = Double::class.java
                }
            }
            2 -> {
                if (info != null) {
                    info.name = "dateCreation"
                }
                if (info != null) {
                    info.type = String::class.java
                }
            }
            3 -> {
                if (info != null) {
                    info.name = "typeCompte"
                }
                if (info != null) {
                    info.type = String::class.java
                }
            }
        }
    }
}

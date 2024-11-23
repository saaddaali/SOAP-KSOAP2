package com.example.soap.modele


enum class TypeCompte {
    COURANT,
    EPARGNE;

    companion object {
        @JvmStatic
        fun fromString(value: String): TypeCompte {
            return when(value.toUpperCase()) {
                "COURANT" -> COURANT
                "EPARGNE" -> EPARGNE
                else -> throw IllegalArgumentException("Unknown TypeCompte: $value")
            }
        }
    }
}

package com.example.examen2

data class User(
    val uid: String = "",
    val email: String = "",
    var name: String = "",
    var address: String = "",
    var phone: String = "",
    val role: String = "normal" // "normal" o "admin"
) {
    // Constructor sin argumentos requerido para Firestore
    constructor() : this("", "", "", "", "", "")
}
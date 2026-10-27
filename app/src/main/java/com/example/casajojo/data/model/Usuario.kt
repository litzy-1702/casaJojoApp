package com.example.casajojo.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["username"], unique = true)
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombreCompleto: String,
    val rut: String,
    val correo: String,
    val telefono: String,

    val username: String,
    val rol: String,

    val passwordSalt: String,
    val passwordHash: String,

    val activo: Boolean = true,
    val debeCambiarPassword: Boolean = true
)
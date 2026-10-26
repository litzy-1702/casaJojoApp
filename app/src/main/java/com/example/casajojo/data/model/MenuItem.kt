package com.example.casajojo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val disponible: Boolean = true,
    val imagenUri: String? = null,
    val fechaMenu: String
)

package com.example.casajojo.data.network

import com.example.casajojo.data.model.MenuItem

data class MenuDto(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val disponible: Boolean = true,
    val imagenUri: String? = null,
    val fechaMenu: String
)

fun MenuDto.toEntity() = MenuItem(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    disponible = disponible,
    imagenUri = imagenUri,
    fechaMenu = fechaMenu
)

fun MenuItem.toDto() = MenuDto(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    disponible = disponible,
    imagenUri = imagenUri,
    fechaMenu = fechaMenu
)
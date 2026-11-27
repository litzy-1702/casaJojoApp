package com.casajojo.menuservice.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * Plato del menú del día.
 * Tiene los mismos campos que MenuItem de la app Android.
 */
@Entity
@Table(name = "menu_items")
class MenuItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var nombre: String = "",

    @Column(nullable = false, length = 500)
    var descripcion: String = "",

    @Column(nullable = false)
    var precio: Int = 0,

    @Column(nullable = false)
    var disponible: Boolean = true,

    @Column(length = 1000)
    var imagenUri: String? = null,

    // Formato YYYY-MM-DD, igual que en la app
    @Column(nullable = false, length = 10)
    var fechaMenu: String = ""
)

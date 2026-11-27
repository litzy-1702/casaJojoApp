package com.casajojo.menuservice.repository

import com.casajojo.menuservice.model.MenuItem
import org.springframework.data.jpa.repository.JpaRepository

interface MenuItemRepository : JpaRepository<MenuItem, Int> {

    // Equivale a observarPorFecha() de la app: platos de una fecha ordenados por nombre
    fun findByFechaMenuOrderByNombreAsc(fechaMenu: String): List<MenuItem>
}

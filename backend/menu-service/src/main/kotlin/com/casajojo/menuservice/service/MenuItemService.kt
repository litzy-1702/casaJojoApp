package com.casajojo.menuservice.service

import com.casajojo.menuservice.dto.MenuItemRequest
import com.casajojo.menuservice.model.MenuItem
import com.casajojo.menuservice.repository.MenuItemRepository
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class MenuItemService(
    private val repository: MenuItemRepository
) {

    fun listar(fecha: String?): List<MenuItem> =
        if (fecha.isNullOrBlank()) {
            repository.findAll(Sort.by("fechaMenu", "nombre"))
        } else {
            repository.findByFechaMenuOrderByNombreAsc(fecha)
        }

    fun obtener(id: Int): MenuItem =
        repository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el plato con id $id")
        }

    fun crear(request: MenuItemRequest): MenuItem {
        val item = MenuItem(
            nombre = request.nombre!!.trim(),
            descripcion = request.descripcion!!.trim(),
            precio = request.precio!!,
            disponible = request.disponible,
            imagenUri = request.imagenUri,
            fechaMenu = request.fechaMenu!!
        )
        return repository.save(item)
    }

    fun actualizar(id: Int, request: MenuItemRequest): MenuItem {
        val item = obtener(id)
        item.nombre = request.nombre!!.trim()
        item.descripcion = request.descripcion!!.trim()
        item.precio = request.precio!!
        item.disponible = request.disponible
        item.imagenUri = request.imagenUri
        item.fechaMenu = request.fechaMenu!!
        return repository.save(item)
    }

    fun cambiarDisponibilidad(id: Int, disponible: Boolean): MenuItem {
        val item = obtener(id)
        item.disponible = disponible
        return repository.save(item)
    }

    fun eliminar(id: Int) {
        val item = obtener(id)
        repository.delete(item)
    }
}

package com.casajojo.menuservice.controller

import com.casajojo.menuservice.dto.DisponibilidadRequest
import com.casajojo.menuservice.dto.MenuItemRequest
import com.casajojo.menuservice.model.MenuItem
import com.casajojo.menuservice.service.MenuItemService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/menu")
class MenuItemController(
    private val service: MenuItemService
) {

    @GetMapping
    fun listar(@RequestParam(required = false) fecha: String?): List<MenuItem> =
        service.listar(fecha)

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Int): MenuItem =
        service.obtener(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody request: MenuItemRequest): MenuItem =
        service.crear(request)

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Int,
        @Valid @RequestBody request: MenuItemRequest
    ): MenuItem =
        service.actualizar(id, request)

    @PatchMapping("/{id}/disponibilidad")
    fun cambiarDisponibilidad(
        @PathVariable id: Int,
        @Valid @RequestBody request: DisponibilidadRequest
    ): MenuItem =
        service.cambiarDisponibilidad(id, request.disponible!!)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(@PathVariable id: Int) {
        service.eliminar(id)
    }
}

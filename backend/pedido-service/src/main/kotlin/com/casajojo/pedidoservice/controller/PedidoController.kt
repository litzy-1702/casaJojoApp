package com.casajojo.pedidoservice.controller

import com.casajojo.pedidoservice.dto.EstadoRequest
import com.casajojo.pedidoservice.dto.PagoRequest
import com.casajojo.pedidoservice.dto.PedidoRequest
import com.casajojo.pedidoservice.model.EstadoPedido
import com.casajojo.pedidoservice.model.Pedido
import com.casajojo.pedidoservice.service.PedidoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pedidos")
class PedidoController(
    private val service: PedidoService
) {

    @GetMapping
    fun listar(@RequestParam(required = false) estado: EstadoPedido?): List<Pedido> =
        service.listar(estado)

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Int): Pedido =
        service.obtener(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody request: PedidoRequest): Pedido =
        service.crear(request)

    @PatchMapping("/{id}/estado")
    fun cambiarEstado(
        @PathVariable id: Int,
        @Valid @RequestBody request: EstadoRequest
    ): Pedido =
        service.cambiarEstado(id, request.estado!!)

    @PatchMapping("/{id}/pago")
    fun cambiarPago(
        @PathVariable id: Int,
        @Valid @RequestBody request: PagoRequest
    ): Pedido =
        service.cambiarPago(id, request.pagado!!)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(@PathVariable id: Int) {
        service.eliminar(id)
    }
}

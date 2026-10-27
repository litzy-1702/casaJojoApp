package com.example.casajojo.feature.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.casajojo.R
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.feature.operations.EstadoPedido

@Composable
fun EstadoPedidoScreen(
    viewModel: ClienteViewModel,
    onVolverAlMenu: () -> Unit
) {
    val pedido by viewModel.pedidoSeleccionado.collectAsState()
    val idSeleccionado by viewModel.pedidoSeleccionadoId.collectAsState()
    val historial by viewModel.historial.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EncabezadoCliente(
            titulo = "Estado de tu pedido",
            subtitulo = "Se actualiza automáticamente cuando Caja o Cocina avanzan tu pedido."
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "pedido-actual") {
                val actual = pedido
                when {
                    actual != null -> TarjetaEstadoPedido(pedido = actual)

                    idSeleccionado != null -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Cargando tu pedido…")
                            }
                        }
                    }

                    historial.isEmpty() -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Todavía no has realizado pedidos.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = "Toca un pedido del historial para ver su estado.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (historial.isNotEmpty()) {
                item(key = "titulo-historial") {
                    Text(
                        text = "Pedidos recientes en este dispositivo",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(
                    items = historial,
                    key = { p -> "historial-${p.id}" }
                ) { p ->
                    FilaHistorial(
                        pedido = p,
                        seleccionado = p.id == idSeleccionado,
                        onClick = { viewModel.seleccionarPedido(p.id) }
                    )
                }
            }
        }

        Button(
            onClick = onVolverAlMenu,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú")
        }
    }
}

@Composable
private fun TarjetaEstadoPedido(pedido: Pedido) {
    val colorEstado by animateColorAsState(
        targetValue = colorDeEstado(pedido.estado),
        animationSpec = tween(durationMillis = 500),
        label = "colorEstado"
    )
    val progreso by animateFloatAsState(
        targetValue = progresoDeEstado(pedido.estado),
        animationSpec = tween(durationMillis = 600),
        label = "progresoEstado"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${pedido.id}",
                    style = MaterialTheme.typography.titleLarge
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colorEstado
                ) {
                    Text(
                        text = pedido.estado.etiqueta,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth()
            )

            Text("A nombre de: ${pedido.clienteNombre}")
            Text("Detalle: ${pedido.detalleItems}")
            Text("Retiro: ${pedido.horaComprometida}")

            if (pedido.restricciones.isNotBlank()) {
                Text("Observaciones: ${pedido.restricciones}")
            }

            Text(
                text = "Total: ${formatearPesos(pedido.total)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            AnimatedVisibility(visible = pedido.estado == EstadoPedido.LISTO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cliente_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¡Tu pedido está listo para retiro!",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaHistorial(
    pedido: Pedido,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${pedido.id} · ${pedido.clienteNombre}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Retiro: ${pedido.horaComprometida} · ${formatearPesos(pedido.total)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = pedido.estado.etiqueta,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun colorDeEstado(estado: EstadoPedido): Color {
    return when (estado) {
        EstadoPedido.RECIBIDO -> MaterialTheme.colorScheme.secondaryContainer
        EstadoPedido.EN_PREPARACION -> MaterialTheme.colorScheme.tertiaryContainer
        EstadoPedido.LISTO -> MaterialTheme.colorScheme.primaryContainer
        EstadoPedido.ENTREGADO -> MaterialTheme.colorScheme.surfaceVariant
        EstadoPedido.CANCELADO -> MaterialTheme.colorScheme.errorContainer
    }
}

private fun progresoDeEstado(estado: EstadoPedido): Float {
    return when (estado) {
        EstadoPedido.RECIBIDO -> 0.33f
        EstadoPedido.EN_PREPARACION -> 0.66f
        EstadoPedido.LISTO -> 1f
        EstadoPedido.ENTREGADO -> 1f
        EstadoPedido.CANCELADO -> 0f
    }
}

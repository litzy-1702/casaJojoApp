
package com.example.casajojo.feature.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.feature.operations.EstadoPedido
import com.example.casajojo.feature.operations.OperationsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun AdminPedidosScreen(
    operationsViewModel: OperationsViewModel
) {
    val pedidos by operationsViewModel.pedidos.collectAsState()

    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    var estadoSeleccionado by remember {
        mutableStateOf<EstadoPedido?>(null)
    }

    var filtroFecha by rememberSaveable {
        mutableStateOf("TODOS")
    }

    var filtroPago by rememberSaveable {
        mutableStateOf("TODOS")
    }

    val zonaHoraria = remember {
        ZoneId.systemDefault()
    }

    val hoy = LocalDate.now()

    val pedidosFiltrados = pedidos.filter { pedido ->

        val texto = busqueda.trim()

        val coincideBusqueda =
            texto.isBlank() ||
                    pedido.id.toString().contains(texto, ignoreCase = true) ||
                    pedido.clienteNombre.contains(texto, ignoreCase = true) ||
                    pedido.clienteTelefono.contains(texto, ignoreCase = true)

        val fechaPedido = Instant
            .ofEpochMilli(pedido.horaCreacion)
            .atZone(zonaHoraria)
            .toLocalDate()

        val coincideFecha = when (filtroFecha) {
            "HOY" -> fechaPedido == hoy
            else -> true
        }

        val coincideEstado =
            estadoSeleccionado == null ||
                    pedido.estado == estadoSeleccionado

        val coincidePago = when (filtroPago) {
            "PAGADOS" -> pedido.pagado
            "PENDIENTES" -> !pedido.pagado
            else -> true
        }

        coincideBusqueda &&
                coincideFecha &&
                coincideEstado &&
                coincidePago
    }.sortedByDescending { it.horaCreacion }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Gestión de pedidos",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Consulta los pedidos registrados, sus estados y pagos.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Mostrando ${pedidosFiltrados.size} " +
                            "de ${pedidos.size} pedidos",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        item {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Buscar pedido, cliente o teléfono")
                },
                singleLine = true
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Fecha",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroFecha == "TODOS",
                        onClick = { filtroFecha = "TODOS" },
                        label = { Text("Todas las fechas") }
                    )

                    FilterChip(
                        selected = filtroFecha == "HOY",
                        onClick = { filtroFecha = "HOY" },
                        label = { Text("Hoy") }
                    )
                }
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Estado del pedido",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = estadoSeleccionado == null,
                        onClick = { estadoSeleccionado = null },
                        label = { Text("Todos") }
                    )

                    EstadoPedido.values().forEach { estado ->
                        FilterChip(
                            selected = estadoSeleccionado == estado,
                            onClick = {
                                estadoSeleccionado = estado
                            },
                            label = {
                                Text(estado.etiqueta)
                            }
                        )
                    }
                }
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Estado del pago",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroPago == "TODOS",
                        onClick = { filtroPago = "TODOS" },
                        label = { Text("Todos") }
                    )

                    FilterChip(
                        selected = filtroPago == "PAGADOS",
                        onClick = { filtroPago = "PAGADOS" },
                        label = { Text("Pagados") }
                    )

                    FilterChip(
                        selected = filtroPago == "PENDIENTES",
                        onClick = { filtroPago = "PENDIENTES" },
                        label = { Text("Pendientes de pago") }
                    )
                }
            }
        }

        if (pedidosFiltrados.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No se encontraron pedidos con estos filtros.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(
                items = pedidosFiltrados,
                key = { pedido -> pedido.id }
            ) { pedido ->
                PedidoAdministrativoCard(pedido)
            }
        }
    }
}

@Composable
private fun PedidoAdministrativoCard(
    pedido: Pedido
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = "Pedido #${pedido.id}",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Cliente: ${pedido.clienteNombre}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Teléfono: ${
                    pedido.clienteTelefono.ifBlank {
                        "No informado"
                    }
                }"
            )

            Text(
                text = "Productos: ${pedido.detalleItems}"
            )

            if (pedido.restricciones.isNotBlank()) {
                Text(
                    text = "Observaciones: ${pedido.restricciones}"
                )
            }

            Text(
                text = "Creado: ${formatearFechaPedido(pedido.horaCreacion)}"
            )

            Text(
                text = "Retiro solicitado: ${pedido.horaComprometida}"
            )

            Text(
                text = "Estado: ${pedido.estado.etiqueta}",
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = if (pedido.pagado) {
                    "Pago: Pagado (${pedido.metodoPago})"
                } else {
                    "Pago: Pendiente"
                }
            )

            Text(
                text = "Total: ${formatearTotalPedido(pedido.total)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatearTotalPedido(total: Int): String {
    val formato = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("es-CL")
    )

    return "\$${formato.format(total)}"
}

private fun formatearFechaPedido(timestamp: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.forLanguageTag("es-CL")
    ).format(Date(timestamp))
}


package com.example.casajojo.feature.admin


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.feature.operations.EstadoPedido
import com.example.casajojo.feature.operations.OperationsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    operationsViewModel: OperationsViewModel,
    onGestionarMenu: () -> Unit,
    onGestionarUsuarios: () -> Unit,
    onGestionarPedidos: () -> Unit
) {
    val pedidos by operationsViewModel.pedidos.collectAsState()
    val menu by adminViewModel.menuItems.collectAsState()

    val hoy = LocalDate.now()
    val zona = ZoneId.systemDefault()

    val inicioHoy = hoy
        .atStartOfDay(zona)
        .toInstant()
        .toEpochMilli()

    val inicioManana = hoy
        .plusDays(1)
        .atStartOfDay(zona)
        .toInstant()
        .toEpochMilli()

    val pedidosHoy = pedidos.filter {
        it.horaCreacion >= inicioHoy &&
                it.horaCreacion < inicioManana
    }

    val pendientes = pedidos.count {
        it.estado == EstadoPedido.RECIBIDO
    }

    val enPreparacion = pedidos.count {
        it.estado == EstadoPedido.EN_PREPARACION
    }

    val listos = pedidos.count {
        it.estado == EstadoPedido.LISTO
    }

    val ventasHoy = pedidosHoy
        .filter {
            it.pagado && it.estado != EstadoPedido.CANCELADO
        }
        .sumOf { it.total }

    val platosAgotados = menu.filter {
        !it.disponible
    }

    val pedidosRecientes = pedidosHoy
        .sortedByDescending { it.horaCreacion }
        .take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Panel principal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Casa Jo-Jo · Resumen del ${
                        hoy.format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardMetricCard(
                    title = "Pedidos recibidos hoy",
                    value = pedidosHoy.size.toString(),
                    modifier = Modifier.weight(1f)
                )

                DashboardMetricCard(
                    title = "Pendientes",
                    value = pendientes.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardMetricCard(
                    title = "En preparación",
                    value = enPreparacion.toString(),
                    modifier = Modifier.weight(1f)
                )

                DashboardMetricCard(
                    title = "Listos para retirar",
                    value = listos.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Ventas cobradas hoy",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = formatearPrecioDashboard(ventasHoy),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Suma de pedidos pagados y no cancelados.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Disponibilidad del menú",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Platos marcados como agotados: " +
                                platosAgotados.size,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    when {
                        menu.isEmpty() -> {
                            Text(
                                text = "No hay platos registrados " +
                                        "para el menú de hoy."
                            )
                        }

                        platosAgotados.isEmpty() -> {
                            Text(
                                text = "No hay platos marcados como agotados."
                            )
                        }

                        else -> {
                            platosAgotados.take(5).forEach { plato ->
                                Text("• ${plato.nombre}")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Últimos pedidos de hoy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (pedidosRecientes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Todavía no se han recibido pedidos hoy.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(
                items = pedidosRecientes,
                key = { pedido -> pedido.id }
            ) { pedido ->
                PedidoRecienteCard(pedido)
            }
        }

        item {
            Button(
                onClick = onGestionarMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Administrar menú")
            }
        }

        item {
            OutlinedButton(
                onClick = onGestionarPedidos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestionar pedidos")
            }
        }

        item {
            OutlinedButton(
                onClick = onGestionarUsuarios,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestionar usuarios")
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PedidoRecienteCard(pedido: Pedido) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Pedido #${pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = pedido.clienteNombre,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${formatearHoraDashboard(pedido.horaCreacion)}" +
                            " · ${pedido.estado.etiqueta}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = formatearPrecioDashboard(pedido.total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatearPrecioDashboard(valor: Int): String {
    val formato = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("es-CL")
    )

    return "\$" + formato.format(valor)
}

private fun formatearHoraDashboard(timestamp: Long): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.forLanguageTag("es-CL")
    ).format(Date(timestamp))
}

package com.example.casajojo.feature.caja

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.feature.operations.EstadoPedido
import com.example.casajojo.feature.operations.OperationsViewModel

@Composable
fun CajaScreen(viewModel: OperationsViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Control de Caja",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gestión de comandas y cobros",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(onClick = { viewModel.crearPedidoDemo() }) {
                Text("+ Demo")
            }
        }

        if (pedidos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay comandas registradas en el sistema.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pedidos, key = { it.id }) { pedido ->
                    ComandaCajaCard(pedido = pedido, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ComandaCajaCard(
    pedido: Pedido,
    viewModel: OperationsViewModel
) {
    val atrasado = viewModel.estaAtrasado(pedido.horaComprometida, pedido.estado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (atrasado) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = "Pedido #${pedido.id} - ${pedido.clienteNombre}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = if (atrasado) "ATRASADO" else "A TIEMPO",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            Text(text = "Detalle: ${pedido.detalleItems}")

            if (pedido.restricciones.isNotBlank()) {
                Text(
                    text = "Restricción: ${pedido.restricciones}",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Hora Pedido: ${viewModel.formatearHora(pedido.horaCreacion)}")
                Text(
                    text = "Hora Compromiso: ${pedido.horaComprometida}",
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $${pedido.total}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                FilterChip(
                    selected = pedido.pagado,
                    onClick = { viewModel.cambiarEstadoPago(pedido.id, !pedido.pagado) },
                    label = { Text(if (pedido.pagado) "PAGADO" else "PENDIENTE PAGO") }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text(text = "Cambiar Estado:", fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EstadoPedido.values().forEach { estado ->
                    OutlinedButton(
                        onClick = { viewModel.cambiarEstadoPedido(pedido, estado) },
                        enabled = pedido.estado != estado,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = estado.etiqueta.take(6) + "..",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
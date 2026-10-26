package com.example.casajojo.feature.cocina

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.feature.operations.EstadoPedido
import com.example.casajojo.feature.operations.OperationsViewModel

@Composable
fun CocinaScreen(viewModel: OperationsViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()
    val comandasCocina = pedidos.filter {
        it.estado == EstadoPedido.RECIBIDO || it.estado == EstadoPedido.EN_PREPARACION
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Pantalla de Cocina",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Comandas en cola: ${comandasCocina.size}",
            style = MaterialTheme.typography.titleMedium
        )

        if (comandasCocina.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin comandas pendientes en cocina")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(comandasCocina, key = { it.id }) { pedido ->
                    ComandaCocinaCard(pedido = pedido, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ComandaCocinaCard(
    pedido: Pedido,
    viewModel: OperationsViewModel
) {
    val atrasado = viewModel.estaAtrasado(pedido.horaComprometida, pedido.estado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (atrasado) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.secondaryContainer
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
                    text = "Comanda #${pedido.id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Badge(
                    containerColor = if (atrasado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = if (atrasado) "ATRASADO" else "A TIEMPO",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Text(
                text = "Hora Entrega: ${pedido.horaComprometida}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            Text(
                text = pedido.detalleItems,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            if (pedido.restricciones.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "NOTA / RESTRICCIÓN:\n${pedido.restricciones}",
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pedido.estado == EstadoPedido.RECIBIDO) {
                    Button(
                        onClick = { viewModel.cambiarEstadoPedido(pedido, EstadoPedido.EN_PREPARACION) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cocinar")
                    }
                } else if (pedido.estado == EstadoPedido.EN_PREPARACION) {
                    Button(
                        onClick = { viewModel.cambiarEstadoPedido(pedido, EstadoPedido.LISTO) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Marcar Listo")
                    }
                }
            }
        }
    }
}
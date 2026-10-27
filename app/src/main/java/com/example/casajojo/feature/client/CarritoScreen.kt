
package com.example.casajojo.feature.client

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CarritoScreen(
    viewModel: ClienteViewModel,
    onSeguirEligiendo: () -> Unit,
    onContinuar: () -> Unit
) {
    val carrito by viewModel.carrito.collectAsState()
    val total by viewModel.total.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EncabezadoCliente(
            titulo = "Tu carrito",
            subtitulo =
                "Revisa los platos y las cantidades antes de confirmar."
        )

        if (carrito.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tu carrito está vacío",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Agrega platos desde el menú del día.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = carrito,
                    key = { linea -> linea.lineaId }
                ) { linea ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = linea.descripcionCompleta,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text =
                                    "${formatearPesos(linea.precioUnitario)} c/u",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                SelectorCantidad(
                                    cantidad = linea.cantidad,
                                    onQuitar = {
                                        viewModel.disminuirCantidad(
                                            linea.lineaId
                                        )
                                    },
                                    onAgregar = {
                                        viewModel.aumentarCantidad(
                                            linea.lineaId
                                        )
                                    }
                                )

                                Text(
                                    text = formatearPesos(linea.subtotal),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            TextButton(
                                onClick = {
                                    viewModel.quitarPlato(linea.lineaId)
                                }
                            ) {
                                Text(
                                    text = "Quitar del carrito",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium
                    )

                    AnimatedContent(
                        targetState = total,
                        label = "total"
                    ) { valor ->
                        Text(
                            text = formatearPesos(valor),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            Button(
                onClick = onContinuar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar con el pedido")
            }
        }

        OutlinedButton(
            onClick = onSeguirEligiendo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seguir eligiendo platos")
        }
    }
}

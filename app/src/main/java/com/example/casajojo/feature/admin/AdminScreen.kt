package com.example.casajojo.feature.admin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.casajojo.data.model.MenuItem

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onAgregarClick: () -> Unit,
    onEditarClick: (MenuItem) -> Unit
) {
    val menu by viewModel.menuItems.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Administración del menú",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Gestiona los platos disponibles para hoy.",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = {
                viewModel.nuevoPlato()
                onAgregarClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar plato")
        }

        mensaje?.let { texto ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = texto,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = viewModel::limpiarMensaje
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }

        if (menu.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Todavía no hay platos registrados para hoy.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = menu,
                    key = { it.id }
                ) { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.nombre,
                                style =
                                    MaterialTheme.typography.titleLarge
                            )

                            Text(item.descripcion)

                            Text("Precio: $${item.precio}")

                            Text(
                                if (item.disponible) {
                                    "Disponible para pedidos"
                                } else {
                                    "Agotado"
                                }
                            )

                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.cargarPlato(item)
                                        onEditarClick(item)
                                    }
                                ) {
                                    Text("Editar")
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.cambiarDisponibilidad(item)
                                    }
                                ) {
                                    Text(
                                        if (item.disponible) {
                                            "Agotar"
                                        } else {
                                            "Activar"
                                        }
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    viewModel.eliminarPlato(item)
                                }
                            ) {
                                Text("Eliminar plato")
                            }
                        }
                    }
                }
            }
        }
    }
}

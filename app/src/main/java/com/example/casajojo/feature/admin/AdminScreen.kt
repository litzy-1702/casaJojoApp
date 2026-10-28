
package com.example.casajojo.feature.admin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.casajojo.data.model.MenuItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onAgregarClick: () -> Unit,
    onEditarClick: (MenuItem) -> Unit,
    onGestionUsuariosClick: () -> Unit
) {
    val menu by viewModel.menuItems.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var platoAEliminar by remember {
        mutableStateOf<MenuItem?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "LA CASA JO-JO",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Administración del menú",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Gestiona los platos y su disponibilidad.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Button(
            onClick = {
                viewModel.nuevoPlato()
                onAgregarClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar plato")
        }

        Text(
            text = "Puedes cargar los seis platos base de Casa Jojo. " +
                    "Solo se agregarán los que falten para hoy.",
            style = MaterialTheme.typography.bodySmall
        )

        Button(
            onClick = viewModel::cargarMenuBase,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cargar menú base de Casa Jojo")
        }

        Button(
            onClick = onGestionUsuariosClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar usuarios")
        }

        mensaje?.let { texto ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = texto,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    TextButton(
                        onClick = viewModel::limpiarMensaje
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Platos del menú",
                style = MaterialTheme.typography.titleMedium
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "${menu.size} registrados",
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (menu.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Todavía no hay platos",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Pulsa «Cargar menú base de Casa Jojo» " +
                                        "para registrar los seis platos iniciales.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                val grupos =
                    menu.groupBy { it.precio }.toSortedMap()

                grupos.forEach { (precio, platos) ->
                    item(key = "precio-$precio") {
                        Text(
                            text = "PLATOS DE ${formatearPrecio(precio)}",
                            modifier = Modifier.padding(
                                top = 8.dp,
                                bottom = 2.dp
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(
                        items = platos,
                        key = { plato ->
                            "plato-${plato.id}-${plato.nombre}"
                        }
                    ) { plato ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = plato.nombre,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                if (plato.descripcion.isNotBlank()) {
                                    Text(
                                        text = plato.descripcion,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Text(
                                    text = formatearPrecio(plato.precio),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (plato.disponible) {
                                        MaterialTheme.colorScheme
                                            .secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme
                                            .errorContainer
                                    }
                                ) {
                                    Text(
                                        text = if (plato.disponible) {
                                            "Disponible para pedidos"
                                        } else {
                                            "Agotado"
                                        },
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        ),
                                        style =
                                            MaterialTheme.typography.labelMedium
                                    )
                                }

                                Row(
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.cargarPlato(plato)
                                            onEditarClick(plato)
                                        }
                                    ) {
                                        Text("Editar")
                                    }

                                    TextButton(
                                        onClick = {
                                            viewModel.cambiarDisponibilidad(
                                                plato
                                            )
                                        }
                                    ) {
                                        Text(
                                            if (plato.disponible) {
                                                "Agotar"
                                            } else {
                                                "Activar"
                                            }
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        platoAEliminar = plato
                                    }
                                ) {
                                    Text(
                                        text = "Eliminar plato",
                                        color =
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    platoAEliminar?.let { plato ->
        AlertDialog(
            onDismissRequest = {
                platoAEliminar = null
            },
            title = {
                Text("Eliminar plato")
            },
            text = {
                Text(
                    "¿Seguro que deseas eliminar " +
                            "\"${plato.nombre}\" del menú?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarPlato(plato)
                        platoAEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        platoAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun formatearPrecio(precio: Int): String {
    val formato = NumberFormat.getIntegerInstance(
        Locale("es", "CL")
    )

    return "\$" + formato.format(precio)
}

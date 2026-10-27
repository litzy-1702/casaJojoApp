
package com.example.casajojo.feature.client

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.casajojo.R
import com.example.casajojo.data.model.MenuCasaJojo
import com.example.casajojo.data.model.MenuItem
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LOCALE_CHILE: Locale =
    Locale.forLanguageTag("es-CL")

fun formatearPesos(valor: Int): String {
    return "\$" +
            NumberFormat.getIntegerInstance(LOCALE_CHILE)
                .format(valor)
}

@Composable
fun MenuClienteScreen(
    viewModel: ClienteViewModel,
    onIrAlCarrito: () -> Unit,
    onVerMisPedidos: () -> Unit
) {
    val menu by viewModel.menuDisponible.collectAsState()
    val carrito by viewModel.carrito.collectAsState()
    val total by viewModel.total.collectAsState()
    val cantidadPlatos by viewModel.cantidadPlatos.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val mostrarMensaje by viewModel.mostrarMensaje.collectAsState()

    var platoParaPersonalizar by remember {
        mutableStateOf<MenuItem?>(null)
    }

    val acompanamientosSeleccionados =
        remember { mutableStateListOf<String>() }

    fun cerrarPersonalizacion() {
        platoParaPersonalizar = null
        acompanamientosSeleccionados.clear()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EncabezadoCliente(
            titulo = "Menú del día",
            subtitulo = fechaLegible(viewModel.fechaMenu)
        )

        TextButton(onClick = onVerMisPedidos) {
            Text("Ver el estado de mis pedidos")
        }

        AnimatedVisibility(
            visible = mostrarMensaje,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
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
                    Icon(
                        painter = painterResource(
                            id = R.drawable.ic_cliente_check
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val lista = menu) {
                null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    if (lista.isEmpty()) {
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
                                    text =
                                        "Aún no hay platos disponibles",
                                    style =
                                        MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text =
                                        "El administrador debe registrar " +
                                                "los platos disponibles para hoy.",
                                    style =
                                        MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        val grupos =
                            lista.groupBy { it.precio }.toSortedMap()

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                PaddingValues(bottom = 8.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            grupos.forEach { (precio, platos) ->
                                item(key = "precio-$precio") {
                                    Text(
                                        text =
                                            "PLATOS DE ${formatearPesos(precio)}",
                                        modifier =
                                            Modifier.padding(top = 8.dp),
                                        style =
                                            MaterialTheme.typography.titleMedium,
                                        color =
                                            MaterialTheme.colorScheme.primary
                                    )
                                }

                                items(
                                    items = platos,
                                    key = { plato ->
                                        "plato-${plato.id}"
                                    }
                                ) { plato ->
                                    val cantidad = carrito
                                        .filter {
                                            it.menuItemId == plato.id
                                        }
                                        .sumOf { it.cantidad }

                                    TarjetaPlato(
                                        plato = plato,
                                        cantidad = cantidad,
                                        onPersonalizar = {
                                            acompanamientosSeleccionados.clear()
                                            platoParaPersonalizar = plato
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = carrito.isNotEmpty(),
            enter = slideInVertically { altura -> altura } + fadeIn(),
            exit = slideOutVertically { altura -> altura } + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (cantidadPlatos == 1) {
                                "1 plato"
                            } else {
                                "$cantidadPlatos platos"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = formatearPesos(total),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Button(onClick = onIrAlCarrito) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_cliente_carrito
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver carrito")
                    }
                }
            }
        }
    }

    val platoDialogo = platoParaPersonalizar

    if (platoDialogo != null) {
        AlertDialog(
            onDismissRequest = {
                cerrarPersonalizacion()
            },
            title = {
                Text("Personaliza tu plato")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = platoDialogo.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text =
                            "Precio: ${formatearPesos(platoDialogo.precio)}",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Selecciona uno o dos acompañamientos:")

                    MenuCasaJojo.AGREGADOS.forEach { opcion ->
                        val seleccionado =
                            opcion in acompanamientosSeleccionados

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = seleccionado,
                                enabled = seleccionado ||
                                        acompanamientosSeleccionados.size <
                                        MenuCasaJojo.MAX_AGREGADOS,
                                onCheckedChange = { marcado ->
                                    if (marcado) {
                                        if (
                                            acompanamientosSeleccionados.size <
                                            MenuCasaJojo.MAX_AGREGADOS &&
                                            opcion !in acompanamientosSeleccionados
                                        ) {
                                            acompanamientosSeleccionados.add(
                                                opcion
                                            )
                                        }
                                    } else {
                                        acompanamientosSeleccionados.remove(
                                            opcion
                                        )
                                    }
                                }
                            )

                            Text(
                                text = opcion,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (
                        acompanamientosSeleccionados.size ==
                        MenuCasaJojo.MAX_AGREGADOS
                    ) {
                        Text(
                            text =
                                "Ya seleccionaste los dos acompañamientos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text =
                            "La ensalada mix está incluida en el precio.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = acompanamientosSeleccionados.size in
                            MenuCasaJojo.MIN_AGREGADOS..
                            MenuCasaJojo.MAX_AGREGADOS,
                    onClick = {
                        viewModel.agregarPlato(
                            plato = platoDialogo,
                            acompanamientos =
                                acompanamientosSeleccionados.toList()
                        )

                        cerrarPersonalizacion()
                    }
                ) {
                    Text("Agregar al carrito")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        cerrarPersonalizacion()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaPlato(
    plato: MenuItem,
    cantidad: Int,
    onPersonalizar: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (plato.imagenUri != null) {
                AsyncImage(
                    model = plato.imagenUri,
                    contentDescription =
                        "Fotografía de ${plato.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatearPesos(plato.precio),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (cantidad > 0) {
                        Text(
                            text = "En el carrito: $cantidad",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Button(onClick = onPersonalizar) {
                    Text("Elegir agregados")
                }
            }
        }
    }
}

@Composable
internal fun SelectorCantidad(
    cantidad: Int,
    onQuitar: () -> Unit,
    onAgregar: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(onClick = onQuitar) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleLarge
            )
        }

        AnimatedContent(
            targetState = cantidad,
            label = "cantidad"
        ) { valor ->
            Text(
                text = valor.toString(),
                modifier = Modifier.widthIn(min = 32.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }

        FilledTonalIconButton(onClick = onAgregar) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
internal fun EncabezadoCliente(
    titulo: String,
    subtitulo: String
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
                text = titulo,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun fechaLegible(fechaIso: String): String {
    return try {
        val formato = DateTimeFormatter.ofPattern(
            "EEEE d 'de' MMMM",
            LOCALE_CHILE
        )

        LocalDate.parse(fechaIso)
            .format(formato)
            .replaceFirstChar {
                it.titlecase(LOCALE_CHILE)
            }
    } catch (e: Exception) {
        fechaIso
    }
}

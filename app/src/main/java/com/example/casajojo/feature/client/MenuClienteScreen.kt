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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.casajojo.R
import com.example.casajojo.data.model.MenuItem
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LOCALE_CHILE: Locale = Locale.forLanguageTag("es-CL")

/** Formatea pesos chilenos: 4600 -> "$4.600". */
fun formatearPesos(valor: Int): String {
    return "\$" + NumberFormat.getIntegerInstance(LOCALE_CHILE).format(valor)
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

        // Retroalimentación animada al agregar un plato.
        AnimatedVisibility(
            visible = mostrarMensaje,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cliente_check),
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
            val lista = menu
            when {
                lista == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                lista.isEmpty() -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Aún no hay platos disponibles",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Cuando el local publique el menú de hoy, aparecerá aquí.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    // Agrupados por precio, igual que el menú real del local.
                    val grupos = lista.groupBy { it.precio }.toSortedMap()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        grupos.forEach { (precio, platos) ->
                            item(key = "precio-$precio") {
                                Text(
                                    text = "PLATOS DE ${formatearPesos(precio)}",
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(
                                items = platos,
                                key = { plato -> "plato-${plato.id}" }
                            ) { plato ->
                                val cantidad = carrito
                                    .firstOrNull { it.menuItemId == plato.id }
                                    ?.cantidad ?: 0

                                TarjetaPlato(
                                    plato = plato,
                                    cantidad = cantidad,
                                    onAgregar = { viewModel.agregarPlato(plato) },
                                    onQuitar = { viewModel.disminuirCantidad(plato.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Barra del carrito: aparece deslizándose cuando hay platos.
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
                            text = if (cantidadPlatos == 1) "1 plato" else "$cantidadPlatos platos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatearPesos(total),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Button(onClick = onIrAlCarrito) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cliente_carrito),
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
}

@Composable
private fun TarjetaPlato(
    plato: MenuItem,
    cantidad: Int,
    onAgregar: () -> Unit,
    onQuitar: () -> Unit
) {
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
            if (plato.imagenUri != null) {
                AsyncImage(
                    model = plato.imagenUri,
                    contentDescription = "Fotografía de ${plato.nombre}",
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
                Text(
                    text = formatearPesos(plato.precio),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (cantidad == 0) {
                    Button(onClick = onAgregar) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cliente_carrito),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar")
                    }
                } else {
                    SelectorCantidad(
                        cantidad = cantidad,
                        onQuitar = onQuitar,
                        onAgregar = onAgregar
                    )
                }
            }
        }
    }
}

/** Botones − cantidad +. Lo usan el menú y el carrito. */
@Composable
internal fun SelectorCantidad(
    cantidad: Int,
    onQuitar: () -> Unit,
    onAgregar: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onQuitar) {
            Text(text = "−", style = MaterialTheme.typography.titleLarge)
        }

        // El número cambia con una animación.
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
            Text(text = "+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

/** Encabezado común de las pantallas del cliente (mismo estilo que Admin). */
@Composable
internal fun EncabezadoCliente(
    titulo: String,
    subtitulo: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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

/** "2026-10-26" -> "Lunes 26 de octubre". */
private fun fechaLegible(fechaIso: String): String {
    return try {
        val formato = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", LOCALE_CHILE)
        LocalDate.parse(fechaIso)
            .format(formato)
            .replaceFirstChar { it.titlecase(LOCALE_CHILE) }
    } catch (e: Exception) {
        fechaIso
    }
}

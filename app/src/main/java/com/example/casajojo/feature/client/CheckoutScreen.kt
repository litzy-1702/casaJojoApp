package com.example.casajojo.feature.client

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.casajojo.R
import java.time.LocalTime

@Composable
fun CheckoutScreen(
    viewModel: ClienteViewModel,
    onVolverAlCarrito: () -> Unit,
    onPedidoConfirmado: () -> Unit
) {
    val estado by viewModel.checkout.collectAsState()
    val carrito by viewModel.carrito.collectAsState()
    val total by viewModel.total.collectAsState()
    val context = LocalContext.current
    val errores = estado.errores

    // Navega SOLO cuando Room ya guardó el pedido (el ViewModel deja el id).
    LaunchedEffect(estado.pedidoConfirmadoId) {
        if (estado.pedidoConfirmadoId != null) {
            viewModel.confirmacionAtendida()
            onPedidoConfirmado()
        }
    }

    // Recurso nativo: selector de hora del sistema Android.
    fun abrirSelectorHora() {
        val inicial = estado.horaRetiro
            ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
            ?: LocalTime.now()

        TimePickerDialog(
            context,
            { _, hora, minuto -> viewModel.onHoraRetiroSeleccionada(hora, minuto) },
            inicial.hour,
            inicial.minute,
            true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EncabezadoCliente(
            titulo = "Confirmar pedido",
            subtitulo = "Completa tus datos para enviar la comanda al local."
        )

        // ---------- Resumen ----------
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Resumen",
                    style = MaterialTheme.typography.titleMedium
                )
                if (carrito.isEmpty()) {
                    Text(
                        text = "No hay platos en tu carrito.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                carrito.forEach { linea ->
                    Text(
                        text = "${linea.cantidad}x ${linea.nombrePlato} — " +
                                formatearPesos(linea.subtotal),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Total: ${formatearPesos(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(visible = errores.carrito != null) {
            MensajeError(texto = errores.carrito.orEmpty())
        }

        // ---------- Nombre ----------
        OutlinedTextField(
            value = estado.nombre,
            onValueChange = viewModel::onNombreChange,
            label = { Text("Nombre para el retiro") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cliente_persona),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (errores.nombre != null) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cliente_error),
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = errores.nombre != null,
            supportingText = {
                errores.nombre?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- Hora de retiro ----------
        Text(
            text = "Hora de retiro",
            style = MaterialTheme.typography.titleMedium
        )

        OpcionRetiro(
            texto = "Lo antes posible",
            seleccionado = estado.retiroLoAntesPosible,
            onClick = { viewModel.onRetiroLoAntesPosibleChange(true) }
        )

        OpcionRetiro(
            texto = "A una hora específica",
            seleccionado = !estado.retiroLoAntesPosible,
            onClick = { viewModel.onRetiroLoAntesPosibleChange(false) }
        )

        AnimatedVisibility(visible = !estado.retiroLoAntesPosible) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { abrirSelectorHora() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cliente_hora),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = estado.horaRetiro?.let { "Retiro a las $it (cambiar)" }
                            ?: "Seleccionar hora de retiro"
                    )
                }

                errores.horaRetiro?.let { MensajeError(texto = it) }
            }
        }

        // ---------- Observaciones / restricciones ----------
        OutlinedTextField(
            value = estado.restricciones,
            onValueChange = viewModel::onRestriccionesChange,
            label = { Text("Observaciones o restricciones alimentarias") },
            placeholder = { Text("Ej.: sin sal, sin cebolla") },
            minLines = 2,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cliente_nota),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (errores.restricciones != null) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cliente_error),
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = errores.restricciones != null,
            supportingText = {
                val error = errores.restricciones
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(
                        "Opcional · ${estado.restricciones.trim().length}/" +
                                "${PedidoValidator.RESTRICCIONES_MAX}"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- Error al guardar en Room ----------
        AnimatedVisibility(visible = estado.errorGuardado != null) {
            MensajeError(texto = estado.errorGuardado.orEmpty())
        }

        // ---------- Confirmar ----------
        // Solo se deshabilita mientras se guarda (evita el doble envío).
        // Con el carrito vacío sigue activo para que PedidoValidator
        // muestre su mensaje.
        Button(
            onClick = { viewModel.confirmarPedido() },
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Confirmar pedido")
            }
        }

        TextButton(
            onClick = onVolverAlCarrito,
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al carrito")
        }
    }
}

@Composable
private fun OpcionRetiro(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = seleccionado,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = seleccionado,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun MensajeError(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.ic_cliente_error),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

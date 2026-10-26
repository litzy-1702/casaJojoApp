package com.example.casajojo.feature.admin

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun MenuFormScreen(
    viewModel: AdminViewModel,
    onVolver: () -> Unit
) {
    val form by viewModel.form.collectAsState()
    val context = LocalContext.current

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            viewModel.onImagenChange(uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (form.id == null) {
                "Registrar plato"
            } else {
                "Editar plato"
            },
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Fecha del menú: ${form.fechaMenu}")

        if (form.imagenUri != null) {
            AsyncImage(
                model = form.imagenUri,
                contentDescription = "Fotografía del plato",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
        }

        Button(
            onClick = {
                selectorImagen.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar fotografía")
        }

        OutlinedTextField(
            value = form.nombre,
            onValueChange = viewModel::onNombreChange,
            label = { Text("Nombre del plato") },
            isError = form.errores.nombre != null,
            supportingText = {
                form.errores.nombre?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.descripcion,
            onValueChange = viewModel::onDescripcionChange,
            label = { Text("Descripción") },
            minLines = 3,
            isError = form.errores.descripcion != null,
            supportingText = {
                form.errores.descripcion?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.precio,
            onValueChange = viewModel::onPrecioChange,
            label = { Text("Precio en pesos chilenos") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = form.errores.precio != null,
            supportingText = {
                form.errores.precio?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Switch(
                checked = form.disponible,
                onCheckedChange = viewModel::onDisponibleChange
            )

            Text(
                if (form.disponible) {
                    "Plato disponible"
                } else {
                    "Plato agotado"
                }
            )
        }

        Button(
            onClick = {
                if (viewModel.guardarPlato()) {
                    onVolver()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar plato")
        }

        TextButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

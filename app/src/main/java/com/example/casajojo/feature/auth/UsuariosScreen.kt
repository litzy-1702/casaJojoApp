package com.example.casajojo.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.casajojo.data.model.Usuario

@Composable
fun UsuariosScreen(
    viewModel: UsuariosViewModel,
    onVolver: () -> Unit
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val estado by viewModel.uiState.collectAsState()

    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf("CAJERA") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gestión de usuarios",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Crea y administra las cuentas de caja y cocina.",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Crear trabajador",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT") },
                    singleLine = true,
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Rol del trabajador")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rol == "CAJERA") {
                        Button(onClick = { rol = "CAJERA" }) {
                            Text("Caja")
                        }
                    } else {
                        OutlinedButton(onClick = { rol = "CAJERA" }) {
                            Text("Caja")
                        }
                    }

                    if (rol == "COCINA") {
                        Button(onClick = { rol = "COCINA" }) {
                            Text("Cocina")
                        }
                    } else {
                        OutlinedButton(onClick = { rol = "COCINA" }) {
                            Text("Cocina")
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.crearUsuario(
                            nombre = nombre,
                            rut = rut,
                            correo = correo,
                            telefono = telefono,
                            rol = rol
                        )
                    },
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.cargando) {
                        CircularProgressIndicator()
                    } else {
                        Text("Crear cuenta")
                    }
                }
            }
        }

        estado.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(mensaje)

                    TextButton(
                        onClick = viewModel::limpiarMensaje
                    ) {
                        Text("Ocultar mensaje")
                    }
                }
            }
        }

        Text(
            text = "Trabajadores registrados (${usuarios.size})",
            style = MaterialTheme.typography.titleMedium
        )

        if (usuarios.isEmpty()) {
            Text("Todavía no hay trabajadores registrados.")
        }

        usuarios.forEach { usuario ->
            UsuarioCard(
                usuario = usuario,
                habilitado = !estado.cargando,
                onCambiarEstado = {
                    viewModel.cambiarEstado(usuario)
                },
                onRestablecerPassword = {
                    viewModel.restablecerPassword(usuario)
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a administración")
        }
    }
}

@Composable
private fun UsuarioCard(
    usuario: Usuario,
    habilitado: Boolean,
    onCambiarEstado: () -> Unit,
    onRestablecerPassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = usuario.nombreCompleto,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Usuario: ${usuario.username}")
            Text("Rol: ${usuario.rol}")
            Text("RUT: ${usuario.rut}")
            Text("Correo: ${usuario.correo}")
            Text("Teléfono: ${usuario.telefono}")

            Text(
                text = if (usuario.activo) "Cuenta activa" else "Cuenta desactivada",
                color = if (usuario.activo) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCambiarEstado,
                    enabled = habilitado
                ) {
                    Text(
                        if (usuario.activo) "Desactivar" else "Activar"
                    )
                }

                OutlinedButton(
                    onClick = onRestablecerPassword,
                    enabled = habilitado
                ) {
                    Text("Restablecer clave")
                }
            }
        }
    }
}

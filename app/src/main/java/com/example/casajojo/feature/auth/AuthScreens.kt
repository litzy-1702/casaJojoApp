
package com.example.casajojo.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.casajojo.R

@Composable
fun InicioScreen(
    onCliente: () -> Unit,
    onRolInterno: (RolUsuario) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Logo CasaJojo",
            modifier = Modifier.height(110.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "LA CASA JO-JO",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Selecciona cómo deseas ingresar",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onCliente,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cliente")
        }

        Button(
            onClick = { onRolInterno(RolUsuario.ADMIN) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Administrador")
        }

        Button(
            onClick = { onRolInterno(RolUsuario.CAJERA) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Caja")
        }

        Button(
            onClick = { onRolInterno(RolUsuario.COCINA) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cocina")
        }
    }
}

@Composable
fun LoginScreen(
    rol: RolUsuario,
    inicializando: Boolean,
    cargando: Boolean,
    error: String?,
    onIniciarSesion: (String, String) -> Unit,
    onVolver: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ingreso: ${rol.etiqueta}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            singleLine = true,
            enabled = !inicializando && !cargando,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !inicializando && !cargando,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                onIniciarSesion(username, password)
            },
            enabled = !inicializando && !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (inicializando || cargando) {
                CircularProgressIndicator()
            } else {
                Text("Iniciar sesión")
            }
        }

        TextButton(
            onClick = onVolver,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun CambiarPasswordScreen(
    username: String,
    cargando: Boolean,
    error: String?,
    onGuardar: (String, String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var nueva by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Seguridad de la cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Usuario: $username")

        Text(
            text = "Debes crear una contraseña nueva para continuar."
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nueva,
            onValueChange = { nueva = it },
            label = { Text("Nueva contraseña") },
            singleLine = true,
            enabled = !cargando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmacion,
            onValueChange = { confirmacion = it },
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            enabled = !cargando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onGuardar(nueva, confirmacion) },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar contraseña")
        }

        TextButton(
            onClick = onCerrarSesion,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun ClienteDatosScreen(
    onContinuar: (String, String) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a CasaJojo",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Ingresa tus datos para continuar al menú.")

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                error = null
            },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = {
                telefono = it
                error = null
            },
            label = { Text("Teléfono") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val digitos = telefono.filter { it.isDigit() }

                when {
                    nombre.trim().length < 3 -> {
                        error = "Ingresa tu nombre completo."
                    }

                    digitos.length !in 8..12 -> {
                        error = "Revisa el número de teléfono."
                    }

                    else -> {
                        onContinuar(
                            nombre.trim(),
                            telefono.trim()
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar al menú")
        }

        TextButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}


package com.example.casajojo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.casajojo.data.model.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UsuariosUiState(
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class UsuariosViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    val usuarios: StateFlow<List<Usuario>> =
        repository.observarUsuarios()
            .map { lista ->
                lista.filter {
                    it.rol == "CAJERA" || it.rol == "COCINA"
                }.sortedBy { it.nombreCompleto.lowercase() }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _uiState = MutableStateFlow(UsuariosUiState())
    val uiState = _uiState.asStateFlow()

    fun crearUsuario(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        rol: String
    ) {
        ejecutarOperacion {
            val credenciales = repository.crearUsuarioInterno(
                nombreCompleto = nombre,
                rut = rut,
                correo = correo,
                telefono = telefono,
                rol = rol
            )

            "Usuario creado correctamente.\n" +
                    "Usuario: ${credenciales.usuario.username}\n" +
                    "Contraseña temporal: ${credenciales.passwordTemporal}\n" +
                    "Entrega la contraseña al trabajador de forma segura."
        }
    }

    fun cambiarEstado(usuario: Usuario) {
        ejecutarOperacion {
            val nuevoEstado = !usuario.activo

            repository.cambiarEstadoUsuario(
                id = usuario.id,
                activo = nuevoEstado
            )

            if (nuevoEstado) {
                "La cuenta de ${usuario.username} está activa."
            } else {
                "La cuenta de ${usuario.username} está desactivada."
            }
        }
    }

    fun restablecerPassword(usuario: Usuario) {
        ejecutarOperacion {
            val credenciales =
                repository.restablecerPasswordUsuario(usuario.id)

            "Contraseña restablecida.\n" +
                    "Usuario: ${credenciales.usuario.username}\n" +
                    "Nueva contraseña temporal: ${credenciales.passwordTemporal}\n" +
                    "El trabajador deberá cambiarla al ingresar."
        }
    }

    fun limpiarMensaje() {
        _uiState.value = UsuariosUiState()
    }

    private fun ejecutarOperacion(
        operacion: suspend () -> String
    ) {
        if (_uiState.value.cargando) return

        viewModelScope.launch {
            _uiState.value = UsuariosUiState(cargando = true)

            try {
                val mensaje = operacion()

                _uiState.value = UsuariosUiState(
                    cargando = false,
                    mensaje = mensaje
                )
            } catch (e: IllegalArgumentException) {
                _uiState.value = UsuariosUiState(
                    cargando = false,
                    error = e.message ?: "Revisa los datos ingresados."
                )
            } catch (_: Exception) {
                _uiState.value = UsuariosUiState(
                    cargando = false,
                    error = "No se pudo completar la operación."
                )
            }
        }
    }
}

class UsuariosViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(UsuariosViewModel::class.java)
        ) {
            @Suppress("UNCHECKED_CAST")
            return UsuariosViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}

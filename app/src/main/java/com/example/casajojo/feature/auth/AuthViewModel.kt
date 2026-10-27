
package com.example.casajojo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.casajojo.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RolUsuario(val etiqueta: String) {
    ADMIN("Administrador"),
    CAJERA("Caja"),
    COCINA("Cocina")
}

data class AuthUiState(
    val inicializando: Boolean = true,
    val cargando: Boolean = false,
    val error: String? = null,
    val usuarioActual: Usuario? = null,
    val debeCambiarPassword: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.inicializarCuentasIniciales()

                _uiState.update {
                    it.copy(
                        inicializando = false,
                        error = null
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        inicializando = false,
                        error = "No se pudieron preparar las cuentas. " +
                                "Revisa la base de datos."
                    )
                }
            }
        }
    }

    fun iniciarSesion(
        username: String,
        password: String,
        rolEsperado: RolUsuario
    ) {
        val estado = _uiState.value

        if (estado.inicializando || estado.cargando) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            try {
                when (
                    val resultado = repository.autenticar(
                        username,
                        password
                    )
                ) {
                    is LoginResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                error = resultado.mensaje
                            )
                        }
                    }

                    is LoginResult.Success -> {
                        val usuario = resultado.usuario

                        if (usuario.rol != rolEsperado.name) {
                            _uiState.update {
                                it.copy(
                                    cargando = false,
                                    error = "Esta cuenta no corresponde " +
                                            "al rol seleccionado."
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    cargando = false,
                                    error = null,
                                    usuarioActual = usuario,
                                    debeCambiarPassword =
                                        usuario.debeCambiarPassword
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No fue posible iniciar sesión."
                    )
                }
            }
        }
    }

    fun cambiarPassword(
        nuevaPassword: String,
        confirmacion: String
    ) {
        val estado = _uiState.value
        val usuario = estado.usuarioActual

        if (estado.cargando) return

        if (usuario == null || !estado.debeCambiarPassword) {
            _uiState.update {
                it.copy(error = "No hay un cambio de contraseña pendiente.")
            }
            return
        }

        if (nuevaPassword.length < 8) {
            _uiState.update {
                it.copy(error = "Usa al menos 8 caracteres.")
            }
            return
        }

        if (nuevaPassword != confirmacion) {
            _uiState.update {
                it.copy(error = "Las contraseñas no coinciden.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(cargando = true, error = null)
            }

            try {
                val actualizado = repository.cambiarPassword(
                    usuario,
                    nuevaPassword
                )

                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = null,
                        usuarioActual = actualizado,
                        debeCambiarPassword = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo guardar la nueva contraseña."
                    )
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    fun cerrarSesion() {
        _uiState.update {
            AuthUiState(
                inicializando = false
            )
        }
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}

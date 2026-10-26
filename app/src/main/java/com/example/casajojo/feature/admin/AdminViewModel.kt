
package com.example.casajojo.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casajojo.data.model.MenuItem
import com.example.casajojo.data.repository.MenuRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuFormState(
    val id: Int? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val disponible: Boolean = true,
    val imagenUri: String? = null,
    val fechaMenu: String = LocalDate.now().toString(),
    val errores: MenuFormErrors = MenuFormErrors()
)

class AdminViewModel(
    private val repository: MenuRepository
) : ViewModel() {

    private val fechaHoy = LocalDate.now().toString()

    val menuItems: StateFlow<List<MenuItem>> =
        repository.observarMenuDelDia(fechaHoy).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _form = MutableStateFlow(
        MenuFormState(fechaMenu = fechaHoy)
    )
    val form: StateFlow<MenuFormState> = _form.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun nuevoPlato() {
        _form.value = MenuFormState(fechaMenu = fechaHoy)
    }

    fun cargarPlato(item: MenuItem) {
        _form.value = MenuFormState(
            id = item.id,
            nombre = item.nombre,
            descripcion = item.descripcion,
            precio = item.precio.toString(),
            disponible = item.disponible,
            imagenUri = item.imagenUri,
            fechaMenu = item.fechaMenu
        )
    }

    fun onNombreChange(valor: String) {
        _form.update {
            it.copy(
                nombre = valor,
                errores = it.errores.copy(nombre = null)
            )
        }
    }

    fun onDescripcionChange(valor: String) {
        _form.update {
            it.copy(
                descripcion = valor,
                errores = it.errores.copy(descripcion = null)
            )
        }
    }

    fun onPrecioChange(valor: String) {
        _form.update {
            it.copy(
                precio = valor,
                errores = it.errores.copy(precio = null)
            )
        }
    }

    fun onDisponibleChange(valor: Boolean) {
        _form.update { it.copy(disponible = valor) }
    }

    fun onImagenChange(uri: String?) {
        _form.update { it.copy(imagenUri = uri) }
    }

    fun guardarPlato(): Boolean {
        val actual = _form.value

        val errores = validarMenu(
            nombre = actual.nombre,
            descripcion = actual.descripcion,
            precio = actual.precio
        )

        if (errores.tieneErrores) {
            _form.update { it.copy(errores = errores) }
            return false
        }

        val item = MenuItem(
            id = actual.id ?: 0,
            nombre = actual.nombre.trim(),
            descripcion = actual.descripcion.trim(),
            precio = actual.precio.toInt(),
            disponible = actual.disponible,
            imagenUri = actual.imagenUri,
            fechaMenu = actual.fechaMenu
        )

        viewModelScope.launch {
            repository.guardar(item)

            _mensaje.value = if (actual.id == null) {
                "Plato agregado al menú."
            } else {
                "Cambios guardados correctamente."
            }

            _form.value = MenuFormState(fechaMenu = fechaHoy)
        }

        return true
    }

    fun cambiarDisponibilidad(item: MenuItem) {
        viewModelScope.launch {
            repository.cambiarDisponibilidad(
                id = item.id,
                disponible = !item.disponible
            )
            _mensaje.value = "Disponibilidad actualizada."
        }
    }

    fun eliminarPlato(item: MenuItem) {
        viewModelScope.launch {
            repository.eliminar(item)
            _mensaje.value = "Plato eliminado del menú."
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}

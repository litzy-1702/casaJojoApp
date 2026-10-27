
package com.example.casajojo.feature.auth

import com.example.casajojo.data.local.UsuarioDao
import com.example.casajojo.data.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    private class FakeUsuarioDao : UsuarioDao {

        private val usuarios = mutableListOf<Usuario>()

        override suspend fun obtenerPorUsername(
            username: String
        ): Usuario? {
            return usuarios.firstOrNull {
                it.username == username
            }
        }

        override suspend fun obtenerPorId(id: Int): Usuario? {
            return usuarios.firstOrNull { it.id == id }
        }

        override fun observarTodos(): Flow<List<Usuario>> {
            return flowOf(usuarios.toList())
        }

        override suspend fun insertar(usuario: Usuario): Long {
            require(
                usuarios.none { it.username == usuario.username }
            ) {
                "El nombre de usuario ya existe."
            }

            val nuevoId =
                (usuarios.maxOfOrNull { it.id } ?: 0) + 1

            val guardado = usuario.copy(id = nuevoId)
            usuarios.add(guardado)

            return nuevoId.toLong()
        }

        override suspend fun actualizar(usuario: Usuario) {
            val indice = usuarios.indexOfFirst {
                it.id == usuario.id
            }

            require(indice >= 0) {
                "El usuario no existe."
            }

            usuarios[indice] = usuario
        }

        fun todos(): List<Usuario> = usuarios.toList()
    }

    @Test
    fun inicializaCuentasInicialesUnaSolaVez() = runBlocking {
        val dao = FakeUsuarioDao()
        val repository = AuthRepository(dao)

        repository.inicializarCuentasIniciales()
        repository.inicializarCuentasIniciales()

        assertEquals(3, dao.todos().size)

        val admin = dao.obtenerPorUsername("admin")
        assertNotNull(admin)

        assertNotEquals(
            "admin123",
            admin!!.passwordHash
        )

        assertTrue(admin.passwordSalt.isNotBlank())
        assertTrue(admin.debeCambiarPassword)
    }

    @Test
    fun permiteAutenticarAdministradorConUsuarioEnMayusculas() =
        runBlocking {
            val dao = FakeUsuarioDao()
            val repository = AuthRepository(dao)

            repository.inicializarCuentasIniciales()

            val resultado = repository.autenticar(
                username = "ADMIN",
                password = "admin123"
            )

            assertTrue(resultado is LoginResult.Success)

            val usuario =
                (resultado as LoginResult.Success).usuario

            assertEquals("ADMIN", usuario.rol)
            assertTrue(usuario.debeCambiarPassword)
        }

    @Test
    fun rechazaUnaContrasenaIncorrecta() = runBlocking {
        val dao = FakeUsuarioDao()
        val repository = AuthRepository(dao)

        repository.inicializarCuentasIniciales()

        val resultado = repository.autenticar(
            username = "admin",
            password = "incorrecta"
        )

        assertTrue(resultado is LoginResult.Failure)
    }

    @Test
    fun cambiarContrasenaInvalidaLaClaveProvisional() = runBlocking {
        val dao = FakeUsuarioDao()
        val repository = AuthRepository(dao)

        repository.inicializarCuentasIniciales()

        val admin = dao.obtenerPorUsername("admin")!!

        val actualizado = repository.cambiarPassword(
            usuario = admin,
            nuevaPassword = "ClaveNueva123"
        )

        assertFalse(actualizado.debeCambiarPassword)

        val claveAntigua = repository.autenticar(
            "admin",
            "admin123"
        )

        assertTrue(claveAntigua is LoginResult.Failure)

        val claveNueva = repository.autenticar(
            "admin",
            "ClaveNueva123"
        )

        assertTrue(claveNueva is LoginResult.Success)
    }

    @Test
    fun rechazaUnaCuentaDesactivada() = runBlocking {
        val dao = FakeUsuarioDao()
        val repository = AuthRepository(dao)

        repository.inicializarCuentasIniciales()

        val admin = dao.obtenerPorUsername("admin")!!

        dao.actualizar(admin.copy(activo = false))

        val resultado = repository.autenticar(
            "admin",
            "admin123"
        )

        assertTrue(resultado is LoginResult.Failure)

        assertEquals(
            "Esta cuenta está desactivada.",
            (resultado as LoginResult.Failure).mensaje
        )
    }
}

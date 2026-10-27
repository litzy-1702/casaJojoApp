
package com.example.casajojo.feature.auth

import java.util.Base64
import com.example.casajojo.data.local.UsuarioDao
import com.example.casajojo.data.model.Usuario
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import java.text.Normalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

sealed interface LoginResult {
    data class Success(val usuario: Usuario) : LoginResult
    data class Failure(val mensaje: String) : LoginResult
}

class AuthRepository(
    private val usuarioDao: UsuarioDao
) {

    suspend fun inicializarCuentasIniciales() {
        val cuentas = listOf(
            CuentaInicial(
                nombre = "Administrador",
                username = "admin",
                rol = "ADMIN",
                password = "admin123"
            ),
            CuentaInicial(
                nombre = "Cajera",
                username = "cajera",
                rol = "CAJERA",
                password = "cajera123"
            ),
            CuentaInicial(
                nombre = "Cocina",
                username = "cocina",
                rol = "COCINA",
                password = "cocina123"
            )
        )

        cuentas.forEach { cuenta ->
            val existente =
                usuarioDao.obtenerPorUsername(cuenta.username)

            if (existente == null) {
                val credenciales =
                    PasswordHasher.crear(cuenta.password)

                usuarioDao.insertar(
                    Usuario(
                        nombreCompleto = cuenta.nombre,
                        rut = "",
                        correo = "",
                        telefono = "",
                        username = cuenta.username,
                        rol = cuenta.rol,
                        passwordSalt = credenciales.first,
                        passwordHash = credenciales.second,
                        activo = true,
                        debeCambiarPassword = true
                    )
                )
            }
        }
    }

    suspend fun autenticar(
        username: String,
        password: String
    ): LoginResult {
        val usuarioNormalizado =
            username.trim().lowercase(Locale.ROOT)

        if (usuarioNormalizado.isBlank() || password.isBlank()) {
            return LoginResult.Failure(
                "Ingresa el usuario y la contraseña."
            )
        }

        val usuario =
            usuarioDao.obtenerPorUsername(usuarioNormalizado)
                ?: return LoginResult.Failure(
                    "Usuario o contraseña incorrectos."
                )

        if (!usuario.activo) {
            return LoginResult.Failure(
                "Esta cuenta está desactivada."
            )
        }

        val valido = PasswordHasher.verificar(
            password = password,
            saltBase64 = usuario.passwordSalt,
            hashBase64 = usuario.passwordHash
        )

        if (!valido) {
            return LoginResult.Failure(
                "Usuario o contraseña incorrectos."
            )
        }

        return LoginResult.Success(usuario)
    }

    suspend fun cambiarPassword(
        usuario: Usuario,
        nuevaPassword: String
    ): Usuario {
        require(nuevaPassword.length >= 8) {
            "La contraseña debe tener al menos 8 caracteres."
        }

        val credenciales = PasswordHasher.crear(nuevaPassword)

        val actualizado = usuario.copy(
            passwordSalt = credenciales.first,
            passwordHash = credenciales.second,
            debeCambiarPassword = false
        )

        usuarioDao.actualizar(actualizado)

        return actualizado
    }


    fun observarUsuarios(): Flow<List<Usuario>> {
        return usuarioDao.observarTodos()
    }

    suspend fun crearUsuarioInterno(
        nombreCompleto: String,
        rut: String,
        correo: String,
        telefono: String,
        rol: String
    ): CredencialesTemporales {
        val nombre = nombreCompleto.trim()
        require(nombre.split(Regex("\\s+")).size >= 2) {
            "Ingresa el nombre y al menos un apellido."
        }

        val rutNormalizado = normalizarRut(rut)
        require(validarRut(rutNormalizado)) {
            "El RUT ingresado no es válido."
        }

        val correoNormalizado = correo.trim().lowercase(Locale.ROOT)
        require(
            Regex(
                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
                RegexOption.IGNORE_CASE
            ).matches(correoNormalizado)
        ) {
            "Ingresa un correo válido."
        }

        val digitosTelefono = telefono.filter { it.isDigit() }
        require(digitosTelefono.length in 8..12) {
            "Ingresa un teléfono válido."
        }

        val rolNormalizado = rol.trim().uppercase(Locale.ROOT)
        require(rolNormalizado == "CAJERA" || rolNormalizado == "COCINA") {
            "Solo puedes crear cuentas de caja o cocina."
        }

        val usuariosExistentes = usuarioDao.observarTodos().first()

        require(
            usuariosExistentes.none {
                it.rut.isNotBlank() &&
                        normalizarRut(it.rut) == rutNormalizado
            }
        ) {
            "Ya existe una cuenta con ese RUT."
        }

        val baseUsername = generarUsername(nombre)
        var username = baseUsername
        var sufijo = 2

        while (usuarioDao.obtenerPorUsername(username) != null) {
            username = "$baseUsername$sufijo"
            sufijo++
        }

        val passwordTemporal = generarPasswordTemporal()
        val credenciales = PasswordHasher.crear(passwordTemporal)

        val usuario = Usuario(
            nombreCompleto = nombre,
            rut = rutNormalizado,
            correo = correoNormalizado,
            telefono = telefono.trim(),
            username = username,
            rol = rolNormalizado,
            passwordSalt = credenciales.first,
            passwordHash = credenciales.second,
            activo = true,
            debeCambiarPassword = true
        )

        val id = usuarioDao.insertar(usuario)

        return CredencialesTemporales(
            usuario = usuario.copy(id = id.toInt()),
            passwordTemporal = passwordTemporal
        )
    }

    suspend fun cambiarEstadoUsuario(
        id: Int,
        activo: Boolean
    ) {
        val usuario = usuarioDao.obtenerPorId(id)
            ?: throw IllegalArgumentException("Usuario no encontrado.")

        require(usuario.rol == "CAJERA" || usuario.rol == "COCINA") {
            "Solo puedes modificar cuentas de caja o cocina."
        }

        usuarioDao.actualizar(usuario.copy(activo = activo))
    }

    suspend fun restablecerPasswordUsuario(
        id: Int
    ): CredencialesTemporales {
        val usuario = usuarioDao.obtenerPorId(id)
            ?: throw IllegalArgumentException("Usuario no encontrado.")

        require(usuario.rol == "CAJERA" || usuario.rol == "COCINA") {
            "Solo puedes restablecer cuentas de caja o cocina."
        }

        val passwordTemporal = generarPasswordTemporal()
        val credenciales = PasswordHasher.crear(passwordTemporal)

        val actualizado = usuario.copy(
            passwordSalt = credenciales.first,
            passwordHash = credenciales.second,
            debeCambiarPassword = true
        )

        usuarioDao.actualizar(actualizado)

        return CredencialesTemporales(
            usuario = actualizado,
            passwordTemporal = passwordTemporal
        )
    }

    private fun generarUsername(nombreCompleto: String): String {
        val sinAcentos = Normalizer.normalize(
            nombreCompleto,
            Normalizer.Form.NFD
        ).replace(Regex("\\p{Mn}+"), "")

        val palabras = sinAcentos
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        require(palabras.size >= 2) {
            "No fue posible generar el usuario."
        }

        return palabras.first().first() .toString() + palabras.last()
    }

    private fun generarPasswordTemporal(): String {
        val caracteres =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%"

        val random = SecureRandom()

        return buildString {
            repeat(12) {
                append(caracteres[random.nextInt(caracteres.length)])
            }
        }
    }

    private fun normalizarRut(rut: String): String {
        return rut
            .replace(".", "")
            .replace("-", "")
            .replace(" ", "")
            .uppercase(Locale.ROOT)
    }

    private fun validarRut(rut: String): Boolean {
        if (rut.length !in 8..9) return false

        val cuerpo = rut.dropLast(1)
        val digitoVerificador = rut.last().toString()

        if (!cuerpo.all { it.isDigit() }) return false

        var suma = 0
        var factor = 2

        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }

        val resultado = 11 - (suma % 11)

        val esperado = when (resultado) {
            11 -> "0"
            10 -> "K"
            else -> resultado.toString()
        }

        return esperado == digitoVerificador
    }

    data class CredencialesTemporales(
        val usuario: Usuario,
        val passwordTemporal: String
    )


    private data class CuentaInicial(
        val nombre: String,
        val username: String,
        val rol: String,
        val password: String
    )
}


private object PasswordHasher {

    private const val ITERACIONES = 120_000
    private const val LONGITUD_HASH_BITS = 256

    fun crear(password: String): Pair<String, String> {
        val salt = ByteArray(16)
        java.security.SecureRandom().nextBytes(salt)

        val hash = derivar(password, salt)

        return Base64.getEncoder().encodeToString(salt) to
                Base64.getEncoder().encodeToString(hash)
    }

    fun verificar(
        password: String,
        saltBase64: String,
        hashBase64: String
    ): Boolean {
        return try {
            val salt = Base64.getDecoder().decode(saltBase64)
            val hashEsperado = Base64.getDecoder().decode(hashBase64)
            val hashCalculado = derivar(password, salt)

            java.security.MessageDigest.isEqual(
                hashEsperado,
                hashCalculado
            )
        } catch (_: Exception) {
            false
        }
    }

    private fun derivar(
        password: String,
        salt: ByteArray
    ): ByteArray {
        val especificacion = javax.crypto.spec.PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERACIONES,
            LONGITUD_HASH_BITS
        )

        return try {
            javax.crypto.SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(especificacion)
                .encoded
        } finally {
            especificacion.clearPassword()
        }
    }
}

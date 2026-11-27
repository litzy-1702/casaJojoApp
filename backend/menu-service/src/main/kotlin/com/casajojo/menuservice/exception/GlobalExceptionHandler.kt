package com.casajojo.menuservice.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

/**
 * Respuesta de error uniforme para la app.
 * "errores" contiene el mensaje de cada campo inválido (solo en validaciones).
 */
data class ErrorResponse(
    val status: Int,
    val error: String,
    val mensaje: String,
    val errores: Map<String, String> = emptyMap()
)

/**
 * Convierte las excepciones en respuestas 400 / 404 con un mensaje claro en español.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    // Campos que no cumplen las validaciones (@NotBlank, @Positive, etc.) -> 400
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun manejarValidacion(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errores = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Valor inválido")
        }
        return respuesta(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", errores)
    }

    // JSON mal formado o con un valor que no corresponde (por ejemplo, un estado inexistente) -> 400
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun manejarCuerpoInvalido(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> =
        respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido")

    // Parámetro de la URL con un tipo incorrecto (por ejemplo, /api/... /abc) -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun manejarParametroInvalido(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        respuesta(HttpStatus.BAD_REQUEST, "El parámetro '${ex.name}' tiene un valor inválido: ${ex.value}")

    // Errores lanzados por los servicios, por ejemplo un id inexistente -> 404
    @ExceptionHandler(ResponseStatusException::class)
    fun manejarEstado(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        return respuesta(status, ex.reason ?: status.reasonPhrase)
    }

    private fun respuesta(
        status: HttpStatus,
        mensaje: String,
        errores: Map<String, String> = emptyMap()
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status)
            .body(ErrorResponse(status.value(), status.reasonPhrase, mensaje, errores))
}

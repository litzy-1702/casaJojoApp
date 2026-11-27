package com.casajojo.menuservice.config

import com.casajojo.menuservice.model.MenuItem
import com.casajojo.menuservice.repository.MenuItemRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Locale

/**
 * Datos iniciales: al arrancar, agrega para la fecha de hoy los seis platos base
 * de Casa Jojo que todavía no existan (mismos platos que MenuCasaJojo en la app).
 * No duplica platos que ya estén cargados.
 */
@Component
class MenuDataInitializer(
    private val repository: MenuItemRepository
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(MenuDataInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        val hoy = LocalDate.now().toString()
        val descripcion = "Incluye mix de ensaladas. Selecciona uno o dos agregados."

        val platosBase = listOf(
            "Guatitas a la jardinera" to 4600,
            "Cerdo mongoliano" to 4600,
            "Filete de pollo" to 4600,
            "Tortilla de porotos verdes" to 4600,
            "Pollo asado" to 4800,
            "Carne al jugo" to 4800
        )

        val existentes = repository.findByFechaMenuOrderByNombreAsc(hoy)
            .map { normalizar(it.nombre) }
            .toSet()

        val faltantes = platosBase
            .filter { (nombre, _) -> normalizar(nombre) !in existentes }
            .map { (nombre, precio) ->
                MenuItem(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    disponible = true,
                    fechaMenu = hoy
                )
            }

        if (faltantes.isEmpty()) {
            log.info("El menú base de {} ya estaba cargado", hoy)
        } else {
            repository.saveAll(faltantes)
            log.info("Se cargaron {} platos base para {}", faltantes.size, hoy)
        }
    }

    private fun normalizar(nombre: String): String = nombre.trim().lowercase(Locale.ROOT)
}

package com.example.casajojo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.casajojo.data.model.MenuItem
import com.example.casajojo.data.model.Pedido
import com.example.casajojo.data.model.Usuario

@Database(
    entities = [
        MenuItem::class,
        Pedido::class,
        Usuario::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun menuDao(): MenuDao

    abstract fun pedidoDao(): PedidoDao

    abstract fun usuarioDao(): UsuarioDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `usuarios` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nombreCompleto` TEXT NOT NULL,
                        `rut` TEXT NOT NULL,
                        `correo` TEXT NOT NULL,
                        `telefono` TEXT NOT NULL,
                        `username` TEXT NOT NULL,
                        `rol` TEXT NOT NULL,
                        `passwordSalt` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `activo` INTEGER NOT NULL,
                        `debeCambiarPassword` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_usuarios_username`
                    ON `usuarios` (`username`)
                    """.trimIndent()
                )
            }
        }

        fun getInstance(
            context: Context
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "casajojo_database.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
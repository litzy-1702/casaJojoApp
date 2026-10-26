package com.example.casajojo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.casajojo.data.model.MenuItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {

    @Query(
        """
        SELECT * FROM menu_items
        WHERE fechaMenu = :fecha
        ORDER BY nombre COLLATE NOCASE ASC
        """
    )
    fun observarPorFecha(fecha: String): Flow<List<MenuItem>>

    @Insert
    suspend fun insertar(item: MenuItem)

    @Update
    suspend fun actualizar(item: MenuItem)

    @Delete
    suspend fun eliminar(item: MenuItem)

    @Query(
        """
        UPDATE menu_items
        SET disponible = :disponible
        WHERE id = :id
        """
    )
    suspend fun actualizarDisponibilidad(
        id: Int,
        disponible: Boolean
    )
}

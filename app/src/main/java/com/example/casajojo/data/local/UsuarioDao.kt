package com.example.casajojo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.casajojo.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query(
        """
        SELECT * FROM usuarios
        WHERE username = :username
        LIMIT 1
        """
    )
    suspend fun obtenerPorUsername(
        username: String
    ): Usuario?

    @Query(
        """
        SELECT * FROM usuarios
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun obtenerPorId(
        id: Int
    ): Usuario?

    @Query(
        """
        SELECT * FROM usuarios
        ORDER BY nombreCompleto COLLATE NOCASE ASC
        """
    )
    fun observarTodos(): Flow<List<Usuario>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    @Update
    suspend fun actualizar(usuario: Usuario)
}
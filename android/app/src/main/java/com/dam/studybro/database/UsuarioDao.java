package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/**
 * Nivel 4: room - Las Instrucciones SQL (DAO)
 */
@Dao
public interface UsuarioDao {
    @Query("SELECT * FROM usuarios")
    List<Usuario> obtenerTodos();

    @Query("SELECT * FROM usuarios WHERE email = :correo LIMIT 1")
    Usuario buscarPorCorreo(String correo);

    @Insert
    void insertarUsuario(Usuario usuario);

    @androidx.room.Update
    void actualizarUsuario(Usuario usuario);
}

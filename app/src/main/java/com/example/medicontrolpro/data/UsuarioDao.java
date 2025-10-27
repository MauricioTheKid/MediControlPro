package com.example.medicontrolpro.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UsuarioDao {

    @Insert
    void insert(UsuarioEntity usuario);

    @Query("SELECT * FROM usuarios WHERE email = :email")
    UsuarioEntity getUsuarioByEmail(String email);

    @Query("SELECT * FROM usuarios")
    LiveData<List<UsuarioEntity>> getAllUsuarios();

    @Query("UPDATE usuarios SET nombreCompleto = :nombreCompleto, telefono = :telefono, " +
            "direccion = :direccion, tipoSangre = :tipoSangre, fechaNacimiento = :fechaNacimiento, " +
            "genero = :genero, alergias = :alergias, condicionesMedicas = :condicionesMedicas, " +
            "medicamentosActuales = :medicamentosActuales, sincronizado = :sincronizado " +
            "WHERE email = :email")
    void actualizarPerfil(String email, String nombreCompleto, String telefono, String direccion,
                          String tipoSangre, String fechaNacimiento, String genero, String alergias,
                          String condicionesMedicas, String medicamentosActuales, boolean sincronizado);

    @Delete
    void delete(UsuarioEntity usuario);

    @Query("DELETE FROM usuarios WHERE email = :email")
    void deleteByEmail(String email);

    @Query("DELETE FROM usuarios")
    void deleteAll();
}
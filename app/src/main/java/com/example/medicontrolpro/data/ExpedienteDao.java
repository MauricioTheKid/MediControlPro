package com.example.medicontrolpro.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpedienteDao {
    @Insert
    long insert(ExpedienteEntity expediente);

    @Update
    void update(ExpedienteEntity expediente);

    @Delete
    void delete(ExpedienteEntity expediente);

    @Query("SELECT * FROM expedientes ORDER BY fechaCreacion DESC")
    LiveData<List<ExpedienteEntity>> getAllExpedientes();

    @Query("SELECT * FROM expedientes WHERE id = :id")
    LiveData<ExpedienteEntity> getExpedienteById(int id);

    @Query("SELECT * FROM expedientes WHERE citaId = :citaId")
    LiveData<ExpedienteEntity> getExpedienteByCitaId(int citaId);

    @Query("SELECT * FROM expedientes WHERE doctor LIKE '%' || :query || '%' OR especialidad LIKE '%' || :query || '%' OR diagnostico LIKE '%' || :query || '%' ORDER BY fechaCreacion DESC")
    LiveData<List<ExpedienteEntity>> searchExpedientes(String query);

    @Query("SELECT * FROM expedientes WHERE fechaCita = :fecha ORDER BY fechaCreacion DESC")
    LiveData<List<ExpedienteEntity>> getExpedientesByFecha(String fecha);
}
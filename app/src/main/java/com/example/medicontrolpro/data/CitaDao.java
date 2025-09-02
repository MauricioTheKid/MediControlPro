package com.example.medicontrolpro.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CitaDao {
    @Insert
    long insert(CitaEntity cita);

    @Update
    void update(CitaEntity cita);

    @Delete
    void delete(CitaEntity cita);

    @Query("SELECT * FROM citas ORDER BY fechaTimestamp DESC")
    LiveData<List<CitaEntity>> getAllCitas();

    @Query("SELECT * FROM citas WHERE id = :id")
    LiveData<CitaEntity> getCitaById(int id);

    @Query("SELECT * FROM citas WHERE doctor LIKE '%' || :query || '%' OR especialidad LIKE '%' || :query || '%' OR motivo LIKE '%' || :query || '%' ORDER BY fechaTimestamp DESC")
    LiveData<List<CitaEntity>> searchCitas(String query);

    @Query("SELECT * FROM citas WHERE fecha = :fecha ORDER BY hora ASC")
    LiveData<List<CitaEntity>> getCitasByFecha(String fecha);

    @Query("SELECT * FROM citas WHERE estado = :estado ORDER BY fechaTimestamp DESC")
    LiveData<List<CitaEntity>> getCitasByEstado(String estado);

    @Query("SELECT * FROM citas WHERE fecha = :fecha AND hora > :horaActual ORDER BY hora ASC")
    LiveData<List<CitaEntity>> getCitasProximas(String fecha, String horaActual);
}
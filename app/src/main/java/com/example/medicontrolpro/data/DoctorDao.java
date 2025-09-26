package com.example.medicontrolpro.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DoctorDao {
    @Insert
    long insert(DoctorEntity doctor);

    @Update
    void update(DoctorEntity doctor);

    @Delete
    void delete(DoctorEntity doctor);

    @Query("SELECT * FROM doctores ORDER BY esFavorito DESC, nombre ASC")
    LiveData<List<DoctorEntity>> getAllDoctores();

    @Query("SELECT * FROM doctores WHERE id = :id")
    LiveData<DoctorEntity> getDoctorById(int id);

    @Query("SELECT * FROM doctores WHERE esFavorito = 1 ORDER BY nombre ASC")
    LiveData<List<DoctorEntity>> getDoctoresFavoritos();

    @Query("SELECT * FROM doctores WHERE nombre LIKE '%' || :query || '%' OR especialidad LIKE '%' || :query || '%' ORDER BY nombre ASC")
    LiveData<List<DoctorEntity>> searchDoctores(String query);

    @Query("SELECT * FROM doctores WHERE especialidad = :especialidad ORDER BY nombre ASC")
    LiveData<List<DoctorEntity>> getDoctoresByEspecialidad(String especialidad);

    @Query("UPDATE doctores SET esFavorito = :esFavorito WHERE id = :id")
    void updateFavorito(int id, boolean esFavorito);
}
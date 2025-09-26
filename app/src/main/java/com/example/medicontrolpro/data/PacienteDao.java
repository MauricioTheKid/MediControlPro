package com.example.medicontrolpro.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PacienteDao {
    @Insert
    void insert(PacienteEntity paciente);

    @Update
    void update(PacienteEntity paciente);

    @Query("SELECT * FROM pacientes LIMIT 1")
    LiveData<PacienteEntity> getPaciente();

    @Query("SELECT COUNT(*) FROM pacientes")
    int count();
}
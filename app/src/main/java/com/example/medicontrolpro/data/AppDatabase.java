package com.example.medicontrolpro.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CitaEntity.class, ExpedienteEntity.class, DoctorEntity.class, PacienteEntity.class},
        version = 5,  // ← CAMBIAR de 4 a 5
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CitaDao citaDao();
    public abstract ExpedienteDao expedienteDao();
    public abstract DoctorDao doctorDao();
    public abstract PacienteDao pacienteDao();
}
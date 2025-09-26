package com.example.medicontrolpro.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {CitaEntity.class, ExpedienteEntity.class, DoctorEntity.class, PacienteEntity.class},
        version = 6,  // ← INCREMENTAR A 6 por los cambios
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CitaDao citaDao();
    public abstract ExpedienteDao expedienteDao();
    public abstract DoctorDao doctorDao();
    public abstract PacienteDao pacienteDao();
}
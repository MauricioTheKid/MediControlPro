package com.example.medicontrolpro.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CitaEntity.class, ExpedienteEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CitaDao citaDao();
    public abstract ExpedienteDao expedienteDao();
}
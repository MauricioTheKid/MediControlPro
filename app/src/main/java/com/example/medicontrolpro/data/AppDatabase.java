package com.example.medicontrolpro.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CitaEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CitaDao citaDao();
}
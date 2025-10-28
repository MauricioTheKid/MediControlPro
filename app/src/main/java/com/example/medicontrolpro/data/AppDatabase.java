// data/AppDatabase.java
package com.example.medicontrolpro.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {
                CitaEntity.class,
                ExpedienteEntity.class,
                DoctorEntity.class,
                PacienteEntity.class,
                UsuarioEntity.class
        },
        version = 11, // ✅ INCREMENTA A 11
        exportSchema = false
)
@TypeConverters({})
public abstract class AppDatabase extends RoomDatabase {

    public abstract CitaDao citaDao();
    public abstract ExpedienteDao expedienteDao();
    public abstract DoctorDao doctorDao();
    public abstract PacienteDao pacienteDao();
    public abstract UsuarioDao usuarioDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "medicontrol_database")
                            .fallbackToDestructiveMigration() // ✅ ESTO ES CRÍTICO
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static final java.util.concurrent.ExecutorService databaseWriteExecutor =
            java.util.concurrent.Executors.newFixedThreadPool(4);
}
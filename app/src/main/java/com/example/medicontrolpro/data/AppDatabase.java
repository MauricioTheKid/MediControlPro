// data/AppDatabase.java
package com.example.medicontrolpro.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Clase principal de la base de datos Room para la aplicación MediControlPro.
 * Aquí se declaran todas las entidades (tablas) y los DAOs correspondientes.
 *
 * La versión de la base de datos debe incrementarse cada vez que se realice un cambio
 * estructural en las tablas o entidades (por ejemplo, agregar un nuevo campo o entidad).
 */
@Database(
        entities = {
                CitaEntity.class,
                ExpedienteEntity.class,
                DoctorEntity.class,
                PacienteEntity.class,
                UsuarioEntity.class // ← NUEVA entidad incluida
        },
        version = 9, // ← Incrementar a 8 debido a cambios recientes en las entidades
        exportSchema = false
)
@TypeConverters({}) // Aquí puedes agregar tus TypeConverters si usas tipos personalizados
public abstract class AppDatabase extends RoomDatabase {

    // DAOs (Data Access Objects) — Interfaces que definen operaciones de base de datos
    public abstract CitaDao citaDao();
    public abstract ExpedienteDao expedienteDao();
    public abstract DoctorDao doctorDao();
    public abstract PacienteDao pacienteDao();
    public abstract UsuarioDao usuarioDao(); // ← Nuevo DAO agregado para manejar usuarios

    // NUEVO: Patrón Singleton para obtener instancia de la base de datos
    private static volatile AppDatabase INSTANCE;

    /**
     * Obtiene la instancia única de la base de datos usando el patrón Singleton.
     *
     * @param context Contexto de la aplicación
     * @return Instancia de AppDatabase
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "medicontrol_database")
                            .fallbackToDestructiveMigration() // Elimina y recrea la BD si hay cambios de versión
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // NUEVO: Executor para operaciones de base de datos en segundo plano
    public static final java.util.concurrent.ExecutorService databaseWriteExecutor =
            java.util.concurrent.Executors.newFixedThreadPool(4);
}
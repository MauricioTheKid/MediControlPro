// data/DatabaseClient.java (asegurar migración)
package com.example.medicontrolpro.data;

import android.content.Context;
import androidx.room.Room;

public class DatabaseClient {
    private static DatabaseClient instance;
    private AppDatabase appDatabase;

    private DatabaseClient(Context context) {
        appDatabase = Room.databaseBuilder(context, AppDatabase.class, "medicontrol_db")
                .fallbackToDestructiveMigration() // ← IMPORTANTE: Elimina y recrea la BD
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
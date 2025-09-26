package com.example.medicontrolpro.data;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorRepository {
    private DoctorDao doctorDao;
    private ExecutorService executorService;
    private static final String TAG = "DoctorRepository";

    public DoctorRepository(Context context) {
        try {
            AppDatabase database = DatabaseClient.getInstance(context.getApplicationContext()).getAppDatabase();
            this.doctorDao = database.doctorDao();
            this.executorService = Executors.newSingleThreadExecutor();
            Log.d(TAG, "DoctorRepository inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar DoctorRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insert(DoctorEntity doctor) {
        executorService.execute(() -> {
            try {
                long id = doctorDao.insert(doctor);
                Log.d(TAG, "Doctor insertado con ID: " + id);
            } catch (Exception e) {
                Log.e(TAG, "Error al insertar doctor: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void update(DoctorEntity doctor) {
        executorService.execute(() -> {
            try {
                doctorDao.update(doctor);
                Log.d(TAG, "Doctor actualizado: " + doctor.nombre);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar doctor: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void delete(DoctorEntity doctor) {
        executorService.execute(() -> {
            try {
                doctorDao.delete(doctor);
                Log.d(TAG, "Doctor eliminado: " + doctor.nombre);
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar doctor: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public LiveData<List<DoctorEntity>> getAllDoctores() {
        return doctorDao.getAllDoctores();
    }

    public LiveData<DoctorEntity> getDoctorById(int id) {
        return doctorDao.getDoctorById(id);
    }

    public LiveData<List<DoctorEntity>> getDoctoresFavoritos() {
        return doctorDao.getDoctoresFavoritos();
    }

    public LiveData<List<DoctorEntity>> searchDoctores(String query) {
        return doctorDao.searchDoctores(query);
    }

    public LiveData<List<DoctorEntity>> getDoctoresByEspecialidad(String especialidad) {
        return doctorDao.getDoctoresByEspecialidad(especialidad);
    }

    public void updateFavorito(int id, boolean esFavorito) {
        executorService.execute(() -> {
            try {
                doctorDao.updateFavorito(id, esFavorito);
                Log.d(TAG, "Favorito actualizado para doctor ID: " + id);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar favorito: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
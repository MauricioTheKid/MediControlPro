package com.example.medicontrolpro.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CitaRepository {
    private CitaDao citaDao;
    private ExecutorService executorService;
    private Context context;

    public CitaRepository(Context context) {
        AppDatabase database = DatabaseClient.getInstance(context).getAppDatabase();
        this.citaDao = database.citaDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.context = context.getApplicationContext();

        // Crear canal de notificaciones
        com.example.medicontrolpro.utils.NotificationHelper.createNotificationChannel(this.context);
    }

    public void insert(CitaEntity cita) {
        executorService.execute(() -> {
            long id = citaDao.insert(cita);
            cita.id = (int) id;
            // Programar notificación para la nueva cita
            com.example.medicontrolpro.utils.NotificationScheduler.scheduleCitaNotification(context, cita);
        });
    }

    public void update(CitaEntity cita) {
        executorService.execute(() -> {
            citaDao.update(cita);
            // Reprogramar notificación
            com.example.medicontrolpro.utils.NotificationScheduler.cancelCitaNotification(context, cita.id);
            com.example.medicontrolpro.utils.NotificationScheduler.scheduleCitaNotification(context, cita);
        });
    }

    public void delete(CitaEntity cita) {
        executorService.execute(() -> {
            citaDao.delete(cita);
            // Cancelar notificación
            com.example.medicontrolpro.utils.NotificationScheduler.cancelCitaNotification(context, cita.id);
        });
    }

    // Los demás métodos permanecen igual...
    public LiveData<List<CitaEntity>> getAllCitas() { return citaDao.getAllCitas(); }
    public LiveData<CitaEntity> getCitaById(int id) { return citaDao.getCitaById(id); }
    public LiveData<List<CitaEntity>> searchCitas(String query) { return citaDao.searchCitas(query); }
    public LiveData<List<CitaEntity>> getCitasByFecha(String fecha) { return citaDao.getCitasByFecha(fecha); }
    public LiveData<List<CitaEntity>> getCitasByEstado(String estado) { return citaDao.getCitasByEstado(estado); }
    public LiveData<List<CitaEntity>> getCitasProximas(String fecha, String horaActual) {
        return citaDao.getCitasProximas(fecha, horaActual);
    }
}
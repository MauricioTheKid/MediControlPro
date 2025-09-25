package com.example.medicontrolpro.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpedienteRepository {
    private ExpedienteDao expedienteDao;
    private ExecutorService executorService;

    public ExpedienteRepository(Context context) {
        AppDatabase database = DatabaseClient.getInstance(context).getAppDatabase();
        this.expedienteDao = database.expedienteDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(ExpedienteEntity expediente) {
        executorService.execute(() -> expedienteDao.insert(expediente));
    }

    public void update(ExpedienteEntity expediente) {
        executorService.execute(() -> expedienteDao.update(expediente));
    }

    public void delete(ExpedienteEntity expediente) {
        executorService.execute(() -> expedienteDao.delete(expediente));
    }

    public LiveData<List<ExpedienteEntity>> getAllExpedientes() {
        return expedienteDao.getAllExpedientes();
    }

    public LiveData<ExpedienteEntity> getExpedienteById(int id) {
        return expedienteDao.getExpedienteById(id);
    }

    public LiveData<ExpedienteEntity> getExpedienteByCitaId(int citaId) {
        return expedienteDao.getExpedienteByCitaId(citaId);
    }

    public LiveData<List<ExpedienteEntity>> searchExpedientes(String query) {
        return expedienteDao.searchExpedientes(query);
    }

    public LiveData<List<ExpedienteEntity>> getExpedientesByFecha(String fecha) {
        return expedienteDao.getExpedientesByFecha(fecha);
    }
}
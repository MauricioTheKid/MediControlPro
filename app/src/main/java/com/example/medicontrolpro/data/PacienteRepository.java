package com.example.medicontrolpro.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacienteRepository {
    private PacienteDao pacienteDao;
    private LiveData<PacienteEntity> paciente;
    private ExecutorService executorService;

    public PacienteRepository(Application application) {
        AppDatabase database = DatabaseClient.getInstance(application).getAppDatabase();
        pacienteDao = database.pacienteDao();
        paciente = pacienteDao.getPaciente();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<PacienteEntity> getPaciente() {
        return paciente;
    }

    public void insert(PacienteEntity paciente) {
        executorService.execute(() -> {
            if (pacienteDao.count() == 0) {
                pacienteDao.insert(paciente);
            }
        });
    }

    public void update(PacienteEntity paciente) {
        executorService.execute(() -> {
            paciente.fechaActualizacion = java.time.LocalDateTime.now().toString();
            pacienteDao.update(paciente);
        });
    }
}
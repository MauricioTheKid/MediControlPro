package com.example.medicontrolpro.ui.expediente;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.medicontrolpro.data.ExpedienteEntity;
import com.example.medicontrolpro.data.ExpedienteRepository;
import java.util.List;

public class ExpedienteViewModel extends AndroidViewModel {
    private ExpedienteRepository expedienteRepository;
    private LiveData<List<ExpedienteEntity>> allExpedientes;
    private MutableLiveData<String> mText;

    public ExpedienteViewModel(Application application) {
        super(application);
        expedienteRepository = new ExpedienteRepository(application);
        allExpedientes = expedienteRepository.getAllExpedientes();
        mText = new MutableLiveData<>();
        mText.setValue("Gestión de Expedientes Médicos");
    }

    public LiveData<String> getText() { return mText; }
    public LiveData<List<ExpedienteEntity>> getAllExpedientes() { return allExpedientes; }

    public void insert(ExpedienteEntity expediente) { expedienteRepository.insert(expediente); }
    public void update(ExpedienteEntity expediente) { expedienteRepository.update(expediente); }
    public void delete(ExpedienteEntity expediente) { expedienteRepository.delete(expediente); }
    public LiveData<ExpedienteEntity> getExpedienteById(int id) { return expedienteRepository.getExpedienteById(id); }
    public LiveData<ExpedienteEntity> getExpedienteByCitaId(int citaId) { return expedienteRepository.getExpedienteByCitaId(citaId); }
    public LiveData<List<ExpedienteEntity>> searchExpedientes(String query) { return expedienteRepository.searchExpedientes(query); }

    public static class Expediente {
        public int id;
        public int citaId;
        public String doctor;
        public String especialidad;
        public String fechaCita;
        public String diagnostico;
        public String tratamiento;
        public String medicamentos;
        public String notas;
        public String fechaCreacion;
        public String fechaModificacion;

        public Expediente(int id, int citaId, String doctor, String especialidad, String fechaCita,
                          String diagnostico, String tratamiento, String medicamentos, String notas,
                          String fechaCreacion, String fechaModificacion) {
            this.id = id;
            this.citaId = citaId;
            this.doctor = doctor;
            this.especialidad = especialidad;
            this.fechaCita = fechaCita;
            this.diagnostico = diagnostico;
            this.tratamiento = tratamiento;
            this.medicamentos = medicamentos;
            this.notas = notas;
            this.fechaCreacion = fechaCreacion;
            this.fechaModificacion = fechaModificacion;
        }

        // Getters
        public int getId() { return id; }
        public int getCitaId() { return citaId; }
        public String getDoctor() { return doctor; }
        public String getEspecialidad() { return especialidad; }
        public String getFechaCita() { return fechaCita; }
        public String getDiagnostico() { return diagnostico; }
        public String getTratamiento() { return tratamiento; }
        public String getMedicamentos() { return medicamentos; }
        public String getNotas() { return notas; }
        public String getFechaCreacion() { return fechaCreacion; }
        public String getFechaModificacion() { return fechaModificacion; }
    }
}
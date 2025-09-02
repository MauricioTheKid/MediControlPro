package com.example.medicontrolpro.ui.citas;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.medicontrolpro.data.CitaEntity;
import com.example.medicontrolpro.data.CitaRepository;
import java.util.ArrayList;
import java.util.List;

public class CitasViewModel extends AndroidViewModel {
    private CitaRepository citaRepository;
    private LiveData<List<CitaEntity>> allCitas;
    private MutableLiveData<String> mText;

    public CitasViewModel(Application application) {
        super(application);
        citaRepository = new CitaRepository(application);
        allCitas = citaRepository.getAllCitas();
        mText = new MutableLiveData<>();
        mText.setValue("Gestión de Citas Médicas");
    }

    public LiveData<String> getText() { return mText; }
    public LiveData<List<CitaEntity>> getAllCitas() { return allCitas; }

    public void insert(CitaEntity cita) { citaRepository.insert(cita); }
    public void update(CitaEntity cita) { citaRepository.update(cita); }
    public void delete(CitaEntity cita) { citaRepository.delete(cita); }
    public LiveData<CitaEntity> getCitaById(int id) { return citaRepository.getCitaById(id); }
    public LiveData<List<CitaEntity>> searchCitas(String query) { return citaRepository.searchCitas(query); }

    public static class Cita {
        public int id;
        public String doctor;
        public String especialidad;
        public String fecha;
        public String hora;
        public String motivo;
        public String estado;

        // Constructor para nueva cita (sin ID)
        public Cita(String doctor, String especialidad, String fecha, String hora, String motivo, String estado) {
            this.doctor = doctor;
            this.especialidad = especialidad;
            this.fecha = fecha;
            this.hora = hora;
            this.motivo = motivo;
            this.estado = estado;
        }

        // Constructor para cita existente (con ID)
        public Cita(int id, String doctor, String especialidad, String fecha, String hora, String motivo, String estado) {
            this.id = id;
            this.doctor = doctor;
            this.especialidad = especialidad;
            this.fecha = fecha;
            this.hora = hora;
            this.motivo = motivo;
            this.estado = estado;
        }

        // Getters y Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getDoctor() { return doctor; }
        public void setDoctor(String doctor) { this.doctor = doctor; }

        public String getEspecialidad() { return especialidad; }
        public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }

        public String getHora() { return hora; }
        public void setHora(String hora) { this.hora = hora; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}
package com.example.medicontrolpro.ui.perfil;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.medicontrolpro.data.PacienteEntity;
import com.example.medicontrolpro.data.PacienteRepository;

public class PerfilViewModel extends AndroidViewModel {

    private PacienteRepository pacienteRepository;
    private LiveData<PacienteEntity> pacienteLiveData;

    public PerfilViewModel(Application application) {
        super(application);
        pacienteRepository = new PacienteRepository(application);
        pacienteLiveData = pacienteRepository.getPaciente();
    }

    public LiveData<PacienteEntity> getPaciente() {
        return pacienteLiveData;
    }

    public void actualizarPaciente(PacienteEntity paciente) {
        pacienteRepository.update(paciente);
    }

    public void insertarPaciente(PacienteEntity paciente) {
        pacienteRepository.insert(paciente);
    }

    public static class Paciente {
        private int id;
        private String nombreCompleto;
        private String email;
        private String fechaNacimiento;
        private String genero;
        private String telefono;
        private String direccion;
        private String tipoSangre;
        private String alergias;
        private String condicionesMedicas;
        private String medicamentos;
        private String fotoPath;

        public Paciente(int id, String nombreCompleto, String email, String fechaNacimiento,
                        String genero, String telefono, String direccion,
                        String tipoSangre, String alergias, String condicionesMedicas,
                        String medicamentos, String fotoPath) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
            this.email = email;
            this.fechaNacimiento = fechaNacimiento;
            this.genero = genero;
            this.telefono = telefono;
            this.direccion = direccion;
            this.tipoSangre = tipoSangre;
            this.alergias = alergias;
            this.condicionesMedicas = condicionesMedicas;
            this.medicamentos = medicamentos;
            this.fotoPath = fotoPath;
        }

        public Paciente(PacienteEntity entity) {
            this.id = entity.id;
            this.nombreCompleto = entity.nombreCompleto;
            this.email = entity.email;
            this.fechaNacimiento = entity.fechaNacimiento;
            this.genero = entity.genero;
            this.telefono = entity.telefono;
            this.direccion = entity.direccion;
            this.tipoSangre = entity.tipoSangre;
            this.alergias = entity.alergias;
            this.condicionesMedicas = entity.condicionesMedicas;
            this.medicamentos = entity.medicamentos;
            this.fotoPath = entity.fotoPath;
        }

        public int getId() { return id; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getEmail() { return email; }
        public String getFechaNacimiento() { return fechaNacimiento; }
        public String getGenero() { return genero; }
        public String getTelefono() { return telefono; }
        public String getDireccion() { return direccion; }
        public String getTipoSangre() { return tipoSangre; }
        public String getAlergias() { return alergias; }
        public String getCondicionesMedicas() { return condicionesMedicas; }
        public String getMedicamentos() { return medicamentos; }
        public String getFotoPath() { return fotoPath; }
    }
}
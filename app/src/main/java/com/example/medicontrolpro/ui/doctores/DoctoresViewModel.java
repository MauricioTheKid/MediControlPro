package com.example.medicontrolpro.ui.doctores;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.medicontrolpro.data.DoctorEntity;
import com.example.medicontrolpro.data.DoctorRepository;
import java.util.List;

public class DoctoresViewModel extends AndroidViewModel {
    private DoctorRepository doctorRepository;
    private LiveData<List<DoctorEntity>> allDoctores;
    private MutableLiveData<String> mText;

    public DoctoresViewModel(Application application) {
        super(application);
        doctorRepository = new DoctorRepository(application);
        allDoctores = doctorRepository.getAllDoctores();
        mText = new MutableLiveData<>();
        mText.setValue("Mis Doctores");
    }

    public LiveData<String> getText() { return mText; }
    public LiveData<List<DoctorEntity>> getAllDoctores() { return allDoctores; }
    public LiveData<List<DoctorEntity>> getDoctoresFavoritos() { return doctorRepository.getDoctoresFavoritos(); }

    public void insert(DoctorEntity doctor) { doctorRepository.insert(doctor); }
    public void update(DoctorEntity doctor) { doctorRepository.update(doctor); }
    public void delete(DoctorEntity doctor) { doctorRepository.delete(doctor); }
    public LiveData<DoctorEntity> getDoctorById(int id) { return doctorRepository.getDoctorById(id); }
    public LiveData<List<DoctorEntity>> searchDoctores(String query) { return doctorRepository.searchDoctores(query); }
    public void updateFavorito(int id, boolean esFavorito) { doctorRepository.updateFavorito(id, esFavorito); }

    public static class Doctor {
        public int id;
        public String nombre;
        public String especialidad;
        public String telefono;
        public String email;
        public String direccion;
        public String horarios;
        public boolean esFavorito;
        public float calificacion;
        public String notasPaciente;
        public String fotoPath; // ✅ NUEVO CAMPO

        public Doctor(int id, String nombre, String especialidad, String telefono, String email,
                      String direccion, String horarios, boolean esFavorito, float calificacion, String notasPaciente) {
            this.id = id;
            this.nombre = nombre;
            this.especialidad = especialidad;
            this.telefono = telefono;
            this.email = email;
            this.direccion = direccion;
            this.horarios = horarios;
            this.esFavorito = esFavorito;
            this.calificacion = calificacion;
            this.notasPaciente = notasPaciente;
            this.fotoPath = ""; // ✅ Inicializar vacío
        }

        // ✅ NUEVO CONSTRUCTOR CON FOTO
        public Doctor(int id, String nombre, String especialidad, String telefono, String email,
                      String direccion, String horarios, boolean esFavorito, float calificacion,
                      String notasPaciente, String fotoPath) {
            this.id = id;
            this.nombre = nombre;
            this.especialidad = especialidad;
            this.telefono = telefono;
            this.email = email;
            this.direccion = direccion;
            this.horarios = horarios;
            this.esFavorito = esFavorito;
            this.calificacion = calificacion;
            this.notasPaciente = notasPaciente;
            this.fotoPath = fotoPath;
        }

        // ✅ GETTERS
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getEspecialidad() { return especialidad; }
        public String getTelefono() { return telefono; }
        public String getEmail() { return email; }
        public String getDireccion() { return direccion; }
        public String getHorarios() { return horarios; }
        public boolean isEsFavorito() { return esFavorito; }
        public float getCalificacion() { return calificacion; }
        public String getNotasPaciente() { return notasPaciente; }
        public String getFotoPath() { return fotoPath; } // ✅ NUEVO GETTER

        // ✅ SETTERS (IMPORTANTE: AGREGAR TODOS LOS SETTERS)
        public void setId(int id) { this.id = id; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public void setEmail(String email) { this.email = email; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public void setHorarios(String horarios) { this.horarios = horarios; }
        public void setEsFavorito(boolean esFavorito) { this.esFavorito = esFavorito; }
        public void setCalificacion(float calificacion) { this.calificacion = calificacion; }
        public void setNotasPaciente(String notasPaciente) { this.notasPaciente = notasPaciente; }

        // ✅ NUEVO SETTER PARA FOTO PATH (CRÍTICO)
        public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }
    }
}
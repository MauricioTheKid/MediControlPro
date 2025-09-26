package com.example.medicontrolpro.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "doctores")
public class DoctorEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String nombre = "";

    @NonNull
    public String especialidad = "";

    public String telefono = "";
    public String email = "";
    public String direccion = "";
    public String horarios = "";

    public boolean esFavorito = false;
    public float calificacion = 0f;
    public String notasPaciente = "";

    public long fechaCreacion;
    public boolean sincronizado = false;

    // Constructor por defecto (IMPORTANTE para Room)
    public DoctorEntity() {
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getNombre() { return nombre; }
    public void setNombre(@NonNull String nombre) { this.nombre = nombre; }

    @NonNull
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(@NonNull String especialidad) { this.especialidad = especialidad; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getHorarios() { return horarios; }
    public void setHorarios(String horarios) { this.horarios = horarios; }

    public boolean isEsFavorito() { return esFavorito; }
    public void setEsFavorito(boolean esFavorito) { this.esFavorito = esFavorito; }

    public float getCalificacion() { return calificacion; }
    public void setCalificacion(float calificacion) { this.calificacion = calificacion; }

    public String getNotasPaciente() { return notasPaciente; }
    public void setNotasPaciente(String notasPaciente) { this.notasPaciente = notasPaciente; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}
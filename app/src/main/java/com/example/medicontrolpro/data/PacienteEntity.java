package com.example.medicontrolpro.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pacientes")
public class PacienteEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombreCompleto;
    public String email;
    public String fechaNacimiento;
    public String genero;
    public String telefono;
    public String direccion;
    public String tipoSangre;
    public String alergias;
    public String condicionesMedicas;
    public String medicamentos;
    public String fotoPath;
    public String fechaCreacion;
    public String fechaActualizacion;

    public PacienteEntity() {
        this.fechaCreacion = java.time.LocalDateTime.now().toString();
        this.fechaActualizacion = java.time.LocalDateTime.now().toString();
    }
}
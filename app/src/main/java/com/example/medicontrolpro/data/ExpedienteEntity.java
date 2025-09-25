package com.example.medicontrolpro.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.annotation.NonNull;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "expedientes",
        foreignKeys = @ForeignKey(entity = CitaEntity.class,
                parentColumns = "id",
                childColumns = "citaId",
                onDelete = CASCADE))
public class ExpedienteEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int citaId; // ID de la cita relacionada

    @NonNull
    public String doctor = "";

    @NonNull
    public String especialidad = "";

    @NonNull
    public String fechaCita = "";

    @NonNull
    public String diagnostico = "";

    @NonNull
    public String tratamiento = "";

    @NonNull
    public String medicamentos = "";

    @NonNull
    public String notas = "";

    @NonNull
    public String fechaCreacion = "";

    @NonNull
    public String fechaModificacion = "";

    public boolean sincronizado = false;

    // Constructor por defecto
    public ExpedienteEntity() {}

    // Constructor desde cita
    public ExpedienteEntity(CitaEntity cita) {
        this.citaId = cita.id;
        this.doctor = cita.doctor;
        this.especialidad = cita.especialidad;
        this.fechaCita = cita.fecha;
        this.diagnostico = "";
        this.tratamiento = "";
        this.medicamentos = "";
        this.notas = "Cita completada: " + cita.motivo;
        this.fechaCreacion = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        this.fechaModificacion = this.fechaCreacion;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCitaId() { return citaId; }
    public void setCitaId(int citaId) { this.citaId = citaId; }

    @NonNull
    public String getDoctor() { return doctor; }
    public void setDoctor(@NonNull String doctor) { this.doctor = doctor; }

    @NonNull
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(@NonNull String especialidad) { this.especialidad = especialidad; }

    @NonNull
    public String getFechaCita() { return fechaCita; }
    public void setFechaCita(@NonNull String fechaCita) { this.fechaCita = fechaCita; }

    @NonNull
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(@NonNull String diagnostico) { this.diagnostico = diagnostico; }

    @NonNull
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(@NonNull String tratamiento) { this.tratamiento = tratamiento; }

    @NonNull
    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(@NonNull String medicamentos) { this.medicamentos = medicamentos; }

    @NonNull
    public String getNotas() { return notas; }
    public void setNotas(@NonNull String notas) { this.notas = notas; }

    @NonNull
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(@NonNull String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @NonNull
    public String getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(@NonNull String fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}
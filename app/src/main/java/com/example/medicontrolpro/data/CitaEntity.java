package com.example.medicontrolpro.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "citas")
public class CitaEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String doctor = "";

    @NonNull
    public String especialidad = "";

    @NonNull
    public String fecha = "";

    @NonNull
    public String hora = "";

    @NonNull
    public String motivo = "";

    @NonNull
    public String estado = "Pendiente";

    public long fechaTimestamp;
    public boolean sincronizado = false;

    // Constructor por defecto (requerido por Room)
    public CitaEntity() {}

    // Constructor conveniente
    public CitaEntity(@NonNull String doctor, @NonNull String especialidad,
                      @NonNull String fecha, @NonNull String hora,
                      @NonNull String motivo, @NonNull String estado) {
        this.doctor = doctor;
        this.especialidad = especialidad;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
        this.fechaTimestamp = System.currentTimeMillis();
    }

    // Getters y Setters (Room los necesita)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getDoctor() { return doctor; }
    public void setDoctor(@NonNull String doctor) { this.doctor = doctor; }

    @NonNull
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(@NonNull String especialidad) { this.especialidad = especialidad; }

    @NonNull
    public String getFecha() { return fecha; }
    public void setFecha(@NonNull String fecha) { this.fecha = fecha; }

    @NonNull
    public String getHora() { return hora; }
    public void setHora(@NonNull String hora) { this.hora = hora; }

    @NonNull
    public String getMotivo() { return motivo; }
    public void setMotivo(@NonNull String motivo) { this.motivo = motivo; }

    @NonNull
    public String getEstado() { return estado; }
    public void setEstado(@NonNull String estado) { this.estado = estado; }

    public long getFechaTimestamp() { return fechaTimestamp; }
    public void setFechaTimestamp(long fechaTimestamp) { this.fechaTimestamp = fechaTimestamp; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}
package com.example.medicontrolpro.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class UsuarioEntity implements Parcelable {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String email;

    public String nombreCompleto;
    public String password;
    public String telefono;
    public String direccion;
    public String tipoSangre;
    public String fechaNacimiento;
    public String genero;
    public String alergias;
    public String condicionesMedicas;
    public String medicamentosActuales;
    public boolean sincronizado;

    // ✅ NUEVO CAMPO: Foto de perfil
    @ColumnInfo(name = "foto_perfil")
    public String fotoPerfil;

    public UsuarioEntity() {
        // Constructor vacío necesario para Room
        this.email = ""; // Inicializar con valor por defecto
        this.fotoPerfil = ""; // Inicializar como string vacío
    }

    // Constructor para crear usuarios fácilmente
    public UsuarioEntity(@NonNull String email, String nombreCompleto) {
        this.email = email;
        this.nombreCompleto = nombreCompleto != null ? nombreCompleto : "Usuario";
        this.password = "";
        this.sincronizado = false;
        this.fotoPerfil = ""; // Inicializar como string vacío
    }

    protected UsuarioEntity(Parcel in) {
        email = in.readString();
        nombreCompleto = in.readString();
        password = in.readString();
        telefono = in.readString();
        direccion = in.readString();
        tipoSangre = in.readString();
        fechaNacimiento = in.readString();
        genero = in.readString();
        alergias = in.readString();
        condicionesMedicas = in.readString();
        medicamentosActuales = in.readString();
        sincronizado = in.readByte() != 0;
        fotoPerfil = in.readString(); // ✅ NUEVO: Leer fotoPerfil
    }

    public static final Creator<UsuarioEntity> CREATOR = new Creator<UsuarioEntity>() {
        @Override
        public UsuarioEntity createFromParcel(Parcel in) {
            return new UsuarioEntity(in);
        }

        @Override
        public UsuarioEntity[] newArray(int size) {
            return new UsuarioEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(nombreCompleto);
        dest.writeString(password);
        dest.writeString(telefono);
        dest.writeString(direccion);
        dest.writeString(tipoSangre);
        dest.writeString(fechaNacimiento);
        dest.writeString(genero);
        dest.writeString(alergias);
        dest.writeString(condicionesMedicas);
        dest.writeString(medicamentosActuales);
        dest.writeByte((byte) (sincronizado ? 1 : 0));
        dest.writeString(fotoPerfil); // ✅ NUEVO: Escribir fotoPerfil
    }

    // Getters y Setters útiles
    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTipoSangre() {
        return tipoSangre;
    }

    public void setTipoSangre(String tipoSangre) {
        this.tipoSangre = tipoSangre;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getCondicionesMedicas() {
        return condicionesMedicas;
    }

    public void setCondicionesMedicas(String condicionesMedicas) {
        this.condicionesMedicas = condicionesMedicas;
    }

    public String getMedicamentosActuales() {
        return medicamentosActuales;
    }

    public void setMedicamentosActuales(String medicamentosActuales) {
        this.medicamentosActuales = medicamentosActuales;
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }

    // ✅ NUEVO: Getter y Setter para fotoPerfil
    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}
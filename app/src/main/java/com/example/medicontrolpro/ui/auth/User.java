package com.example.medicontrolpro.ui.auth;

public class User {
    private String email;
    private String nombreCompleto;
    private String password;
    private boolean sincronizadoFirebase;

    public User() {
        // Constructor vacío requerido
    }

    public User(String email, String nombreCompleto, String password) {
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.password = password;
        this.sincronizadoFirebase = false;
    }

    // Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isSincronizadoFirebase() { return sincronizadoFirebase; }
    public void setSincronizadoFirebase(boolean sincronizadoFirebase) { this.sincronizadoFirebase = sincronizadoFirebase; }
}
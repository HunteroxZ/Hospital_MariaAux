
package com.mariaaux.hospital_backend.dto;

public class CredencialesAdmin {
    private String dni;
    private String contrasena;

    public CredencialesAdmin() {
    }

    public CredencialesAdmin(String dni, String contrasena) {
        this.dni = dni;
        this.contrasena = contrasena;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
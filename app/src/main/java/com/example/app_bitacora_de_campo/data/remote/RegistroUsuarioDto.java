package com.example.app_bitacora_de_campo.data.remote;

public class RegistroUsuarioDto {
    public String correo;
    public String password;
    public String rol;
    public RegistroUsuarioDto(String correo, String password, String rol) {
        this.correo = correo;
        this.password = password;
        this.rol = rol;
    }
}
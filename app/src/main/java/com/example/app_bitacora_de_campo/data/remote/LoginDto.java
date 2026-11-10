package com.example.app_bitacora_de_campo.data.remote;

public class LoginDto {
    public String correo;
    public String password;
    public LoginDto(String correo, String password) {
        this.correo = correo;
        this.password = password;
    }
}
package com.example.app_bitacora_de_campo.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {
    private static final String PREF_NAME = "bitacora_secure_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ROL = "user_rol";
    private SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void guardarSesion(String token, String rol) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_TOKEN, token).putString(KEY_ROL, rol).apply();
        }
    }

    public String obtenerToken() {
        return sharedPreferences != null ? sharedPreferences.getString(KEY_TOKEN, null) : null;
    }

    public String obtenerRol() {
        return sharedPreferences != null ? sharedPreferences.getString(KEY_ROL, null) : null;
    }

    public void cerrarSesion() {
        if (sharedPreferences != null) sharedPreferences.edit().clear().apply();
    }

    public boolean estaLogueado() {
        return obtenerToken() != null;
    }
}
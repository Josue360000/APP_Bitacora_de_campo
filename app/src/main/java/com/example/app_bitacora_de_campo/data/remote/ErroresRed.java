package com.example.app_bitacora_de_campo.data.remote;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ErroresRed {

    public static String mensajeHttp(int codigo) {
        if (codigo == 401) return "Tu sesión expiró o no es válida (401)";
        if (codigo == 403) return "No tienes permiso para ver esto (403)";
        if (codigo == 404) return "No se encontró el recurso (404)";
        if (codigo >= 500) return "El servidor tuvo un problema (" + codigo + ")";
        return "Respuesta inesperada del servidor (" + codigo + ")";
    }

    public static String mensajeExcepcion(Throwable t) {
        if (t instanceof UnknownHostException) return "Sin conexión a internet";
        if (t instanceof SocketTimeoutException) return "El servidor tardó demasiado en responder";
        if (t instanceof ConnectException) return "No se pudo conectar con el servidor";
        if (t instanceof JsonSyntaxException) return "La respuesta no tiene el formato esperado";
        if (t instanceof IOException) return "Error de red";
        return "Ocurrió un error inesperado";
    }

    public static String mensajeVolley(VolleyError e) {
        if (e.networkResponse != null) return mensajeHttp(e.networkResponse.statusCode);
        if (e instanceof TimeoutError) return "El servidor tardó demasiado en responder";
        if (e instanceof NoConnectionError) {
            if (e.getCause() instanceof UnknownHostException) return "Sin conexión a internet";
            return "No se pudo conectar con el servidor";
        }
        if (e instanceof AuthFailureError) return mensajeHttp(401);
        if (e instanceof ServerError) return mensajeHttp(500);
        if (e instanceof NetworkError) return "Error de red";
        if (e instanceof ParseError) return "La respuesta no tiene el formato esperado";
        return "Ocurrió un error inesperado";
    }
}

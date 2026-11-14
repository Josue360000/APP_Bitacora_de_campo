package com.example.app_bitacora_de_campo.laboratorio;

import com.google.gson.JsonParseException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

// Los mensajes que ve el usuario cuando algo falla.
// Son los mismos para las tres librerías, así la comparación es justa.
public final class MensajesError {

    public static final String TIEMPO_AGOTADO =
            "La API tardó demasiado en responder (tiempo de espera agotado).";
    public static final String SIN_CONEXION =
            "No se pudo conectar con la API. Revisa el internet o si la API está encendida.";
    public static final String RESPUESTA_INVALIDA =
            "La API respondió algo que no es la lista de registros.";
    public static final String ERROR_RED = "Error de red al consultar la API.";

    private MensajesError() {
    }

    // La API SÍ respondió, pero con un código de error.
    public static String paraCodigoHttp(int codigo) {
        if (codigo == 401) {
            return "401: no autorizado. El token venció o no se envió; inicia sesión de nuevo.";
        }
        if (codigo == 403) {
            return "403: no tienes permiso para ver esto.";
        }
        if (codigo == 404) {
            return "404: esa dirección no existe en la API.";
        }
        if (codigo >= 500) {
            return codigo + ": error interno del servidor.";
        }
        return "La API respondió con el código " + codigo + ".";
    }

    // La API NO respondió (o respondió algo que no se pudo leer).
    public static String paraExcepcion(Throwable error) {
        if (error instanceof SocketTimeoutException) {
            return TIEMPO_AGOTADO;
        }
        if (error instanceof ConnectException || error instanceof UnknownHostException) {
            return SIN_CONEXION;
        }
        if (error instanceof JsonParseException) {
            return RESPUESTA_INVALIDA;
        }
        return ERROR_RED;
    }
}

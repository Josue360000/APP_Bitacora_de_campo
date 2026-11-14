package com.example.app_bitacora_de_campo.laboratorio;

import android.content.Context;
import android.os.SystemClock;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_bitacora_de_campo.data.remote.RetrofitClient;
import com.example.app_bitacora_de_campo.data.remote.SessionManager;

import java.util.HashMap;
import java.util.Map;

// Consulta GET /api/registros con VOLLEY:
//   1. armar un JsonArrayRequest (una petición que espera un arreglo JSON),
//   2. poner el token sobrescribiendo getHeaders(),
//   3. configurar el tiempo de espera y los reintentos con DefaultRetryPolicy,
//   4. meter la petición en la cola (RequestQueue), que la manda en segundo plano.
// Volley entrega la respuesta en el HILO PRINCIPAL, así que se puede tocar la pantalla directo.
public class ConsultaVolley {

    private static final String URL = RetrofitClient.BASE_URL + "api/registros";
    private static final int TIEMPO_ESPERA_MS = 30000; // 30 s, igual que Retrofit y OkHttp
    private static final int REINTENTOS = 0;          // sin reintentos, para medir parejo

    private final RequestQueue cola;
    private final SessionManager sesion;

    public ConsultaVolley(Context context) {
        cola = Volley.newRequestQueue(context.getApplicationContext());
        sesion = new SessionManager(context.getApplicationContext());
    }

    public void listar(ResultadoConsulta resultado) {
        long inicio = SystemClock.elapsedRealtime(); // cronómetro
        String token = sesion.obtenerToken();        // el token que se guardó al iniciar sesión

        // 1. La petición: GET a la dirección, sin cuerpo (null).
        JsonArrayRequest peticion = new JsonArrayRequest(Request.Method.GET, URL, null,

                // Respuesta correcta (200): Volley ya la convirtió en un arreglo JSON; lo contamos.
                respuesta -> resultado.onExito(respuesta.length(),
                        SystemClock.elapsedRealtime() - inicio),

                // Cualquier falla llega aquí como VolleyError, INCLUSO el 401 y el 500.
                error -> resultado.onError(mensajePara(error), detallePara(error),
                        SystemClock.elapsedRealtime() - inicio)) {

            // 2. Volley pide los encabezados con este método: aquí va el token.
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> encabezados = new HashMap<>();
                encabezados.put("Accept", "application/json");
                if (token != null) {
                    encabezados.put("Authorization", "Bearer " + token);
                }
                return encabezados;
            }
        };

        // 3. Tiempo de espera y reintentos.
        peticion.setRetryPolicy(new DefaultRetryPolicy(
                TIEMPO_ESPERA_MS, REINTENTOS, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        peticion.setShouldCache(false); // sin caché: cada medición va de verdad a la API

        // 4. A la cola: Volley la ejecuta en segundo plano y nos avisa en el hilo principal.
        cola.add(peticion);
    }

    // Traduce el VolleyError al mismo mensaje que usan Retrofit y OkHttp.
    private static String mensajePara(VolleyError error) {
        if (error.networkResponse != null) {
            // La API sí respondió, pero con un código de error (401, 500...).
            return MensajesError.paraCodigoHttp(error.networkResponse.statusCode);
        }
        if (error instanceof TimeoutError) {
            return MensajesError.TIEMPO_AGOTADO;
        }
        if (error instanceof NoConnectionError) {
            return MensajesError.SIN_CONEXION;
        }
        if (error instanceof ParseError) {
            return MensajesError.RESPUESTA_INVALIDA;
        }
        return MensajesError.ERROR_RED;
    }

    // El detalle técnico para Logcat: el tipo de error de Volley y su causa.
    private static String detallePara(VolleyError error) {
        String tipo = error.getClass().getSimpleName();
        if (error.networkResponse != null) {
            return tipo + " (HTTP " + error.networkResponse.statusCode + ")";
        }
        return (error.getCause() != null) ? tipo + ": " + error.getCause() : tipo;
    }
}

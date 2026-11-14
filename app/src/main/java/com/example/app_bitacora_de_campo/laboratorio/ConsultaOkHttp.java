package com.example.app_bitacora_de_campo.laboratorio;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.remote.RetrofitClient;
import com.example.app_bitacora_de_campo.data.remote.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Consulta GET /api/registros con OKHTTP, a mano:
//   1. armar el Request (dirección + token),
//   2. mandarlo con enqueue() (en segundo plano),
//   3. convertir el JSON en objetos con Gson,
//   4. volver al hilo principal para avisar a la pantalla.
public class ConsultaOkHttp {

    private static final String URL = RetrofitClient.BASE_URL + "api/registros";

    // Un solo cliente para todas las consultas (crear uno nuevo cada vez es más lento).
    // Mismo tiempo de espera que el Retrofit de la app (30 s), para comparar parejo.
    private static final OkHttpClient CLIENTE = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final SessionManager sesion;
    private final Gson gson = new Gson();

    // OkHttp responde en un hilo de fondo. Con este Handler mandamos el resultado
    // al hilo principal, que es el único que puede tocar la pantalla.
    private final Handler hiloPrincipal = new Handler(Looper.getMainLooper());

    public ConsultaOkHttp(Context context) {
        sesion = new SessionManager(context.getApplicationContext());
    }

    public void listar(ResultadoConsulta resultado) {
        long inicio = SystemClock.elapsedRealtime(); // cronómetro

        // 1. Armar la petición a mano: la dirección y los encabezados.
        Request.Builder constructor = new Request.Builder()
                .url(URL)
                .header("Accept", "application/json");

        String token = sesion.obtenerToken(); // el token que se guardó al iniciar sesión
        if (token != null) {
            constructor.header("Authorization", "Bearer " + token);
        }
        Request peticion = constructor.build();

        // 2. Mandarla con enqueue(): se ejecuta en segundo plano y no congela la pantalla.
        CLIENTE.newCall(peticion).enqueue(new Callback() {

            // OJO: onResponse y onFailure corren en un HILO DE FONDO, no en el principal.

            @Override
            public void onResponse(Call call, Response response) {
                // try (...) cierra la respuesta al terminar (OkHttp lo pide para liberar la conexión).
                try (Response r = response) {
                    String json = (r.body() != null) ? r.body().string() : "";

                    if (!r.isSuccessful()) {
                        // La API respondió con error (401, 500...). OkHttp NO lo trata como falla:
                        // llega aquí y hay que revisar el código a mano.
                        entregarError(resultado, MensajesError.paraCodigoHttp(r.code()),
                                "HTTP " + r.code(), inicio);
                        return;
                    }

                    // 3. Convertir el JSON (texto) en una lista de objetos Java con Gson.
                    Type tipoLista = new TypeToken<List<RegistroDto>>() { }.getType();
                    List<RegistroDto> lista = gson.fromJson(json, tipoLista);
                    int cantidad = (lista != null) ? lista.size() : 0;

                    entregarExito(resultado, cantidad, inicio);

                } catch (IOException | RuntimeException e) {
                    // No se pudo leer o convertir la respuesta. Se atrapa para que la app no se cierre.
                    entregarError(resultado, MensajesError.paraExcepcion(e),
                            e.getClass().getSimpleName() + ": " + e.getMessage(), inicio);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // No hubo respuesta: API apagada, sin internet, tiempo agotado...
                entregarError(resultado, MensajesError.paraExcepcion(e),
                        e.getClass().getSimpleName() + ": " + e.getMessage(), inicio);
            }
        });
    }

    // 4. Volver al hilo principal. El tiempo se toma al llegar ahí,
    //    igual que en Retrofit y Volley, para que la comparación sea justa.
    private void entregarExito(ResultadoConsulta resultado, int cantidad, long inicio) {
        hiloPrincipal.post(() ->
                resultado.onExito(cantidad, SystemClock.elapsedRealtime() - inicio));
    }

    private void entregarError(ResultadoConsulta resultado, String mensaje, String detalle, long inicio) {
        hiloPrincipal.post(() ->
                resultado.onError(mensaje, detalle, SystemClock.elapsedRealtime() - inicio));
    }
}

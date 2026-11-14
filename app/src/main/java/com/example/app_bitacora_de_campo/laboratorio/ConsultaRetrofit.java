package com.example.app_bitacora_de_campo.laboratorio;

import android.os.SystemClock;

import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.repository.RegistroRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Consulta GET /api/registros con RETROFIT.
// Reutiliza el repositorio de la Tarea 2.3 (RegistroRepository), como pide la tarea.
// Retrofit ya se encarga de armar la petición, mandar el token y convertir el JSON.
public class ConsultaRetrofit {

    private final RegistroRepository repositorio = new RegistroRepository();

    public void listar(ResultadoConsulta resultado) {
        // Cronómetro: guardamos la hora de inicio en milisegundos.
        long inicio = SystemClock.elapsedRealtime();

        repositorio.listar(new Callback<List<RegistroDto>>() {

            // Retrofit llama a onResponse y onFailure en el HILO PRINCIPAL,
            // así que se puede avisar a la pantalla directamente.
            @Override
            public void onResponse(Call<List<RegistroDto>> call, Response<List<RegistroDto>> response) {
                long ms = SystemClock.elapsedRealtime() - inicio;

                if (response.isSuccessful() && response.body() != null) {
                    // 200: la lista ya llega convertida de JSON a objetos Java (lo hace Gson por dentro).
                    resultado.onExito(response.body().size(), ms);
                } else {
                    // La API respondió, pero con error (401, 500...).
                    // Para Retrofit eso NO es una falla: llega aquí y hay que revisar el código.
                    resultado.onError(MensajesError.paraCodigoHttp(response.code()),
                            "HTTP " + response.code(), ms);
                }
            }

            @Override
            public void onFailure(Call<List<RegistroDto>> call, Throwable t) {
                // No hubo respuesta: API apagada, sin internet, tiempo agotado...
                long ms = SystemClock.elapsedRealtime() - inicio;
                resultado.onError(MensajesError.paraExcepcion(t),
                        t.getClass().getSimpleName() + ": " + t.getMessage(), ms);
            }
        });
    }
}

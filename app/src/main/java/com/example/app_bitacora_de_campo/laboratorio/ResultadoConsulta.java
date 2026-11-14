package com.example.app_bitacora_de_campo.laboratorio;

// Así le avisa cada librería (Retrofit, OkHttp o Volley) a la pantalla cómo le fue.
// Los dos métodos se llaman SIEMPRE en el hilo principal, que es el único que puede tocar la pantalla.
public interface ResultadoConsulta {

    // La API respondió bien: cuántos registros llegaron y cuánto tardó.
    void onExito(int cantidad, long milisegundos);

    // Algo falló: el mensaje para el usuario, el detalle técnico (para Logcat) y cuánto tardó en fallar.
    void onError(String mensajeUsuario, String detalleTecnico, long milisegundos);
}

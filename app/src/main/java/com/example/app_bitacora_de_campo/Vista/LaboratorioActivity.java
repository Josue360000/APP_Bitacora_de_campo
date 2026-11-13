package com.example.app_bitacora_de_campo.Vista;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_bitacora_de_campo.R;

// Tarea 2.5: pantalla de laboratorio.
// Cada botón va a pedir la lista de registros (GET /api/registros) con una librería distinta
// (Retrofit, OkHttp o Volley) y a mostrar cuántos registros llegaron y cuánto tardó.
// Por ahora los botones solo avisan que todavía no están conectados.
public class LaboratorioActivity extends AppCompatActivity {

    private TextView txtResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laboratorio);

        // 1. Buscar en el diseño (activity_laboratorio.xml) lo que vamos a usar.
        txtResultado = findViewById(R.id.txtResultado);
        Button btnRetrofit = findViewById(R.id.btnRetrofit);
        Button btnOkHttp = findViewById(R.id.btnOkHttp);
        Button btnVolley = findViewById(R.id.btnVolley);
        Button btnVolver = findViewById(R.id.btnVolver);

        // 2. Decir qué pasa al tocar cada botón.
        btnRetrofit.setOnClickListener(v -> mostrarPendiente("Retrofit"));
        btnOkHttp.setOnClickListener(v -> mostrarPendiente("OkHttp"));
        btnVolley.setOnClickListener(v -> mostrarPendiente("Volley"));
        btnVolver.setOnClickListener(v -> finish()); // cierra esta pantalla y vuelve a la lista
    }

    // Temporal: en los siguientes pasos cada botón hará la consulta real.
    private void mostrarPendiente(String libreria) {
        txtResultado.setText(libreria + ": todavía no está conectado a la API.");
    }
}

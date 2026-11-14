package com.example.app_bitacora_de_campo.Vista;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_bitacora_de_campo.R;
import com.example.app_bitacora_de_campo.data.remote.RetrofitClient;
import com.example.app_bitacora_de_campo.laboratorio.ConsultaOkHttp;
import com.example.app_bitacora_de_campo.laboratorio.ConsultaRetrofit;
import com.example.app_bitacora_de_campo.laboratorio.ConsultaVolley;
import com.example.app_bitacora_de_campo.laboratorio.ResultadoConsulta;

// Tarea 2.5: pantalla de laboratorio.
// Cada botón pide la lista de registros (GET /api/registros) con una librería distinta
// (Retrofit, OkHttp o Volley) y muestra cuántos registros llegaron y cuánto tardó.
public class LaboratorioActivity extends AppCompatActivity {

    // Etiqueta para buscar estos mensajes en Logcat (sirve para las capturas del PDF).
    private static final String TAG = "LABORATORIO";

    private TextView txtResultado;
    private final ConsultaRetrofit consultaRetrofit = new ConsultaRetrofit();
    private ConsultaOkHttp consultaOkHttp;
    private ConsultaVolley consultaVolley;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laboratorio);

        // Para que Retrofit encuentre el token guardado al iniciar sesión.
        RetrofitClient.inicializar(this);
        consultaOkHttp = new ConsultaOkHttp(this);
        consultaVolley = new ConsultaVolley(this);

        // 1. Buscar en el diseño (activity_laboratorio.xml) lo que vamos a usar.
        txtResultado = findViewById(R.id.txtResultado);
        Button btnRetrofit = findViewById(R.id.btnRetrofit);
        Button btnOkHttp = findViewById(R.id.btnOkHttp);
        Button btnVolley = findViewById(R.id.btnVolley);
        Button btnVolver = findViewById(R.id.btnVolver);

        // 2. Decir qué pasa al tocar cada botón.
        btnRetrofit.setOnClickListener(v -> consultarConRetrofit());
        btnOkHttp.setOnClickListener(v -> consultarConOkHttp());
        btnVolley.setOnClickListener(v -> consultarConVolley());
        btnVolver.setOnClickListener(v -> finish()); // cierra esta pantalla y vuelve a la lista
    }

    private void consultarConRetrofit() {
        txtResultado.setText("Retrofit: consultando...");
        consultaRetrofit.listar(mostrarEnPantalla("Retrofit"));
    }

    private void consultarConOkHttp() {
        txtResultado.setText("OkHttp: consultando...");
        consultaOkHttp.listar(mostrarEnPantalla("OkHttp"));
    }

    private void consultarConVolley() {
        txtResultado.setText("Volley: consultando...");
        consultaVolley.listar(mostrarEnPantalla("Volley"));
    }

    // Lo que se hace con el resultado de cualquier librería: escribirlo en el cuadro gris y en Logcat.
    private ResultadoConsulta mostrarEnPantalla(String libreria) {
        return new ResultadoConsulta() {
            @Override
            public void onExito(int cantidad, long milisegundos) {
                txtResultado.setText(libreria
                        + "\nRegistros recibidos: " + cantidad
                        + "\nTiempo: " + milisegundos + " ms");
                Log.d(TAG, libreria + " OK: " + cantidad + " registros en " + milisegundos + " ms");
            }

            @Override
            public void onError(String mensajeUsuario, String detalleTecnico, long milisegundos) {
                txtResultado.setText(libreria
                        + "\nError: " + mensajeUsuario
                        + "\nTiempo: " + milisegundos + " ms");
                Log.e(TAG, libreria + " ERROR en " + milisegundos + " ms -> " + detalleTecnico);
            }
        };
    }
}

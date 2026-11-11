package com.example.app_bitacora_de_campo.Vista;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_bitacora_de_campo.R;
import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.remote.RetrofitClient;
import com.example.app_bitacora_de_campo.data.remote.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;

public class ComparativaActivity extends AppCompatActivity {

    private TextView txtResultadoVolley;
    private TextView txtResultadoOkHttp;
    private TextView txtResultadoRetrofit;
    private SessionManager sessionManager;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa);

        RetrofitClient.inicializar(this);
        sessionManager = new SessionManager(this);
        token = sessionManager.obtenerToken();

        txtResultadoVolley = findViewById(R.id.txtVolley);
        txtResultadoOkHttp = findViewById(R.id.txtOkHttp);
        txtResultadoRetrofit = findViewById(R.id.txtRetrofit);

        Button btnVolley = findViewById(R.id.btnVolley);
        Button btnOkHttp = findViewById(R.id.btnOkHttp);
        Button btnRetrofit = findViewById(R.id.btnRetrofit);

        btnVolley.setOnClickListener(v -> probarVolley());
        btnOkHttp.setOnClickListener(v -> probarOkHttp());
        btnRetrofit.setOnClickListener(v -> probarRetrofit());
    }

    private void probarVolley() {
        txtResultadoVolley.setText("Consultando...");
        long inicio = System.currentTimeMillis();

        String url = "https://proyectosic.lat/api/registros";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    long fin = System.currentTimeMillis();
                    long tiempo = fin - inicio;
                    int cantidad = response.length();
                    txtResultadoVolley.setText("Registros: " + cantidad + " | Tiempo: " + tiempo + " ms");
                },
                error -> {
                    long fin = System.currentTimeMillis();
                    long tiempo = fin - inicio;
                    String mensaje = "Error: " + error.toString();
                    if (error.networkResponse != null) {
                        mensaje = "Error codigo: " + error.networkResponse.statusCode;
                    }
                    txtResultadoVolley.setText(mensaje + " | Tiempo: " + tiempo + " ms");
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void probarOkHttp() {
        txtResultadoOkHttp.setText("Consultando...");
        long inicio = System.currentTimeMillis();

        OkHttpClient client = new OkHttpClient();



        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://proyectosic.lat/api/registros")
                .addHeader("Authorization", "Bearer " + (token != null ? token : ""))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                long fin = System.currentTimeMillis();
                long tiempo = fin - inicio;
                runOnUiThread(() -> {
                    txtResultadoOkHttp.setText("Error de red: " + e.getMessage() + " | Tiempo: " + tiempo + " ms");
                    Toast.makeText(ComparativaActivity.this, "Error de red", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                long fin = System.currentTimeMillis();
                long tiempo = fin - inicio;
                String body = response.body() != null ? response.body().string() : "";
                int codigo = response.code();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            Gson gson = new Gson();
                            Type tipo = new TypeToken<List<RegistroDto>>() {}.getType();
                            List<RegistroDto> lista = gson.fromJson(body, tipo);
                            int cantidad = lista != null ? lista.size() : 0;
                            txtResultadoOkHttp.setText("Registros: " + cantidad + " | Tiempo: " + tiempo + " ms");
                        } catch (Exception e) {
                            txtResultadoOkHttp.setText("Error al parsear JSON | Tiempo: " + tiempo + " ms");
                        }
                    } else {
                        txtResultadoOkHttp.setText("Error codigo: " + codigo + " | Tiempo: " + tiempo + " ms");
                        Toast.makeText(ComparativaActivity.this, "Error " + codigo, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void probarRetrofit() {
        txtResultadoRetrofit.setText("Consultando...");
        long inicio = System.currentTimeMillis();

        RetrofitClient.getApi().listar().enqueue(new retrofit2.Callback<List<RegistroDto>>() {
            @Override
            public void onResponse(retrofit2.Call<List<RegistroDto>> call, retrofit2.Response<List<RegistroDto>> response) {
                long fin = System.currentTimeMillis();
                long tiempo = fin - inicio;
                if (response.isSuccessful() && response.body() != null) {
                    int cantidad = response.body().size();
                    txtResultadoRetrofit.setText("Registros: " + cantidad + " | Tiempo: " + tiempo + " ms");
                } else {
                    txtResultadoRetrofit.setText("Error codigo: " + response.code() + " | Tiempo: " + tiempo + " ms");
                    Toast.makeText(ComparativaActivity.this, "Error " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<RegistroDto>> call, Throwable t) {
                long fin = System.currentTimeMillis();
                long tiempo = fin - inicio;
                txtResultadoRetrofit.setText("Error de red: " + t.getMessage() + " | Tiempo: " + tiempo + " ms");
                Toast.makeText(ComparativaActivity.this, "Error de red", Toast.LENGTH_LONG).show();
            }
        });
    }
}
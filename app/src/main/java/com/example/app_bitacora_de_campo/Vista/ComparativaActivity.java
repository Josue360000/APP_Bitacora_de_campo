package com.example.app_bitacora_de_campo.Vista;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.app_bitacora_de_campo.R;
import com.example.app_bitacora_de_campo.data.remote.ErroresRed;
import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.remote.RetrofitClient;
import com.example.app_bitacora_de_campo.data.remote.SessionManager;
import com.example.app_bitacora_de_campo.data.repository.RegistroRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ComparativaActivity extends AppCompatActivity {

    private static final String TAG = "COMPARATIVA";
    private static final String TOKEN_FALSO = "eyJhbGciOiJIUzI1NiJ9.e30.firma_invalida";

    private EditText edtBaseUrl;
    private RadioGroup grupoEndpoint, grupoToken;
    private TextView txtResultado, txtHistorial;
    private ProgressBar progress;

    private SessionManager sessionManager;
    private RequestQueue colaVolley;
    private OkHttpClient clienteOkHttp;
    private RegistroRepository repositorio;
    private final Gson gson = new Gson();
    private final Type tipoLista = new TypeToken<List<RegistroDto>>() {}.getType();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa);

        RetrofitClient.inicializar(this);
        sessionManager = new SessionManager(this);
        repositorio = new RegistroRepository();
        colaVolley = Volley.newRequestQueue(getApplicationContext());
        clienteOkHttp = new OkHttpClient.Builder()
                .connectTimeout(RetrofitClient.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .readTimeout(RetrofitClient.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .build();

        edtBaseUrl = findViewById(R.id.edtBaseUrl);
        grupoEndpoint = findViewById(R.id.grupoEndpoint);
        grupoToken = findViewById(R.id.grupoToken);
        txtResultado = findViewById(R.id.txtResultado);
        txtHistorial = findViewById(R.id.txtHistorial);
        progress = findViewById(R.id.progressComparativa);

        edtBaseUrl.setText(RetrofitClient.BASE_URL);

        Button btnVolley = findViewById(R.id.btnVolley);
        Button btnOkHttp = findViewById(R.id.btnOkHttp);
        Button btnRetrofit = findViewById(R.id.btnRetrofit);
        Button btnLimpiar = findViewById(R.id.btnLimpiar);

        btnVolley.setOnClickListener(v -> consultarConVolley());
        btnOkHttp.setOnClickListener(v -> consultarConOkHttp());
        btnRetrofit.setOnClickListener(v -> consultarConRetrofit());
        btnLimpiar.setOnClickListener(v -> txtHistorial.setText(""));
    }

    private String urlCompleta() {
        String base = edtBaseUrl.getText().toString().trim();
        if (!base.endsWith("/")) base = base + "/";
        int id = grupoEndpoint.getCheckedRadioButtonId();
        if (id == R.id.rbLento) return base + "api/pruebas/lento";
        if (id == R.id.rbError) return base + "api/pruebas/error";
        return base + "api/registros";
    }

    // null = no mandar el encabezado
    private String encabezadoAuth() {
        int id = grupoToken.getCheckedRadioButtonId();
        if (id == R.id.rbSinToken) return null;
        if (id == R.id.rbTokenInvalido) return "Bearer " + TOKEN_FALSO;
        String token = sessionManager.obtenerToken();
        return token != null ? "Bearer " + token : null;
    }

    private boolean urlValida(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) return true;
        txtResultado.setText("La URL base debe empezar con http:// o https://");
        return false;
    }

    // ---------------- VOLLEY ----------------
    private void consultarConVolley() {
        String url = urlCompleta();
        if (!urlValida(url)) return;
        String auth = encabezadoAuth();
        iniciar("Volley");
        long inicio = SystemClock.elapsedRealtime();

        StringRequest peticion = new StringRequest(Request.Method.GET, url,
                respuesta -> {
                    long ms = SystemClock.elapsedRealtime() - inicio;
                    try {
                        List<RegistroDto> lista = gson.fromJson(respuesta, tipoLista);
                        exito("Volley", lista, ms);
                    } catch (Exception e) {
                        Log.e(TAG, "Volley: error al convertir JSON", e);
                        fallo("Volley", ErroresRed.mensajeExcepcion(e), e.getClass().getSimpleName(), ms);
                    }
                },
                error -> {
                    long ms = SystemClock.elapsedRealtime() - inicio;
                    String tecnico = error.getClass().getSimpleName();
                    if (error.networkResponse != null) tecnico += " HTTP " + error.networkResponse.statusCode;
                    if (error.getCause() != null) tecnico += " / " + error.getCause().getClass().getSimpleName();
                    Log.e(TAG, "Volley: " + tecnico, error);
                    fallo("Volley", ErroresRed.mensajeVolley(error), tecnico, ms);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                if (auth != null) headers.put("Authorization", auth);
                return headers;
            }
        };

        peticion.setRetryPolicy(new DefaultRetryPolicy(
                RetrofitClient.TIMEOUT_SEGUNDOS * 1000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        peticion.setShouldCache(false);
        peticion.setTag(TAG);
        colaVolley.add(peticion);
    }

    // ---------------- OKHTTP ----------------
    private void consultarConOkHttp() {
        String url = urlCompleta();
        if (!urlValida(url)) return;
        String auth = encabezadoAuth();
        iniciar("OkHttp");
        long inicio = SystemClock.elapsedRealtime();

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json");
        if (auth != null) builder.header("Authorization", auth);
        okhttp3.Request peticion = builder.build();

        clienteOkHttp.newCall(peticion).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                Log.e(TAG, "OkHttp: " + e.getClass().getSimpleName(), e);
                // este callback no corre en el hilo principal
                runOnUiThread(() -> fallo("OkHttp", ErroresRed.mensajeExcepcion(e), e.getClass().getSimpleName(), ms));
            }

            @Override
            public void onResponse(Call call, Response response) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                try (Response r = response) {
                    if (!r.isSuccessful()) {
                        int codigo = r.code();
                        Log.e(TAG, "OkHttp: HTTP " + codigo);
                        runOnUiThread(() -> fallo("OkHttp", ErroresRed.mensajeHttp(codigo), "HTTP " + codigo, ms));
                        return;
                    }
                    String cuerpo = r.body() != null ? r.body().string() : "[]";
                    List<RegistroDto> lista = gson.fromJson(cuerpo, tipoLista);
                    runOnUiThread(() -> exito("OkHttp", lista, ms));
                } catch (Exception e) {
                    Log.e(TAG, "OkHttp: error leyendo respuesta", e);
                    runOnUiThread(() -> fallo("OkHttp", ErroresRed.mensajeExcepcion(e), e.getClass().getSimpleName(), ms));
                }
            }
        });
    }

    // ---------------- RETROFIT ----------------
    private void consultarConRetrofit() {
        String url = urlCompleta();
        if (!urlValida(url)) return;
        int idToken = grupoToken.getCheckedRadioButtonId();
        // con "" el interceptor no agrega el token guardado
        String auth = idToken == R.id.rbSinToken ? "" : (idToken == R.id.rbTokenInvalido ? "Bearer " + TOKEN_FALSO : null);
        iniciar("Retrofit");
        long inicio = SystemClock.elapsedRealtime();

        repositorio.listarDesde(url, auth, new retrofit2.Callback<List<RegistroDto>>() {
            @Override
            public void onResponse(retrofit2.Call<List<RegistroDto>> call, retrofit2.Response<List<RegistroDto>> response) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                if (response.isSuccessful()) {
                    exito("Retrofit", response.body(), ms);
                } else {
                    Log.e(TAG, "Retrofit: HTTP " + response.code());
                    fallo("Retrofit", ErroresRed.mensajeHttp(response.code()), "HTTP " + response.code(), ms);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<RegistroDto>> call, Throwable t) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                Log.e(TAG, "Retrofit: " + t.getClass().getSimpleName(), t);
                fallo("Retrofit", ErroresRed.mensajeExcepcion(t), t.getClass().getSimpleName(), ms);
            }
        });
    }

    // ---------------- UI ----------------
    private void iniciar(String libreria) {
        progress.setVisibility(View.VISIBLE);
        txtResultado.setText("Consultando con " + libreria + "...");
        Log.d(TAG, libreria + " -> GET " + urlCompleta());
    }

    private void exito(String libreria, List<RegistroDto> lista, long ms) {
        if (isFinishing() || isDestroyed()) return;
        int cantidad = lista != null ? lista.size() : 0;
        progress.setVisibility(View.GONE);
        txtResultado.setText(libreria + "\nRegistros: " + cantidad + "\nTiempo: " + ms + " ms");
        Log.d(TAG, libreria + " OK -> " + cantidad + " registros en " + ms + " ms");
        agregarHistorial(libreria + " | OK | " + cantidad + " reg | " + ms + " ms");
    }

    private void fallo(String libreria, String mensaje, String tecnico, long ms) {
        if (isFinishing() || isDestroyed()) return;
        progress.setVisibility(View.GONE);
        txtResultado.setText(libreria + "\n" + mensaje + "\nTiempo: " + ms + " ms");
        agregarHistorial(libreria + " | ERROR | " + tecnico + " | " + ms + " ms");
    }

    private void agregarHistorial(String linea) {
        String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        txtHistorial.setText(hora + "  " + linea + "\n" + txtHistorial.getText());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (colaVolley != null) colaVolley.cancelAll(TAG);
        if (clienteOkHttp != null) clienteOkHttp.dispatcher().cancelAll();
    }
}

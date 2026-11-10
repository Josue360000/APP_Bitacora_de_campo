package com.example.app_bitacora_de_campo.Vista;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app_bitacora_de_campo.R;
import com.example.app_bitacora_de_campo.data.remote.RegistroUsuarioDto;
import com.example.app_bitacora_de_campo.data.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        RetrofitClient.inicializar(this);

        EditText edtCorreo = findViewById(R.id.edtCorreoRegistro);
        EditText edtPassword = findViewById(R.id.edtPasswordRegistro);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(v -> {
            String correo = edtCorreo.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            RetrofitClient.getApi().registrar(new RegistroUsuarioDto(correo, pass, "Usuario"))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(RegistroActivity.this, "Registrado con éxito", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RegistroActivity.this, "Error al registrar", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(RegistroActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
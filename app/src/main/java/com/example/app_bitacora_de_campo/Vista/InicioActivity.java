package com.example.app_bitacora_de_campo.Vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_bitacora_de_campo.R;

public class InicioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        Button btn = findViewById(R.id.btnIngresar);
        btn.setOnClickListener(v -> {
            Intent i = new Intent(InicioActivity.this, ListadoActivity.class);
            startActivity(i);
        });
    }
}
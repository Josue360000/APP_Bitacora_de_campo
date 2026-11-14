package com.example.app_bitacora_de_campo.Vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_bitacora_de_campo.R;
import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.remote.RetrofitClient;
import com.example.app_bitacora_de_campo.data.remote.SessionManager;
import com.example.app_bitacora_de_campo.viewmodel.ListadoViewModel;

public class ListadoActivity extends AppCompatActivity {
    private ListadoViewModel viewModel;
    private BitacoraAdapter adapter;
    private RecyclerView recycler;
    private ProgressBar progressBar;
    private LinearLayout layoutVacio;
    private Button btnReintentar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);


        RetrofitClient.inicializar(this);
        sessionManager = new SessionManager(this);


        if (!sessionManager.estaLogueado()) {
            irAlLogin();
            return;
        }

        recycler = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progressBarListado);
        layoutVacio = findViewById(R.id.layoutVacio);
        btnReintentar = findViewById(R.id.btnReintentar);
        Button btnNuevo = findViewById(R.id.btnNuevo);
        Button btnLaboratorio = findViewById(R.id.btnLaboratorio);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BitacoraAdapter();
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ListadoViewModel.class);

        btnNuevo.setOnClickListener(v -> {
            Intent i = new Intent(this, FormularioActivity.class);
            startActivity(i);
        });

        // Tarea 2.5: el botón "Comparar" abre la pantalla de laboratorio.
        btnLaboratorio.setOnClickListener(v ->
                startActivity(new Intent(this, LaboratorioActivity.class)));

        btnReintentar.setOnClickListener(v -> viewModel.cargarRegistros());

        adapter.setOnItemClickListener(new BitacoraAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RegistroDto item) {
                Intent i = new Intent(ListadoActivity.this, DetalleActivity.class);
                i.putExtra("id", item.getId());
                startActivity(i);
            }

            @Override
            public void onItemLongClick(RegistroDto item) {

                String rol = sessionManager.obtenerRol();
                if ("Admin".equals(rol)) {
                    new AlertDialog.Builder(ListadoActivity.this)
                            .setTitle("Eliminar")
                            .setMessage("¿Estás seguro de eliminar el item seleccionado?")
                            .setPositiveButton("Sí", (d, w) -> viewModel.eliminarRegistro(item.getId()))
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    Toast.makeText(ListadoActivity.this, "Solo los administradores pueden eliminar.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        observarEstados();
        viewModel.cargarRegistros();
    }

    private void observarEstados() {
        viewModel.cargando.observe(this, cargando -> {
            if (cargando != null) {
                progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
                recycler.setVisibility(cargando ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.lista.observe(this, lista -> {
            if (lista != null) {
                adapter.setLista(lista);
                if (lista.isEmpty()) {
                    layoutVacio.setVisibility(View.VISIBLE);
                    recycler.setVisibility(View.GONE);
                } else {
                    layoutVacio.setVisibility(View.GONE);
                    recycler.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.error.observe(this, err -> {
            if (err != null) {
                // Manejo de errores de seguridad
                if (err.contains("401")) {
                    Toast.makeText(this, "Sesión expirada. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show();
                    irAlLogin();
                } else if (err.contains("403")) {
                    Toast.makeText(this, "No tienes permisos para esta acción.", Toast.LENGTH_LONG).show();
                    // No cerramos sesión, solo informamos
                } else {
                    Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                    btnReintentar.setVisibility(View.VISIBLE);
                }
            } else {
                btnReintentar.setVisibility(View.GONE);
            }
        });
    }

    private void irAlLogin() {
        sessionManager.cerrarSesion();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.estaLogueado()) {
            viewModel.cargarRegistros();
        }
    }
}
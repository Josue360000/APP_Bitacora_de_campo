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
import com.example.app_bitacora_de_campo.viewmodel.ListadoViewModel;

public class ListadoActivity extends AppCompatActivity {
    private ListadoViewModel viewModel;
    private BitacoraAdapter adapter;
    private RecyclerView recycler;
    private ProgressBar progressBar;
    private LinearLayout layoutVacio;
    private Button btnReintentar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        recycler = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progressBarListado);
        layoutVacio = findViewById(R.id.layoutVacio);
        btnReintentar = findViewById(R.id.btnReintentar);
        Button btnNuevo = findViewById(R.id.btnNuevo);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BitacoraAdapter();
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ListadoViewModel.class);

        btnNuevo.setOnClickListener(v -> {
            Intent i = new Intent(this, FormularioActivity.class);
            startActivity(i);
        });

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
                new AlertDialog.Builder(ListadoActivity.this)
                        .setTitle("Eliminar")
                        .setMessage("Esta seguro de eliminar el item seleccionado?")
                        .setPositiveButton("Si", (d, w) -> viewModel.eliminarRegistro(item.getId()))
                        .setNegativeButton("No", null)
                        .show();
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
                Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                btnReintentar.setVisibility(View.VISIBLE);
            } else {
                btnReintentar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.cargarRegistros();
    }
}
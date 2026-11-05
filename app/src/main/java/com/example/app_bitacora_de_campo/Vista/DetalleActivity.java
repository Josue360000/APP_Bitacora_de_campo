package com.example.app_bitacora_de_campo.Vista;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.app_bitacora_de_campo.R;
import com.example.app_bitacora_de_campo.viewmodel.DetalleViewModel;

public class DetalleActivity extends AppCompatActivity {
    private DetalleViewModel viewModel;
    private int idActual = -1;
    private ProgressBar progressBar;
    private ImageView img;
    private TextView txtTitulo, txtFecha, txtDescripcion;
    private Button btnEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        idActual = getIntent().getIntExtra("id", -1);

        progressBar = findViewById(R.id.progressBarDetalle);
        img = findViewById(R.id.imgFoto);
        txtTitulo = findViewById(R.id.txtTituloDetalle);
        txtFecha = findViewById(R.id.txtFechaDetalle);
        txtDescripcion = findViewById(R.id.txtDescripcionDetalle);
        btnEditar = findViewById(R.id.btnEditar);

        viewModel = new ViewModelProvider(this).get(DetalleViewModel.class);

        btnEditar.setOnClickListener(v -> {
            Intent i = new Intent(DetalleActivity.this, FormularioActivity.class);
            i.putExtra("id", idActual);
            startActivity(i);
            finish();
        });

        observarEstados();
        if (idActual > 0) {
            viewModel.obtener(idActual);
        }
    }

    private void observarEstados() {
        viewModel.cargando.observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
            img.setVisibility(cargando ? View.GONE : View.VISIBLE);
            txtTitulo.setVisibility(cargando ? View.GONE : View.VISIBLE);
            txtFecha.setVisibility(cargando ? View.GONE : View.VISIBLE);
            txtDescripcion.setVisibility(cargando ? View.GONE : View.VISIBLE);
            btnEditar.setVisibility(cargando ? View.GONE : View.VISIBLE);
        });

        viewModel.registro.observe(this, b -> {
            if (b != null) {
                txtTitulo.setText(b.getTitulo());
                txtFecha.setText(formatearFecha(b.getFechaCreacion()));
                txtDescripcion.setText(b.getDescripcion());
                if (b.getFotoBase64() != null && !b.getFotoBase64().isEmpty()) {
                    byte[] bytes = Base64.decode(b.getFotoBase64(), Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    img.setImageBitmap(bmp);
                } else {
                    img.setImageResource(R.color.gris);
                }
            }
        });

        viewModel.error.observe(this, err -> {
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String formatearFecha(String fechaOriginal) {
        if (fechaOriginal == null || fechaOriginal.isEmpty()) return "";
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            formatoEntrada.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            formatoSalida.setTimeZone(TimeZone.getDefault());
            Date fecha = formatoEntrada.parse(fechaOriginal);
            return fecha != null ? formatoSalida.format(fecha) : fechaOriginal;
        } catch (ParseException e) {
            return fechaOriginal;
        }
    }
}
package com.example.app_bitacora_de_campo.Vista;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.app_bitacora_de_campo.R;
import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.viewmodel.FormularioViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class FormularioActivity extends AppCompatActivity {
    private static final int REQ_FOTO = 100;
    private static final int REQ_VOZ = 200;

    private EditText edtTitulo, edtDescripcion;
    private ImageView imgFoto;
    private FormularioViewModel viewModel;
    private String fotoBase64 = "";
    private String rutaFotoActual;
    private int idEditar = -1;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);
        viewModel = new ViewModelProvider(this).get(FormularioViewModel.class);

        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescripcion = findViewById(R.id.edtDescripcion);
        imgFoto = findViewById(R.id.imgFotoForm);
        Button btnCamara = findViewById(R.id.btnCamara);
        Button btnDictar = findViewById(R.id.btnDictar);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        idEditar = getIntent().getIntExtra("id", -1);

        if (idEditar > 0) {
            setTitle("Editar Bitacora");
            viewModel.obtener(idEditar);
        } else {
            setTitle("Nueva Bitacora");
        }

        btnCamara.setOnClickListener(v -> abrirCamara());
        btnDictar.setOnClickListener(v -> iniciarDictado());
        btnGuardar.setOnClickListener(v -> guardar());

        observarEstados();
    }

    private void observarEstados() {
        pd = new ProgressDialog(this);
        pd.setMessage("Guardando...");
        pd.setCancelable(false);

        viewModel.cargando.observe(this, cargando -> {
            if (cargando) pd.show(); else pd.dismiss();
        });

        viewModel.datosExistentes.observe(this, b -> {
            if (b != null) {
                edtTitulo.setText(b.getTitulo());
                edtDescripcion.setText(b.getDescripcion());
                fotoBase64 = b.getFotoBase64() != null ? b.getFotoBase64() : "";
                if (!fotoBase64.isEmpty()) {
                    byte[] bytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imgFoto.setImageBitmap(bmp);
                }
            }
        });

        viewModel.guardadoExitoso.observe(this, exito -> {
            if (exito != null && exito) {
                Toast.makeText(this, "Guardado exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.error.observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });
    }

    private void iniciarDictado() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora para dictar la descripcion");

        try {
            startActivityForResult(intent, REQ_VOZ);
        } catch (Exception e) {
            Toast.makeText(this, "El dictado por voz no esta disponible en este dispositivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirCamara() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File foto = File.createTempFile("foto_", ".jpg", dir);
            rutaFotoActual = foto.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", foto);
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(i, REQ_FOTO);
        } catch (IOException e) {
            Toast.makeText(this, "Error al abrir camara", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardar() {
        String titulo = edtTitulo.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "El titulo y la descripcion son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        RegistroDto dto = new RegistroDto();
        dto.setTitulo(titulo);
        dto.setDescripcion(descripcion);
        dto.setFotoBase64(fotoBase64);

        viewModel.guardar(dto, idEditar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_FOTO && resultCode == RESULT_OK) {
            Bitmap bmp = BitmapFactory.decodeFile(rutaFotoActual);

            int ancho = bmp.getWidth();
            int alto = bmp.getHeight();
            if (ancho > 1024 || alto > 1024) {
                float ratio = (float) ancho / (float) alto;
                if (ratio > 1) {
                    ancho = 1024;
                    alto = (int) (ancho / ratio);
                } else {
                    alto = 1024;
                    ancho = (int) (alto * ratio);
                }
                bmp = Bitmap.createScaledBitmap(bmp, ancho, alto, true);
            }

            imgFoto.setImageBitmap(bmp);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            fotoBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        }

        if (requestCode == REQ_VOZ && resultCode == RESULT_OK && data != null) {
            ArrayList<String> resultados = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (resultados != null && !resultados.isEmpty()) {
                String textoDictado = resultados.get(0);
                edtDescripcion.setText(textoDictado);
            }
        }
    }
}
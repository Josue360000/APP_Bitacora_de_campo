package com.example.app_bitacora_de_campo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.repository.RegistroRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormularioViewModel extends ViewModel {
    private final RegistroRepository repository;
    public final MutableLiveData<Boolean> guardadoExitoso = new MutableLiveData<>();
    public final MutableLiveData<Boolean> cargando = new MutableLiveData<>();
    public final MutableLiveData<String> error = new MutableLiveData<>();

    public FormularioViewModel() {
        repository = new RegistroRepository();
    }

    public void guardar(RegistroDto dto, int idEditar) {
        cargando.setValue(true);
        error.setValue(null);
        if (idEditar > 0) {
            repository.actualizar(idEditar, dto, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    cargando.setValue(false);
                    if (response.isSuccessful()) {
                        guardadoExitoso.setValue(true);
                    } else {
                        error.setValue("Error al actualizar: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    cargando.setValue(false);
                    error.setValue("Sin conexion al actualizar");
                }
            });
        } else {
            repository.crear(dto, new Callback<RegistroDto>() {
                @Override
                public void onResponse(Call<RegistroDto> call, Response<RegistroDto> response) {
                    cargando.setValue(false);
                    if (response.isSuccessful()) {
                        guardadoExitoso.setValue(true);
                    } else {
                        error.setValue("Error al crear: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<RegistroDto> call, Throwable t) {
                    cargando.setValue(false);
                    error.setValue("Sin conexion al crear");
                }
            });
        }
    }

    public void obtener(int id) {
        cargando.setValue(true);
        repository.obtener(id, new Callback<RegistroDto>() {
            @Override
            public void onResponse(Call<RegistroDto> call, Response<RegistroDto> response) {
                cargando.setValue(false);
                if (response.isSuccessful()) {
                    cargarDatosExistentes(response.body());
                } else {
                    error.setValue("Error al obtener datos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegistroDto> call, Throwable t) {
                cargando.setValue(false);
                error.setValue("Sin conexion al obtener datos");
            }
        });
    }

    public final MutableLiveData<RegistroDto> datosExistentes = new MutableLiveData<>();
    private void cargarDatosExistentes(RegistroDto dto) {
        datosExistentes.setValue(dto);
    }
}
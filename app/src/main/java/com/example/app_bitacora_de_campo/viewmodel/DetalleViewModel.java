package com.example.app_bitacora_de_campo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.repository.RegistroRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleViewModel extends ViewModel {
    private final RegistroRepository repository;
    public final MutableLiveData<RegistroDto> registro = new MutableLiveData<>();
    public final MutableLiveData<Boolean> cargando = new MutableLiveData<>();
    public final MutableLiveData<String> error = new MutableLiveData<>();

    public DetalleViewModel() {
        repository = new RegistroRepository();
    }

    public void obtener(int id) {
        cargando.setValue(true);
        error.setValue(null);
        repository.obtener(id, new Callback<RegistroDto>() {
            @Override
            public void onResponse(Call<RegistroDto> call, Response<RegistroDto> response) {
                cargando.setValue(false);
                if (response.isSuccessful()) {
                    registro.setValue(response.body());
                } else {
                    error.setValue("Error del servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegistroDto> call, Throwable t) {
                cargando.setValue(false);
                error.setValue("Sin conexion o tiempo de espera agotado");
            }
        });
    }
}
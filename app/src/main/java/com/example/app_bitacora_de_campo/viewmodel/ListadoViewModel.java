package com.example.app_bitacora_de_campo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.repository.RegistroRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListadoViewModel extends ViewModel {
    private final RegistroRepository repository;
    public final MutableLiveData<List<RegistroDto>> lista = new MutableLiveData<>();
    public final MutableLiveData<Boolean> cargando = new MutableLiveData<>();
    public final MutableLiveData<String> error = new MutableLiveData<>();

    public ListadoViewModel() {
        repository = new RegistroRepository();
    }

    public void cargarRegistros() {
        cargando.setValue(true);
        error.setValue(null);
        repository.listar(new Callback<List<RegistroDto>>() {
            @Override
            public void onResponse(Call<List<RegistroDto>> call, Response<List<RegistroDto>> response) {
                cargando.setValue(false);
                if (response.isSuccessful()) {
                    lista.setValue(response.body());
                } else {
                    error.setValue("Error del servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RegistroDto>> call, Throwable t) {
                cargando.setValue(false);
                error.setValue("Sin conexion o tiempo de espera agotado");
            }
        });
    }

    public void eliminarRegistro(int id) {
        repository.eliminar(id, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cargarRegistros();
                } else {
                    error.setValue("Error al eliminar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                error.setValue("Sin conexion al eliminar");
            }
        });
    }
}
package com.example.app_bitacora_de_campo.data.repository;

import com.example.app_bitacora_de_campo.data.remote.RegistroApi;
import com.example.app_bitacora_de_campo.data.remote.RegistroDto;
import com.example.app_bitacora_de_campo.data.remote.RetrofitClient;

import java.util.List;
import retrofit2.Callback;

public class RegistroRepository {
    private final RegistroApi api;

    public RegistroRepository() {
        this.api = RetrofitClient.getApi();
    }

    public void listar(Callback<List<RegistroDto>> callback) {
        api.listar().enqueue(callback);
    }

    public void listarDesde(String url, String authorization, Callback<List<RegistroDto>> callback) {
        api.listarDesde(url, authorization).enqueue(callback);
    }

    public void obtener(int id, Callback<RegistroDto> callback) {
        api.obtener(id).enqueue(callback);
    }

    public void crear(RegistroDto dto, Callback<RegistroDto> callback) {
        api.crear(dto).enqueue(callback);
    }

    public void actualizar(int id, RegistroDto dto, Callback<Void> callback) {
        api.actualizar(id, dto).enqueue(callback);
    }

    public void eliminar(int id, Callback<Void> callback) {
        api.eliminar(id).enqueue(callback);
    }
}
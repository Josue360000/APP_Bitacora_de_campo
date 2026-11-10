package com.example.app_bitacora_de_campo.data.remote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RegistroApi {
    @GET("api/registros")
    Call<List<RegistroDto>> listar();

    @GET("api/registros/{id}")
    Call<RegistroDto> obtener(@Path("id") int id);

    @POST("api/registros")
    Call<RegistroDto> crear(@Body RegistroDto registro);

    @PUT("api/registros/{id}")
    Call<Void> actualizar(@Path("id") int id, @Body RegistroDto registro);

    @DELETE("api/registros/{id}")
    Call<Void> eliminar(@Path("id") int id);

    // NUEVOS ENDPOINTS
    @POST("api/auth/login")
    Call<AuthResponseDto> login(@Body LoginDto login);

    @POST("api/auth/registro")
    Call<Void> registrar(@Body RegistroUsuarioDto registro);
}
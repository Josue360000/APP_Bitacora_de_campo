package com.example.app_bitacora_de_campo.Vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_bitacora_de_campo.R;
import com.example.app_bitacora_de_campo.data.remote.RegistroDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BitacoraAdapter extends RecyclerView.Adapter<BitacoraAdapter.VH> {
    private List<RegistroDto> lista = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RegistroDto item);
        void onItemLongClick(RegistroDto item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setLista(List<RegistroDto> lista) {
        this.lista = lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bitacora, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RegistroDto b = lista.get(position);
        h.txtTitulo.setText(b.getTitulo());

        // Aqui llamamos al metodo que formatea la fecha
        h.txtFecha.setText(formatearFecha(b.getFechaCreacion()));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(b);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(b);
            return true;
        });
    }

    private String formatearFecha(String fechaOriginal) {
        if (fechaOriginal == null || fechaOriginal.isEmpty()) {
            return "";
        }
        try {
            // Formato en el que viene de la API (ISO 8601)
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            formatoEntrada.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Formato en el que lo queremos mostrar
            SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            formatoSalida.setTimeZone(TimeZone.getDefault());

            Date fecha = formatoEntrada.parse(fechaOriginal);
            return fecha != null ? formatoSalida.format(fecha) : fechaOriginal;

        } catch (ParseException e) {
            e.printStackTrace();
            return fechaOriginal;
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtFecha;
        public VH(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtFecha = itemView.findViewById(R.id.txtFecha);
        }
    }
}
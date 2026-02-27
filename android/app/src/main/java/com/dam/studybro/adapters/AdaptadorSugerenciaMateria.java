package com.dam.studybro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studybro.R;
import com.dam.studybro.database.SugerenciaMateria;
import com.dam.studybro.database.SugerenciaMateriaConCentro;
import java.util.List;

public class AdaptadorSugerenciaMateria extends RecyclerView.Adapter<AdaptadorSugerenciaMateria.ViewHolder> {

    private List<SugerenciaMateriaConCentro> sugerencias;
    private final OnSugerenciaClickListener listener;

    public interface OnSugerenciaClickListener {
        void onAprobar(SugerenciaMateria sugerencia);
        void onRechazar(SugerenciaMateria sugerencia);
    }

    public AdaptadorSugerenciaMateria(List<SugerenciaMateriaConCentro> sugerencias, OnSugerenciaClickListener listener) {
        this.sugerencias = sugerencias;
        this.listener = listener;
    }

    public void actualizarLista(List<SugerenciaMateriaConCentro> nuevaLista) {
        this.sugerencias = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sugerencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SugerenciaMateriaConCentro item = sugerencias.get(position);
        SugerenciaMateria sub = item.sugerencia;
        holder.tvNombre.setText(sub.nombreSugerido);
        String detalles = "Curso: " + sub.cursoSugerido + " | Centro: " + item.nombreCentro;
        if (item.nombreEspecialidad != null) {
            detalles += " | Esp: " + item.nombreEspecialidad;
        }
        detalles += " | Por: " + sub.emailSolicitante;
        holder.tvDetalles.setText(detalles);
        
        holder.btnAprobar.setOnClickListener(v -> {
            if (listener != null) listener.onAprobar(sub);
        });

        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) listener.onRechazar(sub);
        });
    }

    @Override
    public int getItemCount() {
        return sugerencias != null ? sugerencias.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDetalles;
        ImageButton btnAprobar, btnRechazar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreSugerido);
            tvDetalles = itemView.findViewById(R.id.tvDetallesSugerencia);
            btnAprobar = itemView.findViewById(R.id.btnAprobar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}

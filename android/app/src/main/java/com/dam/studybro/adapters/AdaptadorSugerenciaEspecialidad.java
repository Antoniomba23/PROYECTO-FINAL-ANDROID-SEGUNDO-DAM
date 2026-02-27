package com.dam.studybro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studybro.R;
import com.dam.studybro.database.SugerenciaEspecialidad;
import com.dam.studybro.database.SugerenciaEspecialidadConCentro;
import java.util.List;

public class AdaptadorSugerenciaEspecialidad extends RecyclerView.Adapter<AdaptadorSugerenciaEspecialidad.ViewHolder> {

    private List<SugerenciaEspecialidadConCentro> sugerencias;
    private final OnSugerenciaClickListener listener;

    public interface OnSugerenciaClickListener {
        void onAprobar(SugerenciaEspecialidad sugerencia);
        void onRechazar(SugerenciaEspecialidad sugerencia);
    }

    public AdaptadorSugerenciaEspecialidad(List<SugerenciaEspecialidadConCentro> sugerencias, OnSugerenciaClickListener listener) {
        this.sugerencias = sugerencias;
        this.listener = listener;
    }

    public void actualizarLista(List<SugerenciaEspecialidadConCentro> nuevaLista) {
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
        SugerenciaEspecialidadConCentro item = sugerencias.get(position);
        SugerenciaEspecialidad sub = item.sugerencia;
        holder.tvNombre.setText(sub.nombreSugerido);
        holder.tvDetalles.setText("Centro: " + item.nombreCentro + " | Por: " + sub.emailSolicitante);
        
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

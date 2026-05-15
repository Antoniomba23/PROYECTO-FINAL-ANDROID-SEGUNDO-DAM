package com.dam.studyfiles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.models.Comentario;

import java.util.List;

public class AdaptadorComentarios extends RecyclerView.Adapter<AdaptadorComentarios.ViewHolder> {

    private List<Comentario> comentarios;

    public AdaptadorComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public void actualizar(List<Comentario> nuevos) {
        this.comentarios.clear();
        this.comentarios.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comentario c = comentarios.get(position);
        holder.tvNombre.setText(c.usuarioNombre);
        holder.tvTexto.setText(c.texto);
        
        // Mostrar fecha formateada si existe
        if (c.fecha != null && c.fecha.length() >= 10) {
            String soloFecha = c.fecha.substring(0, 10);
            holder.tvFecha.setText(soloFecha);
            holder.tvFecha.setVisibility(View.VISIBLE);
        } else {
            holder.tvFecha.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comentarios != null ? comentarios.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTexto, tvFecha;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvComentarioNombre);
            tvTexto = itemView.findViewById(R.id.tvComentarioTexto);
            tvFecha = itemView.findViewById(R.id.tvComentarioFecha);
        }
    }
}

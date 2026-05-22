package com.dam.studyfiles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.models.Comentario;

import java.util.List;

public class AdaptadorComentarios extends RecyclerView.Adapter<AdaptadorComentarios.ViewHolder> {

    private List<Comentario> comentarios;
    private String currentUserId;
    private OnComentarioActionListener listener;

    public interface OnComentarioActionListener {
        void onEditar(Comentario c);
        void onEliminar(Comentario c);
    }

    public AdaptadorComentarios(List<Comentario> comentarios, String currentUserId, OnComentarioActionListener listener) {
        this.comentarios = comentarios;
        this.currentUserId = currentUserId;
        this.listener = listener;
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
        holder.tvNombre.setText(c.usuarioNombre != null ? c.usuarioNombre : "Anónimo");
        holder.tvTexto.setText(c.contenido);
        
        if (c.fecha > 0) {
            java.util.Date date = new java.util.Date(c.fecha);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            holder.tvFecha.setText(sdf.format(date));
            holder.tvFecha.setVisibility(View.VISIBLE);
        } else {
            holder.tvFecha.setVisibility(View.GONE);
        }

        // Lógica de opciones (Solo para el dueño)
        if (currentUserId != null && currentUserId.equals(c.usuarioId)) {
            holder.ivOpciones.setVisibility(View.VISIBLE);
            holder.ivOpciones.setOnClickListener(v -> mostrarMenuOpciones(v, c));
        } else {
            holder.ivOpciones.setVisibility(View.GONE);
        }
    }

    private void mostrarMenuOpciones(View view, Comentario c) {
        PopupMenu menu = new PopupMenu(view.getContext(), view);
        menu.getMenu().add(0, 1, 0, "Editar");
        menu.getMenu().add(0, 2, 0, "Eliminar");
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) listener.onEditar(c);
            if (item.getItemId() == 2) listener.onEliminar(c);
            return true;
        });
        menu.show();
    }

    @Override
    public int getItemCount() {
        return comentarios != null ? comentarios.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTexto, tvFecha;
        ImageView ivOpciones;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvComentarioNombre);
            tvTexto = itemView.findViewById(R.id.tvComentarioTexto);
            tvFecha = itemView.findViewById(R.id.tvComentarioFecha);
            ivOpciones = itemView.findViewById(R.id.ivComentarioOpciones);
        }
    }
}

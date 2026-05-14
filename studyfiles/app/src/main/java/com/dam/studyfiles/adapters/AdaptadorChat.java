package com.dam.studyfiles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.models.MensajeChat;

import java.util.List;

public class AdaptadorChat extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private List<MensajeChat> mensajes;

    public AdaptadorChat(List<MensajeChat> mensajes) {
        this.mensajes = mensajes;
    }

    public void actualizarMensajes(List<MensajeChat> nuevos) {
        this.mensajes = nuevos;
        notifyDataSetChanged();
    }

    public void agregarMensaje(MensajeChat msg) {
        this.mensajes.add(msg);
        notifyItemInserted(this.mensajes.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if ("user".equals(mensajes.get(position).rol)) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MensajeChat msg = mensajes.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMensaje.setText(msg.mensaje);
        } else if (holder instanceof AIViewHolder) {
            ((AIViewHolder) holder).tvMensaje.setText(msg.mensaje);
        }
    }

    @Override
    public int getItemCount() {
        return mensajes != null ? mensajes.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        UserViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
        }
    }

    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        AIViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
        }
    }
}

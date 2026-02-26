package com.dam.studybro.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studybro.R;
import com.dam.studybro.database.Usuario;

import java.util.List;

public class AdaptadorUsuarios extends RecyclerView.Adapter<AdaptadorUsuarios.UsuarioViewHolder> {

    private List<Usuario> listaUsuarios;

    public AdaptadorUsuarios(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    public void actualizarDatos(List<Usuario> nuevos) {
        this.listaUsuarios = nuevos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario u = listaUsuarios.get(position);
        holder.tvNombre.setText(u.nombre);
        holder.tvEmail.setText(u.email);
        
        holder.tvRol.setText(u.rol);
        if ("ADMIN".equals(u.rol)) {
            holder.tvRol.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            holder.tvRol.setBackgroundColor(Color.parseColor("#9E9E9E")); // Gris
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail, tvRol;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuario);
            tvEmail  = itemView.findViewById(R.id.tvEmailUsuario);
            tvRol    = itemView.findViewById(R.id.tvRolUsuario);
        }
    }
}

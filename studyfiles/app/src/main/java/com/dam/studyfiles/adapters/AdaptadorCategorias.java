package com.dam.studyfiles.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studyfiles.R;
import java.util.List;

public class AdaptadorCategorias extends RecyclerView.Adapter<AdaptadorCategorias.ViewHolder> {

    public interface OnCategoriaClick {
        void onClick(String nombre, int iconRes);
    }

    public static class Categoria {
        public String nombre;
        public int    iconRes;
        public int    colorRes;

        public Categoria(String nombre, int iconRes, int colorRes) {
            this.nombre   = nombre;
            this.iconRes  = iconRes;
            this.colorRes = colorRes;
        }
    }

    private final List<Categoria>    lista;
    private final OnCategoriaClick   listener;

    public AdaptadorCategorias(List<Categoria> lista, OnCategoriaClick listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Categoria cat = lista.get(position);
        h.tvNombre.setText(cat.nombre);
        h.ivIcono.setImageResource(cat.iconRes);
        h.ivIcono.setColorFilter(h.itemView.getContext().getColor(cat.colorRes));
        h.itemView.setOnClickListener(v -> listener.onClick(cat.nombre, cat.iconRes));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcono;
        TextView  tvNombre;
        ViewHolder(@NonNull View v) {
            super(v);
            ivIcono  = v.findViewById(R.id.ivIconoCategoria);
            tvNombre = v.findViewById(R.id.tvNombreCategoria);
        }
    }
}

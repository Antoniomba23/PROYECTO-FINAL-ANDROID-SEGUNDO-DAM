package com.dam.studybro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studybro.R;
import com.dam.studybro.database.ValoracionCentro;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AdaptadorResenas extends RecyclerView.Adapter<AdaptadorResenas.ViewHolder> {

    private List<ValoracionCentro> lista;

    public AdaptadorResenas(List<ValoracionCentro> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resena, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ValoracionCentro item = lista.get(position);
        
        String autor = "Estudiante";
        String contenido = item.comentario;

        if (item.comentario != null && item.comentario.contains("|||")) {
            String[] partes = item.comentario.split("\\|\\|\\|");
            if (partes.length >= 2) {
                String email = partes[0];
                autor = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                autor = autor.substring(0, 1).toUpperCase() + autor.substring(1).toLowerCase();
                contenido = partes[1];
            }
        }

        holder.tvAuthor.setText(autor); 
        holder.tvContent.setText(contenido);
        
        // Evita el bug de Infinite Layout Request Loop del RatingBar en RecyclerView
        if (holder.ratingIndicator.getRating() != item.puntuacion) {
            holder.ratingIndicator.setRating(item.puntuacion);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new java.util.Date(item.fecha)));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void actualizarDatos(List<ValoracionCentro> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvContent, tvDate;
        RatingBar ratingIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            ratingIndicator = itemView.findViewById(R.id.ratingIndicator);
        }
    }
}

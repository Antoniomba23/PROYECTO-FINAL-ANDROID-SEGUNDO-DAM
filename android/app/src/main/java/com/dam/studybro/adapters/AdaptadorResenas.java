package com.dam.studybro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Reutilizamos item_comentario porque es idéntico visualmente
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ValoracionCentro item = lista.get(position);
        
        // Simular nombre usuario (en app real haríamos un JOIN o query extra)
        holder.tvAuthor.setText("Usuario " + item.usuarioId); 
        
        holder.tvContent.setText(item.puntuacion + " ★ - " + item.comentario);
        
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}

package com.dam.studybro.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studybro.R;
import com.dam.studybro.database.Comentario;

import java.util.List;

public class AdaptadorComentarios extends RecyclerView.Adapter<AdaptadorComentarios.ViewHolder> {

    private final List<Comentario> lista;

    public AdaptadorComentarios(List<Comentario> lista) {
        this.lista = lista;
    }

    public void actualizarDatos(List<Comentario> nuevos) {
        lista.clear();
        lista.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Comentario c = lista.get(position);
        h.tvAuthor.setText(c.usuarioId);
        h.tvContent.setText(c.contenido);
        // Fecha relativa ("Hace 3 minutos")
        h.tvDate.setText(DateUtils.getRelativeTimeSpanString(
                c.fecha, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvDate, tvContent;
        ViewHolder(View v) {
            super(v);
            tvAuthor  = v.findViewById(R.id.tvAuthor);
            tvDate    = v.findViewById(R.id.tvDate);
            tvContent = v.findViewById(R.id.tvContent);
        }
    }
}

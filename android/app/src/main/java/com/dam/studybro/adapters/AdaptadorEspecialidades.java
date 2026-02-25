package com.dam.studybro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studybro.R;
import com.dam.studybro.database.Especialidad;
import java.util.List;

public class AdaptadorEspecialidades extends RecyclerView.Adapter<AdaptadorEspecialidades.ViewHolder> {

    private List<Especialidad> lista;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Especialidad especialidad);
    }

    public AdaptadorEspecialidades(List<Especialidad> lista, OnItemClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_especialidad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Especialidad item = lista.get(position);
        holder.tvNombre.setText(item.nombre);
        // Clic en toda la tarjeta
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void actualizarDatos(List<Especialidad> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvEspecialidadNombre);
        }
    }
}

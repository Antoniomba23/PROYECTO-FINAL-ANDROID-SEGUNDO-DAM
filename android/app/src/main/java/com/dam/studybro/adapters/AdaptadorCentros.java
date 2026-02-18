package com.dam.studybro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studybro.R;
import com.dam.studybro.database.Centro;
import java.util.List;

public class AdaptadorCentros extends RecyclerView.Adapter<AdaptadorCentros.ViewHolder> {

    private List<Centro> listaCentros;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Centro centro);
    }

    public AdaptadorCentros(List<Centro> listaCentros, OnItemClickListener listener) {
        this.listaCentros = listaCentros;
        this.listener = listener;
    }

    public void actualizarDatos(List<Centro> nuevosCentros) {
        this.listaCentros = nuevosCentros;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_centro, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Centro centro = listaCentros.get(position);
        holder.nombre.setText(centro.nombre);
        
        // Combinar dirección y ciudad para la ubicación
        String ubicacionTexto = "";
        if (centro.direccion != null && !centro.direccion.isEmpty()) {
            ubicacionTexto += centro.direccion;
        }
        if (centro.ciudad != null && !centro.ciudad.isEmpty()) {
            if (!ubicacionTexto.isEmpty()) ubicacionTexto += ", ";
            ubicacionTexto += centro.ciudad;
        }
        holder.ubicacion.setText(ubicacionTexto.isEmpty() ? "Ubicación desconocida" : ubicacionTexto);
        
        // El TextView de contador (tvPostCount) lo usamos para mostrar "Ver Detalles" por ahora
        holder.contador.setText("Ver detalles");
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(centro));
    }

    @Override
    public int getItemCount() {
        return listaCentros != null ? listaCentros.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre, ubicacion, contador;

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tvCenterName);
            ubicacion = itemView.findViewById(R.id.tvLocation);
            contador = itemView.findViewById(R.id.tvPostCount);
        }
    }
}

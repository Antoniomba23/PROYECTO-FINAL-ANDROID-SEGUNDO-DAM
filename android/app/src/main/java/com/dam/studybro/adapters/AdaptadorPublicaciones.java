package com.dam.studybro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studybro.R;
import com.dam.studybro.database.Publicacion;
import com.google.android.material.chip.Chip;
import java.util.List;

/**
 * Nivel 3: Adapter - El Camarero
 * Se encarga de conectar la lista de datos (La Comida) con las vistas (El Plato)
 */
public class AdaptadorPublicaciones extends RecyclerView.Adapter<AdaptadorPublicaciones.ClaseViewHolder> {

    public interface OnPublicacionClickListener {
        void onClick(Publicacion publicacion);
    }

    private List<Publicacion> listaPublicaciones;
    private final OnPublicacionClickListener listener;

    public AdaptadorPublicaciones(List<Publicacion> listaPublicaciones, OnPublicacionClickListener listener) {
        this.listaPublicaciones = listaPublicaciones;
        this.listener = listener;
    }

    // Compatibilidad con código que no pasa listener
    public AdaptadorPublicaciones(List<Publicacion> listaPublicaciones) {
        this(listaPublicaciones, null);
    }

    public void actualizarDatos(List<Publicacion> nuevas) {
        listaPublicaciones.clear();
        listaPublicaciones.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el diseño de la fila (El Plato: item_publicacion.xml)
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publicacion, parent, false);
        return new ClaseViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaseViewHolder holder, int position) {
        // Obtenemos la publicación actual
        Publicacion publicacion = listaPublicaciones.get(position);

        // Asignamos los datos a los componentes visuales
        // Asignamos los datos a los componentes visuales
        holder.tvTitulo.setText(publicacion.titulo);
        holder.tvDescripcion.setText(publicacion.descripcion);
        holder.chipSubject.setText("Asignatura " + publicacion.asignaturaId); // Temporal: ID
        holder.chipType.setText(publicacion.tipo);
        holder.tvAutor.setText("Usuario " + publicacion.usuarioId); // Temporal: ID
        
        // Formateo simple de fecha
        long diff = System.currentTimeMillis() - publicacion.fechaSubida;
        String tiempo = diff < 3600000 ? "Hace un momento" : "Hace " + (diff / 3600000) + "h";
        holder.tvDate.setText(tiempo);

        // Click → abrir detalle
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onClick(publicacion));
        }
    }

    @Override
    public int getItemCount() {
        return listaPublicaciones.size();
    }

    /**
     * ViewHolder - El Plato vacío esperando comida
     */
    public static class ClaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvAutor, tvDate;
        Chip chipSubject, chipType;

        public ClaseViewHolder(@NonNull View itemView) {
            super(itemView);
            // Buscamos las referencias una sola vez (como findViewById)
            tvTitulo = itemView.findViewById(R.id.tvTitle);
            tvDescripcion = itemView.findViewById(R.id.tvDescription);
            tvAutor = itemView.findViewById(R.id.tvAuthor);
            tvDate = itemView.findViewById(R.id.tvDate);
            chipSubject = itemView.findViewById(R.id.chipSubject);
            chipType = itemView.findViewById(R.id.chipType);
        }
    }
}

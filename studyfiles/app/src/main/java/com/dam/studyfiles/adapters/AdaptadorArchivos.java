package com.dam.studyfiles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studyfiles.R;
import com.dam.studyfiles.models.Archivo;
import java.util.List;

public class AdaptadorArchivos extends RecyclerView.Adapter<AdaptadorArchivos.ViewHolder> {

    public interface OnArchivoClick {
        void onClick(Archivo archivo);
    }

    private List<Archivo>    lista;
    private final OnArchivoClick listener;

    public AdaptadorArchivos(List<Archivo> lista, OnArchivoClick listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    public void actualizar(List<Archivo> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_archivo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Archivo a = lista.get(position);
        h.tvNombre.setText(a.nombre);
        h.tvUploader.setText("Por: " + (a.uploader != null ? a.uploader : "Anónimo"));
        h.tvTipo.setText(a.tipoArchivo != null ? a.tipoArchivo.toUpperCase() : "FILE");

        // Barra de likes/dislikes
        int total = a.likes + a.dislikes;
        if (total > 0) {
            h.pbVotos.setProgress((int)((float) a.likes / total * 100));
        } else {
            h.pbVotos.setProgress(0);
        }
        h.tvVotos.setText(a.likes + " 👍  " + a.dislikes + " 👎");
        h.itemView.setOnClickListener(v -> listener.onClick(a));
    }

    @Override
    public int getItemCount() { return lista != null ? lista.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvNombre, tvUploader, tvTipo, tvVotos;
        ProgressBar pbVotos;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNombre   = v.findViewById(R.id.tvNombreArchivo);
            tvUploader = v.findViewById(R.id.tvUploader);
            tvTipo     = v.findViewById(R.id.tvTipoArchivo);
            tvVotos    = v.findViewById(R.id.tvVotos);
            pbVotos    = v.findViewById(R.id.pbVotos);
        }
    }
}

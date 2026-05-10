package com.dam.studyfiles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studyfiles.R;
import com.dam.studyfiles.models.MiArchivo;
import java.util.List;

public class AdaptadorMisArchivos extends RecyclerView.Adapter<AdaptadorMisArchivos.ViewHolder> {

    public interface OnArchivoClick     { void onClick(MiArchivo a); }
    public interface OnArchivoLongClick { void onLongClick(MiArchivo a, View anchor); }

    private List<MiArchivo>         lista;
    private final OnArchivoClick     onClick;
    private final OnArchivoLongClick onLongClick;

    public AdaptadorMisArchivos(List<MiArchivo> lista,
                                 OnArchivoClick onClick,
                                 OnArchivoLongClick onLongClick) {
        this.lista       = lista;
        this.onClick     = onClick;
        this.onLongClick = onLongClick;
    }

    public void actualizar(List<MiArchivo> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    public MiArchivo getItem(int pos) { return lista.get(pos); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mi_archivo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MiArchivo a = lista.get(position);
        h.tvNombre.setText(a.nombre);
        h.tvTipo.setText(a.tipoArchivo != null ? a.tipoArchivo.toUpperCase() : "FILE");
        h.itemView.setOnClickListener(v -> onClick.onClick(a));
        h.itemView.setOnLongClickListener(v -> {
            onLongClick.onLongClick(a, v);
            return true;
        });
    }

    @Override public int getItemCount() { return lista != null ? lista.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcono;
        TextView  tvNombre, tvTipo;
        ViewHolder(@NonNull View v) {
            super(v);
            ivIcono  = v.findViewById(R.id.ivIconoMiArchivo);
            tvNombre = v.findViewById(R.id.tvNombreMiArchivo);
            tvTipo   = v.findViewById(R.id.tvTipoMiArchivo);
        }
    }
}

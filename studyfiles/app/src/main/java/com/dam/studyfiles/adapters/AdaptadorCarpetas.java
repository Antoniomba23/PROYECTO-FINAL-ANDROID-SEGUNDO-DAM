package com.dam.studyfiles.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studyfiles.R;
import com.dam.studyfiles.models.MiCarpeta;
import java.util.List;

public class AdaptadorCarpetas extends RecyclerView.Adapter<AdaptadorCarpetas.ViewHolder> {

    public interface OnCarpetaClick     { void onClick(MiCarpeta c); }
    public interface OnCarpetaLongClick { void onLongClick(MiCarpeta c, View anchor); }

    private List<MiCarpeta>       lista;
    private final OnCarpetaClick      onClick;
    private final OnCarpetaLongClick  onLongClick;

    public AdaptadorCarpetas(List<MiCarpeta> lista,
                              OnCarpetaClick onClick,
                              OnCarpetaLongClick onLongClick) {
        this.lista       = lista;
        this.onClick     = onClick;
        this.onLongClick = onLongClick;
    }

    public void actualizar(List<MiCarpeta> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carpeta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MiCarpeta c = lista.get(position);
        h.tvNombre.setText(c.nombre);
        try {
            String col = c.color != null ? c.color : "#1565C0";
            h.ivIcono.setColorFilter(Color.parseColor(col));
        } catch (Exception ignored) {
            h.ivIcono.setColorFilter(Color.parseColor("#1565C0"));
        }
        h.itemView.setOnClickListener(v -> onClick.onClick(c));
        h.itemView.setOnLongClickListener(v -> {
            onLongClick.onLongClick(c, v);
            return true;
        });
    }

    @Override public int getItemCount() { return lista != null ? lista.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcono;
        TextView  tvNombre;
        ViewHolder(@NonNull View v) {
            super(v);
            ivIcono  = v.findViewById(R.id.ivIconoCarpeta);
            tvNombre = v.findViewById(R.id.tvNombreCarpeta);
        }
    }
}

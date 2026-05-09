package com.dam.studyfiles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studyfiles.R;
import com.dam.studyfiles.database.Favorito;
import java.util.List;

public class AdaptadorFavoritos extends RecyclerView.Adapter<AdaptadorFavoritos.ViewHolder> {

    public interface OnFavoritoClick {
        void onClick(Favorito favorito);
    }

    private List<Favorito>    lista;
    private final OnFavoritoClick listener;

    public AdaptadorFavoritos(List<Favorito> lista, OnFavoritoClick listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    public Favorito getItem(int pos) { return lista.get(pos); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorito, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Favorito f = lista.get(position);
        h.tvNombre.setText(f.nombre);
        h.tvCategoria.setText(f.categoria);
        h.tvUploader.setText("Por: " + (f.uploader != null ? f.uploader : "Anónimo"));
        h.tvTipo.setText(f.tipoArchivo != null ? f.tipoArchivo.toUpperCase() : "FILE");
        h.itemView.setOnClickListener(v -> listener.onClick(f));
    }

    @Override
    public int getItemCount() { return lista != null ? lista.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCategoria, tvUploader, tvTipo;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNombre   = v.findViewById(R.id.tvNombreFav);
            tvCategoria= v.findViewById(R.id.tvCategoriaFav);
            tvUploader = v.findViewById(R.id.tvUploaderFav);
            tvTipo     = v.findViewById(R.id.tvTipoFav);
        }
    }
}

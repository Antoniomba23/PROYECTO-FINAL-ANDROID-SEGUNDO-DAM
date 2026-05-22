package com.dam.studyfiles.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.database.Favorito;

import java.io.File;
import java.util.List;

public class AdaptadorFavoritos extends RecyclerView.Adapter<AdaptadorFavoritos.ViewHolder> {

    public interface OnFavoritoClick      { void onClick(Favorito favorito); }
    public interface OnFavoritoLongClick  { void onLongClick(Favorito favorito); }

    private List<Favorito>    lista;
    private final OnFavoritoClick     listener;
    private final OnFavoritoLongClick longListener;

    public AdaptadorFavoritos(List<Favorito> lista, OnFavoritoClick listener) {
        this.lista        = lista;
        this.listener     = listener;
        this.longListener = null;
    }

    public AdaptadorFavoritos(List<Favorito> lista, OnFavoritoClick listener, OnFavoritoLongClick longListener) {
        this.lista        = lista;
        this.listener     = listener;
        this.longListener = longListener;
    }

    public Favorito getItem(int pos) { return lista.get(pos); }

    @NonNull @Override
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

        // Indicador visual de disponibilidad offline
        boolean tieneLocal = f.rutaLocal != null && new File(f.rutaLocal).exists();
        h.tvOffline.setText(tieneLocal ? "📴 Disponible sin internet" : "🌐 Requiere internet");
        h.tvOffline.setAlpha(tieneLocal ? 1f : 0.5f);

        h.itemView.setOnClickListener(v -> {
            if (tieneLocal) {
                try {
                    File file = new File(f.rutaLocal);
                    Uri uri = FileProvider.getUriForFile(v.getContext(),
                            v.getContext().getPackageName() + ".fileprovider", file);
                    String mime = getMimeType(f.tipoArchivo);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, mime);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    v.getContext().startActivity(Intent.createChooser(intent, "Abrir con..."));
                } catch (Exception e) {
                    Toast.makeText(v.getContext(),
                            "No se puede abrir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                listener.onClick(f);
            }
        });

        // Pulsación larga → menú de opciones
        h.itemView.setOnLongClickListener(v -> {
            if (longListener != null) longListener.onLongClick(f);
            return true;
        });
    }

    @Override public int getItemCount() { return lista != null ? lista.size() : 0; }

    private String getMimeType(String ext) {
        if (ext == null) return "*/*";
        switch (ext.toLowerCase()) {
            case "pdf":  return "application/pdf";
            case "doc":  return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt":  return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xls":  return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "png":  return "image/png";
            case "jpg":  case "jpeg": return "image/jpeg";
            case "txt":  return "text/plain";
            case "zip":  return "application/zip";
            default:     return "*/*";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCategoria, tvUploader, tvTipo, tvOffline;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNombre   = v.findViewById(R.id.tvNombreFav);
            tvCategoria= v.findViewById(R.id.tvCategoriaFav);
            tvUploader = v.findViewById(R.id.tvUploaderFav);
            tvTipo     = v.findViewById(R.id.tvTipoFav);
            tvOffline  = v.findViewById(R.id.tvOfflineFav);
        }
    }
}

package com.dam.studyfiles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.models.MensajeChat;

import java.util.List;

public class AdaptadorChat extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private List<MensajeChat> mensajes;

    public AdaptadorChat(List<MensajeChat> mensajes) {
        this.mensajes = mensajes;
    }

    public void actualizarMensajes(List<MensajeChat> nuevos) {
        this.mensajes = nuevos;
        notifyDataSetChanged();
    }

    public void agregarMensaje(MensajeChat msg) {
        this.mensajes.add(msg);
        notifyItemInserted(this.mensajes.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if ("user".equals(mensajes.get(position).rol)) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje_tutor, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MensajeChat msg = mensajes.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMensaje.setText(msg.mensaje);
        } else if (holder instanceof AIViewHolder) {
            AIViewHolder aiHolder = (AIViewHolder) holder;
            String texto = msg.mensaje;

            aiHolder.llArchivos.removeAllViews();
            aiHolder.llArchivos.setVisibility(View.GONE);

            // Buscar patrón [file:id:nombre]
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\[file:(.*?):(.*?)\\]");
            java.util.regex.Matcher m = p.matcher(texto);

            boolean hasFiles = false;
            while (m.find()) {
                hasFiles = true;
                String idStr = m.group(1);
                String nombre = m.group(2);

                android.widget.Button btn = new android.widget.Button(aiHolder.itemView.getContext());
                btn.setText(nombre);
                btn.setAllCaps(false);
                btn.setBackgroundColor(android.graphics.Color.parseColor("#3949AB"));
                btn.setTextColor(android.graphics.Color.WHITE);
                
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 0);
                btn.setLayoutParams(params);

                btn.setOnClickListener(v -> abrirArchivoDetalle(aiHolder.itemView.getContext(), idStr, btn));

                aiHolder.llArchivos.addView(btn);
            }

            if (hasFiles) {
                texto = m.replaceAll("").trim();
                aiHolder.llArchivos.setVisibility(View.VISIBLE);
            }

            aiHolder.tvMensaje.setText(texto);
        }
    }

    private void abrirArchivoDetalle(android.content.Context context, String idStr, android.widget.Button btn) {
        btn.setEnabled(false);
        btn.setText("Cargando...");
        com.dam.studyfiles.network.SupabaseClient.getApi().getArchivoPorId("eq." + idStr).enqueue(new retrofit2.Callback<List<com.dam.studyfiles.models.Archivo>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.dam.studyfiles.models.Archivo>> call, retrofit2.Response<List<com.dam.studyfiles.models.Archivo>> response) {
                btn.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    com.dam.studyfiles.models.Archivo a = response.body().get(0);
                    android.content.Intent i = new android.content.Intent(context, com.dam.studyfiles.activities.ActividadDetalle.class);
                    i.putExtra("archivo_id", a.id);
                    i.putExtra("nombre", a.nombre);
                    i.putExtra("descripcion", a.descripcion);
                    i.putExtra("categoria", a.categoria);
                    i.putExtra("uploader", a.uploader);
                    i.putExtra("url_archivo", a.urlArchivo);
                    i.putExtra("tipo_archivo", a.tipoArchivo);
                    i.putExtra("likes", a.likes);
                    i.putExtra("dislikes", a.dislikes);
                    i.putExtra("reportes", a.reportes);
                    i.putExtra("institucion", a.institucion);
                    i.putExtra("nivel_estudios", a.nivelEstudios);
                    context.startActivity(i);
                } else {
                    android.widget.Toast.makeText(context, "No se pudo cargar el archivo", android.widget.Toast.LENGTH_SHORT).show();
                }
                btn.setText(btn.getText().toString().replace("Cargando...", "").trim()); // Restaurar texto (aproximado)
            }

            @Override
            public void onFailure(retrofit2.Call<List<com.dam.studyfiles.models.Archivo>> call, Throwable t) {
                btn.setEnabled(true);
                android.widget.Toast.makeText(context, "Error de red", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mensajes != null ? mensajes.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        UserViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
        }
    }

    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        android.widget.LinearLayout llArchivos;
        AIViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
            llArchivos = itemView.findViewById(R.id.llArchivos);
        }
    }
}

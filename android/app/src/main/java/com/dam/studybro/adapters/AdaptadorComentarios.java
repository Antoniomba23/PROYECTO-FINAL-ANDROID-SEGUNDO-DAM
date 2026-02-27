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
    
    public interface OnReplyClickListener {
        void onReplyClick(Comentario parent);
    }
    
    public interface OnCommentActionListener {
        void onEditClick(Comentario c);
        void onDeleteClick(Comentario c);
    }

    private final List<Comentario> lista;
    private final OnReplyClickListener replyListener;
    private final OnCommentActionListener actionListener;
    private final String currentUserEmail;

    public AdaptadorComentarios(List<Comentario> lista, String currentUserEmail, OnReplyClickListener replyListener, OnCommentActionListener actionListener) {
        this.lista = lista;
        this.currentUserEmail = currentUserEmail;
        this.replyListener = replyListener;
        this.actionListener = actionListener;
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
        
        // Formatear autor (si es email, quitar dominio)
        String autor = c.usuarioId != null && c.usuarioId.contains("@")
                ? c.usuarioId.substring(0, c.usuarioId.indexOf("@"))
                : (c.usuarioId != null ? c.usuarioId : "Anónimo");
        h.tvAuthor.setText(autor);
        
        h.tvContent.setText(c.contenido);
        // Fecha relativa ("Hace 3 minutos")
        h.tvDate.setText(DateUtils.getRelativeTimeSpanString(
                c.fecha, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));

        // Manejar sangría si es respuesta
        ViewGroup.LayoutParams baseParams = h.itemView.getLayoutParams();
        if (baseParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) baseParams;
            if (c.parentId != null) {
                // Es una respuesta, aplicar margen izquierdo (sangría)
                int margin = (int) (24 * h.itemView.getContext().getResources().getDisplayMetrics().density);
                params.setMargins(margin, params.topMargin, params.rightMargin, params.bottomMargin);
                h.btnReply.setVisibility(View.GONE); // No permitimos hilos infinitos por ahora (solo 1 nivel)
            } else {
                // Es comentario raíz
                params.setMargins(0, params.topMargin, params.rightMargin, params.bottomMargin);
                h.btnReply.setVisibility(View.VISIBLE);
                h.btnReply.setOnClickListener(v -> {
                    if (replyListener != null) replyListener.onReplyClick(c);
                });
            }
            h.itemView.setLayoutParams(params);
        } else {
            // Fallback si el layout base no lo soporta
            if (c.parentId != null) {
                h.btnReply.setVisibility(View.GONE);
            } else {
                h.btnReply.setVisibility(View.VISIBLE);
                h.btnReply.setOnClickListener(v -> {
                    if (replyListener != null) replyListener.onReplyClick(c);
                });
            }
        }
        
        // Si el comentario está eliminado, forzamos a ocultar el botón de responder
        if ("[Eliminado]".equals(c.contenido)) {
            h.btnReply.setVisibility(View.GONE);
        }
        
        // Menú contextual para Editar/Borrar si es del usuario actual y no está [Eliminado]
        if (c.usuarioId != null && c.usuarioId.equals(currentUserEmail) && !"[Eliminado]".equals(c.contenido)) {
            h.itemView.setOnLongClickListener(v -> {
                if (actionListener != null) {
                    mostrarMenuOpciones(v, c);
                }
                return true;
            });
        } else {
            h.itemView.setOnLongClickListener(null); // Quitar listener si no es dueño
        }
    }
    
    private void mostrarMenuOpciones(View v, Comentario c) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), v);
        popup.getMenu().add(0, 1, 0, "Editar Comentario");
        popup.getMenu().add(0, 2, 1, "Eliminar Comentario");
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    actionListener.onEditClick(c);
                    return true;
                case 2:
                    actionListener.onDeleteClick(c);
                    return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvDate, tvContent, btnReply;
        ViewHolder(View v) {
            super(v);
            tvAuthor  = v.findViewById(R.id.tvAuthor);
            tvDate    = v.findViewById(R.id.tvDate);
            tvContent = v.findViewById(R.id.tvContent);
            btnReply  = v.findViewById(R.id.btnReply);
        }
    }
}

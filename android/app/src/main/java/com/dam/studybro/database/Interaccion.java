package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;import androidx.room.Index;
import androidx.room.Ignore;

/**
 * Interacción de un usuario con una publicación.
 * tipo: "ME_GUSTA" o "GUARDADO"
 * usuarioId: email de Supabase (String), no FK local.
 */
@Entity(tableName = "interacciones",
        indices = {@Index("publicacion_id")})
public class Interaccion {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "tipo")
    public String tipo; // "ME_GUSTA", "GUARDADO"

    /** Email del usuario de Supabase (no FK local) */
    @ColumnInfo(name = "usuario_id")
    public String usuarioId;

    @ColumnInfo(name = "publicacion_id")
    public int publicacionId;

    public Interaccion() {}

    @Ignore
    public Interaccion(String tipo, String usuarioId, int publicacionId) {
        this.tipo          = tipo;
        this.usuarioId     = usuarioId;
        this.publicacionId = publicacionId;
    }
}

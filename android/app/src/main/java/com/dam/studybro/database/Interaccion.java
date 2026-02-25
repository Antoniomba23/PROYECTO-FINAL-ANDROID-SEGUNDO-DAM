package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Interacción de un usuario con una publicación.
 * tipo: "ME_GUSTA" o "GUARDADO"
 * usuarioId: email de Supabase (String), no FK local.
 */
@Entity(tableName = "interacciones",
        foreignKeys = {
            @ForeignKey(entity = Publicacion.class,
                        parentColumns = "id",
                        childColumns = "publicacion_id",
                        onDelete = ForeignKey.CASCADE)
        })
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

    public Interaccion(String tipo, String usuarioId, int publicacionId) {
        this.tipo          = tipo;
        this.usuarioId     = usuarioId;
        this.publicacionId = publicacionId;
    }
}

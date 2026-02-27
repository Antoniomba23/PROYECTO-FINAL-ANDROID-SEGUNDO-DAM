package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Index;

/**
 * Publicacion — sin FK a usuarios (el auth ahora es Supabase, no Room local).
 * Se guarda el UUID de Supabase como String en usuario_id.
 */
@Entity(tableName = "publicaciones",
        foreignKeys = {
            @ForeignKey(entity = Asignatura.class,
                        parentColumns = "id",
                        childColumns = "asignatura_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("asignatura_id")})
public class Publicacion {
    @PrimaryKey(autoGenerate = true)
    @com.google.gson.annotations.SerializedName("id")
    public int id;

    @ColumnInfo(name = "titulo")
    @com.google.gson.annotations.SerializedName("titulo")
    public String titulo;

    @ColumnInfo(name = "descripcion")
    @com.google.gson.annotations.SerializedName("descripcion")
    public String descripcion;

    @ColumnInfo(name = "archivo_url")
    @com.google.gson.annotations.SerializedName("archivo_url")
    public String archivoUrl;  // URL pública del archivo subido a Supabase Storage

    @ColumnInfo(name = "tipo")
    @com.google.gson.annotations.SerializedName("tipo")
    public String tipo; // "APUNTE", "EXAMEN", "DUDA", "TAREA"

    @ColumnInfo(name = "anio_escolar")
    @com.google.gson.annotations.SerializedName("anio_escolar")
    public String anioEscolar;

    @ColumnInfo(name = "fecha_subida")
    @com.google.gson.annotations.SerializedName("fecha_subida")
    public long fechaSubida;

    @ColumnInfo(name = "curso")
    @com.google.gson.annotations.SerializedName("curso")
    public String curso; // "1º", "2º"

    @ColumnInfo(name = "usuario_id")
    @com.google.gson.annotations.SerializedName("usuario_id")
    public String usuarioId;  // UUID de Supabase (String, no int)

    @ColumnInfo(name = "asignatura_id")
    @com.google.gson.annotations.SerializedName("asignatura_id")
    public int asignaturaId;

    public Publicacion() {}
}

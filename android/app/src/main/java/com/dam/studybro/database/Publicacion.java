package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

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
        })
public class Publicacion {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "descripcion")
    public String descripcion;

    @ColumnInfo(name = "archivo_url")
    public String archivoUrl;  // URL pública del archivo subido a Supabase Storage

    @ColumnInfo(name = "tipo")
    public String tipo; // "APUNTE", "EXAMEN", "DUDA", "TAREA"

    @ColumnInfo(name = "anio_escolar")
    public String anioEscolar;

    @ColumnInfo(name = "fecha_subida")
    public long fechaSubida;

    @ColumnInfo(name = "usuario_id")
    public String usuarioId;  // UUID de Supabase (String, no int)

    @ColumnInfo(name = "asignatura_id")
    public int asignaturaId;

    public Publicacion() {}
}

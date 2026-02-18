package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "publicaciones",
        foreignKeys = {
            @ForeignKey(entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.CASCADE),
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
    public String archivoUrl;

    @ColumnInfo(name = "tipo")
    public String tipo; // "TAREA", "APUNTE"

    @ColumnInfo(name = "anio_escolar")
    public String anioEscolar;

    @ColumnInfo(name = "fecha_subida")
    public long fechaSubida;

    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    @ColumnInfo(name = "asignatura_id")
    public int asignaturaId;

    public Publicacion() {}

    public Publicacion(String titulo, String descripcion, String tipo, long fechaSubida, int usuarioId, int asignaturaId) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.fechaSubida = fechaSubida;
        this.usuarioId = usuarioId;
        this.asignaturaId = asignaturaId;
    }
}

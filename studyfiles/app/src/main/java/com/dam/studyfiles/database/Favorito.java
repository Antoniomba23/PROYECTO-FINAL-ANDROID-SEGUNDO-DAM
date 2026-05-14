package com.dam.studyfiles.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para favoritos guardados localmente.
 * Incluye rutaLocal para abrir el archivo SIN INTERNET.
 */
@Entity(tableName = "favoritos")
public class Favorito {

    @PrimaryKey
    public int id;           // = id del archivo en Supabase

    public String nombre;
    public String descripcion;
    public String categoria;
    public String uploader;
    public String urlArchivo;
    public String tipoArchivo;
    public String rutaLocal;  // ruta al archivo descargado en el dispositivo
    public int    likes;
    public int    dislikes;
    public long   fechaGuardado;

    public Favorito() {}

    public Favorito(int id, String nombre, String descripcion, String categoria,
                    String uploader, String urlArchivo, String tipoArchivo,
                    int likes, int dislikes) {
        this.id           = id;
        this.nombre       = nombre;
        this.descripcion  = descripcion;
        this.categoria    = categoria;
        this.uploader     = uploader;
        this.urlArchivo   = urlArchivo;
        this.tipoArchivo  = tipoArchivo;
        this.likes        = likes;
        this.dislikes     = dislikes;
        this.fechaGuardado = System.currentTimeMillis();
    }
}

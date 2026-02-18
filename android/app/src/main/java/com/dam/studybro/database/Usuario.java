package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios",
        foreignKeys = {
            @ForeignKey(entity = Centro.class,
                        parentColumns = "id",
                        childColumns = "centro_id",
                        onDelete = ForeignKey.SET_NULL),
            @ForeignKey(entity = Especialidad.class,
                        parentColumns = "id",
                        childColumns = "especialidad_id",
                        onDelete = ForeignKey.SET_NULL)
        })
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "avatar_url")
    public String avatarUrl;

    @ColumnInfo(name = "rol")
    public String rol; // "ESTUDIANTE", "ADMIN"

    @ColumnInfo(name = "centro_id")
    public Integer centroId; // Nullable

    @ColumnInfo(name = "especialidad_id")
    public Integer especialidadId; // Nullable

    public Usuario() {}

    public Usuario(String nombre, String email, String passwordHash, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
}

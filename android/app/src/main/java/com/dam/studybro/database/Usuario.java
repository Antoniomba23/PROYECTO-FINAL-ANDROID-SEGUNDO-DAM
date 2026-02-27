package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import com.google.gson.annotations.SerializedName;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.Ignore;

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
        },
        indices = {@Index("centro_id"), @Index("especialidad_id")})
public class Usuario {

    @PrimaryKey
    @androidx.annotation.NonNull
    @SerializedName("id")
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "email")
    public String email;

    @SerializedName("password_hash")
    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @SerializedName("avatar_url")
    @ColumnInfo(name = "avatar_url")
    public String avatarUrl;

    @ColumnInfo(name = "rol")
    public String rol; // "ESTUDIANTE", "ADMIN"

    @SerializedName("centro_id")
    @ColumnInfo(name = "centro_id")
    public Integer centroId; // Nullable

    @SerializedName("especialidad_id")
    @ColumnInfo(name = "especialidad_id")
    public Integer especialidadId; // Nullable

    public Usuario() {}

    @Ignore
    public Usuario(String nombre, String email, String passwordHash, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
}

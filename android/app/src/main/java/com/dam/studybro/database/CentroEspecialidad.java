package com.dam.studybro.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * Tabla intermedia para la relación muchos a muchos entre Centros y Especialidades.
 */
@Entity(tableName = "centro_especialidades",
        primaryKeys = {"centro_id", "especialidad_id"},
        foreignKeys = {
                @ForeignKey(entity = Centro.class,
                        parentColumns = "id",
                        childColumns = "centro_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Especialidad.class,
                        parentColumns = "id",
                        childColumns = "especialidad_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("especialidad_id")}
)
public class CentroEspecialidad {
    public int centro_id;
    public int especialidad_id;

    public CentroEspecialidad(int centro_id, int especialidad_id) {
        this.centro_id = centro_id;
        this.especialidad_id = especialidad_id;
    }
}

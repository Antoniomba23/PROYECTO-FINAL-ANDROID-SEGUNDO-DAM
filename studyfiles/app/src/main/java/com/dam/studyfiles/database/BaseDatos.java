package com.dam.studyfiles.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
    entities = { Favorito.class, VotoLocal.class },
    version = 3,
    exportSchema = false
)
public abstract class BaseDatos extends RoomDatabase {

    private static BaseDatos INSTANCE;

    public abstract FavoritoDao favoritoDao();
    public abstract VotoLocalDao votoLocalDao();

    public static synchronized BaseDatos getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    BaseDatos.class,
                    "studyfiles-db"
            ).fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }
}

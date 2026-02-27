package com.dam.studybro.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.dam.studybro.R;
import com.dam.studybro.activities.ActividadLogin;
import com.dam.studybro.activities.ActividadPerfil;
import com.dam.studybro.activities.ActividadPerfilCentro;
import com.dam.studybro.activities.ActividadPrincipal;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    public static void setupBottomNavigation(Activity activity, BottomNavigationView bottomNav) {
        SharedPreferences prefs = activity.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        boolean sesionIniciada = prefs.getBoolean("sesion_iniciada", false);
        String rol = prefs.getString("rol_usuario", "ESTUDIANTE");
        boolean esAdmin = "ADMIN".equals(rol);

        // Controlar visibilidad del botón "Mi Centro"
        bottomNav.getMenu().findItem(R.id.bottom_mi_centro).setVisible(!esAdmin);

        // Marcar el item actual según la actividad
        if (activity instanceof ActividadPrincipal) {
            bottomNav.setSelectedItemId(R.id.bottom_home);
        } else if (activity instanceof ActividadPerfil) {
            bottomNav.setSelectedItemId(R.id.bottom_perfil);
        } else if (activity instanceof ActividadPerfilCentro) {
            int myCentroId = prefs.getInt("centro_id", -1);
            int currentCentroId = activity.getIntent().getIntExtra("centro_id", -2);
            if (myCentroId != -1 && myCentroId == currentCentroId) {
                bottomNav.setSelectedItemId(R.id.bottom_mi_centro);
            } else {
                // Si es un centro ajeno, no marcamos "Mi Centro" para evitar confusión
                bottomNav.getMenu().setGroupCheckable(0, true, false);
                for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                    bottomNav.getMenu().getItem(i).setChecked(false);
                }
                bottomNav.getMenu().setGroupCheckable(0, true, true);
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.bottom_home) {
                if (!(activity instanceof ActividadPrincipal)) {
                    Intent intent = new Intent(activity, ActividadPrincipal.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                }
                return true;
            } else if (id == R.id.bottom_mi_centro) {
                if (!sesionIniciada) {
                    Toast.makeText(activity, "Debe iniciar sesión para ver su centro", Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, ActividadLogin.class));
                } else {
                    int myCentroId = prefs.getInt("centro_id", -1);
                    if (myCentroId != -1) {
                        Intent intent = new Intent(activity, ActividadPerfilCentro.class);
                        intent.putExtra("centro_id", myCentroId);
                        activity.startActivity(intent);
                    } else {
                        Toast.makeText(activity, "No tiene un centro asignado", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            } else if (id == R.id.bottom_perfil) {
                if (!(activity instanceof ActividadPerfil)) {
                    activity.startActivity(new Intent(activity, ActividadPerfil.class));
                }
                return true;
            }
            return false;
        });
    }
}

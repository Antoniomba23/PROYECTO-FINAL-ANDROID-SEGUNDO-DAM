package com.dam.studyfiles.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dam.studyfiles.R;
import com.dam.studyfiles.models.Usuario;
import com.dam.studyfiles.network.SupabaseClient;
import com.dam.studyfiles.utils.DispositivoUtils;
import com.dam.studyfiles.utils.HashUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadLogin extends AppCompatActivity {

    private TextInputEditText etUsuario, etContrasena;
    private Button btnAccion;
    private TextView tvLoginTitulo, tvCambiarModo;
    private ProgressBar pb;

    private boolean modoLogin = true; // true = Login, false = Registro

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cuenta");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnAccion = findViewById(R.id.btnAccion);
        tvLoginTitulo = findViewById(R.id.tvLoginTitulo);
        tvCambiarModo = findViewById(R.id.tvCambiarModo);
        pb = findViewById(R.id.pbLogin);

        btnAccion.setOnClickListener(v -> procesarAccion());
        tvCambiarModo.setOnClickListener(v -> toggleModo());
    }

    private void toggleModo() {
        modoLogin = !modoLogin;
        if (modoLogin) {
            tvLoginTitulo.setText("Iniciar Sesión");
            btnAccion.setText("Entrar");
            tvCambiarModo.setText("¿No tienes cuenta? Regístrate");
        } else {
            tvLoginTitulo.setText("Crear Cuenta");
            btnAccion.setText("Registrar");
            tvCambiarModo.setText("¿Ya tienes cuenta? Inicia sesión");
        }
    }

    private void procesarAccion() {
        String user = etUsuario.getText().toString().trim();
        String pass = etContrasena.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        pb.setVisibility(View.VISIBLE);
        btnAccion.setEnabled(false);

        String hashPass = HashUtils.sha256(pass);

        if (modoLogin) {
            login(user, hashPass);
        } else {
            registrar(user, hashPass);
        }
    }

    private void login(String user, String hashPass) {
        SupabaseClient.getUsuarios().buscarPorNombre("eq." + user)
                .enqueue(new Callback<List<Usuario>>() {
                    @Override
                    public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                        pb.setVisibility(View.GONE);
                        btnAccion.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Usuario u = response.body().get(0);
                            if (u.contrasena.equals(hashPass)) {
                                // Login correcto
                                DispositivoUtils.setUsuario(ActividadLogin.this, String.valueOf(u.id), u.nombreUsuario);
                                Toast.makeText(ActividadLogin.this, "¡Bienvenido " + u.nombreUsuario + "!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ActividadLogin.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ActividadLogin.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Usuario>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        btnAccion.setEnabled(true);
                        Toast.makeText(ActividadLogin.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registrar(String user, String hashPass) {
        // Primero verificar si el usuario ya existe
        SupabaseClient.getUsuarios().buscarPorNombre("eq." + user)
                .enqueue(new Callback<List<Usuario>>() {
                    @Override
                    public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            pb.setVisibility(View.GONE);
                            btnAccion.setEnabled(true);
                            Toast.makeText(ActividadLogin.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                        } else {
                            // No existe, proceder a registrar
                            procederRegistro(user, hashPass);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Usuario>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        btnAccion.setEnabled(true);
                        Toast.makeText(ActividadLogin.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void procederRegistro(String user, String hashPass) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre_usuario", user);
        body.put("contrasena", hashPass);
        body.put("fecha_registro", System.currentTimeMillis());

        SupabaseClient.getUsuarios().registrar(body)
                .enqueue(new Callback<List<Usuario>>() {
                    @Override
                    public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                        pb.setVisibility(View.GONE);
                        btnAccion.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Usuario u = response.body().get(0);
                            DispositivoUtils.setUsuario(ActividadLogin.this, String.valueOf(u.id), u.nombreUsuario);
                            Toast.makeText(ActividadLogin.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ActividadLogin.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Usuario>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        btnAccion.setEnabled(true);
                        Toast.makeText(ActividadLogin.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

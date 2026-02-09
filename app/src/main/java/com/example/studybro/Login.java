package com.example.studybro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        FormUtils formUtils = new FormUtils();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String hashedPassword = sharedPreferences.getString("password", "");
        Log.d("hashedPassword", hashedPassword);

        Button loginButton = findViewById(R.id.loginButton);
        TextView loginTvRegister = findViewById(R.id.HipervinculoRegistrarse);
        TextInputLayout username = findViewById(R.id.TextoInputUsr);
        TextInputLayout password = findViewById(R.id.ContrasenaLogin);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean canContinue = true;


                if (FormUtils.isTilEmpty(username)){
                    username.setErrorEnabled(true);
                    username.setError("Necesitas  acceder con un nombre de usuario");
                    canContinue = false;
                }


                if (FormUtils.isTilEmpty(password)){
                    password.setErrorEnabled(true);
                    password.setError("La contaseña esta vacía");
                    canContinue = false;

                } else if (!FormUtils.checkPassword(FormUtils.getTilText(password),hashedPassword)) {


                    password.setErrorEnabled(true);
                    username.setError("La contraseña es incorrecta");
                    canContinue = false;

                }

                if (canContinue) {

                    Intent intentMain = new Intent(Login.this, MainActivity.class);
                    startActivity(intentMain);
                }
            }
        });


        loginTvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentRegistro = new Intent(Login.this, Register.class);
                startActivity(intentRegistro);
            }
        });



    }
}
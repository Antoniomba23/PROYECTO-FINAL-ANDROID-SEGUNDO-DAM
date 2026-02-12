package com.example.studybro;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

public class PerfilUsuario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.usuarioMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView botonCentro = findViewById(R.id.seccionNombreCentro);
        TextView botonEstudiante = findViewById(R.id.cambiarEstudiante);
        TextView botonEspecialidad = findViewById(R.id.secccionEspecialidad);
        TextView nameTitleCenter = findViewById(R.id.CentroTitulo);
        TextView nameSpeciality = findViewById(R.id.nombreEspecialidad);
        TextView nameStudent = findViewById(R.id.nombreEstudiante);
        TextView descripcionNombreAlumno = findViewById(R.id.descripcionNOmbreAlumno);
        TextView descripcionNombreEstudio = findViewById(R.id.descripcionNombreEstudio);


        botonCentro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(PerfilUsuario.this);
                LayoutInflater inflater = LayoutInflater.from(PerfilUsuario.this);
                View view = inflater.inflate(R.layout.alert_dialog, null);
                TextInputLayout textInputLayout = view.findViewById(R.id.idTExtoINputLayout);
                EditText editText = view.findViewById(R.id.textInputEditText);


                builder.setTitle("Centro educativo")

                        .setMessage("Introduzca el nombre del centro educativo: ")
                        .setView(view)
                        .setNegativeButton("Cancelar", (dialog, which) -> {

                        })
                        .setPositiveButton("Aceptar", (dialog, which) -> {

                            String text = editText.getText().toString();

                            if (text.isEmpty()) {
                                textInputLayout.setErrorEnabled(true);
                                textInputLayout.setError("Este campo no puede estar vacío");

                            } else if (text.isBlank()) {

                                textInputLayout.setErrorEnabled(true);
                                textInputLayout.setError("Este campo no puede  contener caracteres en blanco");
                            } else {
                                nameTitleCenter.setText(text);
                            }
                        });

                builder.create().show();


            }
        });

        botonEspecialidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(PerfilUsuario.this);
                LayoutInflater inflater = LayoutInflater.from(PerfilUsuario.this);
                View view = inflater.inflate(R.layout.alert_dialog_especialidad, null);

                RadioGroup radioGroup = view.findViewById(R.id.grupoOpcionesEspecialidades);


                builder.setTitle("Tipo de Estudio: ")

                        .setMessage("Seleccione una opción de tipo de Estudio: ")
                        .setView(view)
                        .setNegativeButton("Cancelar", (dialog, which) -> {

                        })
                        .setPositiveButton("Aceptar", (dialog, which) -> {

                            if (radioGroup.getCheckedRadioButtonId() == -1) {
                                Toast.makeText(PerfilUsuario.this, "Debes seleccionar una  opción", Toast.LENGTH_SHORT).show();

                            } else {


                                RadioButton radioButton = view.findViewById(radioGroup.getCheckedRadioButtonId());
                                String textoOpcion = radioButton.getText().toString();

                                descripcionNombreEstudio.setText(textoOpcion);


                                AlertDialog.Builder builder2 = new AlertDialog.Builder(PerfilUsuario.this);
                                LayoutInflater inflater2 = LayoutInflater.from(PerfilUsuario.this);
                                View view2 = inflater2.inflate(R.layout.alert_dialog, null);
                                EditText editText = view2.findViewById(R.id.textInputEditText);


                                builder2.setTitle(" Nombre de la Especialidad: ")


                                        .setMessage("Escriba su rama de especialidad: ")
                                        .setView(view2)
                                        .setNegativeButton("Cancelar", (dialog2, which2) -> {

                                        })
                                        .setPositiveButton("Aceptar", (dialog2, which2) -> {

                                            String text = editText.getText().toString();
                                            nameSpeciality.setText(text);


                                        });

                                builder2.create().show();

                            }

                        });

                builder.create().show();


            }
        });


        botonEstudiante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(PerfilUsuario.this);
                LayoutInflater inflater = LayoutInflater.from(PerfilUsuario.this);
                View view = inflater.inflate(R.layout.alert_dialog_nombre_user, null);
                RadioGroup radioGroup2 = findViewById(R.id.grupoNombresUsuario);


                builder.setTitle("Estudiante")

                        .setMessage("Escoga una  de las dos opciones : ")
                        .setView(view)
                        .setNegativeButton("Cancelar", (dialog, which) -> {

                        })
                        .setPositiveButton("Aceptar", (dialog, which) -> {

                            if (radioGroup2.getCheckedRadioButtonId() == -1) {
                                Toast.makeText(PerfilUsuario.this, "Debes seleccionar una  opción", Toast.LENGTH_SHORT).show();

                            } else {

                                RadioButton radioButton = view.findViewById(radioGroup2.getCheckedRadioButtonId()); // Devuelve tl texto de la opción que se ha selccionado
                                String textoOpcion = radioButton.getText().toString();

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(PerfilUsuario.this);
                                LayoutInflater inflater2 = LayoutInflater.from(PerfilUsuario.this);
                                View view2 = inflater2.inflate(R.layout.alert_dialog, null);
                                EditText editText2 = view2.findViewById(R.id.textInputEditText);

                                String tituloView;
                                String mensaje;
                                TextInputLayout textInputLayout = view2.findViewById(R.id.idTExtoINputLayout);


                                if (textoOpcion.equals("NombreUsuario")) {

                                    tituloView = "Nombre";
                                    mensaje = "Escriba su nombre: ";


                                } else {
                                    tituloView = "Descripción";
                                    mensaje = "Escriba breve descripción de usted: ";
                                }

                                builder2.setTitle(tituloView)

                                        .setMessage(mensaje)
                                        .setView(view2)
                                        .setNegativeButton("Cancelar", (dialog2, which2) -> {

                                        })
                                        .setPositiveButton("Aceptar", (dialog2, which2) -> {

                                           String texto =  editText2.getText().toString();

                                            if (texto.isEmpty()) {
                                                textInputLayout.setErrorEnabled(true);
                                                textInputLayout.setError("Este campo no puede estar vacío");

                                            } else if (texto.isBlank()) {

                                                textInputLayout.setErrorEnabled(true);
                                                textInputLayout.setError("Este campo no puede  contener caracteres en blanco");
                                            } else {
                                                nameTitleCenter.setText(texto);
                                            }

                                        });




                            }
                        });

                builder.create().show();


            }
        });


    }


}




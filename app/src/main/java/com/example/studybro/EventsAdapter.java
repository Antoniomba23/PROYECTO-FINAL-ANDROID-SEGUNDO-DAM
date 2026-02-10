package com.example.studybro;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.SostenDeVistas> {

    //Se implmentan los tres dm´´etodos

    //static
    Context context; // El permiso

    CardView cardView;

    ArrayList<EventModel> centros;

    public EventsAdapter(Context context, ArrayList<EventModel> events) {
        this.context = context;
        centros = events;

    }

    @NonNull
    @Override
    public SostenDeVistas onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.tarjeta, parent, false);
        return new SostenDeVistas(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SostenDeVistas sosten, int position) {

        sosten.nombreCentro.setText(centros.get(position).getNombreCentro());
        sosten.tvTipoInsituto.setText(centros.get(position).getNombreCentro());
        sosten.location.setText(centros.get(position).getNombreCentro());
        sosten.tipoAccesibilidad.setText(centros.get(position).getNombreCentro());


        sosten.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });


    }

    @Override
    public int getItemCount() {
        return centros.size();
    }


    public class SostenDeVistas extends RecyclerView.ViewHolder {
        TextView nombreCentro, tipoAccesibilidad, location, tvTipoInsituto;
        CardView card;

        public SostenDeVistas(@NonNull View itemView) {

            super(itemView);
            nombreCentro = itemView.findViewById(R.id.titulocentro);
            location = itemView.findViewById(R.id.ubicacion);
            card = itemView.findViewById(R.id.cardview);
            tipoAccesibilidad = itemView.findViewById(R.id.tipoAccesibilidad);
            tvTipoInsituto = itemView.findViewById(R.id.tipoInstituto);


        }
    }
}







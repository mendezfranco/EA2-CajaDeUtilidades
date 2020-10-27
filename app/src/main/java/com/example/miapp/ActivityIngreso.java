package com.example.miapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ActivityIngreso extends AppCompatActivity {
    public TextView porcentajeBateria;
    private String tokenUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);

        porcentajeBateria = findViewById(R.id.batteryTextView);

        tokenUsuario = getIntent().getStringExtra("token");
        Log.i("Token-Usuario", tokenUsuario);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filterBateria = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent porcentajeTotal = this.registerReceiver(null, filterBateria);
        int nivelDeBateria = porcentajeTotal.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int capacidad = porcentajeTotal.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float porcentaje = nivelDeBateria * 100 / (float) capacidad;
        porcentajeBateria.setText(String.valueOf(porcentaje) + " %");
    }

    public void iniciarActivityAcelerometro(View vista){
        Intent intentAcelerometro = new Intent(this, ActivityAcelerometro.class);
        intentAcelerometro.putExtra("token", tokenUsuario);

        startActivity(intentAcelerometro);
    }

    public void iniciarActivityIluminacion(View vista){
        Intent intentIluminacion = new Intent(this, ActivityIluminacion.class);
        intentIluminacion.putExtra("token", tokenUsuario);

        startActivity(intentIluminacion);
    }

    public void volverALogin(View vista){
        Intent intentLogin = new Intent(this, MainActivity.class);

        startActivity(intentLogin);
    }
}
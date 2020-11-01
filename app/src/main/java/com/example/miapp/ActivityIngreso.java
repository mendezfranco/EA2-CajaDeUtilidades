package com.example.miapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ActivityIngreso extends AppCompatActivity {
    public TextView porcentajeBateria, temperatura;
    private String tokenUsuario;
    public ImageView imagenCargando;
    public RequestThread weatherRequestThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);

        porcentajeBateria = findViewById(R.id.batteryTextView);
        temperatura = findViewById(R.id.temperaturaTextView);
        imagenCargando = findViewById(R.id.loadingImageView);

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

    public void obtenerTemperatura(View vista){
        weatherRequestThread = new RequestThread();
        weatherRequestThread.start();
        Toast.makeText(this, "Obteniendo temperatura actual en Buenos Aires",  Toast.LENGTH_SHORT).show();
    }

    public void mostrarTemperatura(String temperaturaObtenida){
        if(!temperaturaObtenida.equals(" ")){
            imagenCargando.setVisibility(View.GONE);
        }
        else{
            Toast.makeText(this, "Error obteniendo temperatura actual",  Toast.LENGTH_SHORT).show();
        }
        temperatura.setText(temperaturaObtenida);
    }

    class RequestThread extends Thread{
        // Instancio un handler para el thread, y lo asocio al hilo principal a través del Looper
        private Handler threadHandler = new Handler(Looper.getMainLooper());
        JSONObject respuestaJson;
        private String temperaturaObtenida = " ";

        public void run(){
            try {
                String result = Request.generarRequestServicioExterno("https://api.weatherbit.io/v2.0/current?city=Buenos%20Aires&country=AR&key=1b0971b6bb394339ad0f4d94b0f593ce", "GET");
                respuestaJson = new JSONObject(result);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray = respuestaJson.getJSONArray("data"); // Parseo de la respuesta
                        temperaturaObtenida = jsonArray.getJSONObject(0).optString("temp") + " ºC";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        mostrarTemperatura(temperaturaObtenida);
                    }
                }
            });
        }
    }
}
package com.example.miapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ActivityAcelerometro extends AppCompatActivity implements SensorEventListener {
    public TextView valorX, valorY, valorZ, pasosTextView, historialpasosTextView;
    private SensorManager manager;
    Sensor acelerometro;
    private double valorAceleracionAnterior = 0;
    private int stepCount = 0;
    private String tokenUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acelerometro);
        tokenUsuario = getIntent().getStringExtra("token");
        Log.i("Token-Usuario", tokenUsuario);
        JSONObject body = new JSONObject();

        valorX = findViewById(R.id.valorX);
        valorY = findViewById(R.id.valorY);
        valorZ = findViewById(R.id.valorZ);
        pasosTextView = findViewById(R.id.valorPasos);
        historialpasosTextView = findViewById(R.id.historialDePasos);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acelerometro = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        manager.registerListener(ActivityAcelerometro.this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        Log.i("Thread Name:",Thread.currentThread().getName());

        try {
            EventRegisterRequestThread registerRequestThread = new EventRegisterRequestThread(tokenUsuario, armarRequestBody());
            registerRequestThread.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*SharedPreferencesThread threadObtencionDeHistorial = new SharedPreferencesThread();
        threadObtencionDeHistorial.start();*/
        cargarListaDePasos();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        valorX.setText(String.valueOf(event.values[0]));
        valorY.setText(String.valueOf(event.values[1]));
        valorZ.setText(String.valueOf(event.values[2]));
        pasosTextView.setText(String.valueOf(stepCount));

        double valorAceleracion = Math.sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2]);
        double valorDelta = valorAceleracion- valorAceleracionAnterior;
        valorAceleracionAnterior = valorAceleracion;

        if (valorDelta > 6){
            stepCount++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
        almacenarEnSharedPreferences(stepCount);
    }

    @Override
    protected void onResume() {
        super.onResume();

        manager.registerListener(ActivityAcelerometro.this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        cargarListaDePasos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unregisterListener(this);
        almacenarEnSharedPreferences(stepCount);
    }

    public void volverAtras(View view) {
        almacenarEnSharedPreferences(stepCount);
        Intent intentIngreso = new Intent(this, ActivityIngreso.class);
        intentIngreso.putExtra("token", String.valueOf(tokenUsuario));

        startActivity(intentIngreso);
    }

    public JSONObject armarRequestBody() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("env", "TEST");
        body.put("type_events", "Sensando actividad de Acelerometro");
        body.put("description", "El usuario accedio a la utilidad Cuentapasos. Se activo el acelerometro");

        return body;
    }

    public void validarRegistroDeEvento(JSONObject responseJson) throws JSONException {
        Log.i("Registro de evento",responseJson.toString());
        if(responseJson.getString("success") != "true"){
            Toast.makeText(this, "No pudo registrarse la actividad del sensor en el servidor. Debe volver a ingresar", Toast.LENGTH_SHORT).show();
            volverALogin();
        }
        else{
            Toast.makeText(this, "Se registró la actividad del sensor en el servidor correctamente", Toast.LENGTH_SHORT).show();
        }
    }

    public void volverALogin(){
        Intent intentLogin = new Intent(this, MainActivity.class);

        startActivity(intentLogin);
    }

    public void almacenarEnSharedPreferences(int cantidadDePasos){
        SharedPreferences misSharedPreferences = getSharedPreferences("MarcaDePasosAnteriores", Context.MODE_PRIVATE);
        SharedPreferences.Editor modificador = misSharedPreferences.edit();

        modificador.putInt("Cantidad de Pasos", cantidadDePasos);
        modificador.commit();
    }

    public void cargarListaDePasos(){
        SharedPreferences misSharedPreferences = getSharedPreferences("MarcaDePasosAnteriores", Context.MODE_PRIVATE);

        int pasos = misSharedPreferences.getInt("Cantidad de Pasos", 0);
        historialpasosTextView.setText(String.valueOf(pasos));
    }

    class EventRegisterRequestThread extends Thread{
        // Instancio un handler para el thread, y lo asocio al hilo principal a través del Looper
        private Handler threadHandler = new Handler(Looper.getMainLooper());
        JSONObject respuestaJson, body;
        String tokenUsuario;

        public EventRegisterRequestThread(String tokenUsuario, JSONObject body){
            this.tokenUsuario = tokenUsuario;
            this.body = body;
        }

        public void run(){
            try {
                String result = Request.generarRequestEventos("http://so-unlam.net.ar/api/api/event", body, tokenUsuario);
                respuestaJson = new JSONObject(result);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        validarRegistroDeEvento(respuestaJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    class SharedPreferencesThread extends Thread{
        // Instancio un handler para el thread, y lo asocio al hilo principal a través del Looper
        private Handler threadHandler = new Handler(Looper.getMainLooper());

        public void run(){
            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    cargarListaDePasos();
                }
            });
        }
    }
}
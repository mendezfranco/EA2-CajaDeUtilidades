package com.example.miapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ActivityIluminacion extends AppCompatActivity implements SensorEventListener {
    private SensorManager manager;
    private TextView valoresTextView, estadoLinterna;
    Sensor sensorLuz;
    CameraManager camaraUtils;
    private String tokenUsuario;
    public EventRegisterRequestThread registerRequestThread;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iluminacion);

        tokenUsuario = getIntent().getStringExtra("token");
        Log.i("Token-Usuario", tokenUsuario);

        valoresTextView = findViewById(R.id.lightSensorTextView);
        estadoLinterna = findViewById(R.id.linternaTextView);

        camaraUtils = (CameraManager)getSystemService(CAMERA_SERVICE);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorLuz = manager.getDefaultSensor(Sensor.TYPE_LIGHT);

        manager.registerListener(ActivityIluminacion.this, sensorLuz, SensorManager.SENSOR_DELAY_NORMAL);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) || getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            Toast.makeText(ActivityIluminacion.this, "Su smartphone no cuenta con las características necesarias para ejecutar esta funcion", Toast.LENGTH_SHORT);
        }

        try {
            registerRequestThread = new EventRegisterRequestThread(tokenUsuario, armarRequestBody());
            registerRequestThread.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSensorChanged(SensorEvent event) {
        valoresTextView.setText(String.valueOf(event.values[0]));

        if(event.values[0] < 2){
            try {
                camaraUtils.setTorchMode(camaraUtils.getCameraIdList()[0], true);
                estadoLinterna.setText("Linterna Encendida");
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                camaraUtils.setTorchMode(camaraUtils.getCameraIdList()[0], false);
                estadoLinterna.setText("Linterna Apagada");
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
        try {
            camaraUtils.setTorchMode(camaraUtils.getCameraIdList()[0], false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(ActivityIluminacion.this, sensorLuz, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unregisterListener(this);
        try {
            camaraUtils.setTorchMode(camaraUtils.getCameraIdList()[0], false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void volverAtras(View view) {
        Intent intentIngreso = new Intent(this, ActivityIngreso.class);
        intentIngreso.putExtra("token", String.valueOf(tokenUsuario));

        startActivity(intentIngreso);
    }

    public void validarRegistroDeEvento(JSONObject responseJson) throws JSONException {
        Log.i("Registro de evento",responseJson.toString());
        if(!responseJson.getString("success").equals("true")){
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

    public JSONObject armarRequestBody() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("env", "PROD");
        body.put("type_events", "Sensando actividad de Sensor de Luz");
        body.put("description", "El usuario accedio a la utilidad Linterna Inteligente. Se activo el sensor de luminosidad");

        return body;
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
}
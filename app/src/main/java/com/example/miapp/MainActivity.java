package com.example.miapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity{

    public EditText username, password;
    public JSONObject body = new JSONObject();
    public LoginRequestThread loginThread;
    public EventRegisterRequestThread eventRegisterThread;
    public String result, token;
    AlertDialog.Builder popupError;
    ConexionBroadcastReceiver connectionBR = new ConexionBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.usuario);
        password = findViewById(R.id.contraseña);

        popupError = new AlertDialog.Builder(MainActivity.this);
        popupError.setTitle("Error");
        popupError.setMessage("No se pudo ingresar al sistema. Por favor verifique su usuario y contraseña");

        popupError.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void logueo(View vistaInicial){
        try{
            body.put("email", username.getText().toString());
            body.put("password", password.getText().toString());

            Toast.makeText(this, "Validando datos de Usuario. Cargando...", Toast.LENGTH_SHORT).show();
            loginThread = new LoginRequestThread();
            loginThread.start();
        }
        catch(Exception ex){
            Toast.makeText(this, "EXCEPCION en start"+ex, Toast.LENGTH_SHORT).show();
        }
    }

    public void validarIngreso(JSONObject responseJson) throws JSONException {
        Log.i("Thread Name:",Thread.currentThread().getName());
        if(!responseJson.getString("success").equals("true")){
            popupError.create().show();
        }
        else{
            token = responseJson.getString("token");

            //Armo el body para la nueva request
            body = new JSONObject();
            body.put("env", "TEST");
            body.put("type_events", "Login al sistema");
            body.put("description", "Se logueo un usuario en el sistema");

            eventRegisterThread = new EventRegisterRequestThread(responseJson.getString("token"), body);
            eventRegisterThread.start();
        }
    }

    public void iniciarActivityIngreso(String token) throws JSONException {
        Log.i("Thread Name:",Thread.currentThread().getName());
        Intent intentIngreso = new Intent(this, ActivityIngreso.class);
        intentIngreso.putExtra("token", String.valueOf(token));

        startActivity(intentIngreso);
    }

    public void iniciarActivityRegistro(View vista){
        Intent intentRegistro = new Intent(this, ActivityRegistro.class);

        startActivity(intentRegistro);
    }

    public void validarRegistroDeEvento(JSONObject responseJson) throws JSONException {
        if(!responseJson.getString("success").equals("true")){
            Toast.makeText(this, "No se pudo registrar el evento Login en el servidor", Toast.LENGTH_SHORT).show();
        }
        else{
            iniciarActivityIngreso(token);
        }
    }

    class LoginRequestThread extends Thread{
        // Instancio un handler para el thread, y lo asocio al hilo principal a través del Looper
        private Handler threadHandler = new Handler(Looper.getMainLooper());
        JSONObject respuestaJson;

        public void run(){
            try {
                result = Request.generarRequestAutenticacion("http://so-unlam.net.ar/api/api/login", body);
                respuestaJson = new JSONObject(result);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        validarIngreso(respuestaJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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
                result = Request.generarRequestEventos("http://so-unlam.net.ar/api/api/event", body, tokenUsuario);
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

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionBR, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectionBR);
    }
}
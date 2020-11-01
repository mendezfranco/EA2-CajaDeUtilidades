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

public class ActivityRegistro extends AppCompatActivity{

    public EditText nombreNewUser, apellidoNewUser, dniNewUser, emailNewUser, comisionNewUser, passwordNewUser;
    public JSONObject body = new JSONObject();
    public JSONObject respuestaJson;
    public RequestThread connectionThread;
    int responseCode;
    public String result, token;
    AlertDialog.Builder popupError;
    ConexionBroadcastReceiver connectionBR = new ConexionBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        nombreNewUser = findViewById(R.id.nombreRegistro);
        apellidoNewUser = findViewById(R.id.apellidoRegistro);
        dniNewUser = findViewById(R.id.dniRegistro);
        emailNewUser = findViewById(R.id.emailRegistro);
        passwordNewUser = findViewById(R.id.contraseñaRegistro);
        comisionNewUser = findViewById(R.id.comisionRegistro);
        popupError = new AlertDialog.Builder(ActivityRegistro.this);

        popupError.setTitle("Error");

        popupError.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void registrarse(View vistaRegistro){
        try{
            body.put("env", "PROD");
            body.put("name", nombreNewUser.getText().toString());
            body.put("lastname", apellidoNewUser.getText().toString());
            body.put("dni", dniNewUser.getText().toString());
            body.put("email", emailNewUser.getText().toString());
            body.put("password", passwordNewUser.getText().toString());
            body.put("commission", comisionNewUser.getText().toString());

            connectionThread = new RequestThread();
            connectionThread.start();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void iniciarActivityIngreso(String token) {
        Intent intentIngreso = new Intent(this, ActivityIngreso.class);
        intentIngreso.putExtra("token", token);

        startActivity(intentIngreso);
    }

    class RequestThread extends Thread{
        private Handler threadHandler = new Handler(Looper.getMainLooper());
        public void run(){
            try {
                result = Request.generarRequestAutenticacion("http://so-unlam.net.ar/api/api/register", body);
                respuestaJson = new JSONObject(result);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        validarRegistro(respuestaJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            Log.i("Login-ResponseCode",String.valueOf(responseCode));
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

    public void validarRegistro(JSONObject response) throws JSONException {
        if(!response.getString("success").equals("true")){
            popupError.setMessage("Error al crear usuario nuevo. " + response.getString("msg"));
            popupError.create().show();
        }
        else{
            token = response.getString("token");

            //Armo el body para la nueva request
            body = new JSONObject();
            body.put("env", "PROD");
            body.put("type_events", "Nuevo usuario registrado");
            body.put("description", "Se registro un nuevo usuario en el sistema");

            EventRegisterRequestThread eventRegisterThread = new EventRegisterRequestThread(response.getString("token"), body);
            eventRegisterThread.start();

            Toast.makeText(this, "Su usuario ha sido creado correctamente", Toast.LENGTH_SHORT).show();
        }
    }

    public void validarRegistroDeEvento(JSONObject responseJson) throws JSONException {
        if(!responseJson.getString("success").equals("true")){
            Toast.makeText(this, "No pudo registrarse el evento de Registro nuevo en el servidor", Toast.LENGTH_SHORT).show();
        }
        else{
            iniciarActivityIngreso(token);
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
                try {
                    respuestaJson = new JSONObject("{'success':'false'}");
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
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
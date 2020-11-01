package com.example.miapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;

import androidx.appcompat.app.AlertDialog;

public class ConexionBroadcastReceiver extends BroadcastReceiver {
    AlertDialog.Builder popupError;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            boolean sinConexion = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if(sinConexion){
                popupError = new AlertDialog.Builder(context);
                popupError.setTitle("Error");
                popupError.setMessage("No se detecto conexion");

                popupError.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                popupError.create().show();
            }
        }
    }
}

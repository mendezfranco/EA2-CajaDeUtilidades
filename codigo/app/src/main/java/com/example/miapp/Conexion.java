package com.example.miapp;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Conexion {

    public static boolean validarConexionAInternet(Activity requestAct){
        ConnectivityManager connect = (ConnectivityManager) requestAct.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connect != null)
        {
            NetworkInfo[] information = connect.getAllNetworkInfo();
            if (information != null)
                for (int x = 0; x < information.length; x++)
                    if (information[x].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

}

package com.example.miapp;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Request {
    public static URL endpoint;
    public static HttpURLConnection request;
    public static DataOutputStream data;
    public static int responseCode;
    private static String response;

    public static String generarRequestAutenticacion(String ep, JSONObject jsonBody) throws IOException {
        endpoint = new URL(ep);
        request = (HttpURLConnection)endpoint.openConnection();

        request.setDoOutput(true);
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type","application/json");

        data = new DataOutputStream(request.getOutputStream());
        data.writeBytes(jsonBody.toString());
        request.connect();

        responseCode = request.getResponseCode();

        BufferedReader in;
        if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED){
            in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        }
        else{
            in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
        }

        response = convertResponseToInt(in).toString();

        request.disconnect();

        Log.i("Result", response);
        return response;
    }

    public static String generarRequestEventos(String ep, JSONObject jsonBody, String token) throws IOException {
        endpoint = new URL(ep);
        request = (HttpURLConnection)endpoint.openConnection();

        request.setDoInput(true);
        request.setDoOutput(true);
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type","application/json");
        request.setRequestProperty("Authorization", "Bearer "+token);

        data = new DataOutputStream(request.getOutputStream());
        data.writeBytes(jsonBody.toString());

        request.connect();

        responseCode = request.getResponseCode();
        Log.i("ResponseCode",String.valueOf(responseCode));
        BufferedReader in;
        if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED){
            in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        }
        else{
            in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
        }

        response = convertResponseToInt(in).toString();

        request.disconnect();
        Log.i("Registracion-Response", response);
        return response;
    }

    public static String generarRequestServicioExterno(String ep, String method) throws IOException {
        endpoint = new URL(ep);
        request = (HttpURLConnection)endpoint.openConnection();

        request.setRequestMethod(method);

        request.connect();

        responseCode = request.getResponseCode();

        BufferedReader in;
        if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED){
            in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        }
        else{
            in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
        }

        response = convertResponseToInt(in).toString();

        request.disconnect();
        Log.i("Weather-Response", response);
        return response;
    }

    public static StringBuffer convertResponseToInt(BufferedReader input) throws IOException {
        String inputLine;
        StringBuffer stringResponse = new StringBuffer();

        while ((inputLine = input.readLine()) != null) {
            stringResponse.append(inputLine);
        }
        input.close();

        return stringResponse;
    }

}

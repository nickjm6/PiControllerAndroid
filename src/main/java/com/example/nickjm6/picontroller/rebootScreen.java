package com.example.nickjm6.picontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class rebootScreen extends AppCompatActivity {
    private int numTries = 0;
    private String piAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot_screen);
        Intent intent = getIntent();
        final String requestURL = intent.getStringExtra("requestURL");
        piAddress = intent.getStringExtra("piAddress");
        String osName = "";
        if (requestURL.equals("/switchOS")){
            osName =  intent.getStringExtra("osName").toLowerCase().trim();
        }

        String url = "http://" + piAddress + requestURL;

        makePostRequest(url, osName);
    }

    private void makeRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        if (numTries > 7){
            failConnection();
            return;
        }

        String url = "http://" + piAddress + "/osAndVolume";
        final String addr = piAddress;

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String os = "no response from server";
                        int volume = 0;
                        try{
                            JSONObject js = new JSONObject(response);
                            os = js.getString("currentOS");
                            volume = js.getInt("volume");
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                        mainScreen(addr, os, volume);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrorResponse", String.valueOf(error));
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    numTries++;
                    makeRequest();
                }
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void makePostRequest(final String requestURL, final String osName){
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, requestURL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            TimeUnit.SECONDS.sleep(12);
                            makeRequest();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        finish();
                    }
                }
        ) {@Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                if(osName.length() > 0){
                    params.put("osName", osName);
                }
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void failConnection(){
        Intent intent = new Intent(this, failedConnection.class);
        startActivity(intent);
    }

    private void mainScreen(String addr, String os, int volume){
        Intent intent = new Intent(this, SystemCTL.class);
        intent.putExtra("piAddress", addr);
        intent.putExtra("os", os);
        intent.putExtra("volume", volume);
        startActivity(intent);
        finish();
    }
}

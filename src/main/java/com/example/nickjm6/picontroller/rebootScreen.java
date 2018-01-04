package com.example.nickjm6.picontroller;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class rebootScreen extends AppCompatActivity {
    private int numTries = 0;
    private String serverAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot_screen);
        serverAddress = getString(R.string.serverAddress);
        Intent intent = getIntent();
        final String requestURL = intent.getStringExtra("requestURL");
        String osName = "";
        if (requestURL.equals("/switchOS")){
            osName =  intent.getStringExtra("osName").toLowerCase().trim();
        }

        String url = serverAddress + requestURL + "-token";

        makePostRequest(url, osName);
    }

    private void makeRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        if (numTries > 7){
            failConnection();
            return;
        }

        String url = serverAddress + "/piInfo";

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Status", "Got Response! " + response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            String addr = obj.getString("piAddress");
                            String os = obj.getString("os");
                            int volume = Integer.parseInt(obj.getString("volume"));
                            mainScreen(addr, os, volume);
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                params.put("id_token", getAccount().getIdToken());
                return params;
            }
        };
        queue.add(postRequest);
    }

    private GoogleSignInAccount getAccount(){
        return GoogleSignIn.getLastSignedInAccount(this);
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

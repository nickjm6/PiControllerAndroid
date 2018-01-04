package com.example.nickjm6.picontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private int numTries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        makeRequest();
    }

    private void makeRequest(){
        if (numTries > 5){
            failConnection();
            return;
        }
        final RequestQueue queue = Volley.newRequestQueue(this);

        String url = getString(R.string.serverAddress) + "/piInfo";

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
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

    private void failConnection(){
        Intent intent = new Intent(this, failedConnection.class);
        startActivity(intent);
    }

    private void mainScreen(String addr, String os, int volume){
        Intent intent = new Intent(this, SystemCTL.class);
        intent.putExtra("piAddress", addr);
        intent.putExtra("volume", volume);
        intent.putExtra("os", os);
        startActivity(intent);
        finish();
    }
}

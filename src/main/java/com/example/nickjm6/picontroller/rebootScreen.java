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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class rebootScreen extends AppCompatActivity {
    private int numTries = 0;
    private String PiAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot_screen);
        Intent intent = getIntent();
        final String requestURL = intent.getStringExtra("requestURL");
        PiAddress = intent.getStringExtra("piAddress");
        String osName = "";
        if (requestURL.equals("/switchOS")){
            osName =  intent.getStringExtra("osName").toLowerCase().trim();
        }

        String url = PiAddress + requestURL;

        makePostRequest(url, osName);
    }

    private void makeRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        if (numTries > 15){
            failConnection();
            return;
        }

        String url = getString(R.string.serverAddress) + "/piAddress";

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Status", "Got Response! " + response);
                        mainScreen(response.trim());
                        return;
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
        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://" + requestURL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            TimeUnit.SECONDS.sleep(8);
                            makeRequest();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        makeRequest();

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
                if(requestURL.equals(PiAddress + "/switchOS")){
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

    private void mainScreen(String addr){
        Intent intent = new Intent(this, SystemCTL.class);
        intent.putExtra("piAddress", addr);
        startActivity(intent);
        finish();
    }
}

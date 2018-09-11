package com.example.nickjm6.picontroller;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pingRazPi("192.168.0.15");
    }

    private void piSearch(){
        for(int i = 2; i < 255; i++){
            String hostname = "192.168.0." + i;
            getInetAddressByName(hostname);
        }
    }

    private void getInetAddressByName(String name)
    {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>()
        {
            @Override
            protected Void doInBackground(String... params)
            {
                try
                {
                    if(InetAddress.getByName(params[0]).isReachable(300)){
                        pingRazPi(params[0]);
                    }
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        try
        {
            task.execute(name).get();
        }
        catch (InterruptedException e)
        {
        }
        catch (ExecutionException e)
        {
        }

    }

    private void pingRazPi(final String address){
        final RequestQueue queue = Volley.newRequestQueue(this);

        final String url = "http://" + address + "/";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ping response", response);
                        if(response.equals("MyRazPi")){
                            getOS(address);
                            return;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ping response", "not the raspberry pi");
            }
        });
        queue.add(stringRequest);
    }

    private void getOS(final String address){
        final RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://" + address + "/" + "currentOS";

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mainScreen(address, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrorResponse", String.valueOf(error));
                failConnection();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void failConnection(){
        Intent intent = new Intent(this, failedConnection.class);
        startActivity(intent);
    }

    private void mainScreen(String addr, String os){
        Intent intent = new Intent(this, SystemCTL.class);
        intent.putExtra("piAddress", addr);
        intent.putExtra("os", os);
        startActivity(intent);
        finish();
    }
}

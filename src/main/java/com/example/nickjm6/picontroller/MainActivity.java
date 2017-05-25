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

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private int numTries = 0;
    private String PiAddress = "";
    static SharedPreferences settings;
    static SharedPreferences.Editor editor;
    static boolean stop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = this.getPreferences(0);
        try {
            Intent intent = getIntent();
            PiAddress = intent.getStringExtra("piaddress");
            Log.d("Pi Address", PiAddress);
            editor = settings.edit();
            editor.putString("piaddress", PiAddress);
            editor.commit();
        }catch(Exception e){
            Log.e("error", "could not get info from intent");
        }
        PiAddress = settings.getString("piaddress", "0");
        TextView text = (TextView) findViewById(R.id.ipaddr);
        text.setText(PiAddress);
        numTries = 0;
        if(PiAddress.equals("0")) {
            setIP();
        }
        else{
            stop = false;
            makeRequest();
        }


    }

    private void makeRequest(){
        if (stop){
            return;
        }
        if (numTries > 5){
            failConnection();
            return;
        }
        final RequestQueue queue = Volley.newRequestQueue(this);

        String url = PiAddress;

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Status", "Found Server!");
                        mainScreen();
                        return;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrorResponse", String.valueOf(error));
                Log.d("Pi Address", PiAddress);
                try {
                    TimeUnit.SECONDS.sleep(1);
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
        intent.putExtra("piaddress", PiAddress);
        startActivity(intent);
    }

    private void mainScreen(){
        Intent intent = new Intent(this, SystemCTL.class);
        intent.putExtra("piaddress", PiAddress);
        startActivity(intent);
        finish();
    }

    private void setIP(){
        Intent intent = new Intent(this, SetIP.class);
        intent.putExtra("piaddress", PiAddress);
        startActivity(intent);
        finish();
    }

    public void settingClick(View view){
        stop = true;
        setIP();
    }

}

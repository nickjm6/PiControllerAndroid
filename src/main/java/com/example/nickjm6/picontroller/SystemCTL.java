package com.example.nickjm6.picontroller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SystemCTL extends AppCompatActivity {

    private String currentOS = "";
    private String PiAddress = "";
    private String[] OSes = {"Raspbian", "Rasplex", "Kodi", "Retropie"};
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_ctl);
        progressBar = (ProgressBar) findViewById(R.id.volume);
        Intent intent = getIntent();
        setAddr(intent.getStringExtra("piAddress"));
        getVol();
        getCurrentOS();
    }

    public void refresh(View view){
        reloadScreen();
    }

    private void reloadScreen(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void rebootScreen(String requestURL){
        Intent intent = new Intent(this, rebootScreen.class);
        intent.putExtra("requestURL", requestURL);
        intent.putExtra("piAddress", PiAddress);
        startActivity(intent);
    }

    public void reboot(View view){
        rebootScreen("/reboot");
    }

    public void hdmi(View view){
        rebootScreen("/hdmi");
    }

    public void rca(View view){
        rebootScreen("/rca");
    }

    public void volumeup(View view){
        volumeRequest("/volumeup");
    }

    public void volumedown(View view){
        volumeRequest("/volumedown");
    }

    public void getVol(){
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = getString(R.string.serverAddress) + "/getVol";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response", response);
                        int intResponse = Integer.parseInt(response.trim());
                        setVolume(intResponse);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrorResponse", String.valueOf(error));
//                reloadScreen();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void volumeRequest(String upOrDown){
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://" + PiAddress + upOrDown;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response", response);
                        int intResponse = Integer.parseInt(response.trim());
                        progressBar.setProgress(Integer.parseInt(response.trim()));
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrorResponse", String.valueOf(error));
                reloadScreen();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void setVolume(int vol){
        progressBar.setProgress(vol);
    }

    private void setAddr(String val){
        PiAddress = val;
        final TextView mTextView = (TextView) findViewById(R.id.ipAddr);
        mTextView.setText(val);
    }

    private void getCurrentOS(){
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = getString(R.string.serverAddress) + "/currentOS";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        String result = response.trim();
                        Log.d("CurrentOS", result);
                        if(!result.equals(currentOS))
                            setCurrentOS(result);
//                        try {
//                            TimeUnit.SECONDS.sleep(5);
//                            getCurrentOS();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrorResponse", String.valueOf(error));
                reloadScreen();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void setCurrentOS(String newOS){
        currentOS = newOS;
        final TextView mTextView = (TextView) findViewById(R.id.currentOS);
        String capNewOS = newOS.substring(0, 1).toUpperCase() + newOS.substring(1);
        mTextView.setText(capNewOS);
        ImageView img = (ImageView) findViewById(R.id.osLogo);
        ImageView img2 = (ImageView) findViewById(R.id.osLogo2);
        switch (newOS){
            case "rasplex":
                img.setImageResource(R.drawable.rasplex);
                img2.setImageResource(R.drawable.rasplex);
                break;
            case "raspbian":
                img.setImageResource(R.drawable.raspbian);
                img2.setImageResource(R.drawable.raspbian);
                break;
            case "retropie":
                img.setImageResource(R.drawable.retropie);
                img2.setImageResource(R.drawable.retropie);
                break;
            case "kodi":
                img.setImageResource(R.drawable.kodi);
                img2.setImageResource(R.drawable.kodi);
                break;
            default:
                Intent intent = new Intent(SystemCTL.this, MainActivity.class);
                startActivity(intent);
                break;
        }

        ImageView[] osImages = new ImageView[3];
        ImageView osImage1 = (ImageView) findViewById(R.id.OS1);
        ImageView osImage2 = (ImageView) findViewById(R.id.OS2);
        ImageView osImage3 = (ImageView) findViewById(R.id.OS3);
        osImages[0] = osImage1;
        osImages[1] = osImage2;
        osImages[2] = osImage3;

        int i = 0;
        for (final String s: OSes){
            if (!s.equals(capNewOS)){
                if(i < 3) {
                    ImageView currentImage = osImages[i];
                    switch (s){
                        case "Raspbian":
                            currentImage.setImageResource(R.drawable.raspbian);
                            break;
                        case "Rasplex":
                            currentImage.setImageResource(R.drawable.rasplex);
                            break;
                        case "Kodi":
                            currentImage.setImageResource(R.drawable.kodi);
                            break;
                        case "Retropie":
                            currentImage.setImageResource(R.drawable.retropie);
                    }
                    i++;
                    currentImage.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            // Perform action on click
                            Intent intent = new Intent(SystemCTL.this, rebootScreen.class);
                            intent.putExtra("osName", s);
                            intent.putExtra("requestURL", "/switchOS");
                            intent.putExtra("piAddress", PiAddress);
                            startActivity(intent);
                        }
                    });
                }
            }
        }
    }
}

package com.example.nickjm6.picontroller;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.security.auth.callback.Callback;

public class SystemCTL extends AppCompatActivity {

    private String currentOS = "";
    private String PiAddress = "";
    private String[] OSes = {"Raspbian", "Rasplex", "Kodi", "Retropie"};
    private ProgressBar progressBar;
    private Menu barMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_ctl);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        progressBar = (ProgressBar) findViewById(R.id.volume);

        Intent intent = getIntent();
        setAddr(intent.getStringExtra("piAddress"));
        setCurrentOS(intent.getStringExtra("os"));
        progressBar.setProgress(intent.getIntExtra("volume", 0));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        barMenu = menu;
        return true;
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
        rebootScreen("/power/reboot");
    }

    public void poweroff(View view){
        rebootScreen("/power/off");
    }

    public void refresh(View view){
        reloadScreen();
    }

    public void hdmi(View view){
        rebootScreen("/hdmi");
    }

    public void rca(View view){
        rebootScreen("/rca");
    }

    public void volumeup(View view){
        volumeRequest("/volume/up");
    }

    public void volumedown(View view){
        volumeRequest("/volume/down");
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
                        try{
                            JSONObject js = new JSONObject(response);
                            int vol = js.getInt("volume");
                            progressBar.setProgress(vol);
                        }catch(JSONException e) {
                            e.printStackTrace();
                        }
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

    private void setAddr(String val){
        PiAddress = val;
        final TextView mTextView = (TextView) findViewById(R.id.ipAddr);
        mTextView.setText(val);
    }

    private void setCurrentOS(String newOS){
        currentOS = newOS;
        final TextView mTextView = (TextView) findViewById(R.id.currentOS);
        String capNewOS = newOS.substring(0, 1).toUpperCase() + newOS.substring(1);
        mTextView.setText(capNewOS);
        ImageView img = (ImageView) findViewById(R.id.osLogo);
        ImageView img2 = (ImageView) findViewById(R.id.osLogo2);


        setImages();
    }

    private void setOSListener(final String osName, ImageView image){
        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(SystemCTL.this, rebootScreen.class);
                intent.putExtra("osName", osName);
                intent.putExtra("requestURL", "/operatingSystem/switch");
                intent.putExtra("piAddress", PiAddress);
                startActivity(intent);
            }
        });
    }

    private String[] filterOS(){
        String[] res = new String[3];
        int i = 0;
        for(String s: OSes){
            if(!s.toLowerCase().equals(currentOS)){
                if(i < 3){
                    res[i] = s;
                    i++;
                }
            }
        }
        return res;
    }

    private void setImages(){
        ImageView img = (ImageView) findViewById(R.id.osLogo);
        ImageView img2 = (ImageView) findViewById(R.id.osLogo2);

        int drawableID = 0;
        switch (currentOS){
            case "rasplex":
                drawableID = R.drawable.rasplex;
                break;
            case "raspbian":
                drawableID = R.drawable.raspbian;
                break;
            case "retropie":
                drawableID = R.drawable.retropie;
                break;
            case "kodi":
                drawableID = R.drawable.kodi;
                break;
        }
        img.setImageResource(drawableID);
        img2.setImageResource(drawableID);

        ImageView osImage1 = (ImageView) findViewById(R.id.OS1);
        ImageView osImage2 = (ImageView) findViewById(R.id.OS2);
        ImageView osImage3 = (ImageView) findViewById(R.id.OS3);

        ImageView[] images = {osImage1, osImage2, osImage3};
        String[] filtered = filterOS();
        for(int i = 0; i < 3; i++){
            String s = filtered[i];
            switch(s){
                case "Raspbian":
                    images[i].setImageResource(R.drawable.raspbian);
                    break;
                case "Retropie":
                    images[i].setImageResource(R.drawable.retropie);
                    break;
                case "Rasplex":
                    images[i].setImageResource(R.drawable.rasplex);
                    break;
                case "Kodi":
                    images[i].setImageResource(R.drawable.kodi);
                    break;
            }
            setOSListener(s, images[i]);
        }
    }
}

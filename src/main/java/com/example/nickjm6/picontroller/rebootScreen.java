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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

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
        if (requestURL.equals("/operatingSystem/switch")){
            osName =  intent.getStringExtra("osName").toLowerCase().trim();
        }

        makePostRequest(piAddress, requestURL, osName);
    }

    private void makeRequest(){
        if(numTries > 7){
            failConnection();
            return;
        }
        PiHTTPClient.setPiAddress(piAddress);
        PiHTTPClient.get("piInfo", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try{
                    String currentOs = response.getString("currentOS");
                    int volume = response.getInt("volume");
                    Log.d("Os And Volume", currentOs + ", " + volume);
                    mainScreen(piAddress, currentOs, volume);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                numTries++;
                try {
                    Thread.sleep(1000);
                    numTries++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                makeRequest();
            }
        });
    }

    private void makePostRequest(final String address, final String endpoint, final String osName){
        PiHTTPClient.setPiAddress(address);
        JSONObject requestParams = new JSONObject();
        try {
            requestParams.put("osName", osName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Context context = getApplicationContext();
        PiHTTPClient.post(context, endpoint, requestParams, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try {
                    Log.d("Response", response.getString("message"));
                } catch (JSONException e) {
                    Log.e("Response", "An unexpected error occured");
                }
                try {
                    TimeUnit.SECONDS.sleep(12);
                    makeRequest();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("Error.Response", errorResponse.toString());
                finish();
            }
        });
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

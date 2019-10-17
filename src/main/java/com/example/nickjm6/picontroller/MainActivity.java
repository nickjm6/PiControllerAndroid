package com.example.nickjm6.picontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.Header;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {

    NsdHelper nsdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        SharedPreferences settings = getApplicationContext().getSharedPreferences("AddressCache", 0);
//        String previousAddress = settings.getString("piAddress", null);
//        if(previousAddress != null){
//            Log.d("AddressCache", "it's working");
//            getOSandVolume(previousAddress);
//        } else{
//            piSearch(addresses);
//        }
        nsdHelper = new NsdHelper(this);
        nsdHelper.initializeNsd();
        try {
            String piAddress = nsdHelper.getPiAddress();
            Log.d("PiAddress", piAddress);
            getOSandVolume(piAddress);
        } catch (Exception e) {
            Log.e("PiAddress", e.getMessage());
            failConnection();
        }
    }


    private void getOSandVolume(final String address){
        PiHTTPClient.get(address, "osAndVolume", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try{
                    String currentOs = response.getString("currentOS");
                    int volume = response.getInt("volume");
                    Log.d("Os And Volume", currentOs + ", " + volume);
                    mainScreen(address, currentOs, volume);
                }catch(JSONException e){
                    e.printStackTrace();
                    failConnection();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("PI ERROR", String.valueOf(statusCode));
                SharedPreferences settings = getApplicationContext().getSharedPreferences("AddressCache", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear();
                editor.apply();
                failConnection();
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

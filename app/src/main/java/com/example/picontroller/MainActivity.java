package com.example.picontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import cz.msebera.android.httpclient.Header;

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
        PiHTTPClient.get(address, "piInfo", new JsonHttpResponseHandler(){
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

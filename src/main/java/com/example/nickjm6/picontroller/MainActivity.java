package com.example.nickjm6.picontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<String> addresses = new ArrayList<String>();
        for(int i = 2; i < 255; i++){
            addresses.add("192.168.0." + i);
        }
        SharedPreferences settings = getApplicationContext().getSharedPreferences("AddressCache", 0);
        String previousAddress = settings.getString("piAddress", null);
        if(previousAddress != null){
            Log.d("AddressCache", "it's working");
            getOSandVolume(previousAddress);
        } else{
            piSearch(addresses);
        }
    }

    private void piSearch(final ArrayList<String> addresses){
        for(final String address: addresses){
            PiHTTPClient.get(address, "ping", new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Log.d("Found Address", address);
                        if (response.getString("message").equals("MyRazPi")){
                            Log.d("Found Pi Address", address);
                            SharedPreferences settings = getApplicationContext().getSharedPreferences("AddressCache", 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("piAddress", address);
                            editor.apply();
                            getOSandVolume(address);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if(address.equals(addresses.get(addresses.size() - 1))) {
                        Log.e("END", "Reached end of list");
                        failConnection();
                    }
                }
            });
        }
    }

//    public boolean pingPi(final String piAddress) {
//        PiHTTPClient.get(piAddress, "ping", new JsonHttpResponseHandler(){
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
//                try {
//                    if(response.getString("message").equals("MyRazPi")){
//                        getOSandVolume(piAddress);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                Log.e("Pi Response", "Failure: " + throwable.getMessage() + ": " +  piAddress);
//            }
//        });
//    }


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

    public byte[] extractBytes(int ip){
        return new byte[] {
                (byte) (ip >> 24),
                (byte) (ip >> 16),
                (byte) (ip >> 8),
                (byte) (ip)
        };
    }

    public ArrayList<String> getAddresses(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        short networkPrefix = 0;
        byte[] bytes = extractBytes(50374848);
        try {
            InetAddress inetAddress = InetAddress.getByName(formatIP(dhcpInfo.ipAddress));
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                if(address.toString().contains(formatIP(dhcpInfo.ipAddress)))
                    networkPrefix = address.getNetworkPrefixLength();
            }
        } catch (Exception e) {
            Log.e("Network Error", e.getMessage());
        }
        Log.d("Net Prefix", String.valueOf(networkPrefix));
        ArrayList<String> res = new ArrayList<String>();
//        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        DhcpInfo dhcpInfo = wifiMan.getDhcpInfo();
//        String address = formatIP(dhcpInfo.ipAddress);
//        String mask = formatIP(dhcpInfo.netmask);
//        String gateway = formatIP(dhcpInfo.gateway);
//        String serverAddr = formatIP(dhcpInfo.serverAddress);
//        String formattedBaseAddr = formatIP(dhcpInfo.ipAddress & dhcpInfo.gateway);
//        int subnet = gateway.length() - gateway.replace("0", "").length();
//        Log.d("IP", address);
//        Log.d("Gateway", gateway);
//        Log.d("ServerAddress", serverAddr);
//        Log.d("Base Address", formattedBaseAddr);
//        Log.d("Subnet", String.valueOf(subnet));
        return res;
    }

    public String formatIP(int ip){
        return String.format("%d.%d.%d.%d", (ip & 0xff),(ip >> 8 & 0xff),(ip >> 16 & 0xff),(ip >> 24 & 0xff));
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

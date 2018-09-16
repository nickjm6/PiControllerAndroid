package com.example.nickjm6.picontroller;

import android.content.Context;
import android.content.Intent;
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
        ArrayList<String> addresses = getAddresses();
//        piSearch();
        pingPi("192.168.0.15");
    }

    private void piSearch(){
        for(int i = 2; i < 255; i++){
            final String hostname = "192.168.0." + i;
            pingPi(hostname);
        }
    }

    public void pingPi(final String piAddress) {
        PiHTTPClient.get(piAddress, "ping", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try {
                    if(response.getString("message").equals("MyRazPi")){
                        getOSandVolume(piAddress);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("Pi Response", "Failure: " + throwable.getMessage() + ": " +  piAddress);
            }
        });
    }

    private void pingRazPi(final String address){
        final RequestQueue queue = Volley.newRequestQueue(this);

        final String url = "http://" + address + "/ping";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String message;
                        if(address == "192.168.0.15"){
                            Log.d("status", "found it");
                        }
                        try{
                            JSONObject js = new JSONObject(response);
                            message = js.getString("message");
                        }catch(JSONException e){
                            e.printStackTrace();
                            return;
                        }
                        Log.d("ping response", response);
                        if(message.equals("MyRazPi")){
                            getOSandVolume(address);
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

    private void getOSandVolume(final String address){
        final RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://" + address + "/osAndVolume";

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        String currentOS;
                        int volume;
                        try{
                            JSONObject js = new JSONObject(response);
                            currentOS = js.getString("currentOS");
                            volume = js.getInt("volume");

                        }catch(JSONException e){
                            e.printStackTrace();
                            return;
                        }
                        mainScreen(address, currentOS, volume);
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

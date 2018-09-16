package com.example.nickjm6.picontroller;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class PiHTTPClient {
    private static String piAddress;
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String endpoint, JsonHttpResponseHandler responseHandler){
        if(piAddress == null){
            Log.e("Null Pi Address", "Pi address is null!");
            return;
        }
        String fullURL = "http://" + piAddress + "/" + endpoint;
        client.post(fullURL, responseHandler);
    }

    public static void get(String address, String endpoint, AsyncHttpResponseHandler responseHandler){
        String fullURL = "http://" + address + "/" + endpoint;
        client.get(fullURL, responseHandler);
    }

    public static void post(String endpoint, RequestParams params, AsyncHttpResponseHandler responseHandler){
        if(piAddress == null){
            Log.e("Null Pi Address", "Pi address is null!");
            return;
        }
        String fullURL = "http://" + piAddress + "/" + endpoint;
        client.post(fullURL, params, responseHandler);
    }

    public static void setPiAddress(String newAddress){
        piAddress = newAddress;
    }
}

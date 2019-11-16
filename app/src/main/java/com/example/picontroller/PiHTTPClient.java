package com.example.picontroller;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class PiHTTPClient {
    private static String piAddress;
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String endpoint, JsonHttpResponseHandler responseHandler){
        if(piAddress == null){
            Log.e("Null Pi Address", "Pi address is null!");
            return;
        }
        String fullURL = "http://" + piAddress + "/" + endpoint;
        client.get(fullURL, responseHandler);
    }

    public static void get(String address, String endpoint, AsyncHttpResponseHandler responseHandler){
        String fullURL = "http://" + address + "/" + endpoint;
        client.get(fullURL, responseHandler);
    }

    public static void post(Context context, String endpoint, JSONObject params, AsyncHttpResponseHandler responseHandler){
        if(piAddress == null){
            Log.e("Null Pi Address", "Pi address is null!");
            return;
        }
        String fullURL = "http://" + piAddress + "/" + endpoint;
        StringEntity entity = null;
        try {
            entity = new StringEntity(params.toString());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(context, fullURL, entity, "application/json", responseHandler);
    }

    public static void setPiAddress(String newAddress){
        piAddress = newAddress;
    }
}

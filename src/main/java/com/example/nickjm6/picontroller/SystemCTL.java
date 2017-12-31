package com.example.nickjm6.picontroller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SystemCTL extends AppCompatActivity {

    private String currentOS = "";
    private String PiAddress = "";
    private String[] OSes = {"Raspbian", "Rasplex", "Kodi", "Retropie"};
    private ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    private Menu barMenu;
    private String authToken;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_ctl);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        progressBar = (ProgressBar) findViewById(R.id.volume);
        Intent intent = getIntent();
        setAddr(intent.getStringExtra("piAddress"));
        getVol();
        getCurrentOS();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        barMenu = menu;
        updateUI();
        return true;
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
//        rebootScreen("/reboot");
        postRequest();
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

    private void signInRequest(final String token){
        Log.d("status", token);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://myrazpi.com/auth/google";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Sign in Response", response);
                        authToken = response;
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e("Error.Response", error.toString());
                    }
                }
        ) {@Override
        protected Map<String, String> getParams()
        {
            Map<String, String>  params = new HashMap<String, String>();
            params.put("id_token", token);
            return params;
        }
        };
        queue.add(postRequest);
    }

    private void postRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://myrazpi.com/validateLogin";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrorResponse", String.valueOf(error));
            }
        }) {@Override
        protected Map<String, String> getParams()
        {
            Map<String, String>  params = new HashMap<String, String>();
            params.put("id_token", authToken);
            return params;
        }
        };
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signInOut:
                if(isSignedIn())
                    signOut();
                else
                    signIn();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        updateUI();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String idToken = account.getIdToken();
            signInRequest(idToken);

            updateUI();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void signOut(){
        mGoogleSignInClient.signOut();
        updateUI();
    }

    private boolean isSignedIn(){
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateUI(){
        boolean signedIn = isSignedIn();

        Button reboot = (Button) findViewById(R.id.reboot);
        reboot.setEnabled(signedIn);
        reboot.setBackgroundColor(Color.rgb(221,221,221));

        Button rca = (Button) findViewById(R.id.rca);
        rca.setEnabled(signedIn);

        Button hdmi = (Button) findViewById(R.id.hdmi);
        hdmi.setEnabled(signedIn);

        Button vUp = (Button) findViewById(R.id.volumeup);
        vUp.setEnabled(signedIn);

        Button vDown = (Button) findViewById(R.id.volumedown);
        vDown.setEnabled(signedIn);

        ImageView osImage1 = (ImageView) findViewById(R.id.OS1);
        osImage1.setEnabled(signedIn);

        ImageView osImage2 = (ImageView) findViewById(R.id.OS2);
        osImage2.setEnabled(signedIn);

        ImageView osImage3 = (ImageView) findViewById(R.id.OS3);
        osImage3.setEnabled(signedIn);

        if(signedIn){
            reboot.setBackgroundColor(getColor(R.color.reboot));
            rca.setBackgroundColor(getColor(R.color.rca));
            hdmi.setBackgroundColor(getColor(R.color.hdmi));
            vUp.setBackgroundColor(getColor(R.color.myGrey));
            vDown.setBackgroundColor(getColor(R.color.myGrey));
            setImages();
//
        } else{
            reboot.setBackgroundColor(getColor(R.color.disabled));
            rca.setBackgroundColor(getColor(R.color.disabled));
            hdmi.setBackgroundColor(getColor(R.color.disabled));
            vUp.setBackgroundColor(getColor(R.color.disabled));
            vDown.setBackgroundColor(getColor(R.color.disabled));
            osImage1.setImageResource(R.drawable.dont);
            osImage2.setImageResource(R.drawable.dont);
            osImage3.setImageResource(R.drawable.dont);
        }

        try{
            MenuItem signInButton = barMenu.findItem(R.id.signInOut);
            if(signedIn)
                signInButton.setTitle("Sign Out");
            else
                signInButton.setTitle("Sign In");
        } catch (Exception e){
            if(barMenu == null){
                Log.e("Menu Status", "NULL");
            }
            Log.e("Menu alert", "Menu button error");
        }


    }
}

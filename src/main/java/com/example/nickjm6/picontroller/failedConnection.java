package com.example.nickjm6.picontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class failedConnection extends AppCompatActivity {

    private String PiAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failed_connection);

        Intent intent = getIntent();
        PiAddress = intent.getStringExtra("piaddress");
    }

    public void tryAgain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void setIP(View view){
        Intent intent = new Intent(this, SetIP.class);
        intent.putExtra("piaddress", PiAddress);
        startActivity(intent);
    }
}

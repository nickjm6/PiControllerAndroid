package com.example.nickjm6.picontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class SetIP extends AppCompatActivity {

    private String ipAddr;
    private String portNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ip);

        Intent intent = getIntent();
        String[] PiAddress = intent.getStringExtra("piaddress").split("//")[1].split(":");
        ipAddr = PiAddress[0];
        portNo = PiAddress[1];

        EditText ipField = (EditText) findViewById(R.id.ip);
        ipField.setText(ipAddr);
        EditText portField = (EditText) findViewById(R.id.port);
        portField.setText(portNo);
    }

    public void setIP(View view){
        EditText ip = (EditText) findViewById(R.id.ip);
        String ipAddress = ip.getText().toString();
        EditText port = (EditText) findViewById(R.id.port);
        String portNo = port.getText().toString();
        String PiAddress = "http://" + ipAddress + ":" + portNo;
        Intent intent = new Intent(SetIP.this, MainActivity.class);
        intent.putExtra("piaddress", PiAddress);
        startActivity(intent);
        finish();
    }
}

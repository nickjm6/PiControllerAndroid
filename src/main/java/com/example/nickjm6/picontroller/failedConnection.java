package com.example.nickjm6.picontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class failedConnection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failed_connection);
    }

    public void tryAgain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}

package com.example.timecraft2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
    }
    public void register(View v) {
        Intent i = new Intent(this, Register.class);
        startActivity(i);
    }

    public void login(View v) {
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

}
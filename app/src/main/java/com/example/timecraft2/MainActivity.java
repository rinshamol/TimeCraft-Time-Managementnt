package com.example.timecraft2;

import static com.example.timecraft2.AddNewTask.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {
    EditText etToken;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Check if the user is logged in
            if (isLoggedIn()) {

                startActivity(new Intent(this, Home.class));
                finish();
            }else{
                startActivity(new Intent(this, MainPage.class));
                finish();
            }
        }

        private boolean isLoggedIn() {
            // Check if there is a saved user session
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("isLoggedIn", false);
        }

        private void saveLoginState(boolean isLoggedIn) {
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", isLoggedIn);
            editor.apply();
        }

}
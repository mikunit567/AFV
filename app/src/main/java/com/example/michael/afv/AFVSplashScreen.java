package com.example.michael.afv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AFVSplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Thread countdown = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(3000); // amount of second the splash screen will appear on the screen
                    Intent intent = new Intent(AFVSplashScreen.this,AFVFileViewer.class); // from the splash screen it will then transition to the main page
                    startActivity(intent); // start the intent activity
//                    finish();

                } catch (InterruptedException e) {e.printStackTrace(); } }
        };
        countdown.start();
    }
}
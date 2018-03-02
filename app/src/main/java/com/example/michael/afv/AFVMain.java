package com.example.michael.mbook;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.Toast;

public class AFVMain extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afvmain);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton web = findViewById(R.id.fab);
        web.setOnClickListener(view -> {
            Uri uriUrl = Uri.parse("https://www.google.co.uk/");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);

        });

        Button help = findViewById(R.id.HELP);
        help.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(AFVMain.this).create();
            alertDialog.setTitle("Help");
            alertDialog.setMessage("Hello to the MBOOK app where you can read, edit and view your files in different formats. " +
                    "The search button below can be used to use the browser of you choice.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CLOSE",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        });

        Button files = findViewById(R.id.FILES);
        files.setOnClickListener(view -> {
            Intent intent = new Intent(AFVMain.this, AFVFileViewer.class);
            startActivity(intent);
        });


        Button textspeech = findViewById(R.id.teextspeech);
        textspeech.setOnClickListener(view -> {

            Intent intent = new Intent(AFVMain.this, AFVTextToSpeech.class);
            startActivity(intent);

                });


        Button chocoo = findViewById(R.id.choco);
        chocoo.setOnClickListener(view -> {

            Intent intent = new Intent(AFVMain.this, AFVMapsActivity.class);
            startActivity(intent);

        });


        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(view -> {

            Intent intent = new Intent(AFVMain.this, AFVLogin.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Successful Log Out", Toast.LENGTH_SHORT).show();


        });



    }


}


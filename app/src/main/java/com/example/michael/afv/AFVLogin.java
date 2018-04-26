/**
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 **/

package com.example.michael.afv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class AFVLogin extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance(); // instance initialisation of the firebase authentication

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(AFVLogin.this, AFVSplashScreen.class));
            finish(); // once the user authenticated the login will transition to the splash screen
        }

        setContentView(R.layout.activity_afvlogin);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        Button btnSignup = findViewById(R.id.btn_signup);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnReset = findViewById(R.id.btn_reset_password);

        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(v -> startActivity(new Intent(AFVLogin.this, AFVSignup.class)));

        btnReset.setOnClickListener(v -> startActivity(new Intent(AFVLogin.this, AFVPasswordReset.class)));

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString();
            final String password = inputPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                return; // code used to test if something has been inputted within the email text field
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                return; // check if something has been enter in the password text field
            }

            progressBar.setVisibility(View.VISIBLE);

            // the block of code beneath sets the parameters for a successful login if not then it will not authenticate

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(AFVLogin.this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            if (password.length() < 6) {
                                inputPassword.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(AFVLogin.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Intent intent = new Intent(AFVLogin.this, AFVSplashScreen.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        });
    }
}

/* REF https://www.androidhive.info/2016/06/android-getting-started-firebase-simple-login-registration-auth/ ***/
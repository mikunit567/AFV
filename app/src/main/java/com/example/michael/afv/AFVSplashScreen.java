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
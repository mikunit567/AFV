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

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;


public class AFVVideoPlayer extends Activity {
    VideoView vw;
    Uri video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the status bar
        if (Build.VERSION.SDK_INT < 16)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.video_afvplayer);
        vw = (VideoView) findViewById(R.id.videoView);
        vw.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if (Build.VERSION.SDK_INT < 16)
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                else
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        });
        MediaController mc = new MediaController(this);
        mc.setAnchorView(vw);
        mc.setMediaPlayer(vw);
        try {
            video = Uri.parse(getIntent().getStringExtra("file"));
        } catch (Exception e) {
            finish();
        }
        vw.setMediaController(mc);
        vw.setVideoURI(video);
        vw.requestFocus();
        vw.start();
    }

    @Override
    protected void onStop() {
        if (vw != null) vw.stopPlayback();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (vw != null) vw.stopPlayback();
        super.onDestroy();
    }
}





















/*** Reference
 * URL : https://github.com/praharshjain/Vudit
 * Year of Access: 2017/18
 * Name: Praharsh Jain
 */

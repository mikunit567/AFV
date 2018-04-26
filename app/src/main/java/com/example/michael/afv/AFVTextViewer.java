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

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URLEncoder;

public class AFVTextViewer extends AppCompatActivity {
    WebView wv;
    String file_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webviewafv);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        wv = findViewById(R.id.browser);
        wv.setWebViewClient(new MyClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
            }
        });
        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        wv.setScrollbarFadingEnabled(true);
        String file_path = getIntent().getStringExtra("file");
        File file = new File(Uri.parse(file_path).getPath());
        try {
            file_path = URLEncoder.encode(file_path, "UTF-8");
        } catch (Exception e) {
        }
        wv.loadUrl("file:///android_asset/textviewer/index.html?file=" + file_path);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            file_text = text.toString();
            wv.addJavascriptInterface(new WebAppInterface(this, file_text), "Android");
            wv.loadUrl("javascript:showFile('" + file_text + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (wv.canGoBack()) {
                wv.goBack();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    class MyClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    class WebAppInterface {
        Context mContext;
        String text;

        WebAppInterface(Context c, String file_text) {
            mContext = c;
            text = file_text;
        }

        @JavascriptInterface
        public String getText() {
            return text;
        }
    }
}











/*** Reference
 * URL : https://github.com/praharshjain/Vudit
 * Year of Access: 2017/18
 * Name: Praharsh Jain
 */

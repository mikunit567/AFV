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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends Activity {
	private Button btn_save;
	private EditText edit_origurl;
    private TextView tipText;

	private String origurl ;

/***This class adds the URL page to the list creating a directory of save pages ***/


    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle("Enter URL to save");
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.add_activity);

        btn_save = findViewById(R.id.save_btn);
        tipText = findViewById(R.id.tipText);

        edit_origurl = findViewById(R.id.frst_editTxt);
		edit_origurl.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));

		origurl = edit_origurl.getText().toString().trim();
		if (origurl.length() > 0) {
			startSave(origurl);
		}
	}

	public void btn_paste(View view) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if (edit_origurl.getText().length() == 0) {
			edit_origurl.append(clipboard.getText());
		} else {
            edit_origurl.append(System.getProperty("line.separator") + clipboard.getText());
        }
	}

	public void okButtonClick(View view) {
		origurl = edit_origurl.getText().toString().trim();
        if (isValidUrlText(origurl)) {
            String[] urls = origurl.split("[\\r\\n]+");
            for (String url : urls) {
                startSave(url);
			}
        } else {
            Toast.makeText(this, "invalid URL. Make sure you included the 'http://' part, also be wary of symbols used.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidUrlText(String urltext) {
        String[] urls = urltext.split("[\\r\\n]+");
        for (String url : urls) {
            if (url.length() == 0 || (!url.startsWith("http"))) {
                return false;
            }
        }
        return true;
    }


	private void startSave(String url) {
		Intent intent = new Intent(this, SaveService.class);
		intent.putExtra(Intent.EXTRA_TEXT, url);
		startService(intent);
		finish();
	}
}

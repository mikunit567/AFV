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
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ViewActivity extends Activity {
	private Intent incomingIntent;
	private SharedPreferences preferences;
	
	private String title;
	private String fileLocation;
	private String date;
	private WebView webview;
	private WebView.HitTestResult result;
	private boolean invertedRendering;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		incomingIntent = getIntent();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (preferences.getBoolean("dark_mode", false)) {
			setTheme(android.R.style.Theme_Holo);
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				setContentView(R.layout.view_activity);
			}
		});


		title = incomingIntent.getStringExtra(AFVDatabase.TITLE);
		fileLocation = incomingIntent.getStringExtra(AFVDatabase.FILE_LOCATION);
		date = incomingIntent.getStringExtra(AFVDatabase.TIMESTAMP);
		


		setProgressBarIndeterminateVisibility(true);

		webview = findViewById(R.id.webview);
		setupWebView();
		
		invertedRendering = preferences.getBoolean("dark_mode", false);
		webview.loadUrl("file://" + fileLocation);
    }

	@Override
	protected void onResume() {
		super.onResume();
		if (invertedRendering) {
			float[] mNegativeColorArray = { 
				-1.0f, 0, 0, 0, 255, // red
				0, -1.0f, 0, 0, 255, // green
				0, 0, -1.0f, 0, 255, // blue
				0, 0, 0, 1.0f, 0 // alpha
			};
			Paint mPaint = new Paint();
			ColorMatrixColorFilter filterInvert = new ColorMatrixColorFilter(mNegativeColorArray);
			mPaint.setColorFilter(filterInvert);
			webview.setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);
		}
	}
	
	private void setupWebView() {
		String ua = preferences.getString("user_agent", "mobile");
		boolean javaScriptEnabled = preferences.getBoolean("enable_javascript", true);
		
		registerForContextMenu(webview);
		
		webview.getSettings().setUserAgentString(ua);
		webview.getSettings().setLoadWithOverviewMode(true);
		webview.getSettings().setUseWideViewPort(true);
		webview.getSettings().setJavaScriptEnabled(javaScriptEnabled);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setDisplayZoomControls(false);
		webview.getSettings().setAllowFileAccess(true);
		webview.getSettings().setAllowFileAccessFromFileURLs(true);
		webview.getSettings().setDefaultTextEncodingName("UTF-8");
		
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url){
				setProgressBarIndeterminateVisibility(false);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				try {
					//send the user to installed browser instead of opening in the app, as per issue 19.
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				} catch (Exception e) {
					//Activity not found or bad url
					e.printStackTrace();
					return false;
				}
			}
			
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
				if (!url.startsWith("file") && preferences.getBoolean("offline_sandbox_mode", false)) {
					Log.w("ViewActivity", "Request blocked: " + url);
					return new WebResourceResponse(null, null, null);
				} else {
					return null;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.ic_action_settings:
				Intent settings = new Intent(getApplicationContext(), Preferences.class);
				startActivityForResult(settings, 1);
				return true;
				
			case R.id.action_save_page_properties:
				showPropertiesDialog();
				return true;
				
			case R.id.action_open_in_external:
				Intent incomingIntent = getIntent();

				Uri uri = Uri.parse(incomingIntent.getStringExtra(AFVDatabase.ORIGINAL_URL));
				Intent startBrowserIntent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(startBrowserIntent);

				return true;

			case R.id.action_open_file_in_external:
				Intent newIntent = new Intent(Intent.ACTION_VIEW);
				newIntent.setDataAndType(Uri.fromFile(new File(fileLocation)), "text/html");
				newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					startActivity(newIntent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(this, "No installed app can open HTML files", Toast.LENGTH_LONG).show();
				}
				return true;

			case R.id.action_delete:

				AlertDialog.Builder build;
				build = new AlertDialog.Builder(ViewActivity.this);
				build.setTitle("Delete ?");
				build.setMessage(title);
				build.setPositiveButton("Delete",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							SQLiteDatabase dataBase = new AFVDatabase(ViewActivity.this).getWritableDatabase();
							Intent incomingIntent2 = getIntent();

							dataBase.delete(AFVDatabase.TABLE_NAME, AFVDatabase.ID + "=" + incomingIntent2.getStringExtra(AFVDatabase.ID), null);

							String fileLocation = incomingIntent2.getStringExtra(AFVDatabase.FILE_LOCATION);
							DirectoryHelper.deleteDirectory(new File(fileLocation).getParentFile());

							Toast.makeText(ViewActivity.this, "Saved page deleted", Toast.LENGTH_LONG).show();

							finish();
						}
					});

				build.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
				AlertDialog alert = build.create();
				alert.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void showPropertiesDialog() {
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle("Details of saved page");
		View layout = getLayoutInflater().inflate(R.layout.properties_dialog, null);
		build.setView(layout);
		TextView t = layout.findViewById(R.id.properties_dialog_text_title);
		t.setText("Title: \r\n" + title);
		t = layout.findViewById(R.id.properties_dialog_text_file_location);
		t.setText("File location: \r\n" + fileLocation);
		t = layout.findViewById(R.id.properties_dialog_text_date);
		t.setText("Date & Time saved: \r\n" + date);
		t = layout.findViewById(R.id.properties_dialog_text_orig_url);
		t.setText("Saved from: \r\n" + incomingIntent.getStringExtra(AFVDatabase.ORIGINAL_URL));
		build.setPositiveButton("Close",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

				}
			});
		build.setNeutralButton("Copy file location to clipboard", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
					ClipData clip = ClipData.newPlainText(webview.getTitle(), fileLocation);
					clipboard.setPrimaryClip(clip);
					Toast.makeText(ViewActivity.this, "File location copied to clipboard", Toast.LENGTH_SHORT).show();
					
				}
		});
		AlertDialog alert = build.create();
		alert.show();
	}


	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		result = webview.getHitTestResult();

		if (result.getType() == WebView.HitTestResult.ANCHOR_TYPE || result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

			menu.setHeaderTitle(result.getExtra());
			menu.add(3, 3, 3, "Save Link");
			menu.add(4, 4, 4, "Share Link");
			menu.add(6, 6, 6, "Copy Link to clipboard");
			menu.add(5, 5, 5, "Open Link");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == 5) {
			Uri uri = Uri.parse(result.getExtra());
			Intent startBrowserIntent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(startBrowserIntent);
		} else if (item.getItemId() == 4) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_TITLE, webview.getTitle());
			i.putExtra(Intent.EXTRA_TEXT, result.getExtra());
			startActivity(Intent.createChooser(i, "Share Link via"));

		} else if (item.getItemId() == 3) {
			Intent intent = new Intent(this, SaveService.class);
			intent.putExtra(Intent.EXTRA_TEXT, result.getExtra());
			startService(intent);

		} else if (item.getItemId() == 6) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
			ClipData clip = ClipData.newPlainText(webview.getTitle(), result.getExtra());
			clipboard.setPrimaryClip(clip);
			Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();

		}
		return super.onContextItemSelected(item);

	}

}










/*** Reference
 * URL : https://github.com/JonasCz/save-for-offline/tree/master/app/src/main/java/jonas/tool/saveForOffline
 * Year of Access: 2017/18
 * Name: JonasCz
 */

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

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SaveService extends Service {

	private final String TAG = "SaveService";

	private ThreadPoolExecutor executor;
	private SharedPreferences sharedPreferences;
	private PageSaver pageSaver;
	private NotificationTools notificationTools;

	@Override
	public void onCreate() {
		executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SaveService.this);
		pageSaver = new PageSaver(new PageSaveEventCallback());
		notificationTools = new NotificationTools(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getBooleanExtra("USER_CANCELLED", false) || intent.getBooleanExtra("USER_CANCELLED_ALL", false)) {
			if (intent.getBooleanExtra("USER_CANCELLED_ALL", false)) {
				executor.getQueue().clear();
			}
			Log.w(TAG, "Cancelled");
			new Thread(() -> pageSaver.cancel()).start();

			return START_NOT_STICKY;
		}

		String pageUrl = intent.getStringExtra(Intent.EXTRA_TEXT);

		if (pageUrl != null && pageUrl.startsWith("http")) {
			executor.submit(new PageSaveTask(pageUrl));
		} else {
			if (pageUrl == null) {
				notificationTools.notifyFailure("URL null, this is probably a bug", null);
			} else {
				notificationTools.notifyFailure("URL not valid: " + pageUrl, null);
			}
		}

		return START_NOT_STICKY;
	}

	private class PageSaveTask implements Runnable {
		private final String pageUrl;
		private String destinationDirectory;

		public PageSaveTask(String pageUrl) {
			this.pageUrl = pageUrl;
			this.destinationDirectory = DirectoryHelper.getDestinationDirectory(sharedPreferences);
		}

		@Override
		public void run() {
			try {
				pageSaver.resetState();

				notificationTools.notifySaveStarted(executor.getQueue().size());

				pageSaver.getOptions().setUserAgent(sharedPreferences.getString("user_agent", getResources().getStringArray(R.array.entries_list_preference)[1]));

				boolean success = pageSaver.getPage(pageUrl, destinationDirectory, "index.html");

				if (pageSaver.isCancelled() || !success) {
					DirectoryHelper.deleteDirectory(new File(destinationDirectory));
					if (pageSaver.isCancelled()) {
						Log.e("SaveService", "Stopping Service, (Cancelled). Deleting files in: " + destinationDirectory + ", from: " + pageUrl);
						notificationTools.cancelAll();
						stopService();
					} else if (!success) {
						Log.e("SaveService", "Failed. Deleting files in: " + destinationDirectory + ", from: " + pageUrl);
					}
					return;
				}

				notificationTools.updateText(null, "Finishing...", executor.getQueue().size());

				File oldSavedPageDirectory = new File(destinationDirectory);
				File newSavedPageDirectory = new File(getNewDirectoryPath(pageSaver.getPageTitle(), oldSavedPageDirectory.getPath()));
				oldSavedPageDirectory.renameTo(newSavedPageDirectory);

				new AFVDatabase(SaveService.this).addToDatabase(newSavedPageDirectory.getPath() + File.separator, pageSaver.getPageTitle(), pageUrl);

				if (sharedPreferences.getBoolean("generate_saved_page_thumbnails", true)) {
					Intent i = new Intent(SaveService.this, ScreenshotService.class);
					i.putExtra(AFVDatabase.FILE_LOCATION, "file://" + newSavedPageDirectory.getPath() + File.separator + "index.html");
					i.putExtra(AFVDatabase.ORIGINAL_URL, pageUrl);
					i.putExtra(AFVDatabase.THUMBNAIL, newSavedPageDirectory + File.separator + "saveForOffline_thumbnail.png");
					startService(i);
				}

				stopService();

				notificationTools.notifyFinished(pageSaver.getPageTitle(), newSavedPageDirectory.getPath());
			} catch (Exception e) {
				Toast.makeText(SaveService.this, "SaveService Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}

		private String getNewDirectoryPath(String title, String oldDirectoryPath) {
			String returnString = title.replaceAll("[^a-zA-Z0-9-_\\.]", "_") + DirectoryHelper.createUniqueFilename(); //TODO: Fix this to support non A-Z & 0-9 characters

			File f = new File(oldDirectoryPath);
			return f.getParentFile().getAbsolutePath() + File.separator  + returnString + File.separator;
		}
	}

	private class PageSaveEventCallback implements EventCallback {

		@Override
		public void onFatalError(final Throwable e, String pageUrl) {
			Log.e("PageSaverService", e.getMessage(), e);
			stopService();

			notificationTools.notifyFailure(e.getMessage(), pageUrl);
		}

		@Override
		public void onProgressChanged(final int progress, final int maxProgress, final boolean indeterminate) {
			notificationTools.updateProgress(progress, maxProgress, indeterminate, executor.getQueue().size());
		}

		@Override
		public void onProgressMessage(final String message) {
			notificationTools.updateText(null, message, executor.getQueue().size());
		}

		@Override
		public void onPageTitleAvailable(String pageTitle) {
			notificationTools.updateText(pageTitle, null, executor.getQueue().size());
		}

		@Override
		public void onLogMessage(final String message) {
			Log.d("PageSaverService", message);
		}

		@Override
		public void onError(final Throwable e) {
			Log.e("PageSaverService", e.getMessage(), e);
		}

		@Override
		public void onError(String errorMessage) {
			Log.e(TAG, errorMessage);
		}
	}

	private void stopService() {
		if (executor.getQueue().isEmpty()) {
			stopSelf();
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service destroyed");
	}

	@Override
	public IBinder onBind(Intent i) {
		return null;
	}	
}













/*** Reference
 * URL : https://github.com/JonasCz/save-for-offline/tree/master/app/src/main/java/jonas/tool/saveForOffline
 * Year of Access: 2017/18
 * Name: JonasCz
 */

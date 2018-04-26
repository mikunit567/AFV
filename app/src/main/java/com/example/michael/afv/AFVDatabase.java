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


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class AFVDatabase extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME="SavedPagesMeta.db";
	public static final String TABLE_NAME="main";
	public static final String TITLE="title";
	public static final String FILE_LOCATION="file_location";
	public static final String THUMBNAIL="thumbnail";
	public static final String ORIGINAL_URL="origurl";
	public static final String ID="_id";
	public static final String TIMESTAMP="timestamp";
	public static final String SAVED_PAGE_BASE_DIRECTORY="tags";

	/**creates metadata for the database including file location and all the elements listed directly above **/

	
	public AFVDatabase(Context context) {
		super(context, DATABASE_NAME, null, 4);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+" ("
		+ID+" INTEGER PRIMARY KEY, "
		+TITLE+" TEXT, "
		+FILE_LOCATION+" TEXT, "
		+THUMBNAIL+" TEXT, "
		+ORIGINAL_URL+" TEXT, "
		+SAVED_PAGE_BASE_DIRECTORY+" TEXT, "
		+TIMESTAMP+" TEXT DEFAULT CURRENT_TIMESTAMP)";
		
		db.execSQL(CREATE_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		onCreate(db);

	}
	
	public void addToDatabase(String destinationDirectory, String pageTitle, String originalUrl) {

		SQLiteDatabase dataBase = getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(AFVDatabase.FILE_LOCATION, destinationDirectory + "index.html");
		values.put(AFVDatabase.SAVED_PAGE_BASE_DIRECTORY, destinationDirectory);
		values.put(AFVDatabase.TITLE, pageTitle);
		values.put(AFVDatabase.THUMBNAIL, destinationDirectory + "saveForOffline_thumbnail.png");
		values.put(AFVDatabase.ORIGINAL_URL, originalUrl);

		dataBase.insert(AFVDatabase.TABLE_NAME, null, values);

		dataBase.close();
	}

}



















/*** Reference
 * URL : https://github.com/JonasCz/save-for-offline/tree/master/app/src/main/java/jonas/tool/saveForOffline
 * Year of Access: 2017/18
 * Name: JonasCz
 */

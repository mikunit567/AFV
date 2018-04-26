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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class DisplayAdapter extends BaseAdapter {
	
	public enum SortOrder {
		NEWEST_FIRST, OLDEST_FIRST, ALPHABETICAL;
		
		public static int toInt (SortOrder o) {
			switch (o) {
				case OLDEST_FIRST:
					return 1;
				case ALPHABETICAL:
					return 2;
				default:
					return 0;
			}
		}
		
		public static SortOrder fromInt (int i) {
			switch (i) {
				case 1:
					return OLDEST_FIRST;
				case 2:
					return ALPHABETICAL;
				default:
					return NEWEST_FIRST;
			}
		}
	}
	
	public enum Layout {
		DEFAULT, GRID, DETAILS_THUMBNAILS, SMALL_TEXT_ONLY, SMALL_ICON;
		
		private static Layout currentLayout = DEFAULT;
		
		public static void setCurrentLayout (Layout l) {
			currentLayout = l;
		}
		
		public static Layout getCurrentLayout () {
			return currentLayout;
		}
		
		public static Layout fromInt (int i) {
			switch (i) {
				case 2:
					return GRID;
				case 4:
					return DETAILS_THUMBNAILS;
				case 5:
					return SMALL_TEXT_ONLY;
				case 6:
					return SMALL_ICON;
				default:
					return DEFAULT;
			}
		}
		
		public static boolean hasDate (Layout l) {
			return l == Layout.DETAILS_THUMBNAILS || l == Layout.SMALL_TEXT_ONLY || l == Layout.DEFAULT;
		}
	}

	private Context mContext;
	private SQLiteDatabase dataBase;
	private FuzzyDateFormatter fuzzyFormatter;

	private String searchQuery = "";
	private String sqlStatement;

	private boolean darkMode;

	public List<Integer> selectedViewsPositions = new ArrayList<Integer>();

	private Bitmap placeHolder;

	public Cursor dbCursor;

	public void refreshData(String searchQuery, SortOrder order, boolean dataSetChanged) {
		switch (order) {
			case OLDEST_FIRST:
				sqlStatement = "SELECT * FROM " + AFVDatabase.TABLE_NAME + " WHERE " + AFVDatabase.TITLE + " LIKE'%" + searchQuery + "%' ORDER BY " + AFVDatabase.ID + " ASC";
				break;
			case ALPHABETICAL:
				sqlStatement = "SELECT * FROM " + AFVDatabase.TABLE_NAME + " WHERE " + AFVDatabase.TITLE + " LIKE'%" + searchQuery + "%' ORDER BY " + AFVDatabase.TITLE + " ASC";
				break;
			default: //newest first
				sqlStatement = "SELECT * FROM " + AFVDatabase.TABLE_NAME + " WHERE " + AFVDatabase.TITLE + " LIKE'%" + searchQuery + "%' ORDER BY " + AFVDatabase.ID + " DESC";
				break;
		}
		
		dbCursor = dataBase.rawQuery(sqlStatement, null);
		dbCursor.moveToFirst();
		if (dataSetChanged) notifyDataSetChanged();
	}

	public DisplayAdapter(Context c) {
		this.mContext = c;

		dataBase = new AFVDatabase(c).getReadableDatabase();
		fuzzyFormatter = new FuzzyDateFormatter(Calendar.getInstance());

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

		Layout.setCurrentLayout(Layout.fromInt(Integer.parseInt(sharedPref.getString("layout" , "1"))));
		darkMode = sharedPref.getBoolean("dark_mode", false);

		refreshData(null, SortOrder.NEWEST_FIRST, false);

		placeHolder = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_website_large);
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	@Override
	public boolean isEmpty() {
        return dbCursor.getCount() == 0;
	}

	public int getCount() {
		return dbCursor.getCount();
	}

	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (dbCursor.getCount() != 0) {
			return Long.valueOf(dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.ID)));
		} else return 0;
	}

	public String getPropertiesByPosition(int position, String type) {
		dbCursor.moveToPosition(position);
		return dbCursor.getString(dbCursor.getColumnIndex(type));
	}
	
	private View inflateView (View convertView, Holder mHolder) {
		LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		switch (Layout.getCurrentLayout()) {
			case GRID: 
				convertView = layoutInflater.inflate(R.layout.listcell_grid, null); break;
			case DETAILS_THUMBNAILS: 
				convertView = layoutInflater.inflate(R.layout.listcell_list_details, null); break;
			case SMALL_TEXT_ONLY: 
				convertView = layoutInflater.inflate(R.layout.listcell_list_details_small, null); break;
			case SMALL_ICON: 
				convertView = layoutInflater.inflate(R.layout.listcell_list_details_small_icon_only, null); break;
			default: 
				convertView = layoutInflater.inflate(R.layout.listcell_default, null);
		}	
		if (darkMode) {
			convertView.setBackgroundColor(Color.BLACK);
			if (Layout.hasDate(Layout.getCurrentLayout())) {
				mHolder.txt_date = (TextView) convertView.findViewById(R.id.txt_date);
				mHolder.txt_date.setTextColor(Color.WHITE);
			}
			mHolder.txt_id = (TextView) convertView.findViewById(R.id.txt_id);
			mHolder.txt_filelocation = (TextView) convertView.findViewById(R.id.txt_fileLocation);
			mHolder.txt_orig_url = (TextView) convertView.findViewById(R.id.txt_orig_url);
			mHolder.txt_orig_url.setTextColor(Color.WHITE);
			mHolder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
			mHolder.txt_title.setTextColor(Color.WHITE);
		} else {
			if (Layout.hasDate(Layout.getCurrentLayout())) {
				mHolder.txt_date = (TextView) convertView.findViewById(R.id.txt_date);
			}
			mHolder.txt_id = (TextView) convertView.findViewById(R.id.txt_id);
			mHolder.txt_filelocation = (TextView) convertView.findViewById(R.id.txt_fileLocation);
			mHolder.txt_orig_url = (TextView) convertView.findViewById(R.id.txt_orig_url);
			mHolder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
		}

		if (Layout.getCurrentLayout() != Layout.SMALL_TEXT_ONLY) {
			mHolder.listimage = (ImageView) convertView.findViewById(R.id.listimage);
		}
		convertView.setTag(mHolder);
		
		return convertView;
	}
	
	private void setListImage (ImageView imageView) {
		if (Layout.getCurrentLayout() == Layout.SMALL_TEXT_ONLY) return;
		switch ((String) imageView.getTag()) {
			case "show:icon":
				File icon = new File(new File(dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.THUMBNAIL))).getParent(), "saveForOffline_icon.png");
				Picasso.with(mContext).load(icon).error(R.drawable.icon_website_large).into(imageView);
			break;
			case "show:thumbnail":
				File image = new File(dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.THUMBNAIL)));
				Picasso.with(mContext).load(image).placeholder(R.drawable.placeholder).into(imageView);
			break;
			default:
				Log.e("displayAdapter", "Bug: image / icon not set due to imageView.getTag() returning bad value:" + imageView.getTag());
		}
	}



	public View getView(int position, View convertView, ViewGroup parent) {
		dbCursor.moveToPosition(position);

		Holder mHolder = null;
		
		if (convertView == null) {
			mHolder = new Holder();
			convertView = inflateView(convertView, mHolder);
		} else {
			mHolder = (Holder) convertView.getTag();
		}

		if (selectedViewsPositions.contains(position)) {
			convertView.setBackgroundColor(Color.parseColor("#FFC107"));
		} else {
			if (darkMode) {
				convertView.setBackgroundColor(Color.BLACK);
			} else {
				convertView.setBackgroundColor(Color.parseColor("#E2E2E2"));
			}
		}

		if (Layout.hasDate(Layout.getCurrentLayout()) && "show:date".equals(mHolder.txt_date.getTag())) {
			try {
				mHolder.txt_date.setText("Saved " + fuzzyFormatter.getFuzzy(dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.TIMESTAMP))));
			} catch (ParseException e) {
				Log.e("displayAdapter", "attempted to parse date '" + dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.TIMESTAMP)) + "' for display, with format yyyy-MM-dd HH:mm:ss, which resulted in a ParseException");
				mHolder.txt_date.setText("Date unavailable");
			}
		}
		mHolder.txt_id.setText(dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.ID)));
		mHolder.txt_filelocation.setText(dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.FILE_LOCATION)));
		mHolder.txt_orig_url.setText(dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.ORIGINAL_URL)));
		mHolder.txt_title.setText(dbCursor.getString(dbCursor.getColumnIndex(AFVDatabase.TITLE)));
		
		setListImage(mHolder.listimage);

		return convertView;
	}

	static class Holder {
		TextView txt_id;
		TextView txt_filelocation;
		TextView txt_orig_url;
		TextView txt_title;
		TextView txt_date;
		ImageView listimage;
		Bitmap mBitmap;
	}
}














/*** Reference
 * URL : https://github.com/JonasCz/save-for-offline/tree/master/app/src/main/java/jonas/tool/saveForOffline
 * Year of Access: 2017/18
 * Name: Jonas Czech
 */

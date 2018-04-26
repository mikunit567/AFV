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

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private String list_appearance;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if (!preferences.getString("layout" , "1").equals(list_appearance)) {
			setResult(RESULT_FIRST_USER);
		} else if (key.equals("dark_mode")) {
			setResult(RESULT_FIRST_USER);
		} else { setResult(RESULT_OK);}
		disableEnablePreferences();
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setSubtitle("Preferences");
		
        addPreferencesFromResource(R.xml.preferences);
		
		list_appearance = getPreferenceScreen().getSharedPreferences().getString("layout" , "1");
		disableEnablePreferences();
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
	
	private void disableEnablePreferences () {
		//this can probably be done through Preference dependencies, too lazy to figure out how..
		boolean useCustomStorageDirEnabled = getPreferenceScreen().getSharedPreferences().getBoolean("is_custom_storage_dir", true);
		if (useCustomStorageDirEnabled) {
			getPreferenceScreen().findPreference("custom_storage_dir").setEnabled(true);
		} else {
			getPreferenceScreen().findPreference("custom_storage_dir").setEnabled(false);
		}
	}

}

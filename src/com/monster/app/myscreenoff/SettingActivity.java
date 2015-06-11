package com.monster.app.myscreenoff;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.monster.app.util.Utility;

public class SettingActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	DevicePolicyManager mDPM;
	ComponentName mDeviceAdminSample;

	OnClickListener clickAdminActive = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_description));
			startActivity(intent);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		addPreferencesFromResource(R.xml.setting_preference);

		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdminSample = new ComponentName(SettingActivity.this, DevicdAdminReceiver.class);

		((Preference) findPreference(getString(R.string.key_setting_start_popwindow))).setOnPreferenceClickListener(this);
		((CheckBoxPreference) findPreference(getString(R.string.key_setting_return_home))).setOnPreferenceChangeListener(this);
		((CheckBoxPreference) findPreference(getString(R.string.key_setting_boot_popwindow))).setOnPreferenceChangeListener(this);
		((ListPreference) findPreference(getString(R.string.key_setting_icon_size))).setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equalsIgnoreCase(getString(R.string.key_setting_start_popwindow))) {
			if (!mDPM.isAdminActive(mDeviceAdminSample)) {
				Utility.ShowCustomizeDialog(this, getString(R.string.app_name), getString(R.string.app_introduction), "OK", null, clickAdminActive, null, null).show();
			} else {
				if (!Utility.isServiceRunning(this, IconService.class)) {
					startService(new Intent(this, IconService.class));
				}
			}
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equalsIgnoreCase(getString(R.string.key_setting_boot_popwindow))) {
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_setting_full_version), false)) {
				if (!mDPM.isAdminActive(mDeviceAdminSample)) {
					Utility.ShowCustomizeDialog(this, getString(R.string.app_name), getString(R.string.app_introduction), "OK", null, clickAdminActive, null, null).show();
				} else {
					return true;
				}
			} else {
				Utility.ShowCustomizeDialog(this, getString(R.string.full_version_promote_title), getString(R.string.full_version_promote_hint), getString(R.string.full_version_buy),
						getString(R.string.option_cancel), new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						}, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						}, null).show();
			}
		} else if (preference.getKey().equalsIgnoreCase(getString(R.string.key_setting_return_home))) {
			return true;
		} else if (preference.getKey().equalsIgnoreCase(getString(R.string.key_setting_icon_size))) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			Toast.makeText(this, String.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_setting_return_home), true)), Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
package com.monster.app.myscreenoff;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.monster.app.util.IabHelper;
import com.monster.app.util.IabHelper.OnIabPurchaseFinishedListener;
import com.monster.app.util.IabHelper.QueryInventoryFinishedListener;
import com.monster.app.util.IabResult;
import com.monster.app.util.Inventory;
import com.monster.app.util.Purchase;
import com.monster.app.util.Utility;

public class SettingActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	DevicePolicyManager mDPM;
	ComponentName mDeviceAdminSample;

	final static int BuyFullVersionRequestCode = 721010;
	final static String BuyFullVersionCheckCode = "MJSIDb7hU3jAJ+29YcvQ6gA9/2wNbKiua";
	final static String IABKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3JWOe2s4CFCHCAHr7UHQM12PoH6VJ81FLmsCYpHiCfoguRTMJSIDb7hU3jAJ+29YcvQ6gA9/2wNbKiua32v7AxRyVVksCrs8CWpNHRmMkITaZOXTDq9/w44UGIzb71p9lKNPmaPI3iTlrc7svZpYIKRySuujMdMRXMPal2EUAEzHCg+RH/xBJJJYQ3gGi9bQZhcQhIb3iay6h2LTnzX/6Kv5FiESI76ltax+ox+PHfjlo1lXI1AUYFHH7zLWRGAq2sJi1iAsXOHtm7jaeZAec4OzylEccMelG1ld3sY2zZLDTk1El8Yhg4RLuP0NcojbcR0zkiVVZYcOfEL9lUh9PwIDAQAB";
	final static String FullVersionID = "screenoff.full.version";
	IabHelper mHelper;

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

		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdminSample = new ComponentName(SettingActivity.this, DevicdAdminReceiver.class);
		hSetupDefaultData.sendEmptyMessage(0);
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_setting_full_version), false)) {
			mHelper = new IabHelper(this, IABKey);
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				@Override
				public void onIabSetupFinished(IabResult result) {
					if (result.isSuccess()) {
						mHelper.queryInventoryAsync(new QueryInventoryFinishedListener() {
							@Override
							public void onQueryInventoryFinished(IabResult result, Inventory inv) {
								if ((result.isSuccess()) && (inv.hasPurchase(FullVersionID))) {
									PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit().putBoolean(getString(R.string.key_setting_full_version), true).commit();
									Utility.ToastUiThread(SettingActivity.this, getString(R.string.iabhelper_fullversion), Toast.LENGTH_LONG);
									releaseIabHelper();
									hSetupDefaultData.sendEmptyMessage(0);
								}
							}
						});
					} else {
						Utility.ToastUiThread(SettingActivity.this, getString(R.string.iabhelper_failed), Toast.LENGTH_SHORT);
						releaseIabHelper();
					}
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Utility.NOTIFICATION_ID);
	};

	Handler hSetupDefaultData = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			((Preference) findPreference(getString(R.string.key_setting_start_popwindow))).setOnPreferenceClickListener(SettingActivity.this);
			((CheckBoxPreference) findPreference(getString(R.string.key_setting_return_home))).setOnPreferenceChangeListener(SettingActivity.this);
			((CheckBoxPreference) findPreference(getString(R.string.key_setting_boot_popwindow))).setOnPreferenceChangeListener(SettingActivity.this);
			((ListPreference) findPreference(getString(R.string.key_setting_icon_size))).setOnPreferenceChangeListener(SettingActivity.this);

			Preference pref = findPreference(getString(R.string.key_setting_support_me));
			if (pref != null) {
				if (PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).getBoolean(getString(R.string.key_setting_full_version), false)) {
					((PreferenceScreen) findPreference(getString(R.string.key_setting_group))).removePreference(pref);
				} else {
					pref.setOnPreferenceClickListener(SettingActivity.this);
				}
			}
		}
	};

	private void releaseIabHelper() {
		if (mHelper != null) {
			mHelper.dispose();
		}
		mHelper = null;
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
		} else if (preference.getKey().equalsIgnoreCase(getString(R.string.key_setting_support_me))) {
			buyFullVersion();
		}
		return false;
	}

	void buyFullVersion() {
		releaseIabHelper();
		mHelper = new IabHelper(this, IABKey);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (result.isSuccess()) {
					mHelper.launchPurchaseFlow(SettingActivity.this, FullVersionID, BuyFullVersionRequestCode, new OnIabPurchaseFinishedListener() {
						@Override
						public void onIabPurchaseFinished(IabResult result, Purchase info) {
							if ((result.isSuccess()) && (info.getSku().equals(FullVersionID))) {
								PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit().putBoolean(getString(R.string.key_setting_full_version), true).commit();
								Utility.ToastUiThread(SettingActivity.this, getString(R.string.iabhelper_fullversion), Toast.LENGTH_SHORT);
							} else {
								PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit().putBoolean(getString(R.string.key_setting_full_version), false).commit();
								Utility.ToastUiThread(SettingActivity.this, getString(R.string.iabhelper_failed), Toast.LENGTH_SHORT);
							}
							releaseIabHelper();
							hSetupDefaultData.sendEmptyMessage(0);
						}
					}, BuyFullVersionCheckCode);
				} else {
					Utility.ToastUiThread(SettingActivity.this, getString(R.string.iabhelper_failed), Toast.LENGTH_SHORT);
					releaseIabHelper();
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseIabHelper();
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
								buyFullVersion();
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
			if (Utility.isServiceRunning(this, IconService.class)) {
				stopService(new Intent(this, IconService.class));
				startService(new Intent(this, IconService.class));
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit().putBoolean(getString(R.string.key_setting_full_version), false).commit();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit().putBoolean(getString(R.string.key_setting_full_version), true).commit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
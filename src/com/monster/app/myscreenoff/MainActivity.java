/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monster.app.myscreenoff;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.monster.app.util.Utility;

public class MainActivity extends Activity {

	DevicePolicyManager mDPM;
	ComponentName mDeviceAdminSample;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdminSample = new ComponentName(MainActivity.this, DevicdAdminReceiver.class);

		if (!mDPM.isAdminActive(mDeviceAdminSample)) {
			Utility.ShowCustomizeDialog(this, getString(R.string.app_name), getString(R.string.app_introduction), "OK", null, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
					intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_description));
					startActivityForResult(intent, Utility.RESULT_ENABLE);
				}
			}, null, null).show();
		} else {
			lockScreenNow();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Utility.RESULT_ENABLE:
			if (resultCode == Activity.RESULT_OK) {
				Log.i("DeviceAdminSample", "Admin enabled!");
				lockScreenNow();
			} else {
				Log.i("DeviceAdminSample", "Admin enable FAILED!");
				startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
				this.finish();
			}
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void lockScreenNow() {
		mDPM.lockNow();
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_setting_return_home), true)) {
			startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
		}
		this.finish();
	}

}
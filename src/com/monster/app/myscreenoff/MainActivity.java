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
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends Activity {
	static final int RESULT_ENABLE = 1;

	DevicePolicyManager mDPM;
	ActivityManager mAM;
	ComponentName mDeviceAdminSample;

	Button mEnableButton;
	Button mDisableButton;
	Button mForceLockButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mAM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mDeviceAdminSample = new ComponentName(MainActivity.this, DevicdAdminReceiver.class);

		if (!mDPM.isAdminActive(mDeviceAdminSample)) {
			ShowCustomizeDialog(this, getString(R.string.app_name), getString(R.string.app_introduction), "OK", null, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
					intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_description));
					startActivityForResult(intent, RESULT_ENABLE);
				}
			}, null, null).show();
		} else {
			startService(new Intent(getApplicationContext(), IconService.class));
//			 lockScreenNow();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RESULT_ENABLE:
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
		startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
		this.finish();
	}

	// private OnClickListener mDisableListener = new OnClickListener() {
	// public void onClick(View v) {
	// mDPM.removeActiveAdmin(mDeviceAdminSample);
	// updateButtonStates();
	// }
	// };
	//
	// private OnClickListener mForceLockListener = new OnClickListener() {
	// public void onClick(View v) {
	// if (mAM.isUserAMonkey()) {
	// // Don't trust monkeys to do the right thing!
	// AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	// builder.setMessage("You can't lock my screen because you are a monkey!");
	// builder.setPositiveButton("I admit defeat", null);
	// builder.show();
	// return;
	// }
	// boolean active = mDPM.isAdminActive(mDeviceAdminSample);
	// if (active) {
	// mDPM.lockNow();
	// }
	// }
	// };

	public static AlertDialog ShowCustomizeDialog(Context ctx, String title, String message, String positiveMessage, String negativeMessage, DialogInterface.OnClickListener onPositiveListener,
			DialogInterface.OnClickListener onNegativeListener, DialogInterface.OnCancelListener onCancelListener) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);

		if (title != null) {
			dialogBuilder.setTitle(title);
		}

		if (message != null) {
			dialogBuilder.setMessage(message);
		}

		if (onCancelListener != null) {
			dialogBuilder.setOnCancelListener(onCancelListener);
			dialogBuilder.setCancelable(true);
		} else {
			dialogBuilder.setCancelable(false);
		}

		if (onPositiveListener != null) {
			dialogBuilder.setPositiveButton(positiveMessage, onPositiveListener);
		}

		if (onNegativeListener != null) {
			dialogBuilder.setNegativeButton(negativeMessage, onNegativeListener);
		}
		return dialogBuilder.create();
	}
}
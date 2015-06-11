package com.monster.app.myscreenoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.key_setting_boot_popwindow), false)) {
			context.startService(new Intent(context, IconService.class));
		}
	}
}
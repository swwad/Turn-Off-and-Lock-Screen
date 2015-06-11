package com.monster.app.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Utility {

	public static final int RESULT_ENABLE = 12321;

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

	public static boolean isServiceRunning(Context mCtx, Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) mCtx.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static float convertDpToPixel(float dp, Context context) {
		return dp * context.getResources().getDisplayMetrics().density;
	}
}
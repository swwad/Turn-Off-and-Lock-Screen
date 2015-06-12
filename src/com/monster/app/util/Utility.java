package com.monster.app.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

public class Utility {

	public static final int RESULT_ENABLE = 12321;
	public static final int NOTIFICATION_ID = 7210;

	public static AlertDialog ShowCustomizeDialog(Context ctx, String title, String message, String positiveMessage, String negativeMessage, DialogInterface.OnClickListener onPositiveListener,
			DialogInterface.OnClickListener onNegativeListener, DialogInterface.OnCancelListener onCancelListener, View addView) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);

		if (title != null) {
			dialogBuilder.setTitle(title);
		}

		if (message != null) {
			dialogBuilder.setMessage(message);
		}
		
		if (addView != null) {
			dialogBuilder.setView(addView);
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

	public static void ToastUiThread(final Activity mActivity, final String strMessage, final int duration) {
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(mActivity, strMessage, duration).show();
			}
		});
	}
}
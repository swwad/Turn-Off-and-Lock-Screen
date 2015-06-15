package com.monster.app.myscreenoff;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.monster.app.util.Utility;

public class IconService extends Service {

	final static int SINGLE_CLICK_EVENT = 1001;
	final static int DOUBLE_CLICK_EVENT = 1002;

	final static String KEY_POSITION_X = "KEY_POSITION_X";
	final static String KEY_POSITION_Y = "KEY_POSITION_Y";

	private WindowManager windowManager;
	private ImageView chatHead;
	WindowManager.LayoutParams params;
	long startClickTime;
	boolean bDoubleClick = false;

	DevicePolicyManager mDPM;
	ComponentName mDeviceAdminSample;

	void lockScreenNow() {
		mDPM.lockNow();
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_setting_return_home), true)) {
			startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Intent.CATEGORY_HOME));
		}
	}

	Handler mClickHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SINGLE_CLICK_EVENT:
				if (mDPM.isAdminActive(mDeviceAdminSample)) {
					lockScreenNow();
				} else {
					Toast.makeText(IconService.this, getString(R.string.app_introduction), Toast.LENGTH_SHORT).show();
				}
				break;
			case DOUBLE_CLICK_EVENT:
				createNotification();
				IconService.this.stopSelf();
				break;
			}
			bDoubleClick = false;
			this.removeCallbacksAndMessages(null);
			super.handleMessage(msg);
		};
	};

	@Override
	public void onCreate() {
		super.onCreate();
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Utility.NOTIFICATION_ID);

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		chatHead = new ImageView(this);
		chatHead.setAlpha(80);
		chatHead.setImageResource(R.drawable.ic_launcher);

		int popwindow_size = (int) Utility.convertDpToPixel(Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_setting_icon_size), "50")), this);
		params = new WindowManager.LayoutParams(popwindow_size, popwindow_size, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.TOP | Gravity.LEFT;

		params.x = PreferenceManager.getDefaultSharedPreferences(IconService.this).getInt(KEY_POSITION_X, 0);
		params.y = PreferenceManager.getDefaultSharedPreferences(IconService.this).getInt(KEY_POSITION_Y, 100);

		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdminSample = new ComponentName(getApplicationContext(), DevicdAdminReceiver.class);

		chatHead.setOnTouchListener(new OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startClickTime = Calendar.getInstance().getTimeInMillis();
					initialX = params.x;
					initialY = params.y;
					initialTouchX = event.getRawX();
					initialTouchY = event.getRawY();
					break;
				case MotionEvent.ACTION_UP:
					if ((Calendar.getInstance().getTimeInMillis() - startClickTime) < 200) {
						if (!bDoubleClick) {
							bDoubleClick = true;
							mClickHandler.sendEmptyMessageDelayed(SINGLE_CLICK_EVENT, 500);
						} else {
							mClickHandler.removeCallbacksAndMessages(null);
							mClickHandler.sendEmptyMessageDelayed(DOUBLE_CLICK_EVENT, 100);
						}
					} else {
						PreferenceManager.getDefaultSharedPreferences(IconService.this).edit().putInt(KEY_POSITION_X, params.x).commit();
						PreferenceManager.getDefaultSharedPreferences(IconService.this).edit().putInt(KEY_POSITION_Y, params.y).commit();
					}
					break;
				case MotionEvent.ACTION_MOVE:
					params.x = initialX + (int) (event.getRawX() - initialTouchX);
					params.y = initialY + (int) (event.getRawY() - initialTouchY);
					windowManager.updateViewLayout(chatHead, params);
					break;
				}
				return false;
			}
		});
		windowManager.addView(chatHead, params);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatHead != null) {
			windowManager.removeView(chatHead);
		}
	}

	public void createNotification() {
		Intent notificationIntent;
		PendingIntent pendingIntent;
		Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.start_screenoff_popwindow_hint), System.currentTimeMillis());
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_setting_full_version), false)) {
			notificationIntent = new Intent(getApplicationContext(), IconService.class);
			pendingIntent = PendingIntent.getService(getApplicationContext(), 0, notificationIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), getString(R.string.start_screenoff_popwindow_hint), pendingIntent);
		} else {
			notificationIntent = new Intent(getApplicationContext(), SettingActivity.class);
			notificationIntent.putExtra(getString(R.string.full_version_buy), false);
			pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), getString(R.string.only_full_version), pendingIntent);
		}
		notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(Utility.NOTIFICATION_ID, notification);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}

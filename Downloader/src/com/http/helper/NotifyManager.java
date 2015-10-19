package com.http.helper;

import com.progress.downloader.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NotifyManager {
	Context mContext;

	private static final int UPDATE_PROGRESS_CODE = 0x10;
	
	private Notification notification;
	private NotificationManager manager;
	
	int icon = R.drawable.ic_launcher;
	String titleText;
	RemoteViews view;
	private int progress = 0;
	private boolean isTestMode = false;
	Intent contentIntent;
	int sSotificationId = 1;

	public NotifyManager(Context context) {
		mContext = context;
		init();
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public void setContentIntent(Intent contentIntent, int requestCode) {
		notification.contentIntent = PendingIntent.getActivity(mContext,
				requestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private void init() {
		notification = new Notification(icon, titleText, System.currentTimeMillis());
		view = new RemoteViews(mContext.getPackageName(), R.layout.notify_progress_layout);
		notification.contentView = view;
		updateProgressView();

		manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private void updateProgressView() {
		view.setProgressBar(R.id.notify_progressBar, 100, progress, false);
		view.setTextViewText(R.id.notify_progress_tv, progress + "%");
	}

	public void notify(int notificationId) {
		sSotificationId = notificationId;
		manager.cancel(sSotificationId);
		manager.notify(sSotificationId, notification);
		updateProgress(0);
		//new ProgressThread().start();
	}

	public void cancel() {
		manager.cancel(sSotificationId);
	}

	public void updateProgress(int progress) {
		this.progress = progress;
		handler.sendEmptyMessage(UPDATE_PROGRESS_CODE);
	}

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(UPDATE_PROGRESS_CODE == msg.what) {
				manager.notify(sSotificationId, notification);
				updateProgressView();
				if (msg.arg1 >= 100) {
					progress = 0;
					manager.cancel(sSotificationId);
					Toast.makeText(mContext, "Download completed", Toast.LENGTH_SHORT).show();
				}
			}
			
		}
	};

	private class ProgressThread extends Thread {

		@Override
		public void run() {
			while (isTestMode) {
				progress += 10;
				Message msg = handler.obtainMessage();
				msg.arg1 = progress;
				msg.sendToTarget();

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

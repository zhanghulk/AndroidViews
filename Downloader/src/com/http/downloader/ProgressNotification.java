package com.http.downloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class ProgressNotification {

	private static final int UPDATE_PROGRESS_CODE = 0x10;

    private static final String TAG = "ProgressNotification";
	
    Context mContext;
	private Notification notification;
	private NotificationManager manager;
	
	int mId = 1;
	int icon = R.drawable.ic_launcher;
	String tickerText;
	String titleText;
	PendingIntent contentIntent;
	
	RemoteViews view;
	private int progress = 0;
	private boolean isTestMode = false;

	public ProgressNotification(Context context, int id) {
		mContext = context;
		this.mId = id;
		init();
	}

	public void setId(int id) {
        notify(id);
    }

	public void setTickerText(String tickerText) {
        this.tickerText = tickerText;
    }

	public void setIcon(int icon) {
	    this.icon = icon;
	}

	public void setTitleText(String titleText) {
	    this.titleText= titleText; 
	}

	public void setActivityIntent(Intent intent, int requestCode) {
		contentIntent = PendingIntent.getActivity(mContext,
				requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void setServiceIntent(Intent intent, int requestCode) {
        contentIntent = PendingIntent.getService(mContext,
                requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

	public void setBroadcastIntent(Intent intent, int requestCode) {
        contentIntent = PendingIntent.getBroadcast(mContext,
                requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

	private void init() {
		view = new RemoteViews(mContext.getPackageName(), R.layout.notify_progress_layout);
		manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private void startNotify() {
        notification = new Notification(R.drawable.ic_launcher, tickerText, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.contentView = view;
        notification.contentIntent = contentIntent;
        view.setTextViewText(R.id.notify_title_tv, titleText);
        view.setImageViewResource(R.id.notify_icon, icon);
        notifyProgress();
    }

	public void notify(int id) {
	    Log.i(TAG, "start mId= " + mId + ", id=" + id);
	    if(mId != id) {
	        manager.cancel(mId);
	        mId = id;
	    }
	    startNotify();
	}

	public void cancel() {
		manager.cancel(mId);
	}

	private void notifyProgress() {
        view.setProgressBar(R.id.notify_progressBar, 100, progress, false);
        view.setTextViewText(R.id.notify_progress_tv, progress + "%");
        manager.notify(mId, notification);
    }

	public void updateProgress(int progress) {
		this.progress = progress;
		handler.sendEmptyMessage(UPDATE_PROGRESS_CODE);
	}

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(UPDATE_PROGRESS_CODE == msg.what) {
				notifyProgress();
				if (msg.arg1 >= 100) {
					progress = 0;
					manager.cancel(mId);
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

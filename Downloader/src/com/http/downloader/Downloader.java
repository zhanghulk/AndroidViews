package com.http.downloader;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.http.downloader.DownloadTask.DownloadCallback;

public class Downloader implements DownloadCallback, OnClickListener {
	public interface DownloadResultCallback {
		void onDownloadResult(int taskId, String filePath, String errorMsg);
	}

	private Context mContext;
	private Dialog dialog;
	private View view;
	private TextView titleTv;
	private TextView descTv;
	private TextView progressTv;
	private ProgressBar progressBar;
	private Button mBackgroundBtn, mCancelBtn;
	private int theme;
	private int gravity = Gravity.BOTTOM;
	private int fileLength = 1;
	private int progressCount = 0;
	private int progress = 0;
	int notificationId = 1;
	
	private DownloadTask downloadTask;
	private DownloadResultCallback callback;
	NotifyManager mNotifyManager;

	public void setCallback(DownloadResultCallback callback) {
		this.callback = callback;
	}

	Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:

				break;

			default:
				break;
			}
		};
	};

	public Downloader(Context context) {
		mContext = context;
	}

	public Downloader(Context context, int dialogTheme) {
		mContext = context;
		theme = dialogTheme;
		if (dialogTheme > 0) {
			initView();
		}
	}

	public void setTitleText(CharSequence title) {
		if (theme == 0) {
			dialog.setTitle(title);
			titleTv.setVisibility(View.GONE);
		} else {
			titleTv.setText(title);
		}
	}

	public void setDescText(CharSequence descText) {
		descTv.setText(descText);
	}

	public void setBackgroundBtnText(CharSequence text) {
		mBackgroundBtn.setText(text);
	}
	
	public void setCancelBtnText(CharSequence text) {
		mCancelBtn.setText(text);
	}

	public void setTheme(int dialogTheme) {
		theme = dialogTheme;
		initView();
	}

	public void setGravity(int gravity) {
		this.gravity = gravity;
	}

	public void start(String url, String fileDir) {
		downloadTask = new DownloadTask(url);
		downloadTask.setFileDir(fileDir);
		downloadTask.setCallback(this);
		new Thread(downloadTask).start();
		if (theme != 0) {
			showDialog();
		}
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public void setNotificationIcon(int icon) {
		if(mNotifyManager != null) {
			mNotifyManager.setIcon(icon);
		}
	}

	public void setNotifyActivityIntent(Intent contentIntent, int requestCode) {
		if(mNotifyManager != null) {
			mNotifyManager.setActivityIntent(contentIntent, requestCode);
		}
	}

	public void setNotifyServiceIntent(Intent contentIntent, int requestCode) {
        if(mNotifyManager != null) {
            mNotifyManager.setActivityIntent(contentIntent, requestCode);
        }
    }
	
	public void setNotifyBroadcastIntent(Intent contentIntent, int requestCode) {
        if(mNotifyManager != null) {
            mNotifyManager.setActivityIntent(contentIntent, requestCode);
        }
    }

	public void noyify() {
		noyify(notificationId);
	}

	public void noyify(int notificationId) {
		mNotifyManager = new NotifyManager(mContext);
		mNotifyManager.notify(notificationId);
	}

	public void doInDackground() {
		dismissDialog();
		noyify();
	}

	public void cancelTask() {
		if(downloadTask != null) {
			downloadTask.setCanceled(true);
		}
		dismissDialog();
		cancelNotification();
	}

	public void cancelNotification() {
        if (mNotifyManager != null) {
            mNotifyManager.cancel();
        }
    }

	public void showDialog() {
		DialogManager.showViewDialog(mContext, dialog, view, gravity, null,
				null, false);
	}

	public void dismissDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	public void initView() {
		createDialog();
		view = LayoutInflater.from(mContext).inflate(
				R.layout.progress_dialog, null);
		titleTv = (TextView) view.findViewById(R.id.title_tv);
		progressTv = (TextView) view.findViewById(R.id.progress_tv);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		descTv = (TextView) view.findViewById(R.id.desc_tv);
		progressBar.setMax(100);
		view.findViewById(R.id.do_backgroun_btn).setOnClickListener(this);
		view.findViewById(R.id.cancel_btn).setOnClickListener(this);
	}

	private void createDialog() {
		if (theme == 0) {
			dialog = new Dialog(mContext);
		} else {
			dialog = new Dialog(mContext, theme);
		}
	}

	public void setProgressCount(int progressByieCount) {
		this.progressCount = progressByieCount;
		mHandler.post(new Runnable() {
			public void run() {
				progressTv.setText(progressCount + "/" + fileLength);
			}
		});
	}

	public void setProgress(final int progress) {
		this.progress = progress;
		
		mHandler.post(new Runnable() {
			public void run() {
				progressBar.setProgress(progress);
			}
		});
		if (mNotifyManager != null) {
			mNotifyManager.updateProgress(progress);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.do_backgroun_btn:
			doInDackground();
			break;
		case R.id.cancel_btn:
			cancelTask();
			break;

		default:
			break;
		}
	}

	@Override
    public void onDownloadStart(int taskId, int contentLength) {
        fileLength = contentLength;
        setProgressCount(0);
    }

    @Override
    public void onDownloadProgress(int taskId, final int progress, int progressCount) {
        setProgressCount(progressCount);
        setProgress(progress);
    }

    @Override
    public void onDownloadFinished(int taskId, String filePath, String errorMsg) {
        if (callback != null) {
            callback.onDownloadResult(taskId, filePath, errorMsg);
        }
        dismissDialog();
    }
}

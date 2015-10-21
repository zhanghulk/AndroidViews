package com.http.downloader;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.http.downloader.Downloader.DownloadCallback;

public class DownloadDialog implements DownloadTask.Callback, DownloadCallback, OnClickListener {
	public interface DownloadResultCallback {
		void onDownloadResult(int taskId, State state, String filePath, String errorMsg);
	}

	public static final int NOTIFICATION_ID = 0x1212;

    private static final String TAG = "DownloadDialog";

	private Context mContext;
	private Dialog mDialog;
	private ProgressNotification mNotification;
	private View mView;
	private TextView mTitleTv;
	private TextView mDescTv;
	private TextView mProgressTv;
	private ProgressBar mProgressBar;
	private Button mDoInBackgroundBtn, mCancelBtn;

	private int theme;
	private int gravity = Gravity.BOTTOM;
	private long fileLength = 1;
	private int mProgressCount = 0;
	private int mProgress = 0;
	int notificationId = NOTIFICATION_ID;
	int taskId = 0;
	boolean isShowDialog = true;
	boolean dialogCanceledOnTouchOutside = true;
	String filePath;
	
	private DownloadResultCallback callback;
	
	private DownloadTask downloadTask;
	
	private boolean debug = false;

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

	public DownloadDialog(Context context) {
		this(context, 0);
	}

	public DownloadDialog(Context context, boolean showDialog) {
		mContext = context;
		isShowDialog = showDialog;
		init();
	}

	public DownloadDialog(Context context, int dialogTheme) {
        mContext = context;
        theme = dialogTheme;
        init();
    }

	public static String getStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }

	public void setDialogCanceledOnTouchOutside(boolean canceled) {
        this.dialogCanceledOnTouchOutside = canceled;
    }

	public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

	public void setTitleText(String titleText) {
		if (theme == 0) {
			mDialog.setTitle(titleText);
			mTitleTv.setVisibility(View.GONE);
		} else {
			mTitleTv.setText(titleText);
		}
		mNotification.setTitleText(titleText);
	}

	public void setDescText(String descText) {
		mDescTv.setText(descText);
	}

	public void setDoInBackgroundText(String text) {
		mDoInBackgroundBtn.setText(text);
	}
	
	public void setCancelBtnText(String text) {
		mCancelBtn.setText(text);
	}

	public void setTheme(int dialogTheme) {
		theme = dialogTheme;
		init();
	}

	public void setGravity(int gravity) {
		this.gravity = gravity;
	}

	public void start(String url, String filePath) {
	    if(filePath != null && filePath.length() != 0) {
	        this.filePath = filePath;
	    }
		doTask(url, filePath);
		if (isShowDialog) {
		    showDialog();
        }
		noyify();
	}

	public void doTask(String url, String filePath) {
	    downloadTask = new DownloadTask(mContext, this);
        downloadTask.execute(url, filePath);
    }

	public void setNotificationId(int id) {
	    mNotification.setId(id);
	}

	public void setNotificationIcon(int icon) {
        mNotification.setIcon(icon);
    }

	public void setNotificationTitleText(String titleText) {
	    mNotification.setTitleText(titleText);
    }

	public void setNotifyActivityIntent(Intent contentIntent, int requestCode) {
	    mNotification.setActivityIntent(contentIntent, requestCode);
	}

	public void setNotifyServiceIntent(Intent contentIntent, int requestCode) {
	    mNotification.setActivityIntent(contentIntent, requestCode);
    }
	
	public void setNotifyBroadcastIntent(Intent contentIntent, int requestCode) {
	    mNotification.setActivityIntent(contentIntent, requestCode);
    }

	public void noyify() {
		noyify(notificationId);
	}

	public void noyify(int id) {
	    mNotification.notify(id);
	}

	public void doInDackground() {
		dismissDialog();
	}

	public void cancelTask() {
		if(downloadTask != null) {
		    downloadTask.setCanceled(true);
		}
		dismissDialog();
		cancelNotification();
	}

	public void cancelNotification() {
        if (mNotification != null) {
            mNotification.cancel();
        }
    }

	public void showDialog() {
	    mDialog.setCanceledOnTouchOutside(dialogCanceledOnTouchOutside );
		DialogManager.showViewDialog(mContext, mDialog, mView, gravity, null,
				null, false);
	}

	public void dismissDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	public void init() {
	    filePath = getStorageDir();
	    mNotification = new ProgressNotification(mContext, notificationId);
	    if (isShowDialog) {
	        createDialog();
	        mView = LayoutInflater.from(mContext).inflate(
	                R.layout.progress_dialog, null);
	        mTitleTv = (TextView) mView.findViewById(R.id.title_tv);
	        mProgressTv = (TextView) mView.findViewById(R.id.progress_tv);
	        mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
	        mDescTv = (TextView) mView.findViewById(R.id.desc_tv);
	        mProgressBar.setMax(100);
	        mView.findViewById(R.id.do_backgroun_btn).setOnClickListener(this);
	        mView.findViewById(R.id.cancel_btn).setOnClickListener(this);
        }
	}

	private void createDialog() {
		if (theme == 0) {
			mDialog = new Dialog(mContext);
		} else {
			mDialog = new Dialog(mContext, theme);
		}
	}

	public void setProgress(int progress, int progressCount) {
		if(mProgress != progress) {
		    this.mProgress = progress;
		    this.mProgressCount = progressCount;
		    if (mDialog.isShowing()) {
		        mHandler.post(new Runnable() {
	                public void run() {
	                    mProgressBar.setProgress(mProgress);
	                    mProgressTv.setText(mProgressCount + "/" + fileLength);
	                }
	            });
            }
	        mNotification.updateProgress(mProgress);
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
    public void onDownloadStart(int contentLength) {
	    Log.i(TAG, "onDownloadStart contentLength=" + contentLength);
        fileLength = contentLength;
    }

    @Override
    public void onDownloadProgress(int progress, int progressCount) {
        log("onDownloadProgress mProgress=" + progress + ",mProgress=" + progress);
        setProgress(progress, progressCount);
    }

    @Override
    public void onDownloadFinished(State state, String filePath, String errorMsg) {
        Log.i(TAG, "onDownloadFinished state=" + state + ",errorMsg=" + errorMsg);
        finish(state, filePath, errorMsg);
    }

    @Override
    public void onStarteded(long contentLength) {
        Log.i(TAG, "onStarteded contentLength=" + contentLength);
        fileLength = contentLength;
    }
    @Override
    public void onProgressUpdate(int progress, int count) {
        setProgress(progress, count);
    }
    
    @Override
    public void onFinished(State state, String filePath, String errorMsg) {
        if (state == State.SUCCESS) {
            Log.i(TAG, "onFinished download filePath=" + filePath);
        } else {
            Log.w(TAG, "onFinished state=" + state + ",errorMsg=" + errorMsg);
        }
        finish(state, filePath, errorMsg);
    }

    private void finish(State state, String filePath, String errorMsg) {
        if (callback != null) {
            callback.onDownloadResult(taskId, state, filePath, errorMsg);
        }
        dismissDialog();
        mNotification.setTitleText("Download completed");
    }

    private void log(String msg) {
        if(debug) Log.i(TAG, msg);
    }
}

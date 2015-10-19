package com.http.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
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

import com.progress.downloader.R;

public class Downloader implements OnClickListener {
	public interface DownloadResultCallback {
		void onDownloadResult(int taskId, String filePath, String errorMsg);
	}

	public interface DownloadCallback {
		void onDownloadStart(int taskId, int fileLength);
		void onDownloadProgress(int taskId, int progress, int progressCount);
		void onDownloadFinished(int taskId, String filePath, String errorMsg);
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

	public void start(String url) {
		downloadTask = new DownloadTask(url);
		downloadTask.setCallback(mCallback);
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

	public void setNotificationIntent(Intent contentIntent, int requestCode) {
		if(mNotifyManager != null) {
			mNotifyManager.setContentIntent(contentIntent, requestCode);
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

	DownloadCallback mCallback = new DownloadCallback() {
		
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
	};

	public static class DownloadTask implements Runnable {

		private int taskId = 0;
		private String url;
		private String fileDir;
		private String fileName;
		
		private int timeoutMillis;
		private DownloadCallback callback;
		private boolean canceled = false;

		public DownloadTask(String url) {
			this.url = url;
			fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
			fileName = url.substring(url.lastIndexOf(File.separator) + 1);
		}

		public DownloadTask(String url, String fileDir, String fileName) {
			this.url = url;
			this.fileDir = fileDir;
			this.fileName = fileName;
		}

		public void setTaskId(int taskId) {
			this.taskId = taskId;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getFileDir() {
			return fileDir;
		}

		public void setFileDir(String fileDir) {
			this.fileDir = fileDir;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFilePath() {
			File dir = new File(fileDir);
			if(!dir.exists()) {
				dir.mkdirs();
			}
			String filePath = fileDir + fileName;
			File file = new File(filePath);
			if(file.exists()) {
				deleteFile(file);
				System.out.println("DELETE file: " + filePath);
			}
			try {
				boolean res = file.createNewFile();
				System.out.println("createNewFile: " + res + ", file: " + file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return filePath;
		}

		private void deleteFile(File file) {
			if(file != null && file.exists()) {
				file.delete();
			}
		}

		public DownloadCallback getCallback() {
			return callback;
		}

		public void setCallback(DownloadCallback callback) {
			this.callback = callback;
		}

		public void setTimeoutMillis(int timeoutMillis) {
			this.timeoutMillis = timeoutMillis;
		}

		public void setCanceled(boolean canceled) {
			this.canceled = canceled;
		}

		@Override
		public void run() {
			downloadFile();
		}

		public boolean downloadFile() {
			InputStream input = null;
			FileOutputStream output = null;
			String errorMsg = null;
			String filePath = null;
			try {
				if (canceled) return false;
				URL _url = new URL(url);
				HttpURLConnection urlConn = (HttpURLConnection) _url
						.openConnection();
				urlConn.setConnectTimeout(timeoutMillis);
				int len = urlConn.getContentLength();
				if(callback != null) {
					callback.onDownloadStart(taskId, len);
				}
				if(len <= 0) {
					errorMsg = "Content Length is 0";
					return false;
				}
				input = urlConn.getInputStream();
				int count = 0;
				if(input != null) {
					filePath = getFilePath();
					File file = new File(filePath);
					output = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int read = 0;
					while ((read = input.read(buffer)) != -1) {
						output.write(buffer);
						count += read;
						if(callback != null) {
							int progress = (count * 100) / len;
							callback.onDownloadProgress(taskId, progress, count);
						}
						if (canceled) {
							deleteFile(file);
							output.close();
							return false;
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(input != null)
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if(output != null) {
					try {
						output.flush();
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(callback != null) {
					callback.onDownloadFinished(taskId, filePath, errorMsg);
				}
			}
			return true;
		}
	}

}

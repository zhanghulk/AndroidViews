package com.http.downloader.ui;

import com.http.downloader.DownloadTask;
import com.http.downloader.Downloader;
import com.http.downloader.DownloadTask.DownloadCallback;
import com.http.downloader.Downloader.DownloadResultCallback;
import com.progress.downloader.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class DownloadActivity extends Activity {

	protected static final String TAG = "DownloadActivity";
	private static final int NOTIFICATION_ID = 0x12;
	String url = "http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1508/14/c0/11195389_1439563888459_800x600.jpg";
	
	Button mStartBtn;
	ImageView img;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mStartBtn = (Button) findViewById(R.id.start_btn);
		img = (ImageView) findViewById(R.id.img);
		mStartBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//startDownload();
				downloadImg();
			}
		});
	}

	private void downloadImg() {
		Downloader downloader = new Downloader(this, R.style.MMTheme_DataSheet);
		downloader.setTitleText("Downloading img ......");
		downloader.setDescText("Please download latest version");
		downloader.setNotificationId(NOTIFICATION_ID);
		downloader.setNotificationIcon(R.drawable.ic_launcher);
		Intent contentIntent = new Intent(this, DownloadActivity.class);
		downloader.setNotificationIntent(contentIntent , 0);
		downloader.setCallback(new DownloadResultCallback() {
			@Override
			public void onDownloadResult(int taskId, String filePath, String errorMsg) {
				setImg(filePath);
			}
		});
		downloader.start(url);
	}

	private void startDownload() {
		DownloadCallback cb = new DownloadCallback() {
			
			@Override
			public void onDownloadStart(int taskId, int contentLength) {
				Log.i(TAG, "onDownloadStart taskId: " + taskId);
			}
			
			@Override
			public void onDownloadProgress(int taskId, int progress, int progressCount) {
				Log.i(TAG, "onDownloadProgress=" + progress);
			}
			
			@Override
			public void onDownloadFinished(int taskId, String filePath, String errorMsg) {
				Log.i(TAG, "onDownloadFinished filePath: " + filePath + ",errorMsg=" + errorMsg);
				setImg(filePath);
			}
		};
		DownloadTask dl = new DownloadTask(url);
		dl.setCallback(cb);
		new Thread(dl).start();
	}

	public void setImg(final String filePath) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/*Matrix matrix = new Matrix();
				matrix.setScale(1.5f, 1.5f);
				img.setImageMatrix(matrix);*/
				Bitmap bm = BitmapFactory.decodeFile(filePath);
				img.setImageBitmap(bm);
			}
		});
	}
}

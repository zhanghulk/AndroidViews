package com.http.downloader.ui;

import com.http.downloader.ApkManager;
import com.http.downloader.DownloadDialog;
import com.http.downloader.DownloadDialog.DownloadResultCallback;
import com.http.downloader.R;
import com.http.downloader.State;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class DownloadActivity extends Activity {

	protected static final String TAG = "DownloadActivity";
	private static final int APK_INSTALL_REQUEST_CODE = 0x1;
	String url = "http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1508/14/c0/11195389_1439563888459_800x600.jpg";
	String filePath = DownloadDialog.getStorageDir();
	
	ImageView img;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		img = (ImageView) findViewById(R.id.img);
		findViewById(R.id.apk_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    downloadApk();
			}
		});
		findViewById(R.id.load_img_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImg();
            }
        });
	}

	private void downloadApk() {
        String titleText = "Downloading APK ......";
        String descText = "Please download latest version";
        ApkManager.downloadApk(this, ApkManager.APK_TEST_URL, filePath, titleText, descText, new DownloadResultCallback() {
            @Override
            public void onDownloadResult(int taskId, State state, String filePath, String errorMsg) {
                Log.i(TAG, "Download apk state: " + state + ", filePath: " + filePath);
                if (state == State.SUCCESS) {
                    ApkManager.installApk(DownloadActivity.this, filePath, false, APK_INSTALL_REQUEST_CODE);
                }
            }
        });
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
            case APK_INSTALL_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                }
                break;

            default:
                break;
        }
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void downloadImg() {
        String titleText = "Please download net image";
        String descText = "Downloading img ......";
        DownloadDialog downloadDialog = new DownloadDialog(this);
        downloadDialog.setTitleText(titleText);
        downloadDialog.setDescText(descText);
        downloadDialog.setNotificationId(11);
        downloadDialog.setCallback(new DownloadResultCallback() {
            @Override
            public void onDownloadResult(int taskId, State state, String filePath, String errorMsg) {
                Log.i(TAG, "Download Image state: " + state + ",filePath: " + filePath);
                if (state == State.SUCCESS) {
                    setImg(filePath);
                }
            }
        });
        downloadDialog.start(url, filePath);//save in storage root
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

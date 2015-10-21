
package com.http.downloader;

import com.http.downloader.DownloadDialog.DownloadResultCallback;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {

    String filePath = null;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String url = intent.getStringExtra(ApkManager.INTENT_EXTRA_APK_URL);
            filePath = intent.getStringExtra(ApkManager.INTENT_EXTRA_APK_FILE_PATH);
            downloadApk(url, filePath);
        }
        return START_NOT_STICKY;
    }

    private void downloadApk(String url, String filePath) {
        String titleText = "Downloading img ......";
        String descText = "Please download latest version";
        ApkManager.downloadApk(this, url, filePath, titleText, descText, new DownloadResultCallback() {
            @Override
            public void onDownloadResult(int taskId, State state, String filepath, String errorMsg) {
                //ApkManager.installApk(DownloadService.this, filePath, false, 0);
            }
        });
    }
}

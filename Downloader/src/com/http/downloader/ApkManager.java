
package com.http.downloader;

import java.io.File;

import com.http.downloader.DownloadDialog.DownloadResultCallback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ApkManager {

    public static final String ACTION_INSTALL_APK = "com.http.downloader.INSTALL_APK";
    public static final String INTENT_EXTRA_APK_URL = "url";
    public static final String INTENT_EXTRA_APK_FILE_PATH = "file_path";
    
    public static final String APK_TEST_URL = "http://apk.r1.market.hiapk.com/data/upload/marketClient/HiMarket6.0.86_1442302645972.apk";

    ///install apk key:
    public static final String APK_FILE_TYPE = "application/vnd.android.package-archive";
    public static final String PACKAGE_INSTALLER_NAME = "com.android.packageinstaller";
    public static final String PACKAGE_INSTALLER_ACTIVITY = "com.android.packageinstaller.PackageInstallerActivity";
    
    public static void downloadApk(final Context context, String url, String filePath,
            String titleText, String descText, DownloadResultCallback callback) {
        DownloadDialog downloadDialog = new DownloadDialog(context, R.style.PullUp_Dialog);
        downloadDialog.setTitleText(titleText);
        downloadDialog.setDescText(descText);
        downloadDialog.setNotificationIcon(R.drawable.ic_launcher);
        downloadDialog.setNotificationTitleText(titleText);
        Intent intent = new Intent(ACTION_INSTALL_APK);
        intent.putExtra(INTENT_EXTRA_APK_FILE_PATH, filePath);
        downloadDialog.setNotifyActivityIntent(intent, 0);
        downloadDialog.setCallback(callback);
        downloadDialog.start(url, filePath);
    }

    public static Intent getInstallIntent(String apkPath, boolean newTaskFlag) {
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            return null;
        }
        Uri uri = Uri.parse(apkPath);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClassName(PACKAGE_INSTALLER_NAME, PACKAGE_INSTALLER_ACTIVITY);
        intent.setDataAndType(uri, APK_FILE_TYPE);
        if (newTaskFlag) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }
    
    /**
     * installed result will callback original Activity onAvtivityResult().
     * @param act
     * @param path
     */
    public static boolean installApk(Context context, String apkPath, boolean addTaskFlag, int requestCode) {
        if(!isApkFile(apkPath)){
            return false;
        }
        Intent intent = ApkManager.getInstallIntent(apkPath, addTaskFlag);
        if(context instanceof Activity) {
            ((Activity)context).startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }
        return true;
    }

    public static boolean isApkFile(String apkPath) {
        if(apkPath == null || apkPath.length() == 0) {
            return false;
        }
        File apkFile = new File(apkPath);
        if (!apkFile.exists() || !apkFile.isFile()) {
            return false;
        }
        return apkPath.endsWith(".apk") || apkPath.endsWith(".APK");
    }
}

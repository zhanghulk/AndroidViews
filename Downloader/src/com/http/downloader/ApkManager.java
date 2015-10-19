
package com.http.downloader;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class ApkManager {

    ///install apk key:
    public static final String APK_FILE_TYPE = "application/vnd.android.package-archive";
    public static final String PACKAGE_INSTALLER_NAME = "com.android.packageinstaller";
    public static final String PACKAGE_INSTALLER_ACTIVITY = "com.android.packageinstaller.PackageInstallerActivity";

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
    public static void installApk(Activity activity, String apkPath, boolean addTaskFlag, int requestCode) {
        if(!isApkFile(apkPath)) return;
        Intent intent = ApkManager.getInstallIntent(apkPath, addTaskFlag);
        activity.startActivityForResult(intent, requestCode);
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

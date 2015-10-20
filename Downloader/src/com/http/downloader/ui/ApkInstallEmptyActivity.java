
package com.http.downloader.ui;

import com.http.downloader.ApkManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * The empty activity will launch system to install apk.
 * And handle the action return result in onActivityResult() after installing over
 * 
 * @author hulk
 */
public class ApkInstallEmptyActivity extends Activity {

	public static final int REQUEST_ENTERPRISE_AGENT_INSTALL_CODE = 0x2;
	public static final String EXTRA_APK_PATH = "apk_path";
    private static final String TAG = "AgentInstallEmptyActivity";

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        String apkPath = getIntent().getStringExtra(EXTRA_APK_PATH);
        ApkManager.installApk(this, apkPath, false, REQUEST_ENTERPRISE_AGENT_INSTALL_CODE);
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "onActivityResult:requestCode=" + requestCode + ", resultCode=" + resultCode);
		if(REQUEST_ENTERPRISE_AGENT_INSTALL_CODE == requestCode) {
		    
		}
		finish();
	}
    
}

package com.http.downloader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class DialogManager {

	public static void showViewDialog(Context context, final Dialog dlg,
			View view, int gravity, OnShowListener IShow,
			OnCancelListener ICancel, boolean isNativeViewAttr) {
		if (dlg == null) {
			return;
		}
		try {
			if (context instanceof Activity
					&& ((Activity) context).isFinishing()) {
				Log.i("dialog", "activity is finishing ....");
				return;
			}
			if(view.getBackground() == null) {
				view.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
			}
			if (ICancel != null) {
				dlg.setOnCancelListener(ICancel);
			}
			if (IShow != null) {
				dlg.setOnShowListener(IShow);
			}
			if(!isNativeViewAttr) {
				final int cFullFillWidth = 10000;
				view.setMinimumWidth(cFullFillWidth);
				setDialogPosition(dlg, gravity);
			}
			dlg.setContentView(view);
			dlg.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * set a large value put it in bottom
	 * 
	 * @param dlg
	 */
	private static void setDialogPosition(final Dialog dlg, int gravity) {
		Window w = dlg.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.x = 0;
		final int cMakeBottom = -1000;
		lp.y = cMakeBottom;
		lp.gravity = gravity;
		dlg.onWindowAttributesChanged(lp);
	}
}

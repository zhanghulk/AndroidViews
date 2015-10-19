package com.widget.wheelview;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class WheelConfig {
	public static int getTextSize(Context context) {
		int textSize = 25;
		int deviceWith = getDeviceWidth(context);
		switch (deviceWith) {
		case 240:
			textSize = 10;
			break;
		case 320:
			textSize = 15;
			break;
		case 480:
			textSize = 18;
			break;
		case 720:
			textSize = 25;
			break;
		case 1080:
			textSize = 40;
			break;

		default:
			break;
		}
		return textSize;
	}

	public static int getAdditionalItemWidth(Context context) {
		int addWidth = 20;
		int deviceWith = getDeviceWidth(context);
		switch (deviceWith) {
		case 240:
			addWidth = 10;
			break;
		case 320:
			addWidth = 15;
			break;
		case 480:
			addWidth = 18;
			break;
		case 720:
			addWidth = 5;
		case 1080:
			addWidth = 35;
			break;
		default:
			break;
		}
		return addWidth;
	}

	/**
	 * wheelview
	 * 
	 * @added huiych
	 * @return the height
	 */
	public static int getAdditionalItemHeight(Context context) {
		int addHeight = 40;
		int deviceWith = getDeviceWidth(context);
		switch (deviceWith) {
		case 240:
			addHeight = 25;
			break;
		case 320:
			addHeight = 35;
			break;
		case 480:
			addHeight = 60;
			break;
		case 540:
			addHeight = 80;
			break;
		case 640:
			addHeight = 100;
			break;
		case 720:
			addHeight = 90;
			break;
		case 800:
			addHeight = 100;
			break;
		case 1080:
			addHeight = 135;
			break;
		default:
			break;
		}
		return addHeight;
	}

	/**
	 * wheelview
	 * 
	 * @added huiych
	 * @return the height
	 */
	public static int getThreeWheelAdditionalItemHeight(Context context) {
		int addHeight = 40;
		int deviceWith = getDeviceWidth(context);
		switch (deviceWith) {
		case 240:
			addHeight = 25;
			break;
		case 320:
			addHeight = 50;
			break;
		case 480:
			addHeight = 70;
			break;
		case 540:
			addHeight = 90;
			break;
		case 640:
			addHeight = 110;
			break;
		case 720:
			addHeight = 100;
			break;
		case 800:
			addHeight = 110;
			break;
		case 1080:
			addHeight = 155;
			break;

		default:
			break;
		}
		return addHeight;
	}

	public static Display getDisPlay(Context context) {
		return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
	}

	public static Display getDisPlay(Activity activity) {
		return activity.getWindowManager().getDefaultDisplay();
	}

	public static DisplayMetrics getDisPlayMatrics(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		getDisPlay(activity).getMetrics(dm);
		return dm;
	}

	public static int getDeviceWidth(Context context) {
		return getDisPlay(context).getWidth();
	}

	public static int getDeviceHeight(Context context) {
		return getDisPlay(context).getHeight();
	}
}

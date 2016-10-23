package com.slim.widget;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MToast extends Toast {

	Context context;
	ToastView view;
	int duration = 0;
	CharSequence msgText = null;
	int flagIconResId = 0;
	Handler handler = new Handler();
	
	Runnable mShowRunnable = new Runnable() {
		public void run() {
			show();
		}
	};

	public MToast(Context context) {
		super(context);
		this.context = context;
		init();
	}

	private void init() {
		view = new ToastView(context);
		duration = Toast.LENGTH_SHORT;
		setDuration(duration);
		setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		view.setMinimumWidth(10000);
	}

	public ToastView getView() {
		return view;
	}

	public void setView(ToastView view) {
		this.view = view;
		super.setView(view);
	}
	
	@Override
	public void setDuration(int duration) {
		this.duration = duration;
		super.setDuration(duration);
	}

	@Override
	public void setText(CharSequence s) {
		msgText = s;
		view.setMsgText(s);
	}
	
	public void setText(String text) {
		msgText = text;
		view.setMsgText(text);
	}
	
	public void setText(int resId) {
		msgText = context.getString(resId);
		view.setMsgText(resId);
	}
	
	public void setMinimumWidth(int minimumWidth) {
		view.setMinimumWidth(minimumWidth);;
	}

	public CharSequence getMsgText() {
		return msgText;
	}

	public void setMsgText(CharSequence msgText) {
		this.msgText = msgText;
	}
	
	public ImageView getFlagIcon() {
		return view.flagIcon;
	}

	public void setFlagIcon(int resId) {
		flagIconResId = resId;
		view.setFlagIcon(flagIconResId);
	}
	
	public void setFlagIconVisibility(int visibility) {
		view.flagIcon.setVisibility(visibility);
	}

	public int getDuration() {
		return duration;
	}

	public void showMsg() {
		if(!TextUtils.isEmpty(msgText)) {
			show(msgText, duration);
		}
	}

	public void showMsg(String msg) {
		show(msg, duration);
	}

	public void show(CharSequence msgText, int duration) {
		cancel();
		view.setMsgText(msgText);
		super.setDuration(duration);
		super.setView(view);
		handler.removeCallbacks(mShowRunnable);
		handler.post(mShowRunnable);
	}
}

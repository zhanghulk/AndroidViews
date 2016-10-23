package com.slim.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.GridView;

public class MGridView extends GridView {

	public MGridView(Context context) {
		super(context);
		init();
	}

	public MGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@SuppressLint("NewApi")
	private void init() {
		if (Build.VERSION.SDK_INT >= 9) {
			setOverScrollMode(OVER_SCROLL_NEVER);
			setCacheColorHint(Color.TRANSPARENT);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);

		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}

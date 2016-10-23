package com.slim.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * 1. 解决横向纵向滑动事件混淆屏蔽, 能够兼容ViewPager的ScrollView 2. 实现纵向拉伸弹跳缩回效果
 * 
 * @Description: 解决了ViewPager在ScrollView中的滑动反弹问题， 实现纵向拉伸弹跳缩回效果
 * @author hulk
 * 
 */
public class MScrollView extends ScrollView {
	private static final String TAG = "MScrollView";

	/**
	 * 解决横向纵向滑动事件混淆屏蔽
	 */
	private float xDistance, yDistance, xLast, yLast;

	/**
	 * 以下实现纵向拉伸弹跳缩回效果
	 */
	private View inner;// 孩子View

	private float clickY;// 点击时y坐标

	// 矩形(这里只是个形式，只是用于判断是否需要动画.)
	private Rect normal = new Rect();

	private boolean isCount = false;// 是否开始计算
	private boolean debug = false;// 是否开始计算

	private int recoverAnimDuration = 400;
	private final static float RATIO = 3;// 手势下拉距离比.

	private boolean bounceEnable = true;
	private float moveRatio = RATIO;// 手势下拉距离比.

	public MScrollView(Context context) {
		super(context);
		init(context);
	}

	public MScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	@SuppressLint("NewApi")
	private void init(Context context) {
		bounceEnable = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("bounceEnable", true);
		if (Build.VERSION.SDK_INT >= 9) {
			setOverScrollMode(OVER_SCROLL_NEVER);
		}
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isBounceEnable() {
		return bounceEnable;
	}

	public void setBounceEnable(boolean bounceEnable) {
		this.bounceEnable = bounceEnable;
	}

	public float getMoveRatio() {
		return moveRatio;
	}

	public void setMoveRatio(float moveRatio) {
		this.moveRatio = moveRatio;
	}

	public int getRecoverAnimDuration() {
		return recoverAnimDuration;
	}

	public void setRecoverAnimDuration(int duration) {
		this.recoverAnimDuration = duration;
	}

	/***
	 * 根据 XML 生成视图工作完成.该函数在生成视图的最后调用， 在所有子视图添加完之后. 即使子类覆盖了 onFinishInflate
	 * 方法，也应该调用父类的方法，使该方法得以执行.
	 */
	@Override
	protected void onFinishInflate() {
		if (bounceEnable) {
			if (getChildCount() > 0) {
				inner = getChildAt(0);
			}
		}
		super.onFinishInflate();
	}

	/***
	 * 监听touch
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (bounceEnable) {
			if (inner != null) {
				doTouchEvent(ev);
			}
		}
		return super.onTouchEvent(ev);
	}

	/***
	 * 触摸事件
	 * 
	 * @param ev
	 */
	public void doTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_UP:
			// 手指松开.
			if (isNeedAnimation()) {
				recoverAnimation();
				isCount = false;
			}
			break;
		/***
		 * 排除出第一次移动计算，因为第一次无法得知y坐标， 在MotionEvent.ACTION_DOWN中获取不到，
		 * 因为此时是MyScrollView的touch事件传递到到了LIstView的孩子item上面.所以从第二次计算开始.
		 * 然而我们也要进行初始化，就是第一次移动的时候让滑动距离归0. 之后记录准确了就正常执行.
		 */
		case MotionEvent.ACTION_MOVE:
			final float preY = clickY;// 按下时的y坐标
			float nowY = ev.getY();// 时时y坐标
			int deltaY = (int) (preY - nowY);// 滑动距离
			if (!isCount) {
				deltaY = 0; // 在这里要归0.
			}

			clickY = nowY;
			// 当滚动到最上或者最下时就不会再滚动，这时移动布局
			if (isNeedMove()) {
				// 初始化头部矩形
				if (normal.isEmpty()) {
					// 保存正常的布局位置
					normal.set(inner.getLeft(), inner.getTop(),
							inner.getRight(), inner.getBottom());
				}
				if (debug)
					Log.v(TAG,
							"MOVE Rect：" + inner.getLeft() + ","
									+ inner.getTop() + "," + inner.getRight()
									+ "," + inner.getBottom());
				// accodding to deltaY move inner view ( Rect normal) 移动布局
				int deltaMove = (int) (deltaY / moveRatio);
				int top = inner.getTop() - deltaMove;
				int Bottom = inner.getBottom() - deltaMove;
				inner.layout(inner.getLeft(), top, inner.getRight(), Bottom);
			}
			isCount = true;
			break;

		default:
			break;
		}
	}

	/***
	 * 回缩动画
	 */
	public void recoverAnimation() {
		// 开启移动动画
		TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(), normal.top);
		ta.setDuration(recoverAnimDuration);
		inner.startAnimation(ta);
		// 设置回到正常的布局位置
		inner.layout(normal.left, normal.top, normal.right, normal.bottom);

		if (debug)
			Log.d(TAG, "animation：" + normal.left + "," + normal.top + ","
					+ normal.right + "," + normal.bottom);

		normal.setEmpty();
	}

	// 是否需要开启动画
	public boolean isNeedAnimation() {
		return !normal.isEmpty();
	}

	/***
	 * 是否需要移动布局 inner.getMeasuredHeight():获取的是控件的总高度
	 * 
	 * getHeight()：获取的是屏幕的高度
	 * 
	 * @return
	 */
	public boolean isNeedMove() {
		int offset = inner.getMeasuredHeight() - getHeight();
		int scrollY = getScrollY();
		if (debug)
			Log.i(TAG, "isNeedMove scrolly=" + scrollY + ",offset=" + offset);
		// 0是顶部，后面那个是底部
		if (scrollY == 0 || scrollY == offset) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * 解决横向纵向滑动事件混淆屏蔽
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;
			// check Angle
			if (xDistance > yDistance) {
				return false;
			}
		}

		return super.onInterceptTouchEvent(ev);
	}
}

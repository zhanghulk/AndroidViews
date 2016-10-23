package com.slim.widget;

import java.util.Date;

import com.slim.widget.PaddingTopAsynTask.OnPaddingListener;
import com.xikang.android.slimcoach.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MListView extends ListView implements OnScrollListener {

	private static final String TAG = "MListView";
	private boolean debug = false;
	
	public final static int LOAD_DATA = 0;
	public final static int UPDATE_DATASET = 1;
	public final static int INIT_LISTVIEW = 2;
	public final static int LOAD_DATA_START = 3;
	public final static int LOADVIEW_UPDATE = 4;
	public final static int LOADVIEW_DONE = 5;
	public final static int REFRESH_DONE = 6;

	public final static int LOAD_AMOUNT = 30;

	public final static int STATE_RELEASE_To_REFRESH = 0;
	public final static int STATE_PULL_To_REFRESH = 1;
	public final static int STATE_REFRESHING = 2;
	public final static int STATE_REFRESH_DONE = 3;
	public final static int STATE_LOADING = 4;

	public interface OnSlideListener {
		public void onSlideLeft();
		public void onSlideRight();
	}
	
	public interface OnPullListener {
		public void onPulldownRefresh();
		public void onPullupLoadMore(View loadView);
	}
	
	public interface OnSoftInputStateListener {
		public void onShow(boolean isShow);
	}

	// header实际的padding的距离与界面上偏移距离(纵向手势滑动距离)的比例3
	private final static int RATIO = 3;
	int ratio = RATIO;

	Context context;
	private LayoutInflater inflater;

	private LinearLayout headView;

	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;
	LoadingView loadView;

	private RotateAnimation ArrowAnimation;
	private RotateAnimation arrowReverseAnimation;

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;
	boolean hideSoftInput = false;

	private int headContentWidth;
	private int headContentHeight;

	private int startY = 0;
	private int startX = 0;
	private int endX = 0;
	private int endY = 0;
	private int tempX = 0;
	private int tempY = 0;
	private int firstItemIndex;

	private int state;

	private boolean isBack;

	private OnPullListener onPullListener;
	private OnSlideListener mOnSlideListener;
	private OnSoftInputStateListener softInputListener;

	private boolean isRefreshable = false;
	private boolean isLoadable = false;
	private boolean refreshEnable = true;
	private boolean loadEnable = true;
	boolean isDragUpEvent = false;
	boolean headerRestoreAnimEnable = true;

	public MListView(Context context) {
		super(context);
		this.context = context;
		init(context);
	}

	public MListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(context);
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isRefreshEnable() {
		return refreshEnable;
	}

	public void setRefreshEnable(boolean refreshEnable) {
		this.refreshEnable = refreshEnable;
	}

	public boolean isLoadEnable() {
		return loadEnable;
	}

	public void setLoadEnable(boolean loadEnable) {
		this.loadEnable = loadEnable;
	}

	@SuppressLint("NewApi")
	private void init(Context context) {
		if (Build.VERSION.SDK_INT >= 9) {
			setOverScrollMode(OVER_SCROLL_NEVER);
		}
		setCacheColorHint(context.getResources().getColor(R.color.transparent));
		inflater = LayoutInflater.from(context);

		headView = (LinearLayout) inflater.inflate(R.layout.pull_listview_header, null);

		arrowImageView = (ImageView) headView.findViewById(R.id.head_arrowImageView);
		arrowImageView.setMinimumWidth(70);
		arrowImageView.setMinimumHeight(50);
		progressBar = (ProgressBar) headView.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView.findViewById(R.id.head_lastUpdatedTextView);

		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();

		log("width:" + headContentWidth + " height:"
				+ headContentHeight);

		addHeaderView(headView, null, false);
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();
		//bottom load frameLayout
		loadView = new LoadingView(context);
		loadView.setVisibility(View.GONE);
		addFooterView(loadView);
		setOnScrollListener(this);

		ArrowAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		ArrowAnimation.setInterpolator(new LinearInterpolator());
		ArrowAnimation.setDuration(500);
		ArrowAnimation.setFillAfter(true);

		arrowReverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		arrowReverseAnimation.setInterpolator(new LinearInterpolator());
		arrowReverseAnimation.setDuration(500);
		arrowReverseAnimation.setFillAfter(true);

		state = STATE_REFRESH_DONE;
		isRefreshable = false;
		isLoadable = false;
	}

	public void removeLoadingView(){
		removeFooterView(loadView);
	}

	public void onScroll(AbsListView arg0, int firstVisiableItem, int visibleItemCount,
			int totalItemCount) {
		firstItemIndex = firstVisiableItem;
		if (firstVisiableItem + visibleItemCount == totalItemCount) {
			isLoadable = true;
		} else {
			isLoadable = false;
		}
		isLoadable &= loadEnable;
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (isLoadable && scrollState == SCROLL_STATE_IDLE && isDragUpEvent) {
			isLoadable = false;
			onLoad();
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		isRefreshable &= refreshEnable;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstItemIndex == 0 && !isRecored) {
				isRecored = true;
				startX = (int) event.getX();
				startY = (int) event.getY();
				log("ACTION_DOWN startX= " + startX + ",startY= " + startY);
			}
			break;

		case MotionEvent.ACTION_UP:
			endX = (int) event.getX();
			endY = (int) event.getY();
			if (endY - startY > 20) {
				hideSoftInput = true;
			}
			int deltaX = endX - startX;
			log("ACTION_UP endX= " + endX + ",endY= " + endY + ",deltaX=" + deltaX);
			boolean isSlideEvent = isSlideEvent(startX, endX, startY, endY);
			if(isSlideEvent) {
				//横向滑动， 不响应纵向事件
				if(mOnSlideListener != null) {
					if(endX - startX > 0) {
						mOnSlideListener.onSlideLeft();
					} else if(deltaX < 0) {
						mOnSlideListener.onSlideRight();
					}
				}
				Log.i(TAG, "ACTION_UP: this is slide event");
			} else {
				int deltaY = endY - startY;
				isDragUpEvent = deltaY < -20;
				Log.i(TAG, "ACTION_UP: deltaY= " + deltaY + ",isDragUpEvent=" + isDragUpEvent);
				if (isRefreshable && state != STATE_REFRESHING && state != STATE_LOADING) {
					if (state == STATE_REFRESH_DONE) {
						// 什么都不做
					}
					if (state == STATE_PULL_To_REFRESH) {
						state = STATE_REFRESH_DONE;
						changeHeaderViewByState();

						log("由下拉刷新状态，到done状态");
					}
					if (state == STATE_RELEASE_To_REFRESH) {
						state = STATE_REFRESHING;
						changeHeaderViewByState();
						onRefresh();

						log("由松开刷新状态，到done状态");
					}
				}
			}
			isRecored = false;
			isBack = false;

			break;

		case MotionEvent.ACTION_MOVE:
			tempX = (int) event.getX();
			tempY = (int) event.getY();
			log("ACTION_MOVE tempX= " + tempX + ",tempY= " + tempY);
			if (tempY - startY > 20) {
				hideSoftInput = true;
			}
			boolean isSlideMoveEvent = isSlideEvent(startX, tempX, startY, tempY);
			if(isSlideMoveEvent) {
				//横向滑动,不响应纵向事件
				Log.i(TAG, "ACTION_MOVE: this is slide event");
				return false;
			}
		
			if (!isRecored && firstItemIndex == 0) {
				log("MotionEvent.ACTION_MOVE tempY==" + tempY);
				isRecored = true;
				startY = tempY;
				startX = tempX;
			}

			if (isRefreshable && state != STATE_REFRESHING && isRecored && state != STATE_LOADING) {
				// 保证在设置padding的过程中，当前的位置一直是在head，
				//否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动
				// 可以松手去刷新了
				if (state == STATE_RELEASE_To_REFRESH) {

					setSelection(0);

					// 往上推了，推到了屏幕足够掩盖head的程度，
					//但是还没有推到全部掩盖的地步
					if (((tempY - startY) / ratio < headContentHeight)
							&& (tempY - startY) > 0) {
						state = STATE_PULL_To_REFRESH;
						changeHeaderViewByState();

						log("由松开刷新状态转变到下拉刷新状态");
					}
					// 一下子推到顶了
					else if (tempY - startY <= 0) {
						state = STATE_REFRESH_DONE;
						changeHeaderViewByState();

						log("由松开刷新状态转变到done状态");
					}
					// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
					else {
						// 不用进行特别的操作，只用更新paddingTop的值就行了
					}
				}
				// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
				if (state == STATE_PULL_To_REFRESH) {

					setSelection(0);

					// 下拉到可以进入RELEASE_TO_REFRESH的状态
					if ((tempY - startY) / ratio >= headContentHeight) {
						state = STATE_RELEASE_To_REFRESH;
						isBack = true;
						changeHeaderViewByState();

						log("由done或者下拉刷新状态转变到松开刷新");
					}
					// 上推到顶了
					else if (tempY - startY <= 0) {
						state = STATE_REFRESH_DONE;
						changeHeaderViewByState();

						log("由DOne或者下拉刷新状态转变到done状态");
					}
				}

				// done状态下
				if (state == STATE_REFRESH_DONE) {
					if (tempY - startY > 0) {
						state = STATE_PULL_To_REFRESH;
						changeHeaderViewByState();
					}
				}

				// 更新headView的size
				if (state == STATE_PULL_To_REFRESH) {
					headView.setPadding(0, getHeadPaddingContentHeight(startY, tempY), 0, 0);
					//headView.setPadding(0, -1 * headContentHeight
					//		+ (tempY - startY) / RATIO, 0, 0);
				}

				// 更新headView的paddingTop
				if (state == STATE_RELEASE_To_REFRESH) {
					headView.setPadding(0, getHeadPaddingContentHeight(startY, tempY), 0, 0);
					//headView.setPadding(0, (tempY - startY) / RATIO
					//		- headContentHeight, 0, 0);
				}
			}

			break;
		}
		if (hideSoftInput && softInputListener != null) {
			softInputListener.onShow(false);
		}

		return super.onTouchEvent(event);
	}

	/**
	 * 计算header实际的padding的距离与界面上偏移距离(纵向手势滑动距离)的比例
	 * @param startY
	 * @param endY
	 * @return
	 */
	private int getHeadPaddingContentHeight(int startY, int endY) {
		return  (endY - startY) / ratio - headContentHeight;
	}
	
	private boolean isSlideEvent(int startX, int endX, int startY, int endY) {
		int deltaX = endX - startX;
		int deltaY = endY - startY;
		return Math.abs((deltaX)) > Math.abs((deltaY));
	}

	public void updateHeaderViewState(int state) {
		this.state = state;
		changeHeaderViewByState();
	}

	/**
	 * 当状态改变时候，调用该方法，以更新界面
	 */
	private void changeHeaderViewByState() {
		switch (state) {
		case STATE_RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(ArrowAnimation);

			tipsTextview.setText(R.string.release_ref);

			log("当前状态，松开刷新");
			break;
		case STATE_PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(arrowReverseAnimation);
			}
			tipsTextview.setText(R.string.pull_toref);
			break;

		case STATE_REFRESHING:
			refreshingHeader();
			log("当前状态,正在刷新...");
			break;
		case STATE_REFRESH_DONE:
			refreshDoneHeader();
			log("STATE_REFRESH_DONE，restore listview");
			break;
		}
	}

	private void refreshingHeader() {
		headView.setPadding(0, 0, 0, 0);
		progressBar.setVisibility(View.VISIBLE);
		arrowImageView.clearAnimation();
		arrowImageView.setVisibility(View.GONE);
		tipsTextview.setText(R.string.refreshing);
		lastUpdatedTextView.setVisibility(View.VISIBLE);
	}

	private void refreshDoneHeader() {
		if(headerRestoreAnimEnable) {
			OnPaddingListener onPaddingListener = new OnPaddingListener() {
				@Override
				public void onStartPadding(View view) {
					Log.i(TAG, "onStartPadding");
				}
				@Override
				public void onEndPadding(View view) {
					progressBar.setVisibility(View.GONE);
					arrowImageView.clearAnimation();
					arrowImageView.setImageResource(R.drawable.arrow_vertical);
					tipsTextview.setText(R.string.pull_toref);
					lastUpdatedTextView.setVisibility(View.VISIBLE);
				}
			};
			PaddingTopAsynTask task = new PaddingTopAsynTask(headView, headContentHeight);
			task.setOnPaddingListener(onPaddingListener);
			task.execute();
		} else {
			headView.setPadding(0, -1 * headContentHeight, 0, 0);
		}
	}

	public void setOnSoftInputListener(OnSoftInputStateListener softInputListener) {
		this.softInputListener = softInputListener;
	}

	public OnPullListener getOnPullListener() {
		return onPullListener;
	}

	public void setOnPullListener(OnPullListener onPullListener) {
		this.onPullListener = onPullListener;
		isLoadable = true;
		isRefreshable = true;
	}

	public OnSlideListener getOnSlideListener() {
		return mOnSlideListener;
	}

	public void setOnSlideListener(OnSlideListener onSlideListener) {
		this.mOnSlideListener = onSlideListener;
	}

	public boolean isHeaderRestoreAnimEnable() {
		return headerRestoreAnimEnable;
	}

	public void setHeaderRestoreAnimEnable(boolean headerRestoreAnimEnable) {
		this.headerRestoreAnimEnable = headerRestoreAnimEnable;
	}

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	private String refreshTimeText() {
		return context.getString(R.string.latest_update_time, new Date().toLocaleString());
	}

	public void onRefreshComplete() {
		state = STATE_REFRESH_DONE;
		String lastTime = refreshTimeText();
		lastUpdatedTextView.setText(lastTime);
		changeHeaderViewByState();
	}
	
	public void onLoadComplete() {
		setLoadViewVisibility(View.GONE);
		isLoadable = true;
	}

	private void onLoad() {
		if (onPullListener != null) {
			setLoadViewVisibility(View.VISIBLE);
			onPullListener.onPullupLoadMore(loadView);
		}
	}
	
	private void onRefresh() {
		if (onPullListener != null) {
			onPullListener.onPulldownRefresh();
		}
	}

	/**
	 * 此方法直接照搬自网络上的一个下拉刷新的demo，
	 * 此处是“估计”headView的width以及height
	 * @param child
	 */
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setAdapter(BaseAdapter adapter) {
		String lastTime = context.getString(R.string.latest_update_time, new Date().toLocaleString());
		lastUpdatedTextView.setText(lastTime);
		super.setAdapter(adapter);
	}

	public void setLoadViewText(String text) {
		loadView.setLoadText(text);
	}
	
	public void setLoadViewText(int resId) {
		loadView.setLoadText(resId);
	}
	
	public void setLoadViewVisibility(int visibility) {
		loadView.setVisibility(visibility);
	}
	
	public void setLoadProressVisibility(int visibility) {
		loadView.setProgressBarVisibility(visibility);
	}
	
	public void updateLoadView(int visibility, String text) {
		loadView.setVisibility(visibility);
		loadView.setLoadText(text);
	}

	public void updateLoadView(int visibility, int textResId) {
		loadView.setVisibility(visibility);
		loadView.setLoadText(textResId);
	}
	
	private void log(String msg) {
		if(debug) {
			Log.d(TAG, msg);
		}
	}
}

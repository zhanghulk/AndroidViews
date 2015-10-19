package com.custom.listfragment.demo;

import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
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
/**
 * 功能类似ListFragment,天机以下特性：
 * 1. 可下拉刷新,上拉加载，实现CustomListView.OnPullListener接口
 * 3. Slide切换，实现CustomListView.OnSlideListener接口
 * @author zhanghao 20141029
 */
public class CustomListView extends ListView implements OnScrollListener {
	public static enum RefreshState {
		PULL_TO_REFRESH, 
		RELEASE_TO_REFRESH, 
		REFRESHING, 
		REFRESH_DONE
    }

	public static enum LoadMode {
		NONE_LOAD, 
		AUTO_LOAD_MORE, 
		CLICK_LOAD_MORE
    }
		
	private static final String TAG = "CustomListView";
	private static final boolean DEBUG = true;
	private static final boolean VERBOSE = false;

	private final static int DRAG_EVENT_DELTA_XY = 20;
	boolean isDragUpEvent = false;
	// header实际的padding的距离与界面上偏移距离(纵向手势滑动距离)的比例3
	private final static int RATIO = 3;
	int ratio = RATIO;

	Context context;
	private LayoutInflater inflater;

	private View headView;
	private TextView headTextview;
	private TextView lastUpdatedTv;
	private ImageView headArrow;
	private ProgressBar headRefreshBar;

	private View footView;
	private TextView footTextview;
	private ProgressBar footLoadBar;

	private RotateAnimation arrowAnimation;
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

	private boolean isBack;

	private OnPullListener onPullListener;
	private OnSlideListener mOnSlideListener;
	private OnSoftInputStateListener softInputListener;

	//当前下拉刷新设否可用，如果正在刷就不能在往下拉
	private boolean refreshableFlag = true;
	//当前上拉加载是否可用，如果正加载就不能在往上拉
	private boolean loadableFlag = false;
	//是否启用下拉刷新功能
	private boolean isRefreshEnable = true;
	//当前拉动状态： RefreshState.*
	private RefreshState state = RefreshState.REFRESH_DONE;
	//上拉加载模式： LoadMode.*
	private LoadMode loadMode = LoadMode.NONE_LOAD;
	boolean loadingFlag = false;
	boolean headerRestoreAnimEnable = true;
	boolean latestUpdateTimeEnable = false;

	public CustomListView(Context context) {
		super(context);
		this.context = context;
		init(context);
	}

	public CustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(context);
	}

	@SuppressLint("NewApi")
	private void init(Context context) {
		if (Build.VERSION.SDK_INT >= 9) {
			setOverScrollMode(OVER_SCROLL_NEVER);
		}
		setCacheColorHint(context.getResources().getColor(android.R.color.transparent));
		inflater = LayoutInflater.from(context);

		initHeaderView();
		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();
		logv("Head view width:" + headContentWidth + " height:" + headContentHeight);
		addHeaderView(headView, null, false);
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();
		//set it in setLoadmode()
		initFooterView();
		//addFooterView();
		//setOnScrollListener(this);

		arrowAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		arrowAnimation.setInterpolator(new LinearInterpolator());
		arrowAnimation.setDuration(500);
		arrowAnimation.setFillAfter(true);

		arrowReverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		arrowReverseAnimation.setInterpolator(new LinearInterpolator());
		arrowReverseAnimation.setDuration(500);
		arrowReverseAnimation.setFillAfter(true);

		state = RefreshState.REFRESH_DONE;
		loadMode = LoadMode.NONE_LOAD;
	}

	public void initHeaderView() {
		headView = inflater.inflate(R.layout.pull_listview_header, null);
		headArrow = (ImageView) headView.findViewById(R.id.head_arrowImageView);
		headRefreshBar = (ProgressBar) headView.findViewById(R.id.head_progressBar);
		headTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTv = (TextView) headView.findViewById(R.id.head_lastUpdatedTextView);
		int visibility = View.GONE;
		if (latestUpdateTimeEnable) {
			setLastRefreshTimetext();
			visibility = View.VISIBLE;
		}
		lastUpdatedTv.setVisibility(visibility);
		
	}

	public void initFooterView() {
		AbsListView.LayoutParams llp = new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT, 
				AbsListView.LayoutParams.WRAP_CONTENT);
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(llp);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.CENTER);
		ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT); 
		footLoadBar = new ProgressBar(context);
		footLoadBar.setLayoutParams(vlp);
		footTextview = new TextView(context);
		footTextview.setLayoutParams(vlp);
		footTextview.setText("Load More Data...");
		footTextview.setTextAppearance(context, android.R.attr.textAppearanceSmall);
		footTextview.setPadding(20, 20, 20, 20);
		layout.addView(footLoadBar);
		layout.addView(footTextview);
		footView = layout;
	}

	public void addHeaderView(){
		if(headView != null) {
			addHeaderView(headView, null, false);
		}
	}

	public void addFooterView(){
		if(footView != null) {
			addFooterView(footView);
		}
	}

	public void removeHeaderView(){
		if(headView != null) {
			removeHeaderView(headView);
		}
	}

	public void removeFooterView(){
		if(footView != null) {
			removeFooterView(footView);
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisiableItem, int visibleItemCount,
			int totalItemCount) {
		if(isAutoLoadMode()) {
			firstItemIndex = firstVisiableItem;
			if (firstVisiableItem + visibleItemCount == totalItemCount) {
				loadableFlag = true;
			} else {
				loadableFlag = false;
			}
		} else {
			loadableFlag = false;
			logv("Can not Auto loadMode is " + loadMode);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		logd("loadMode= " + loadMode + "， loadableFlag= " + loadableFlag + "，isDragUpEvent= " + isDragUpEvent);
		if (scrollState == SCROLL_STATE_IDLE && loadableFlag && isAutoLoadMode() && isDragUpEvent) {
			logd("Excuting onLoad callback ...... ");
			onLoad();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		refreshableFlag &= isRefreshEnable;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstItemIndex == 0 && !isRecored) {
				isRecored = true;
				startX = (int) event.getX();
				startY = (int) event.getY();
				logv("ACTION_DOWN startX= " + startX + ",startY= " + startY);
			}
			break;

		case MotionEvent.ACTION_UP:
			endX = (int) event.getX();
			endY = (int) event.getY();
			if (endY - startY > DRAG_EVENT_DELTA_XY) {
				hideSoftInput = true;
			}
			int deltaX = endX - startX;
			logv("ACTION_UP endX= " + endX + ",endY= " + endY + ",deltaX=" + deltaX);
			boolean isSlideEvent = isSlideEvent(startX, endX, startY, endY);
			if(isSlideEvent) {
				//横向滑动， 不响应纵向事件
				if(mOnSlideListener != null) {
					if(deltaX > 0) {
						mOnSlideListener.onSlideLeft();
					} else if(deltaX < 0) {
						mOnSlideListener.onSlideRight();
					}
				}
				logd("ACTION_UP: this is slide event");
			} else {
				int deltaY = endY - startY;
				isDragUpEvent = deltaY < -DRAG_EVENT_DELTA_XY;
				logv("ACTION_UP: deltaY= " + deltaY + ",isDragUpEvent=" + isDragUpEvent);
				if (refreshableFlag && state != RefreshState.REFRESHING) {
					if (state == RefreshState.REFRESH_DONE) {
						// do nothing
					} else if (state == RefreshState.PULL_TO_REFRESH) {
						state = RefreshState.REFRESH_DONE;
						logd("由下拉刷新状态，到done状态");
						changeHeaderViewByState();
					} else if (state == RefreshState.RELEASE_TO_REFRESH) {
						state = RefreshState.REFRESHING;
						logd("由松开刷新状态，到refreshing...");
						changeHeaderViewByState();
						onRefresh();
					}
				}
			}
			isRecored = false;
			isBack = false;

			break;

		case MotionEvent.ACTION_MOVE:
			tempX = (int) event.getX();
			tempY = (int) event.getY();
			logv("ACTION_MOVE tempX= " + tempX + ",tempY= " + tempY);
			if (tempY - startY > DRAG_EVENT_DELTA_XY) {
				hideSoftInput = true;
			}
			boolean isSlideMoveEvent = isSlideEvent(startX, tempX, startY, tempY);
			if(isSlideMoveEvent) {
				//横向滑动,不响应纵向事件
				Log.i(TAG, "ACTION_MOVE: this is slide event");
				return false;
			}
		
			if (!isRecored && firstItemIndex == 0) {
				logd("MotionEvent.ACTION_MOVE tempY==" + tempY);
				isRecored = true;
				startY = tempY;
				startX = tempX;
			}

			if (refreshableFlag && state != RefreshState.REFRESHING && isRecored) {
				// 保证在设置padding的过程中，当前的位置一直是在head，
				//否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动
				// 可以松手去刷新了
				if (state == RefreshState.RELEASE_TO_REFRESH) {

					setSelection(0);

					// 往上推了，推到了屏幕足够掩盖head的程度，
					//但是还没有推到全部掩盖的地步
					if (((tempY - startY) / ratio < headContentHeight)
							&& (tempY - startY) > 0) {
						state = RefreshState.PULL_TO_REFRESH;
						changeHeaderViewByState();

						logd("由松开刷新状态转变到下拉刷新状态");
					}
					// 一下子推到顶了
					else if (tempY - startY <= 0) {
						state = RefreshState.REFRESH_DONE;
						changeHeaderViewByState();

						logd("由松开刷新状态转变到done状态");
					}
					// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
					else {
						// 不用进行特别的操作，只用更新paddingTop的值就行了
					}
				}
				// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
				if (state == RefreshState.PULL_TO_REFRESH) {

					setSelection(0);

					// 下拉到可以进入RELEASE_TO_REFRESH的状态
					if ((tempY - startY) / ratio >= headContentHeight) {
						state = RefreshState.RELEASE_TO_REFRESH;
						isBack = true;
						changeHeaderViewByState();

						logd("由done或者下拉刷新状态转变到松开刷新");
					}
					// 上推到顶了
					else if (tempY - startY <= 0) {
						state = RefreshState.REFRESH_DONE;
						changeHeaderViewByState();

						logd("由DOne或者下拉刷新状态转变到done状态");
					}
				}

				// done状态下
				if (state == RefreshState.REFRESH_DONE) {
					if (tempY - startY > 0) {
						state = RefreshState.PULL_TO_REFRESH;
						changeHeaderViewByState();
					}
				}

				// 更新headView的size
				if (state == RefreshState.PULL_TO_REFRESH) {
					headView.setPadding(0, getHeadPaddingContentHeight(startY, tempY), 0, 0);
				}

				// 更新headView的paddingTop
				if (state == RefreshState.RELEASE_TO_REFRESH) {
					headView.setPadding(0, getHeadPaddingContentHeight(startY, tempY), 0, 0);
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
		return deltaX > DRAG_EVENT_DELTA_XY &&  Math.abs((deltaX)) > Math.abs((deltaY));
	}

	/**
	 * 更新Header状态
	 * @param state PullState.*
	 */
	public void updateHeaderViewState(RefreshState state) {
		this.state = state;
		changeHeaderViewByState();
	}

	/**
	 * 当状态改变时候，调用该方法，以更新界面
	 */
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_TO_REFRESH:
			headArrow.setVisibility(View.VISIBLE);
			headRefreshBar.setVisibility(View.GONE);
			headTextview.setVisibility(View.VISIBLE);
			headArrow.clearAnimation();
			headArrow.startAnimation(arrowAnimation);
			headTextview.setText(R.string.release_ref);
			logd("当前状态,松开刷新...");
			break;
		case PULL_TO_REFRESH:
			headRefreshBar.setVisibility(View.GONE);
			headTextview.setVisibility(View.VISIBLE);
			headArrow.clearAnimation();
			headArrow.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				headArrow.clearAnimation();
				headArrow.startAnimation(arrowReverseAnimation);
			}
			headTextview.setText(R.string.pull_toref);
			logd("当前状态,下拉刷新...");
			break;

		case REFRESHING:
			refreshingHeader();
			logd("当前状态,正在刷新...");
			break;
		case REFRESH_DONE:
			refreshDoneHeader();
			logd("当前状态,刷新完成...");
			break;
		default:
			break;
		}
	}

	private void refreshingHeader() {
		headView.setPadding(0, 0, 0, 0);
		headRefreshBar.setVisibility(View.VISIBLE);
		headArrow.clearAnimation();
		headArrow.setVisibility(View.GONE);
		headTextview.setText(R.string.refreshing);
	}

	private void restoreHeader() {
		logd("STATE_REFRESH_DONE，restore listview");
		headRefreshBar.setVisibility(View.GONE);
		headArrow.clearAnimation();
		headArrow.setImageResource(R.drawable.arrow_vertical);
		headTextview.setText(R.string.pull_toref);
	}

	OnPaddingListener onPaddingListener = new OnPaddingListener() {
		@Override
		public void onStartPadding(View view) {
			logv("onStartPadding");
		}
		@Override
		public void onEndPadding(View view) {
			restoreHeader();
		}
	};

	private void refreshDoneHeader() {
		if(headerRestoreAnimEnable) {
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
		loadableFlag = true;
		refreshableFlag = true;
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

	public void onRefreshComplete() {
		setLastRefreshTimetext();
		state = RefreshState.REFRESH_DONE;
		logd("External call onRefreshComplete !!");
		changeHeaderViewByState();
	}
	
	private void setLastRefreshTimetext() {
		if(latestUpdateTimeEnable) {
			String time = new Date().toLocaleString();
			String timeText = context.getString(R.string.latest_update_time, time);
			lastUpdatedTv.setText(timeText);
		}
	}
	
	public void onLoadComplete() {
		if (isClickLoadMode()) {
			footLoadBar.setVisibility(View.GONE);
		} else {
			footView.setVisibility(View.GONE);
		}
		loadableFlag = true;
		loadingFlag = false;
	}

	private void onLoad() {
		if (onPullListener != null) {
			loadableFlag = false;
			loadingFlag = true;
			setFootViewVisibility(View.VISIBLE);
			onPullListener.onPullUpLoadMore(loadMode, footLoadBar, footTextview);
		}
	}

	private void onRefresh() {
		if (onPullListener != null) {
			onPullListener.onPullDownRefresh(headView);
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
		setLastRefreshTimetext();
		super.setAdapter(adapter);
	}

	public void setFootText(String text) {
		footTextview.setText(text);
	}

	public View getFootView() {
		return footView;
	}

	public TextView getFootTextview() {
		return footTextview;
	}

	public ProgressBar getFootLoadBar() {
		return footLoadBar;
	}

	public void setFootViewVisibility(int visibility) {
		footView.setVisibility(visibility);
		footLoadBar.setVisibility(visibility);
		footTextview.setVisibility(visibility);
	}

	public boolean isRefreshEnable() {
		return isRefreshEnable;
	}

	public LoadMode getLoadMode() {
		return loadMode;
	}

	/**
	 * 设置上拉加载模式NONE(default),  AUTO,  CLICK.
	 * @param loadMode: LoadMode.*
	 */
	public void setLoadMode(LoadMode loadMode) {
		this.loadMode = loadMode;
		switch (loadMode) {
		case NONE_LOAD:
			removeFooterView();
			disableLoadMode();
			break;
		case AUTO_LOAD_MORE://自动上拉模式： 上拉到底部时自动显示加载中
			addFooterView();
			disableLoadMode();
			loadableFlag = true;
			setOnScrollListener(CustomListView.this);
			break;
		case CLICK_LOAD_MORE://加载更多文字始终显示，但ProgressBar不显示
			addFooterView();
			loadableFlag = true;
			footView.setVisibility(View.VISIBLE);
			footTextview.setVisibility(View.VISIBLE);
			footLoadBar.setVisibility(View.GONE);
			footView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onLoad();
				}
			});
			break;
		default:
			break;
		}
	}

	private void disableLoadMode() {
		loadableFlag = false;
		setFootViewVisibility(View.GONE);
		footView.setOnClickListener(null);
	}

	public void setRefreshEnable(boolean refreshEnable) {
		this.isRefreshEnable = refreshEnable;
	}

	public boolean isLoadEnable() {
		return loadMode != LoadMode.NONE_LOAD;
	}

	public boolean isAutoLoadMode() {
		return loadMode == LoadMode.AUTO_LOAD_MORE;
	}

	public boolean isClickLoadMode() {
		return loadMode == LoadMode.CLICK_LOAD_MORE;
	}

	public View getHeadView() {
		return headView;
	}

	public ImageView getHeadArrow() {
		return headArrow;
	}

	public TextView getHeadTextview() {
		return headTextview;
	}

	public ProgressBar getHeadRefreshBar() {
		return headRefreshBar;
	}

	public void setHeadViewVisibility(int visibility) {
		headView.setVisibility(visibility);
	}
	
	public void updateLoadView(int visibility, String text) {
		footView.setVisibility(visibility);
		setFootText(text);
	}
	
	private void logd(String msg) {
		if(DEBUG) {
			Log.d(TAG, msg);
		}
	}

	private void logv(String msg) {
		if(VERBOSE) {
			Log.v(TAG, msg);
		}
	}

	private class PaddingTopAsynTask extends AsyncTask<Integer, Integer, Void> {
		private static final String TAG = "PaddingTopAsynTask";
		private static final boolean DEBUG = false;
		private final static int STEP = 3;// 步伐
		private final static int TIME = 3;// 休眠时间

		// 距离（该距离指的是：mHeadView的PaddingTop+mHeadView的高度，及默认位置状态.）
		private int distance;
		// 循环设置padding执行次数.
		private int number;
		// 时时padding距离.
		private int disPadding;
		private View view = null;
		private int viewHeight = 0;
		int count = 0;
		OnPaddingListener onPaddingListener;

		public PaddingTopAsynTask(View view, int viewHeight) {
			this.view = view;
			this.viewHeight = viewHeight;
		}

		public void setOnPaddingListener(OnPaddingListener onPaddingListener) {
			this.onPaddingListener = onPaddingListener;
		}

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				if (view == null) {
					throw new IllegalArgumentException("View is null !!");
				}
				if (viewHeight <= 0) {
					viewHeight = view.getMeasuredHeight();
				}
				distance = view.getPaddingTop() + Math.abs(viewHeight);

				// 获取循环次数.
				if (distance % STEP == 0) {
					number = distance / STEP;
				} else {
					number = distance / STEP + 1;
				}
				if(DEBUG) Log.i(TAG, "distance=" + distance + ",number=" + number
						+ "STEP=" + STEP + ",TIME=" + TIME);
				// 进行循环.
				for (int i = 0; i < number; i++) {
					Thread.sleep(TIME);
					publishProgress(STEP);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			disPadding = Math.max(view.getPaddingTop() - STEP, -1 * viewHeight);
			if(DEBUG) Log.i(TAG, "disPadding==" + disPadding + ",values: " + values[0]);
			if(view != null) {
				view.setPadding(0, disPadding, 0, 0);// 回归设置HeaderPadding
			}
			if(onPaddingListener != null) {
				if(count == 0) {
					onPaddingListener.onStartPadding(view);
				} else {
					count ++;
					if(count == number) {
						onPaddingListener.onEndPadding(view);
					}
				}
			}
		}

	}

	private interface OnPaddingListener {
		void onStartPadding(View view);
		void onEndPadding(View view);
	}

	public interface OnSlideListener {
		public void onSlideLeft();
		public void onSlideRight();
	}
	
	public interface OnPullListener {
		public void onPullDownRefresh(View headView);
		public void onPullUpLoadMore(LoadMode mode, ProgressBar footBar, TextView loadTextView);
	}
	
	public interface OnSoftInputStateListener {
		public void onShow(boolean isShow);
	}
}

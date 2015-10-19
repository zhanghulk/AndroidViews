CustomListView说明：可下拉刷新，上拉加载，横向滑动接口回调，
/**
 * 功能类似ListView,天机以下特性：
 * 1. 可下拉刷新,上拉加载，实现CustomListView.OnPullListener接口
 * 3. Slide切换，实现CustomListView.OnSlideListener接口
 * @author zhanghao 20141029
 */
（需要自定义享用的提示文字）
<string name="refreshing">refreshing</string>
    <string name="loading">loading</string>
    <string name="release_ref">release to refresh</string>
    <string name="pull_toref">pull down to refresh</string>
    <string name="latest_update_time">latest update time: <xliff:g id="UPDATE_TIME">%1$s</xliff:g></string>

代码太长，请参看附件源代码

CustomListFragment功能说明：
具有ListFragment所有功能，天机下拉刷新和上拉加载功能，还有Slide左右滑动切换回调接口；

用法介绍：
使用时可用自己的Fragment继承CustomListFragment，重写onCreateView函数：
1. 直接返回custom_list_fragment_content.xml布局Layout, 
比如
@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        View root = inflater.inflate(R.layout.custom_list_fragment_content, null);
        return root;
    }

2. 需要外加控件自定义布局myfragment.xml:
(包含custom_list_fragment_content.xml)
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >
    <TextView
        android:id="@+id/last_synctime_message"
        android:layout_width="match_parent"
        android:layout_height="@dimen/error_message_height"
        android:paddingLeft="8dip"
        android:paddingRight="8dip"
        android:gravity="right|center_vertical"
        android:text="@string/no_last_synctime"
        android:singleLine="true"
        android:ellipsize="end"
        android:background="@color/synctime_bar_background"
        android:visibility="gone"
        />
    <View
        android:layout_width="match_parent"
        android:layout_height="1px"
        android:background="?android:attr/listDivider" />
  <!-- 包含custom_list_fragment_content.xml -->
    <include
        layout="@layout/custom_list_fragment_content"
        android:id="@+id/list_panel"
        android:layout_width="match_parent"
        android:layout_height="0dip"
        android:layout_weight="1"
        />

    <!-- Message list error overlays are dynamically inserted here -->

</LinearLayout>
然后，
public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.myfragment, null);
        return root;
    }

有兴趣的同学可以看看源代码：
package com.custom.listfragment.demo;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 功能类似ListFragment,天机以下特性：
 * 1. 可下拉刷新，实现CustomListView.OnPullListener接口
 * 2. 上拉加载（默认关闭）
 * 3. Slide切换，实现CustomListView.OnSlideListener接口
 * @author zhanghao 20141029
 */
@SuppressLint("NewApi") public class CustomListFragment extends Fragment {
	final private Handler mHandler = new Handler();

	final private Runnable mRequestFocus = new Runnable() {
	    public void run() {
	        mList.focusableViewAvailable(mList);
	    }
	};

	final private AdapterView.OnItemClickListener mOnClickListener
	        = new AdapterView.OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        onListItemClick((ListView)parent, v, position, id);
	    }
	};

	ListAdapter mAdapter;
	CustomListView mList;
	View mEmptyView;
	TextView mStandardEmptyView;
	View mProgressContainer;
	View mListContainer;
	CharSequence mEmptyText;
	boolean mListShown;

	public CustomListFragment() {
	}

	/**
	 * Provide default implementation to return a simple list view.  Subclasses
	 * can override to replace with their own layout.  If doing so, the
	 * returned view hierarchy <em>must</em> have a ListView whose id
	 * is {@link android.R.id#list android.R.id.list} and can optionally
	 * have a sibling view id {@link android.R.id#empty android.R.id.empty}
	 * that is to be shown when the list is empty.
	 * 
	 * <p>If you are overriding this method with your own custom content,
	 * consider including the standard layout {@link android.R.layout#list_content}
	 * in your layout file, so that you continue to retain all of the standard
	 * behavior of ListFragment.  In particular, this is currently the only
	 * way to have the built-in indeterminant progress state be shown.
	 */

	/**
	 * Attach to list view once the view hierarchy has been created.
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
	    super.onViewCreated(view, savedInstanceState);
	    ensureList();
	}

	/**
	 * Detach from list view.
	 */
	public void onDestroyView() {
	    mHandler.removeCallbacks(mRequestFocus);
	    mList = null;
	    mListShown = false;
	    mEmptyView = mProgressContainer = mListContainer = null;
	    mStandardEmptyView = null;
	    super.onDestroyView();
	}

	/**
	 * This method will be called when an item in the list is selected.
	 * Subclasses should override. Subclasses can call
	 * getListView().getItemAtPosition(position) if they need to access the
	 * data associated with the selected item.
	 *
	 * @param l The ListView where the click happened
	 * @param v The view that was clicked within the ListView
	 * @param position The position of the view in the list
	 * @param id The row id of the item that was clicked
	 */
	public void onListItemClick(ListView l, View v, int position, long id) {
	}

	/**
	 * Provide the cursor for the list view.
	 */
	public void setListAdapter(ListAdapter adapter) {
	    boolean hadAdapter = mAdapter != null;
	    mAdapter = adapter;
	    if (mList != null) {
	        mList.setAdapter(adapter);
	        if (!mListShown && !hadAdapter) {
	            // The list was hidden, and previously didn't have an
	            // adapter.  It is now time to show it.
	            setListShown(true, getView().getWindowToken() != null);
	        }
	    }
	}

	/**
	 * Set the currently selected list item to the specified
	 * position with the adapter's data
	 *
	 * @param position
	 */
	public void setSelection(int position) {
	    ensureList();
	    mList.setSelection(position);
	}

	/**
	 * Get the position of the currently selected list item.
	 */
	public int getSelectedItemPosition() {
	    ensureList();
	    return mList.getSelectedItemPosition();
	}

	/**
	 * Get the cursor row ID of the currently selected list item.
	 */
	public long getSelectedItemId() {
	    ensureList();
	    return mList.getSelectedItemId();
	}

	/**
	 * Get the activity's custom list view widget.
	 */
	public CustomListView getListView() {
	    ensureList();
	    return mList;
	}

	/**
	 * The default content for a ListFragment has a TextView that can
	 * be shown when the list is empty.  If you would like to have it
	 * shown, call this method to supply the text it should use.
	 */
	public void setEmptyText(CharSequence text) {
	    ensureList();
	    if (mStandardEmptyView == null) {
	        throw new IllegalStateException("Can't be used with a custom content view");
	    }
	    mStandardEmptyView.setText(text);
	    if (mEmptyText == null) {
	        mList.setEmptyView(mStandardEmptyView);
	    }
	    mEmptyText = text;
	}

	/**
	 * Control whether the list is being displayed.  You can make it not
	 * displayed if you are waiting for the initial data to show in it.  During
	 * this time an indeterminant progress indicator will be shown instead.
	 * 
	 * <p>Applications do not normally need to use this themselves.  The default
	 * behavior of ListFragment is to start with the list not being shown, only
	 * showing it once an adapter is given with {@link #setListAdapter(ListAdapter)}.
	 * If the list at that point had not been shown, when it does get shown
	 * it will be do without the user ever seeing the hidden state.
	 * 
	 * @param shown If true, the list view is shown; if false, the progress
	 * indicator.  The initial value is true.
	 */
	public void setListShown(boolean shown) {
	    setListShown(shown, true);
	}

	/**
	 * Like {@link #setListShown(boolean)}, but no animation is used when
	 * transitioning from the previous state.
	 */
	public void setListShownNoAnimation(boolean shown) {
	    setListShown(shown, false);
	}

	/**
	 * Control whether the list is being displayed.  You can make it not
	 * displayed if you are waiting for the initial data to show in it.  During
	 * this time an indeterminant progress indicator will be shown instead.
	 * 
	 * @param shown If true, the list view is shown; if false, the progress
	 * indicator.  The initial value is true.
	 * @param animate If true, an animation will be used to transition to the
	 * new state.
	 */
	private void setListShown(boolean shown, boolean animate) {
	    ensureList();
	    if (mProgressContainer == null) {
	        throw new IllegalStateException("Can't be used with a custom content view");
	    }
	    if (mListShown == shown) {
	        return;
	    }
	    mListShown = shown;
	    if (shown) {
	        if (animate) {
	            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_out));
	            mListContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_in));
	        } else {
	            mProgressContainer.clearAnimation();
	            mListContainer.clearAnimation();
	        }
	        mProgressContainer.setVisibility(View.GONE);
	        mListContainer.setVisibility(View.VISIBLE);
	    } else {
	        if (animate) {
	            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_in));
	            mListContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_out));
	        } else {
	            mProgressContainer.clearAnimation();
	            mListContainer.clearAnimation();
	        }
	        mProgressContainer.setVisibility(View.VISIBLE);
	        mListContainer.setVisibility(View.GONE);
	    }
	}

	/**
	 * Get the ListAdapter associated with this activity's ListView.
	 */
	public ListAdapter getListAdapter() {
	    return mAdapter;
	}

	private void ensureList() {
	    if (mList != null) {
	        return;
	    }
	    View root = getView();
	    if (root == null) {
	        throw new IllegalStateException("Content view not yet created");
	    }
	    if (root instanceof CustomListView) {
	        mList = (CustomListView)root;
	    } else {
	        mStandardEmptyView = (TextView)root.findViewById(android.R.id.empty);
	        if (mStandardEmptyView == null) {
	            mEmptyView = root.findViewById(android.R.id.empty);
	        } else {
	            mStandardEmptyView.setVisibility(View.GONE);
	        }
	        mProgressContainer = root.findViewById(R.id.progressContainer);
	        mListContainer = root.findViewById(R.id.listContainer);
	        View rawListView = root.findViewById(android.R.id.list);
	        if (!(rawListView instanceof ListView)) {
	            if (rawListView == null) {
	                throw new RuntimeException(
	                        "Your content must have a ListView whose id attribute is " +
	                        "'android.R.id.list'");
	            }
	            throw new RuntimeException(
	                    "Content has view with id attribute 'android.R.id.list' "
	                    + "that is not a ListView class");
	        }
	        mList = (CustomListView)rawListView;
	        if (mEmptyView != null) {
	            mList.setEmptyView(mEmptyView);
	        } else if (mEmptyText != null) {
	            mStandardEmptyView.setText(mEmptyText);
	            mList.setEmptyView(mStandardEmptyView);
	        }
	    }
	    mListShown = true;
	    mList.setOnItemClickListener(mOnClickListener);
	    if (mAdapter != null) {
	        ListAdapter adapter = mAdapter;
	        mAdapter = null;
	        setListAdapter(adapter);
	    } else {
	        // We are starting without an adapter, so assume we won't
	        // have our data right away and start with the progress indicator.
	        if (mProgressContainer != null) {
	            setListShown(false, false);
	        }
	    }
	    mHandler.post(mRequestFocus);
	}

	public void setListViewOnPullListener(CustomListView.OnPullListener onPullListener) {
		mList.setOnPullListener(onPullListener);
	}

	public void setListViewOnSlideListener(CustomListView.OnSlideListener onSlideListener) {
		mList.setOnSlideListener(onSlideListener);
	}

	public void onRefrshComplete() {
		mList.onRefreshComplete();
	}

	public void onLoadComplete() {
		mList.onLoadComplete();
	}

	public void setPullUpLoadMode(CustomListView.LoadMode loadMode) {
		mList.setLoadMode(loadMode);
	}
}


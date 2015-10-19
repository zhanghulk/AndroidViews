package com.custom.listfragment.demo;

import com.custom.listfragment.demo.CustomListView.LoadMode;
import com.custom.listfragment.demo.CustomListView.OnPullListener;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DetailsFragment extends CustomListFragment implements OnPullListener {
    private static final String TAG = "DetailsFragment";

    Handler mHandler = new Handler();
	/**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static DetailsFragment newInstance(int index) {
        DetailsFragment f = new DetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

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
   
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	setListViewOnPullListener(this);
		setPullUpLoadMode(CustomListView.LoadMode.AUTO_LOAD_MORE);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		int titleIndex = getShownIndex();
		Log.i(TAG, "titleIndex == " + titleIndex);
		setListAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				DataGenerator.getDetailsDataSet(titleIndex)));
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	int headerCount = l.getHeaderViewsCount();
    	int checkedCount = l.getCheckedItemCount();
    	Log.i(TAG, "onListItemClick position=" + position + ", checkedCount=" + checkedCount + ", headerCount=" + headerCount);
    	SparseBooleanArray checkedPositions= l.getCheckedItemPositions();
    	log(checkedPositions);
    }
    
    private void log(SparseBooleanArray checkedPositions) {
    	for (int i = 0; i < checkedPositions.size(); i++) {
			Log.d(TAG, "Checked key=" + checkedPositions.keyAt(i) + ", value=" + checkedPositions.valueAt(i));
		}
	}

	private void refreshData() {
		try {
			Thread.sleep(1 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mHandler.post(new Runnable() {
			public void run() {
				onRefrshComplete();
			}
		});
	}
	
	private void loadData() {
		try {
			Thread.sleep(2 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mHandler.post(new Runnable() {
			public void run() {
				onLoadComplete();
			}
		});
	}

	@Override
	public void onPullDownRefresh(View headView) {
		new Thread(new Runnable() {
			public void run() {
				refreshData();
			}
		}).start();
	}

	@Override
	public void onPullUpLoadMore(LoadMode mode, ProgressBar footBar,
			TextView loadTextView) {
		new Thread(new Runnable() {
			public void run() {
				loadData();
			}
		}).start();
	}

}
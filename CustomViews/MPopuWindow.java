package com.slim.widget;

import java.util.List;

import com.slim.interfaces.OnSelectListener;
import com.xikang.android.slimcoach.R;
import com.xikang.android.slimcoach.adapter.AbsAdapter;
import com.xikang.android.slimcoach.adapter.TextItemAdapter;
import com.xikang.android.slimcoach.cfg.DeviceConf;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MPopuWindow extends PopupWindow implements OnItemClickListener {

	Context context;
	View contentView;
	ListView listView;
	AbsAdapter<String> adapter = null;
	OnSelectListener mOnSelectListener;

	public MPopuWindow(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public MPopuWindow(Context context, View view) {
		super(view);
		this.context = context;
		init();
	}

	public MPopuWindow(Context context, int width, int height) {
		super(width, height);
		this.context = context;
		init();
	}

	public MPopuWindow(Context context, View contentView, int width, int height) {
		super(contentView, width, height);
		this.context = context;
		init();
	}

	private void init() {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(R.layout.popu_list_layout, null);
		listView = (ListView) contentView.findViewById(R.id.popu_listview);
		//int width = context.getResources().getDimensionPixelSize(R.dimen.popu_w);
		//int height = context.getResources().getDimensionPixelSize(R.dimen.popu_h);
		int width = (DeviceConf.getWidth()  * 2) / 5;
		int height = DeviceConf.getHeight() / 2;
		initPopu(contentView, width, height, true, true, new BitmapDrawable());
		adapter = new TextItemAdapter(context);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	public void setView(View contentView, int width, int height) {
		this.contentView = contentView;
		setContentView(contentView);
		setWidth(width);
		setHeight(height);
	}

	public void initPopu(View contentView, int width, int height,
			boolean focusable, boolean outsideTouchable, BitmapDrawable drawable) {
		if(contentView != null) {
			setContentView(contentView);
		}
		if(width > 0) {
			setWidth(width);
		}
		if(height > 0) {
			setHeight(height);
		}
		setPopuProperty(focusable, outsideTouchable, drawable);
	}

	public void setPopuProperty(boolean focusable, boolean outsideTouchable,
			BitmapDrawable drawable) {
		setFocusable(focusable);
		setOutsideTouchable(outsideTouchable);
		setBackgroundDrawable(drawable);
	}

	public OnSelectListener getOnSelectListener() {
		return mOnSelectListener;
	}

	public void setOnSelectListener(OnSelectListener onSelectListener) {
		this.mOnSelectListener = onSelectListener;
	}

	public ListView getListView() {
		return listView;
	}

	public void setListView(ListView listView) {
		this.listView = listView;
	}

	public AbsAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(AbsAdapter adapter) {
		this.adapter = adapter;
		listView.setAdapter(adapter);
	}

	public List<String> getListData() {
		return adapter.getDataSet();
	}

	public void setListData(List<String> listData) {
		adapter.setDataSet(listData);
	}
	
	public void updateListData(List<String> listData) {
		adapter.updateDataSet(listData);
	}
	
	public void updateListData(String[] arrayData) {
		adapter.updateDataSet(arrayData);
	}

	public void show(View parent) {
		showAsDropDown(parent);
	}

	public void show(View parent, int width, int height) {
		setView(contentView, width, height);
		showAsDropDown(parent);
	}

	public void show(View parent, View contentView, int width, int height) {
		setView(contentView, width, height);
		showAsDropDown(parent);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		int location = position - listView.getHeaderViewsCount();
		if (adapter != null && !adapter.isEmpty()) {
			if(mOnSelectListener != null) {
				mOnSelectListener.onSelect(view, position, (int) id,
						adapter.getItem(location));
			} else {
				Toast.makeText(context, adapter.getItem(location), Toast.LENGTH_LONG).show();
			}
		}
		dismiss();
	}
}

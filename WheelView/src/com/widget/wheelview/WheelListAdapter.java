package com.widget.wheelview;

import java.util.List;

import com.widget.wheelview.WheelView.WheelAdapter;


/**
 * The simple List wheel adapter
 * 
 * @param <T>
 *            the element type
 */
public class WheelListAdapter<T> implements WheelAdapter<T> {

	/** The default data length */
	public static final int DEFAULT_LENGTH = -1;

	// data
	private List<T> data;
	// private T data[];
	// length
	private int length;

	/**
	 * Constructor
	 * 
	 * @param data
	 *            the data
	 * @param length
	 *            the max data length
	 */
	public WheelListAdapter(List<T> data, int length) {
		this.data = data;
		this.length = length;
	}

	/**
	 * Contructor
	 * 
	 * @param data
	 *            the data
	 */
	public WheelListAdapter(List<T> data) {
		this(data, DEFAULT_LENGTH);
	}

	public String getItem(int index) {
		if (index >= 0 && data != null && index < data.size()) {
			return data.get(index).toString();
		}
		return null;
	}

	public int getItemsCount() {
		if (data == null) {
			//Log.e("", "getItemsCount :data is null ");
			return 0;
		}
		return data.size();
	}

	public int getMaximumLength() {
		return length;
	}

	public List<T> getDataSet() {
		return data;
	}

	public void setDataSet(List<T> data) {
		this.data = data;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
}

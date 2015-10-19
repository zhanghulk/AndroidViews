package com.widget.wheelview;

import java.util.ArrayList;
import java.util.List;

import com.widget.wheelview.WheelView.WheelAdapter;


/**
 * The simple Array wheel adapter
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> implements WheelAdapter<T> {
	
	/** The default data length */
	public static final int DEFAULT_LENGTH = -1;
	
	// data
	private T[] data;
	// length
	private int length;

	/**
	 * Constructor
	 * @param data the data
	 * @param length the max data length
	 */
	public ArrayWheelAdapter(T items[], int length) {
		this.data = items;
		this.length = length;
	}
	
	/**
	 * Contructor
	 * @param data the data
	 */
	public ArrayWheelAdapter(T items[]) {
		this(items, DEFAULT_LENGTH);
	}

	public String getItem(int index) {
		if (index >= 0 && index < data.length) {
			return data[index].toString();
		}
		return null;
	}

	public int getItemsCount() {
		return data.length;
	}

	public int getMaximumLength() {
		return length;
	}

	public void setData(T[] data) {
		this.data = data;
	}
	
	public T[] getData() {
		return this.data;
	}

	@Override
	public void setDataSet(List<T> data) {
		throw new IllegalArgumentException("This function is invalid, please call setData(T[] data) ");
	}

	@Override
	public List<T> getDataSet() {
		List<T> data = new ArrayList<T>();
		for (T t : data) {
			data.add(t);
		}
		return data;
	}

}

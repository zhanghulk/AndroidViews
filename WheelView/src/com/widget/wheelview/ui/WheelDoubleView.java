package com.widget.wheelview.ui;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.widget.wheelview.R;
import com.widget.wheelview.WheelListAdapter;
import com.widget.wheelview.WheelView;
import com.widget.wheelview.R.id;
import com.widget.wheelview.R.layout;
import com.widget.wheelview.WheelView.OnWheelChangedListener;
import com.widget.wheelview.WheelView.OnWheelScrollListener;
import com.widget.wheelview.WheelView.WheelAdapter;

public class WheelDoubleView<T> extends LinearLayout implements
		OnWheelChangedListener, OnWheelScrollListener {
    public enum WheelTag {
        WHEEL_LEFT, WHEEL_RIGHT
    }

    public interface OnWheelDoubleChangedListener {
        void onDoubleChanged(WheelTag Wheel, int leftItemId, int rightItemId, String leftText, String rightText);
    }

    private static final String TAG = "WheelDoubleView";

	WheelView leftWheel, rightWheel;
	//T leftValue, rightValue;
	int leftItemId, rightItemId;
	List<T> leftData, rightData;
	WheelAdapter<T> leftAdapter = null;
	WheelAdapter<T> rightAdapter = null;
	OnWheelDoubleChangedListener onWheelDoubleChangedListener;
    private boolean debug;
    //selected item text
    String leftText = null;
    String rightText = null;

	public WheelDoubleView(Context context) {
		super(context);
		init();
	}

	public WheelDoubleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	interface valueChangeInterface {
		public void onValueChanged();
	}

	void init() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.wheel_double_view, this);
		leftWheel = (WheelView) view.findViewById(R.id.wheel_left);
		rightWheel = (WheelView) view.findViewById(R.id.wheel_right);
		leftWheel.addChangingListener(this);
		rightWheel.addChangingListener(this);
		leftWheel.addScrollingListener(this);
		rightWheel.addScrollingListener(this);
		leftWheel.setCyclic(false);
		rightWheel.setCyclic(false);
		rightWheel.setBackground(0);
		leftWheel.setBackground(0);
		/*rightWheel.setCenterDrawable(R.drawable.wheel_three_right);
		leftWheel.setCenterDrawable(R.drawable.wheel_three_left);*/
		leftAdapter = new WheelListAdapter<T>(leftData);
		leftWheel.setAdapter(leftAdapter);
		rightAdapter = new WheelListAdapter<T>(rightData);
		rightWheel.setAdapter(rightAdapter);
		rightWheel.setCurrentItem(0);
		leftWheel.setCurrentItem(0);
		setVisibleItems(5);
	}

	public OnWheelDoubleChangedListener getOnWheelDoubleChangedListener() {
        return onWheelDoubleChangedListener;
    }

    public void setOnWheelDoubleChangedListener(
            OnWheelDoubleChangedListener onWheelDoubleChangedListener) {
        this.onWheelDoubleChangedListener = onWheelDoubleChangedListener;
    }

    public void setLeftLabel(String label) {
		leftWheel.setLabel(label);
	}

	public void setRightLabel(String label) {
		rightWheel.setLabel(label);
	}

	public void setLeftLabel2(String label2) {
		leftWheel.setLabel2(label2);
	}

	public void setRightLabel2(String label2) {
		rightWheel.setLabel2(label2);
	}

	public WheelView getLeftWheel() {
		return leftWheel;
	}

	public void setLeftWheel(WheelView leftWheel) {
		this.leftWheel = leftWheel;
	}

	public WheelView getRightWheel() {
		return rightWheel;
	}

	public void setRightWheel(WheelView rightWheel) {
		this.rightWheel = rightWheel;
	}

	public List<T> getLeftData() {
		return leftData;
	}

	public List<T> getRightData() {
		return rightData;
	}

	public WheelAdapter<T> getRightAdapter() {
		return rightAdapter;
	}

	/**
	 * @param leftData
	 *            the leftData to set
	 */
	public void setLeftData(List<T> leftData) {
		setLeftData(leftData, 0);
	}

	/**
	 * @param rightData
	 *            the rightData to set
	 */
	public void setRightData(List<T> rightData) {
		setRightData(rightData, 0);
	}
	
	/**
	 * @param leftData
	 *            the leftData to set
	 */
	public void setLeftData(List<T> leftData, int currentItem) {
		if (leftData == null) return;
		this.leftData = leftData;
		leftAdapter.setDataSet(leftData);
		leftWheel.setAdapter(leftAdapter);
		leftWheel.setCurrentItem(currentItem);
		setRightText(currentItem);
	}

	/**
	 * @param rightData
	 *            the rightData to set
	 */
	public void setRightData(List<T> rightData, int currentItem) {
		if (rightData == null)
			return;
		this.rightData = rightData;
		rightAdapter.setDataSet(rightData);
		rightWheel.setAdapter(rightAdapter);
		rightWheel.setCurrentItem(currentItem);
		setLeftText(currentItem);
	}

	public void setLeftText(int itemId) {
	    try {
	        leftText = String.valueOf(leftData.get(itemId));
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

	public void setRightText(int itemId) {
        try {
            rightText = String.valueOf(rightData.get(itemId));
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

	/**
	 * @return the adapterLeft
	 */
	public WheelAdapter<T> getLeftAdapter() {
		return leftWheel.getAdapter();
	}

	/**
	 * @param adapterLeft
	 *            the adapterLeft to set
	 */
	public void setLeftAdapter(WheelAdapter<T> adapter) {
		leftWheel.setAdapter(adapter);
		leftData = adapter.getDataSet();
		setLeftText(0);
		rightItemId = 0;
		notifyCallback(WheelTag.WHEEL_RIGHT);
	}

	/**
	 * @return the adapterRight
	 */
	public WheelAdapter<T> getARightdapter() {
		return rightWheel.getAdapter();
	}

	/**
	 * @param adapterRight
	 *            the adapterRight to set
	 */
	public void setRightAdapter(WheelAdapter<T> adapter) {
		rightWheel.setAdapter(adapter);
		rightData = adapter.getDataSet();
		setRightText(0);
		rightItemId = 0;
		notifyCallback(WheelTag.WHEEL_RIGHT);
	}

	/**
	 * @return the currentItemLeft
	 */
	public int getLeftCurrentItem() {
		return leftWheel.getCurrentItem();
	}

	/**
	 * @param currentItem
	 *            the currentItemLeft to set
	 */
	public void setLeftCurrentItem(int currentItem) {
		leftWheel.setCurrentItem(currentItem);
	}

	/**
	 * @return the currentItemRight
	 */
	public int getRightCurrentItem() {
		return rightWheel.getCurrentItem();
	}

	/**
	 * @param currentItem
	 *            the currentItemRight to set
	 */
	public void setRightCurrentItem(int currentItem) {
		rightWheel.setCurrentItem(currentItem);
	}

	public void setVisibleLeftItems(int visibleItems) {
		leftWheel.setVisibleItems(visibleItems);
	}

	public void setVisibleRightItems(int visibleItems) {
		rightWheel.setVisibleItems(visibleItems);
	}

	public void setVisibleItems(int visibleItems) {
		leftWheel.setVisibleItems(visibleItems);
		rightWheel.setVisibleItems(visibleItems);
	}

	public void setItemHeight(int height) {
		leftWheel.setItemHeight(height);
		rightWheel.setItemHeight(height);
	}
	
	public void setWheelBackground(int bgresid) {
		leftWheel.setBackground(bgresid);
		rightWheel.setBackground(bgresid);
	}

	public void setCurrentItemBg(int valResid) {
		leftWheel.setCurrentItemBg(valResid);
		rightWheel.setCurrentItemBg(valResid);
	}
	
	public void setLeftCurrentItemBg(int valResid) {
		leftWheel.setCurrentItemBg(valResid);
	}
	
	public void setRightCurrentItemBg(int valResid) {
		rightWheel.setCenterDrawable(valResid);
	}

	public WheelView getWheelView(String which) {
		if ("left".equals(which)) {
			return leftWheel;
		} else if ("right".equals(which)) {
			return rightWheel;
		}
		return null;
	}

	@Override
	public void onChanged(WheelView wheel, int oldId, int newId, String currentValue) {
	    boolean changed = false;
	    WheelTag wheelTag = null;
	    if(wheel.getId() == R.id.wheel_left) {
	        leftItemId = newId;
            wheelTag = WheelTag.WHEEL_LEFT;
            leftText = currentValue;
            changed = true;
	    } else if(wheel.getId() == R.id.wheel_right) {
	        rightItemId = newId;
            wheelTag = WheelTag.WHEEL_RIGHT;
            rightText = currentValue;
            changed = true;
	    }
	    if(changed) {
	        notifyCallback(wheelTag);
	    }
	}

	private void notifyCallback(WheelTag wheelTag) {
	    if(onWheelDoubleChangedListener != null) {
            try {
                if(leftText == null) {
                    setLeftText(0);
                }
                if(rightText == null) {
                    setRightText(0);
                }
            } catch (Exception e) {
            }
            onWheelDoubleChangedListener.onDoubleChanged(wheelTag, leftItemId, rightItemId, leftText, rightText);
        }
    }

	@Override
	public void onScrollingStarted(WheelView wheel) {
	    if(debug) Log.i(TAG, "onScrollingStarted: " + wheel);
	}

	@Override
	public void onScrollingFinished(WheelView wheel) {
	    if(debug) Log.i(TAG, "onScrollingFinished: " + wheel);
	}
}

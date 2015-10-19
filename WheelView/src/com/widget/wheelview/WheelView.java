package com.widget.wheelview;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Numeric wheel view.
 *
 * @author huiych
 */
@SuppressLint({
        "FloatMath", "HandlerLeak"
})
public class WheelView extends View {

    /** Scrolling duration */
    private static final int SCROLLING_DURATION = 400;

    /** Minimum delta for scrolling */
    private static final int MIN_DELTA_FOR_SCROLLING = 1;

    /** Current value & label text color */
    private int currentValueTextColor = Color.DKGRAY;

    /** Items text color */
	private int itemTextColor = Color.LTGRAY;

    /** Top and bottom shadows colors */
    private static final int[] SHADOWS_COLORS = new int[] {
            0xFF111111,
            0x00AAAAAA, 0x00AAAAAA
    };

    /** Text size */
    private int textSize = 50;

    /** Top and bottom items offset (to hide that) */
    private int itemOffset = textSize / TEXTSIZE_ITEMOFFSET_RATIO;// textSize/5

    /** Additional width for items layout */
    private int additionalItemsSpace = 20;
    /** The ratio of textSize/itemOffset */
    private static final int TEXTSIZE_ITEMOFFSET_RATIO = 3;

    /** Label offset */
    private static final int LABEL_OFFSET = 5;

    /** Left and right padding value */
    private static final int PADDING = 5;

    /** Default count of visible items */
    private static final int DEF_VISIBLE_ITEMS = 5;

    // Wheel Values
    private WheelAdapter adapter = null;
    private int currentItem = 0;

    // Widths
    private int itemsWidth = 0;
    private int labelWidth = 0;
    private int labelWidth2 = 0;

    // Count of visible items
    private int visibleItems = WheelView.DEF_VISIBLE_ITEMS;

    // Item height
    private static final int ADDITIONAL_ITEM_HEIGHT = 40;
    private int itemHeight = 0;
    /** Additional items height (is added to standard text item height) */
    private int additionalItemHeight = ADDITIONAL_ITEM_HEIGHT;
    private int threeWheelAdditionalItemHeight = ADDITIONAL_ITEM_HEIGHT;

    // Text paints
    private TextPaint itemsPaint;
    private TextPaint valuePaint;

    // Layouts
    private StaticLayout itemsLayout;
    private StaticLayout itemsLayout2;
    private StaticLayout labelLayout;
    private StaticLayout labelLayout2;
    private StaticLayout valueLayout;

    // Label & background
    private String label;// right text
    private String label2;// left text
    private Drawable centerDrawable;

    // Shadows drawables
    private GradientDrawable topShadow;
    private GradientDrawable bottomShadow;

    // Scrolling
    private boolean isScrollingPerformed;
    private int scrollingOffset;

    // Scrolling animation
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int lastScrollY;

    // Cyclic
    boolean isCyclic = false;

    // the wheelView width
    private int m_totalWidth;
    private int deviceWidth;
    private int shadowColor = Color.RED;

    /**
     * if it is true: With the constant scrolling gestures, wheel will not stop
     * scrolling until you click it again. Usage: the dynamic information can be
     * used to generate random numbers, passwords, Lottery, etc. if it is
     * false:normal to scroll.
     */
    boolean isAnimated = false;

    // Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();

    private int backgroundId = Color.TRANSPARENT;// R.drawable.wheel_bg_three;
    private int centerDrawableId = android.R.color.background_light;//R.drawable.wheel_three_mid;

    /**
     * Constructor
     */
    public WheelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            initData(context);
        }
    }

    /**
     * Constructor
     */
    public WheelView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initData(context);
        }
    }

    /**
     * Constructor
     */
    public WheelView(final Context context) {
        super(context);
        if (!isInEditMode()) {
            initData(context);
        }
    }

    /**
     * Initializes class data
     *
     * @param context the context
     */
    private void initData(final Context context) {
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);

        scroller = new Scroller(context);
        textSize = WheelConfig.getTextSize(context);
        deviceWidth = WheelConfig.getDeviceWidth(context);
        additionalItemHeight = WheelConfig.getAdditionalItemHeight(context);
        threeWheelAdditionalItemHeight = WheelConfig.getThreeWheelAdditionalItemHeight(context);
    }

    /**
     * Gets wheel adapter
     *
     * @return the adapter
     */
    public WheelAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets wheel adapter
     *
     * @param adapter the new wheel adapter
     */
    public void setAdapter(final WheelAdapter adapter) {
        this.adapter = adapter;
        invalidateLayouts();
        invalidate();
    }

    public List getDataSet() {
        return adapter.getDataSet();
    }

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(final Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(getContext(), interpolator);
    }

    /**
     * Set the the specified scrolling background
     *
     * @param background image id
     */
    public void setBackground(final int id) {
        backgroundId = id;
        invalidate();
    }

    /**
     * Set the the specified scrolling centerVal
     *
     * @param centerval image id
     */
    public void setCenterDrawable(final int id) {
        centerDrawableId = id;
        invalidate();
    }

    /**
     * set current select item/ center item background.
     * @param bgid R.drawable.wheel_three_mid
     */
    public void setCurrentItemBg(final int bgid) {
        centerDrawableId = bgid;
        invalidate();
    }

    /**
     * Gets count of visible items
     *
     * @return the count of visible items
     */
    public int getVisibleItems() {
        return visibleItems;
    }

    /**
     * Sets count of visible items
     *
     * @param count the new count
     */
    public void setVisibleItems(final int count) {
        visibleItems = count;
        if (count == 3) {
            additionalItemHeight = threeWheelAdditionalItemHeight;
        }
        invalidate();
    }

    /**
     * Sets item height
     *
     * @param height the height
     */
    public void setItemHeight(final int height) {
        additionalItemHeight = height;
        invalidate();
    }

    /**
     * set wheel text size
     *
     * @param size : the paint's text size. 480:18, 720:25, 1080:40
     */
    public void setTextSize(final int size) {
        this.textSize = size;
        this.itemOffset = textSize / TEXTSIZE_ITEMOFFSET_RATIO;
    }

    /**
     * get the wheel's text size.
     *
     * @return the paint's text size.
     */
    public int getTextSize() {
        return this.textSize;
    }

    public int getCurrentValueTextColor() {
        return currentValueTextColor;
    }

    public void setCurrentValueTextColor(int currentValueTextColor) {
        this.currentValueTextColor = currentValueTextColor;
    }

    public int getItemsTextColor() {
        return itemTextColor;
    }

    public void setItemsTextColor(int itemsTextColor) {
        this.itemTextColor = itemsTextColor;
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    /**
     * set AddItemHeight when three wheel item is 3.
     * @param threeWheelAdditionalItemHeight: eg 480:70, 720:100, 1080:155
     */
    public void setThreeWheelAddItemHeight(int threeWheelItemHeight) {
        this.threeWheelAdditionalItemHeight = threeWheelItemHeight;
    }

    /**
     * Gets label
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    public String getLabel2() {
        return label2;
    }

    /**
     * Sets label
     *
     * @param newLabel the label to set
     */
    public void setLabel(final String newLabel) {
        if ((label == null) || !label.equals(newLabel)) {
            label = newLabel;
            labelLayout = null;
            invalidate();
        }
    }

    public void setLabel2(final String newLabel) {
        if ((label2 == null) || !label2.equals(newLabel)) {
            label2 = newLabel;
            labelLayout2 = null;
            invalidate();
        }
    }

    /**
     * Adds wheel changing listener
     *
     * @param listener the listener
     */
    public void addChangingListener(final OnWheelChangedListener listener) {
        changingListeners.add(listener);
    }

    /**
     * Removes wheel changing listener
     *
     * @param listener the listener
     */
    public void removeChangingListener(final OnWheelChangedListener listener) {
        changingListeners.remove(listener);
    }

    /**
     * Notifies changing listeners
     *
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    protected void notifyChangingListeners(final int oldValue, final int newValue, String currentText) {
        for (OnWheelChangedListener listener : changingListeners) {
            listener.onChanged(this, oldValue, newValue, currentText);
        }
    }

    /**
     * Adds wheel scrolling listener
     *
     * @param listener the listener
     */
    public void addScrollingListener(final OnWheelScrollListener listener) {
        scrollingListeners.add(listener);
    }

    /**
     * Removes wheel scrolling listener
     *
     * @param listener the listener
     */
    public void removeScrollingListener(final OnWheelScrollListener listener) {
        scrollingListeners.remove(listener);
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * Gets current value
     *
     * @return the current value
     */
    public int getCurrentItem() {
        return currentItem;
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index the item index
     * @param isAnimated the animation flag
     */
    public void setCurrentItem(int index, final boolean animated) {
        if ((adapter == null) || (adapter.getItemsCount() == 0)) {
            return; // throw?
        }
        if ((index < 0) || (index >= adapter.getItemsCount())) {
            if (isCyclic) {
                while (index < 0) {
                    index += adapter.getItemsCount();
                }
                index %= adapter.getItemsCount();
            } else {
                return; // throw?
            }
        }
        if (index != currentItem) {
            if (animated) {
                scroll(index - currentItem, WheelView.SCROLLING_DURATION);
            } else {
                invalidateLayouts();

                int old = currentItem;
                currentItem = index;
                
                String currrentValue = getAdapter().getItem(currentItem);
                notifyChangingListeners(old, currentItem, currrentValue);

                invalidate();
            }
        }
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     *
     * @param index the item index
     */
    public void setCurrentItem(final int index) {
        setCurrentItem(index, isAnimated);
    }

    /**
     * Tests if wheel is cyclic. That means before the 1st item there is shown
     * the last one
     *
     * @return true if wheel is cyclic
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    /**
     * Set wheel cyclic flag
     *
     * @param isCyclic the flag to set
     */
    public void setCyclic(final boolean isCyclic) {
        this.isCyclic = isCyclic;

        invalidate();
        invalidateLayouts();
    }

    /**
     * @param animated: if it is true: With the constant scrolling gestures,
     *            wheel will not stop scrolling until you click it again. Usage:
     *            the dynamic information can be used to generate random
     *            numbers, passwords, Lottery, etc. if it is false:normal to
     *            scroll.
     */
    public void setAnimated(final boolean animated) {
        this.isAnimated = animated;
    }

    /**
     * get the wheel animation if it is true: With the constant scrolling
     * gestures, wheel will not stop scrolling until you click it again. Usage:
     * the dynamic information can be used to generate random numbers,
     * passwords, Lottery, etc. if it is false:normal to scroll.
     *
     * @return
     */
    public boolean getAnimated() {
        return isAnimated;
    }

    /**
     * Invalidates layouts
     */
    private void invalidateLayouts() {
        itemsLayout = null;
        itemsLayout2 = null;
        valueLayout = null;
        scrollingOffset = 0;
    }

    /**
     * Initializes resources
     */
    private void initResourcesIfNecessary() {
        if (itemsPaint == null) {
            itemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
                    | Paint.FAKE_BOLD_TEXT_FLAG);
            // itemsPaint.density = getResources().getDisplayMetrics().density;
            itemsPaint.setTextSize(textSize);
        }

        if (valuePaint == null) {
            valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
                    | Paint.FAKE_BOLD_TEXT_FLAG | Paint.DITHER_FLAG);
            // valuePaint.density = getResources().getDisplayMetrics().density;
            valuePaint.setTextSize(textSize);
            valuePaint.setShadowLayer(0.1f, 0, 0.1f, shadowColor);// color
        }

        if (centerDrawable == null) {
            if(centerDrawableId != 0) {
                centerDrawable = getContext().getResources().getDrawable(centerDrawableId);// wheel_val,R.drawable.wheel_val_new
            }
        }

        if (topShadow == null) {
            topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, WheelView.SHADOWS_COLORS);
        }

        if (bottomShadow == null) {
            bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, WheelView.SHADOWS_COLORS);
        }

        setBackgroundResource(backgroundId);// wheel_bg
    }

    /**
     * Calculates desired height for layout
     *
     * @param layout the source layout
     * @return the desired layout height
     */
    private int getDesiredHeight(final Layout layout) {
        if (layout == null) {
            return 0;
        }

        int desired = (getItemHeight() * visibleItems) - (itemOffset * 2)
                - additionalItemHeight;

        // Check against our minimum height
        desired = Math.max(desired, getSuggestedMinimumHeight());

        return desired;
    }

    /**
     * Returns text item by index
     *
     * @param index the item index
     * @return the item or null
     */
    private String getTextItem(int index) {
        if ((adapter == null) || (adapter.getItemsCount() == 0)) {
            return null;
        }
        int count = adapter.getItemsCount();
        if (((index < 0) || (index >= count)) && !isCyclic) {
            return null;
        } else {
            while (index < 0) {
                index = count + index;
            }
        }

        index %= count;
        return adapter.getItem(index);
    }

    /**
     * Builds text depending on current value
     *
     * @param useCurrentValue
     * @return the text
     */
    private String buildText(final boolean useCurrentValue) {
        StringBuilder itemsText = new StringBuilder();
        int addItems = (visibleItems / 2) + 1;

        for (int i = currentItem - addItems; i <= (currentItem + addItems); i++) {
            if (useCurrentValue || (i != currentItem)) {
                String text = getTextItem(i);
                if (text != null) {
                    itemsText.append(text);
                }
            }
            if (i < (currentItem + addItems)) {
                itemsText.append("\n");
            }
        }

        return itemsText.toString();
    }

    /**
     * Returns the max item length that can be present
     *
     * @return the max length
     */
    private int getMaxTextLength() {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return 0;
        }

        int adapterLength = adapter.getMaximumLength();
        if (adapterLength > 0) {
            return adapterLength;
        }

        String maxText = null;
        int addItems = visibleItems / 2;
        for (int i = Math.max(currentItem - addItems, 0); i < Math.min(currentItem + visibleItems, adapter.getItemsCount()); i++) {
            String text = adapter.getItem(i);
            if ((text != null) && ((maxText == null) || (maxText.length() < text.length()))) {
                maxText = text;
            }
        }
        return maxText != null ? (maxText.length() + getChineseCount(maxText)) : 0;
    }

    private int getChineseCount(final String chinese) {
        int count = 0;
        String regEx = "[\\u4e00-\\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(chinese);
        while (m.find()) {
            for (int i = 0; i <= m.groupCount(); i++) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns height of wheel item
     *
     * @return the item height
     */
    private int getItemHeight() {
        if (itemHeight != 0) {
            return itemHeight;
        } else if ((itemsLayout != null) && (itemsLayout.getLineCount() > 2)) {
            itemHeight = itemsLayout.getLineTop(2) - itemsLayout.getLineTop(1);
            return itemHeight;
        }

        return getHeight() / visibleItems;
    }

    /**
     * Calculates control width and creates text layouts
     *
     * @param widthSize the input layout width
     * @param mode the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(final int widthSize, final int mode) {
        initResourcesIfNecessary();

        int width = widthSize;

        int maxLength = getMaxTextLength();
        if (maxLength > 0) {
            float textWidth = FloatMath.ceil(Layout.getDesiredWidth("0", itemsPaint));
            // make situation of two lables woke normal
            itemsWidth = (int) (maxLength * textWidth);
        } else {
            itemsWidth = 0;
        }
        itemsWidth += additionalItemsSpace; // make it some more

        labelWidth = 0;
        if ((label != null) && (label.length() > 0)) {
            labelWidth = (int) FloatMath.ceil(Layout.getDesiredWidth(label, valuePaint));
        }

        // added code
        labelWidth2 = 0;
        if ((label2 != null) && (label2.length() > 0)) {
            labelWidth2 = (int) FloatMath.ceil(Layout.getDesiredWidth(label2, valuePaint));
        }

        boolean recalculate = false;
        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
            recalculate = true;
        } else {
            // added labelWidth2
            width = itemsWidth + labelWidth + labelWidth2 + (4 * WheelView.PADDING);
            if (labelWidth > 0) {
                width += WheelView.LABEL_OFFSET;
            }
            // added code
            if (labelWidth2 > 0) {
                width += WheelView.LABEL_OFFSET;
            }
            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if ((mode == MeasureSpec.AT_MOST) && (widthSize < width)) {
                width = widthSize;
                recalculate = true;
            }
        }

        if (recalculate) {
            // recalculate width
            int pureWidth = width - WheelView.LABEL_OFFSET - (4 * WheelView.PADDING);
            if (pureWidth <= 0) {
                itemsWidth = labelWidth = 0;
            }
            if (labelWidth > 0) {
                double newWidthItems = ((double) itemsWidth * pureWidth)
                        / (itemsWidth + labelWidth);
                itemsWidth = (int) newWidthItems;
                labelWidth = pureWidth - itemsWidth;
            } else {
                itemsWidth = pureWidth + WheelView.LABEL_OFFSET; // no label
            }
        }

        if (itemsWidth > 0) {
            createLayouts(itemsWidth, labelWidth, labelWidth2);
        }

        return width;
    }

    /**
     * Creates layouts
     *
     * @param widthItems width of items layout
     * @param widthLabel width of label layout
     * @param widthLabel width of label2 layout
     */
    private void createLayouts(final int widthItems, final int widthLabel, final int widthLabel2) {
        if ((itemsLayout == null) || (itemsLayout.getWidth() > widthItems)) {
            itemsLayout = new StaticLayout(buildText(isScrollingPerformed), itemsPaint, widthItems,
                    widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER,
                    1, additionalItemHeight, false);
            itemsLayout2 = itemsLayout;
        } else {
            itemsLayout.increaseWidthTo(widthItems);
            itemsLayout2.increaseWidthTo(widthItems);
        }

        if (!isScrollingPerformed && ((valueLayout == null) || (valueLayout.getWidth() > widthItems))) {
            String text = getAdapter() != null ? getAdapter().getItem(currentItem) : null;
            valueLayout = new StaticLayout(text != null ? text : "",
                    valuePaint, widthItems, widthLabel > 0 ?
                            Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER,
                    1, additionalItemHeight, false);
        } else if (isScrollingPerformed) {
            valueLayout = null;
        } else {
            valueLayout.increaseWidthTo(widthItems);
        }

        if (widthLabel > 0) {
            if ((labelLayout == null) || (labelLayout.getWidth() > widthLabel)) {
                labelLayout = new StaticLayout(label, valuePaint,
                        widthLabel, Layout.Alignment.ALIGN_NORMAL, 1,
                        additionalItemHeight, false);
            } else {
                labelLayout.increaseWidthTo(widthLabel);
            }
        }
        // added code
        if (widthLabel2 > 0) {
            if ((labelLayout2 == null) || (labelLayout2.getWidth() > widthLabel)) {
                labelLayout2 = new StaticLayout(label2, valuePaint,
                        widthLabel, Layout.Alignment.ALIGN_NORMAL, 1,
                        additionalItemHeight, false);
            } else {
                labelLayout2.increaseWidthTo(widthLabel);
            }

        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = calculateLayoutWidth(widthSize, widthMode);
        m_totalWidth = width;
        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight(itemsLayout);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        drawCenterRect(canvas);
        if (itemsLayout == null) {
            if (itemsWidth == 0) {
                calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
            } else {
                createLayouts(itemsWidth, labelWidth, labelWidth2);
            }
        }

        if (itemsWidth > 0) {
            canvas.save();
            // Skip padding space and hide a part of top and bottom items
            canvas.translate(WheelView.PADDING, -itemOffset);
            drawItems(canvas);
            drawValue(canvas);
            canvas.restore();
        }
        // drawShadows(canvas);
    }

    /**
     * Draws shadows on top and bottom of control
     *
     * @param canvas the canvas for drawing
     */
    @SuppressWarnings("unused")
    private void drawShadows(final Canvas canvas) {// highlight the position
        topShadow.setBounds(0, 0, getWidth(), getHeight() / visibleItems);
        topShadow.draw(canvas);

        bottomShadow.setBounds(0, getHeight() - (getHeight() / visibleItems),
                getWidth(), getHeight());
        bottomShadow.draw(canvas);
    }

    /**
     * Draws value and label layout
     *
     * @param canvas the canvas for drawing
     */
    private void drawValue(final Canvas canvas) {
        valuePaint.setColor(currentValueTextColor);
        valuePaint.drawableState = getDrawableState();

        Rect bounds = new Rect();
        itemsLayout.getLineBounds(visibleItems / 2, bounds);
        itemsLayout2.getLineBounds(visibleItems / 2, bounds);
        // draw label
        if (labelLayout != null) {
            canvas.save();
            // config the distance between maniword and the widget
            float x = 0f;
            int length = getChineseCount(label);
            if (length == 1) {
                /*if (deviceWidth <= 320) {
                    x = m_totalWidth - labelLayout.getWidth() - getContext().getResources().getDimensionPixelSize(R.dimen.padding_max);
                } else {
                    x = m_totalWidth - labelLayout.getWidth() - getContext().getResources().getDimensionPixelSize(R.dimen.padding_min);
                }*/
                x = m_totalWidth - labelLayout.getWidth();
            } else {
                /*if (deviceWidth <= 320) {
                    x = m_totalWidth - (labelLayout.getWidth() / 2) - getContext().getResources().getDimensionPixelSize(R.dimen.padding_max);
                } else if ((deviceWidth <= 480)) {
                    x = m_totalWidth - (labelLayout.getWidth() / 2) - (3 * getContext().getResources().getDimensionPixelSize(R.dimen.padding_min));
                } else {
                    x = m_totalWidth - (labelLayout.getWidth() / 2) - getContext().getResources().getDimensionPixelSize(R.dimen.padding_min);
                }*/
                x = m_totalWidth - (labelLayout.getWidth() / 2);
            }
            canvas.translate(x, bounds.top);//
            labelLayout.draw(canvas);
            canvas.restore();

        }
        if (labelLayout2 != null) {
            // draw label2
            canvas.save();
            // config the distance between maniword and the widget
            canvas.translate(0, bounds.top);
            labelLayout2.draw(canvas);
            canvas.restore();
        }

        // draw current value
        if (valueLayout != null) {// the value in highlight
            canvas.save();
            canvas.translate((labelWidth2 / 2) + WheelView.PADDING, (bounds.top + scrollingOffset) - 1);
            valueLayout.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * Draws items
     *
     * @param canvas the canvas for drawing
     */
    private void drawItems(final Canvas canvas) {// items outside the highlight
        canvas.save();

        int top = itemsLayout.getLineTop(1);// depend on itemsLayout
        canvas.translate((labelWidth2 / 2) + WheelView.PADDING, -top + scrollingOffset);

        itemsPaint.setColor(itemTextColor);
        itemsPaint.drawableState = getDrawableState();
        itemsLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * Draws rect for current value
     *
     * @param canvas the canvas for drawing
     */
    private void drawCenterRect(final Canvas canvas) {// the highlight shadow
        int center = getHeight() / 2;
        int offset = getItemHeight() / 2;
        centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
        centerDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return true;
        }

        if (!gestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP)) {
            justify();
        }
        return true;
    }

    /**
     * Scrolls the wheel
     *
     * @param delta the scrolling value
     */
    private void doScroll(final int delta) {
        scrollingOffset += delta;

        int count = scrollingOffset / getItemHeight();
        int pos = currentItem - count;
        if (isCyclic && (adapter.getItemsCount() > 0)) {
            // fix position by rotating
            while (pos < 0) {
                pos += adapter.getItemsCount();
            }
            pos %= adapter.getItemsCount();
        } else if (isScrollingPerformed) {
            //
            if (pos < 0) {
                count = currentItem;
                pos = 0;
            } else if (pos >= adapter.getItemsCount()) {
                count = (currentItem - adapter.getItemsCount()) + 1;
                pos = adapter.getItemsCount() - 1;
            }
        } else {
            // fix position
            pos = Math.max(pos, 0);
            pos = Math.min(pos, adapter.getItemsCount() - 1);
        }

        int offset = scrollingOffset;
        if (pos != currentItem) {
            setCurrentItem(pos, isAnimated);
        } else {
            invalidate();
        }

        // update offset
        scrollingOffset = offset - (count * getItemHeight());
        if (scrollingOffset > getHeight()) {
            scrollingOffset = (scrollingOffset % getHeight()) + getHeight();
        }
    }

    // gesture listener
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDown(final MotionEvent e) {
            if (isScrollingPerformed) {
                scroller.forceFinished(true);
                clearMessages();
                return true;
            }
            return false;
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
            startScrolling();
            doScroll((int) -distanceY);
            return true;
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
            lastScrollY = (currentItem * getItemHeight()) + scrollingOffset;
            int maxY = isCyclic ? 0x7FFFFFFF : adapter.getItemsCount() * getItemHeight();
            int minY = isCyclic ? -maxY : 0;
            scroller.fling(0, lastScrollY, 0, (int) -velocityY / 2, 0, 0, minY, maxY);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    // Messages
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;

    /**
     * Set next message to queue. Clears queue before.
     *
     * @param message the message to set
     */
    private void setNextMessage(final int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    // animation handler
    private Handler animationHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            scroller.computeScrollOffset();
            int currY = scroller.getCurrY();
            int delta = lastScrollY - currY;
            lastScrollY = currY;
            if (delta != 0) {
                doScroll(delta);
            }

            // scrolling is not finished when it comes to final Y
            // so, finish it manually
            if (Math.abs(currY - scroller.getFinalY()) < WheelView.MIN_DELTA_FOR_SCROLLING) {
                currY = scroller.getFinalY();
                scroller.forceFinished(true);
            }
            // 濡傛灉婊戝姩娌℃湁缁撴潫锛屽氨缁х画瀹氫綅鏂扮殑浣嶇疆锛屽苟鏇存柊瑙嗗浘
            if (!scroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * Justifies wheel
     */
    private void justify() {
        if (adapter == null) {
            return;
        }

        lastScrollY = 0;
        int offset = scrollingOffset;
        int itemHeight = getItemHeight();
        boolean needToIncrease = offset > 0 ? currentItem < adapter.getItemsCount() : currentItem > 0;
        if ((isCyclic || needToIncrease) && (Math.abs((float) offset) > ((float) itemHeight / 2))) {
            if (offset < 0) {
                offset += itemHeight + WheelView.MIN_DELTA_FOR_SCROLLING;
            } else {
                offset -= itemHeight + WheelView.MIN_DELTA_FOR_SCROLLING;
            }
        }
        if (Math.abs(offset) > WheelView.MIN_DELTA_FOR_SCROLLING) {
            // 寮�濮嬫粦鍔紝offset璺濈锛屾粦鍔ㄦ椂闂�
            scroller.startScroll(0, 0, 0, offset, WheelView.SCROLLING_DURATION);
            setNextMessage(MESSAGE_JUSTIFY);
        } else {
            finishScrolling();
        }
    }

    /**
     * Starts scrolling
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }
    }

    /**
     * Finishes scrolling
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            notifyScrollingListenersAboutEnd();
            isScrollingPerformed = false;
        }
        invalidateLayouts();
        invalidate();
    }

    /**
     * Scroll the wheel
     *
     * @param itemsToScroll items to scroll
     * @param time scrolling duration
     */
    public void scroll(final int itemsToScroll, final int time) {
        scroller.forceFinished(true);

        lastScrollY = scrollingOffset;
        int offset = itemsToScroll * getItemHeight();

        scroller.startScroll(0, lastScrollY, 0, offset - lastScrollY, time);
        setNextMessage(MESSAGE_SCROLL);

        startScrolling();
    }

    public void setLabelBold(final boolean isBold) {
        if (labelLayout != null) {
            labelLayout.getPaint().setFakeBoldText(isBold);
        }
    }

    public void setLabel2Bold(final boolean isBold) {
        if (labelLayout2 != null) {
            labelLayout2.getPaint().setFakeBoldText(isBold);
        }
    }

    public Display getDefaultDisplay() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay();
    }

    
    /**-----------------------interfaces and listeners------------------------*/
    public interface WheelAdapter<T> {
        /**
         * Gets items count
         * @return the count of wheel items
         */
        public int getItemsCount();
        
        /**
         * Gets a wheel item by index.
         * 
         * @param index the item index
         * @return the wheel item text or null
         */
        public String getItem(int index);
        
        /**
         * Gets maximum item length. It is used to determine the wheel width. 
         * If -1 is returned there will be used the default wheel width.
         * 
         * @return the maximum item length or -1
         */
        public int getMaximumLength();

        public void setDataSet(List<T> data);
        
        public List<T> getDataSet();
    }

    /**
     * Wheel scrolled listener interface.
     */
    public interface OnWheelScrollListener {
        /**
         * Callback method to be invoked when scrolling started.
         * @param wheel the wheel view whose state has changed.
         */
        void onScrollingStarted(WheelView wheel);
        
        /**
         * Callback method to be invoked when scrolling ended.
         * @param wheel the wheel view whose state has changed.
         */
        void onScrollingFinished(WheelView wheel);
    }

    /**
     * Wheel changed listener interface.
     * <p>The currentItemChanged() method is called whenever current wheel positions is changed:
     * <li> New Wheel position is set
     * <li> Wheel view is scrolled
     */
    public interface OnWheelChangedListener {
        /**
         * Callback method to be invoked when current item changed
         * @param wheel the wheel view whose state has changed
         * @param oldId the old value id of current item
         * @param newId the new value id of current item
         * @param currentValue the value of current item current selected.
         */
        void onChanged(WheelView wheelView, int oldId, int newId, String currentValue);
    }
}

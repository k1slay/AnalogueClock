package com.k2.analogueclockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * Copyright (C) 2017 K2 CODEWORKS
 * All rights reserved
 *
 * @author Kislay
 * @since 12/7/2017
 */

public class AnalogueClock extends View {

    private final int MIN_SIZE = dpToPixel(Constants.MIN_SIZE_IN_DP);
    private final int IDEAL_SIZE = dpToPixel(Constants.IDEAL_SIZE_IN_DP);
    private int radius = MIN_SIZE / 2;
    private int centerX = 0;
    private int centerY = 0;

    public static final String TAG = "ClockView";
    private Time mTime;

    Paint mPaintOutline, mPaintHour, mPaintMin, mPaintSec;

    //Configurable values
    private int outlineWidth, hourWidth, minWidth, secWidth;
    private int outlineColor, hourColor, minColor, secColor;
    private boolean fillColor;
    private boolean running = false;

    public AnalogueClock(Context context) {
        super(context);
        if (!isInEditMode())
            init(context, null);
    }

    public AnalogueClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {

        if (attrs != null) {

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnalogueClock);
            //Hand width
            setOutlineWidth(a.getInteger(R.styleable.AnalogueClock_outline_width, Constants.WIDTH_OUTLINE));
            setHourHandWidth(a.getInteger(R.styleable.AnalogueClock_hour_hand_width, Constants.WIDTH_HOUR));
            setMinuteHandWidth(a.getInteger(R.styleable.AnalogueClock_minute_hand_width, Constants.WIDTH_MINUTE));
            setSecondHandWidth(a.getInteger(R.styleable.AnalogueClock_second_hand_width, Constants.WIDTH_SEC));
            //Hand color
            setOutlineColor(a.getColor(R.styleable.AnalogueClock_outline_color, ContextCompat.getColor(context, R.color.outline)));
            setHourHandColor(a.getColor(R.styleable.AnalogueClock_hour_hand_color, ContextCompat.getColor(context, R.color.hour)));
            setMinuteHandColor(a.getColor(R.styleable.AnalogueClock_minute_hand_color, ContextCompat.getColor(context, R.color.minute)));
            setSecHandColor(a.getColor(R.styleable.AnalogueClock_second_hand_color, ContextCompat.getColor(context, R.color.second)));

            setFillBackground(a.getBoolean(R.styleable.AnalogueClock_fill_background, false));

            a.recycle();
        }

        mTime = new Time();
        initPaint();

        running = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //This is the minimum size this view will use
        int dimensionMin = MIN_SIZE;

        // Calculate available width and height in pixels
        int givenWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        int paddingVertical = getPaddingTop() + getPaddingBottom();
        int paddingHorizontal = getPaddingLeft() + getPaddingRight();

        givenWidth = givenWidth - paddingHorizontal;
        givenHeight = givenHeight - paddingVertical;
        int givenMin = Math.min(givenHeight, givenWidth);

        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.UNSPECIFIED) {
            // The size has not been specified by the parent, will use a predefined ideal size
            dimensionMin = IDEAL_SIZE;
        }

        // Need the view to be square, so will use same value for width and height
        final int finalSize = Math.max(dimensionMin - paddingHorizontal, givenMin);
        setMeasuredDimension(
                finalSize + paddingHorizontal,
                finalSize + paddingVertical
        );

        radius = (finalSize / 2) - outlineWidth;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mTime.setToNow();

        canvas.drawCircle(centerX, centerY, radius, mPaintOutline);

        drawHands(ClockHand.HOUR, mTime.hour, canvas);
        drawHands(ClockHand.MINUTE, mTime.minute, canvas);
        drawHands(ClockHand.SECOND, mTime.second, canvas);

        if (running) postInvalidateDelayed(1000);
    }

    private void drawHands(ClockHand type, int value, Canvas canvas) {
        int degMultiplier = 1;
        double handLengthVal = 1;
        double backHandLengthVal = 1;
        int handLenPercent = 90;
        int backHandLenPercent = 20;
        Paint paint = null;

        switch (type) {
            case SECOND:
                degMultiplier = Constants.DEGREE_STEPS_SEC;
                handLenPercent = Constants.HAND_LENGTH_PERCENT_SEC;
                backHandLenPercent = Constants.HAND_BACK_LENGTH_PERCENT_SEC;
                paint = mPaintSec;
                break;
            case MINUTE:
                degMultiplier = Constants.DEGREE_STEPS_MIN;
                handLenPercent = Constants.HAND_LENGTH_PERCENT_MIN;
                backHandLenPercent = Constants.HAND_BACK_LENGTH_PERCENT_MIN;
                paint = mPaintMin;
                break;
            case HOUR:
                degMultiplier = Constants.DEGREE_STEPS_HOUR;
                handLenPercent = Constants.HAND_LENGTH_PERCENT_HOUR;
                backHandLenPercent = Constants.HAND_BACK_LENGTH_PERCENT_HOUR;
                paint = mPaintHour;
                break;
        }

        handLengthVal = radius * (handLenPercent / 100D);
        backHandLengthVal = radius * (backHandLenPercent / 100D);

        //Angle for hands
        double angle = value == 0 ? 0 : (value * degMultiplier);
        angle = Math.toRadians(angle - Constants.DEGREE_OFFSET);
        // X and Y axis for the far end of the hand
        double x2 = Math.cos(angle);
        double y2 = Math.sin(angle);

        double rad180 = Math.toRadians(180);
        double x0 = Math.cos(angle + rad180);
        double y0 = Math.sin(angle + rad180);

        double startX = ((double) centerX + (backHandLengthVal * x0));
        double startY = ((double) centerY + (backHandLengthVal * y0));

        double stopX = ((double) centerX + (handLengthVal * x2));
        double stopY = ((double) centerY + (handLengthVal * y2));
        canvas.drawLine(centerX, centerY, (float) stopX, (float) stopY, paint);
        canvas.drawLine(centerX, centerY, (float) startX, (float) startY, paint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        centerY = (bottom - top) / 2;
        centerX = (right - left) / 2;
    }

    /**
     * Convert DP to device specific pixels
     *
     * @param dp Dimension in dp
     * @return Dimension in pixels
     */
    private int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    /**
     * Initialise paint objects for different components
     */
    private void initPaint() {
        mPaintOutline = new Paint() {
            {
                setStyle(fillColor ? Style.FILL : Style.STROKE);
                setStrokeCap(Cap.BUTT);
                setStrokeJoin(Join.BEVEL);
                setColor(outlineColor);
                setStrokeWidth(outlineWidth);
                setAntiAlias(true);
            }
        };

        mPaintHour = new Paint() {
            {
                setStyle(Style.STROKE);
                setStrokeCap(Paint.Cap.ROUND);
                setStrokeJoin(Join.ROUND);
                setColor(hourColor);
                setStrokeWidth(hourWidth);
                setAntiAlias(true);
            }
        };

        mPaintMin = new Paint() {
            {
                setStyle(Style.STROKE);
                setStrokeCap(Paint.Cap.ROUND);
                setStrokeJoin(Join.ROUND);
                setColor(minColor);
                setStrokeWidth(minWidth);
                setAntiAlias(true);
            }
        };

        mPaintSec = new Paint() {
            {
                setStyle(Style.STROKE);
                setStrokeJoin(Join.BEVEL);
                setColor(secColor);
                setStrokeWidth(secWidth);
                setAntiAlias(true);
            }
        };
    }

    private void setStrokeWidth(int value, ClockHand clockHand) {

        if (value < Constants.STROKE_WIDTH_MIN)
            value = Constants.STROKE_WIDTH_MIN;
        else if (value > Constants.STROKE_WIDTH_MAX)
            value = Constants.STROKE_WIDTH_MAX;

        if (clockHand == null) {
            outlineWidth = value;
        } else {
            switch (clockHand) {
                case HOUR:
                    hourWidth = value;
                    break;
                case MINUTE:
                    minWidth = value;
                    break;
                case SECOND:
                    secWidth = value;
                    break;
            }
        }
        redraw();
    }

    /**
     * @param width Width of the outline of the clock
     */
    public void setOutlineWidth(int width) {
        setStrokeWidth(width, null);
    }

    /**
     * @param width Width of the hour hand of the clock
     */
    public void setHourHandWidth(int width) {
        setStrokeWidth(width, ClockHand.HOUR);
    }

    /**
     * @param width Width of the minute hand of the clock
     */
    public void setMinuteHandWidth(int width) {
        setStrokeWidth(width, ClockHand.MINUTE);
    }

    /**
     * @param width Width of the second hand of the clock
     */
    public void setSecondHandWidth(int width) {
        setStrokeWidth(width, ClockHand.SECOND);
    }

    /**
     * @param fill If true, the whole face of the clock will be filled with color as defined in
     *             <code>setOutlineColor</code>
     */
    public void setFillBackground(boolean fill) {
        fillColor = fill;
        redraw();
    }

    /**
     * @param color Color of the hour hand
     */
    public void setHourHandColor(int color) {
        hourColor = color;
        redraw();
    }

    /**
     * @param color Color of the minute hand
     */
    public void setMinuteHandColor(int color) {
        minColor = color;
        redraw();
    }

    /**
     * @param color Color of the second hand
     */
    public void setSecHandColor(int color) {
        secColor = color;
        redraw();
    }

    /**
     * @param color Color for the clock's outline.
     *              If <code>setFillColor</code> is true,
     *              this will also be the background color of the clock.
     */
    public void setOutlineColor(int color) {
        outlineColor = color;
        redraw();
    }

    private void redraw() {
        if (running) {
            initPaint();
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        running = mPaintOutline != null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        running = false;
    }


}

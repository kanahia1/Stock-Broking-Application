package com.kanahia.stockbrokingplatform.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.kanahia.stockbrokingplatform.R;

public class PriceProgressBar extends View {

    private static final float DEFAULT_MIN_PRICE = 0f;
    private static final float DEFAULT_MAX_PRICE = 100f;
    private static final float DEFAULT_CURRENT_PRICE = 50f;

    private static final int DEFAULT_TRACK_COLOR = Color.LTGRAY;
    private static final int DEFAULT_THUMB_COLOR = Color.GRAY;
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;

    private static final float DEFAULT_TRACK_HEIGHT = 4f;
    private static final float DEFAULT_THUMB_SIZE = 12f;
    private static final float DEFAULT_TEXT_SIZE = 18f;
    private static final float DEFAULT_TRIANGLE_SIZE = 8f;

    private Paint mTrackPaint;
    private Paint mThumbPaint;
    private Paint mTextPaint;

    private float mMinPrice;
    private float mMaxPrice;
    private float mCurrentPrice;

    private float mTrackHeight;
    private float mThumbRadius;
    private float mTextSize;
    private float mTriangleSize;

    private RectF mTrackRect;

    private float mProgressPosition;

    private Path mTrianglePath;

    public PriceProgressBar(Context context) {
        super(context);
        init(null);
    }

    public PriceProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PriceProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        mMinPrice = DEFAULT_MIN_PRICE;
        mMaxPrice = DEFAULT_MAX_PRICE;
        mCurrentPrice = DEFAULT_CURRENT_PRICE;
        mTrackHeight = DEFAULT_TRACK_HEIGHT;
        mThumbRadius = DEFAULT_THUMB_SIZE;
        mTextSize = DEFAULT_TEXT_SIZE;
        mTriangleSize = DEFAULT_TRIANGLE_SIZE;
        mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrackPaint.setColor(DEFAULT_TRACK_COLOR);
        mTrackPaint.setStyle(Paint.Style.FILL);
        mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbPaint.setColor(DEFAULT_THUMB_COLOR);
        mThumbPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(DEFAULT_TEXT_COLOR);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mTrackRect = new RectF();
        mTrianglePath = new Path();

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PriceProgressBar);

            mMinPrice = a.getFloat(R.styleable.PriceProgressBar_minPrice, DEFAULT_MIN_PRICE);
            mMaxPrice = a.getFloat(R.styleable.PriceProgressBar_maxPrice, DEFAULT_MAX_PRICE);
            mCurrentPrice = a.getFloat(R.styleable.PriceProgressBar_currentPrice, DEFAULT_CURRENT_PRICE);

            mTrackHeight = a.getDimension(R.styleable.PriceProgressBar_trackHeight, DEFAULT_TRACK_HEIGHT);
            mThumbRadius = a.getDimension(R.styleable.PriceProgressBar_thumbSize, DEFAULT_THUMB_SIZE) / 2;
            mTextSize = a.getDimension(R.styleable.PriceProgressBar_textSize, DEFAULT_TEXT_SIZE);
            mTriangleSize = a.getDimension(R.styleable.PriceProgressBar_triangleSize, DEFAULT_TRIANGLE_SIZE);

            mTrackPaint.setColor(a.getColor(R.styleable.PriceProgressBar_trackColor, DEFAULT_TRACK_COLOR));
            mThumbPaint.setColor(a.getColor(R.styleable.PriceProgressBar_thumbColor, DEFAULT_THUMB_COLOR));
            mTextPaint.setColor(a.getColor(R.styleable.PriceProgressBar_textColor, DEFAULT_TEXT_COLOR));
            mTextPaint.setTextSize(mTextSize);

            a.recycle();
        }

        updateProgressPosition();
    }

    private void updateProgressPosition() {
        if (mMaxPrice == mMinPrice) {
            mProgressPosition = 0.5f; // Middle position if range is zero
        } else {
            mProgressPosition = (mCurrentPrice - mMinPrice) / (mMaxPrice - mMinPrice);
            mProgressPosition = Math.max(0, Math.min(1, mProgressPosition));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float trackLeft = getPaddingLeft() + mThumbRadius;
        float trackRight = w - getPaddingRight() - mThumbRadius;
        float trackTop = h - getPaddingBottom() - mTrackHeight;
        float trackBottom = h - getPaddingBottom();

        mTrackRect.set(trackLeft, trackTop, trackRight, trackBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        canvas.drawRoundRect(mTrackRect, mTrackHeight / 2, mTrackHeight / 2, mTrackPaint);

        float thumbX = mTrackRect.left + mProgressPosition * (mTrackRect.width());
        float thumbY = mTrackRect.top + (mTrackRect.height() / 2);

        String priceText = "$" + String.format("%.2f", mCurrentPrice);
        float textY = thumbY - mThumbRadius - (mTriangleSize * 2);
        canvas.drawText(priceText, thumbX, textY, mTextPaint);

        mTrianglePath.reset();
        mTrianglePath.moveTo(thumbX, thumbY - mThumbRadius);
        mTrianglePath.lineTo(thumbX - mTriangleSize, thumbY - mThumbRadius - mTriangleSize);
        mTrianglePath.lineTo(thumbX + mTriangleSize, thumbY - mThumbRadius - mTriangleSize);
        mTrianglePath.close();
        canvas.drawPath(mTrianglePath, mThumbPaint);
        canvas.drawCircle(thumbX, thumbY, mThumbRadius, mThumbPaint);
    }

    public void setCurrentPrice(float price) {
        this.mCurrentPrice = price;
        updateProgressPosition();
        invalidate();
    }

    public void setMinPrice(float price) {
        this.mMinPrice = price;
        updateProgressPosition();
        invalidate();
    }

    public void setMaxPrice(float price) {
        this.mMaxPrice = price;
        updateProgressPosition();
        invalidate();
    }

    public float getCurrentPrice() {
        return mCurrentPrice;
    }

    public float getMinPrice() {
        return mMinPrice;
    }

    public float getMaxPrice() {
        return mMaxPrice;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) (getPaddingTop() + mTextSize + mTriangleSize * 3 + mThumbRadius * 2 + getPaddingBottom());

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }
}
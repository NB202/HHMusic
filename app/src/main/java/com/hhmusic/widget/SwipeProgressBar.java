package com.hhmusic.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;



final class SwipeProgressBar {


    private final static int COLOR1 = 0xB3000000;
    private final static int COLOR2 = 0x80000000;
    private final static int COLOR3 = 0x4d000000;
    private final static int COLOR4 = 0x1a000000;


    private static final int ANIMATION_DURATION_MS = 2000;


    private static final int FINISH_ANIMATION_DURATION_MS = 1000;


    private static final Interpolator INTERPOLATOR = new LinearInterpolator();

    private final Paint mPaint = new Paint();
    private final RectF mClipRect = new RectF();
    private float mTriggerPercentage;
    private long mStartTime;
    private long mFinishTime;
    private boolean mRunning;


    private int mColor1;
    private int mColor2;
    private int mColor3;
    private int mColor4;
    private View mParent;

    private Rect mBounds = new Rect();

    public SwipeProgressBar(View parent) {
        mParent = parent;
        mColor1 = COLOR1;
        mColor2 = COLOR2;
        mColor3 = COLOR3;
        mColor4 = COLOR4;
    }


    void setColorScheme(int color1, int color2, int color3, int color4) {
        mColor1 = color1;
        mColor2 = color2;
        mColor3 = color3;
        mColor4 = color4;
    }


    void setTriggerPercentage(float triggerPercentage) {
        mTriggerPercentage = triggerPercentage;
        mStartTime = 0;
        ViewCompat.postInvalidateOnAnimation(mParent);
    }


    void start() {
        if (!mRunning) {
            mTriggerPercentage = 0;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mRunning = true;
            mParent.postInvalidate();
        }
    }


    void stop() {
        if (mRunning) {
            mTriggerPercentage = 0;
            mFinishTime = AnimationUtils.currentAnimationTimeMillis();
            mRunning = false;
            mParent.postInvalidate();
        }
    }


    boolean isRunning() {
        return mRunning || mFinishTime > 0;
    }

    void draw(Canvas canvas) {
        final int width = mBounds.width();
        final int height = mBounds.height();
        final int cx = width / 2;

        final int cy = mBounds.bottom - height / 2;
        boolean drawTriggerWhileFinishing = false;
        int restoreCount = canvas.save();
        canvas.clipRect(mBounds);

        if (mRunning || (mFinishTime > 0)) {
            long now = AnimationUtils.currentAnimationTimeMillis();
            long elapsed = (now - mStartTime) % ANIMATION_DURATION_MS;
            long iterations = (now - mStartTime) / ANIMATION_DURATION_MS;
            float rawProgress = (elapsed / (ANIMATION_DURATION_MS / 100f));


            if (!mRunning) {

                if ((now - mFinishTime) >= FINISH_ANIMATION_DURATION_MS) {
                    mFinishTime = 0;
                    return;
                }


                long finishElapsed = (now - mFinishTime) % FINISH_ANIMATION_DURATION_MS;
                float finishProgress = (finishElapsed / (FINISH_ANIMATION_DURATION_MS / 100f));
                float pct = (finishProgress / 100f);

                float clearRadius = width / 2 * INTERPOLATOR.getInterpolation(pct);
                mClipRect.set(cx - clearRadius, 0, cx + clearRadius, height);
                canvas.saveLayerAlpha(mClipRect, 0, 0);

                drawTriggerWhileFinishing = true;
            }


            if (iterations == 0) {
                canvas.drawColor(mColor1);
            } else {
                if (rawProgress >= 0 && rawProgress < 25) {
                    canvas.drawColor(mColor4);
                } else if (rawProgress >= 25 && rawProgress < 50) {
                    canvas.drawColor(mColor1);
                } else if (rawProgress >= 50 && rawProgress < 75) {
                    canvas.drawColor(mColor2);
                } else {
                    canvas.drawColor(mColor3);
                }
            }


            if ((rawProgress >= 0 && rawProgress <= 25)) {
                float pct = (((rawProgress + 25) * 2) / 100f);
                drawCircle(canvas, cx, cy, mColor1, pct);
            }
            if (rawProgress >= 0 && rawProgress <= 50) {
                float pct = ((rawProgress * 2) / 100f);
                drawCircle(canvas, cx, cy, mColor2, pct);
            }
            if (rawProgress >= 25 && rawProgress <= 75) {
                float pct = (((rawProgress - 25) * 2) / 100f);
                drawCircle(canvas, cx, cy, mColor3, pct);
            }
            if (rawProgress >= 50 && rawProgress <= 100) {
                float pct = (((rawProgress - 50) * 2) / 100f);
                drawCircle(canvas, cx, cy, mColor4, pct);
            }
            if ((rawProgress >= 75 && rawProgress <= 100)) {
                float pct = (((rawProgress - 75) * 2) / 100f);
                drawCircle(canvas, cx, cy, mColor1, pct);
            }
            if (mTriggerPercentage > 0 && drawTriggerWhileFinishing) {

                canvas.restoreToCount(restoreCount);
                restoreCount = canvas.save();
                canvas.clipRect(mBounds);
                drawTrigger(canvas, cx, cy);
            }

            ViewCompat.postInvalidateOnAnimation(mParent);
        } else {

            if (mTriggerPercentage > 0 && mTriggerPercentage <= 1.0) {
                drawTrigger(canvas, cx, cy);
            }
        }
        canvas.restoreToCount(restoreCount);
    }

    private void drawTrigger(Canvas canvas, int cx, int cy) {
        mPaint.setColor(mColor1);
        canvas.drawCircle(cx, cy, cx * mTriggerPercentage, mPaint);
    }


    private void drawCircle(Canvas canvas, float cx, float cy, int color, float pct) {
        mPaint.setColor(color);
        canvas.save();
        canvas.translate(cx, cy);
        float radiusScale = INTERPOLATOR.getInterpolation(pct);
        canvas.scale(radiusScale, radiusScale);
        canvas.drawCircle(0, 0, cx, mPaint);
        canvas.restore();
    }


    void setBounds(int left, int top, int right, int bottom) {
        mBounds.left = left;
        mBounds.top = top;
        mBounds.right = right;
        mBounds.bottom = bottom;
    }
}
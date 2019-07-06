package com.hhmusic.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.hhmusic.R;


public class SideBar extends View {

    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;

    public static String[] b = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    private int choose = -1;
    private Paint paint = new Paint();


    private TextView mTextDialog;


    public void setView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }


    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.search_indexbar_bg_prs);
    }


    public SideBar(Context context) {
        super(context);

    }



    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.removeCallbacks(runnable);

        int height = getHeight();
        int width = getWidth();
        int singleHeight = height / b.length;


        for (int i = 0; i < b.length; i++) {

            paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.SANS_SERIF);
            paint.setAntiAlias(true);
            paint.setTextSize(30);

            if (i == choose) {
                paint.setColor(Color.WHITE);
                paint.setFakeBoldText(true);
            }

            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();
        }
        this.postDelayed(runnable, 2000);

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            SideBar.this.setVisibility(INVISIBLE);
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * b.length);


        switch (action) {
            case MotionEvent.ACTION_UP:

                choose = -1;
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }

                break;
            case MotionEvent.ACTION_DOWN:
                this.removeCallbacks(runnable);
                SideBar.this.setVisibility(VISIBLE);
                break;

            default:

                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(b[c]);
                            mTextDialog.setVisibility(View.VISIBLE);
                        }


                        choose = c;
                        invalidate();
                    }
                }


                break;
        }
        return true;
    }



    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }



    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChanged(String s);
    }


    public void setSelected(String nowChar) {
        Log.i("OnRecyclerViewOnScrol", "setSelected:" + nowChar);
        if (nowChar != null) {
            for (int i = 0; i < b.length; i++) {
                if (b[i].equals(nowChar)) {
                    choose = i;
                    break;
                }
                if (i == b.length - 1) {
                    choose = -1;
                }
            }
            invalidate();
        }
    }
}

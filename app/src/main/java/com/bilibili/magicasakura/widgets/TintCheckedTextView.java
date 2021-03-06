package com.bilibili.magicasakura.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import com.bilibili.magicasakura.utils.TintManager;
import com.hhmusic.R;


public class TintCheckedTextView extends CheckedTextView implements Tintable {
    private static final int[] ATTRS = {
            android.R.attr.drawableLeft,
            R.attr.drawableLeftTint
    };

    public TintCheckedTextView(Context context) {
        this(context, null);
    }

    public TintCheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkedTextViewStyle);
    }

    public TintCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, ATTRS);
        final int drawLeftId = array.getResourceId(0, 0);
        final int drawLeftTintId = array.getResourceId(1, 0);
        array.recycle();
        if (drawLeftId != 0 && drawLeftTintId != 0) {
            tintCheckTextView(drawLeftId, drawLeftTintId);
        }
    }

    public void tintCheckTextView(@DrawableRes int resId, int tintId) {
        Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(resId));
        DrawableCompat.setTintList(drawable, TintManager.get(getContext()).getColorStateList(tintId));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
            setCheckMarkDrawable(null);
        } else {
            setCheckMarkDrawable(drawable);
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    @Override
    public void setCheckMarkDrawable(Drawable d) {
        super.setCheckMarkDrawable(d);
        //FIXME:recommend not to use it
    }

    @Override
    public void setCheckMarkDrawable(int resId) {
        super.setCheckMarkDrawable(resId);
        //FIXME:recommend not to use it
    }

    @Override
    public void tint() {

    }
}

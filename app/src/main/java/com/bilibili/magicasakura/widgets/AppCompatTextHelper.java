package com.bilibili.magicasakura.widgets;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import androidx.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.bilibili.magicasakura.utils.TintInfo;
import com.bilibili.magicasakura.utils.TintManager;
import com.hhmusic.R;


public class AppCompatTextHelper extends AppCompatBaseHelper {


    private static final int[] ATTRS = {
            android.R.attr.textColor,
            android.R.attr.textColorLink,
            android.R.attr.textAppearance,
    };

    private int mTextColorId;
    private int mTextLinkColorId;

    private TintInfo mTextColorTintInfo;
    private TintInfo mTextLinkColorTintInfo;

    public AppCompatTextHelper(View view, TintManager tintManager) {
        super(view, tintManager);
    }

    @SuppressWarnings("ResourceType")
    @Override
    void loadFromAttribute(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = mView.getContext().obtainStyledAttributes(attrs, ATTRS, defStyleAttr, 0);

        int textColorId = array.getResourceId(0, 0);
        if (textColorId == 0) {
            setTextAppearanceForTextColor(array.getResourceId(2, 0), false);
        } else {
            setTextColor(textColorId);
        }

        if (array.hasValue(1)) {
            setLinkTextColor(array.getResourceId(1, 0));
        }
        array.recycle();
    }



    public void setTextColor() {
        if (skipNextApply()) return;

        resetTextColorTintResource(0);
        setSkipNextApply(false);
    }


    @Deprecated
    public void setTextLinkColor() {
        if (skipNextApply()) return;

        resetTextLinkColorTintResource(0);
        setSkipNextApply(false);
    }

    public void setTextAppearanceForTextColor(int resId) {
        resetTextColorTintResource(0);
        setTextAppearanceForTextColor(resId, true);
    }

    public void setTextAppearanceForTextColor(int resId, boolean isForced) {
        boolean isTextColorForced = isForced || mTextColorId == 0;
        TypedArray appearance = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (appearance.hasValue(R.styleable.TextAppearance_android_textColor) && isTextColorForced) {
            setTextColor(appearance.getResourceId(R.styleable.TextAppearance_android_textColor, 0));
        }
        appearance.recycle();
    }

    public void setTextColorById(@ColorRes int colorId) {
        setTextColor(colorId);
    }


    private void setTextColor(ColorStateList tint) {
        if (skipNextApply()) return;

        ((TextView) mView).setTextColor(tint);
    }

    private void setTextColor(@ColorRes int resId) {
        if (mTextColorId != resId) {
            resetTextColorTintResource(resId);

            if (resId != 0) {
                setSupportTextColorTint(resId);
            }
        }
    }

    private void setLinkTextColor(@ColorRes int resId) {
        if (mTextLinkColorId != resId) {
            resetTextLinkColorTintResource(resId);

            if (resId != 0) {
                setSupportTextLinkColorTint(resId);
            }
        }
    }

    private void setSupportTextColorTint(int resId) {
        if (resId != 0) {
            if (mTextColorTintInfo == null) {
                mTextColorTintInfo = new TintInfo();
            }
            mTextColorTintInfo.mHasTintList = true;
            mTextColorTintInfo.mTintList = mTintManager.getColorStateList(resId);
        }
        applySupportTextColorTint();
    }

    private void setSupportTextLinkColorTint(int resId) {
        if (resId != 0) {
            if (mTextLinkColorTintInfo == null) {
                mTextLinkColorTintInfo = new TintInfo();
            }
            mTextLinkColorTintInfo.mHasTintList = true;
            mTextLinkColorTintInfo.mTintList = mTintManager.getColorStateList(resId);
        }
        applySupportTextLinkColorTint();
    }

    private void applySupportTextColorTint() {
        if (mTextColorTintInfo != null && mTextColorTintInfo.mHasTintList) {
            setTextColor(mTextColorTintInfo.mTintList);
        }
    }

    private void applySupportTextLinkColorTint() {
        if (mTextLinkColorTintInfo != null && mTextLinkColorTintInfo.mHasTintList) {
            ((TextView) mView).setLinkTextColor(mTextLinkColorTintInfo.mTintList);
        }
    }

    private void resetTextColorTintResource(@ColorRes int resId/*text resource id*/) {
        mTextColorId = resId;
        if (mTextColorTintInfo != null) {
            mTextColorTintInfo.mHasTintList = false;
            mTextColorTintInfo.mTintList = null;
        }
    }

    private void resetTextLinkColorTintResource(@ColorRes int resId/*text resource id*/) {
        mTextLinkColorId = resId;
        if (mTextLinkColorTintInfo != null) {
            mTextLinkColorTintInfo.mHasTintList = false;
            mTextLinkColorTintInfo.mTintList = null;
        }
    }

    @Override
    public void tint() {
        if (mTextColorId != 0) {
            setSupportTextColorTint(mTextColorId);
        }
        if (mTextLinkColorId != 0) {
            setSupportTextLinkColorTint(mTextLinkColorId);
        }
    }

    public interface TextExtensible {
        void setTextColorById(@ColorRes int colorId);
    }
}

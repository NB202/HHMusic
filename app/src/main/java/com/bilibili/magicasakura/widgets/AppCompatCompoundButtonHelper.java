package com.bilibili.magicasakura.widgets;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.CompoundButtonCompat;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.bilibili.magicasakura.utils.DrawableUtils;
import com.bilibili.magicasakura.utils.TintInfo;
import com.bilibili.magicasakura.utils.TintManager;
import com.hhmusic.R;

public class AppCompatCompoundButtonHelper extends AppCompatBaseHelper {
    private static final int[] ATTRS = {
            android.R.attr.button,
            R.attr.compoundButtonTint,
            R.attr.compoundButtonTintMode
    };

    private TintInfo mCompoundButtonTintInfo;
    private int mCompoundButtonResId;
    private int mCompoundButtonTintResId;

    public AppCompatCompoundButtonHelper(CompoundButton view, TintManager tintManager) {
        super(view, tintManager);
    }

    @SuppressWarnings("ResourceType")
    @Override
    void loadFromAttribute(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = mView.getContext().obtainStyledAttributes(attrs, ATTRS, defStyleAttr, 0);
        if (array.hasValue(1)) {
            mCompoundButtonTintResId = array.getResourceId(1, 0);
            if (array.hasValue(2)) {
                setSupportButtonDrawableTintMode(DrawableUtils.parseTintMode(array.getInt(2, 0), null));
            }
            setSupportButtonDrawableTint(mCompoundButtonTintResId);
        } else {
            Drawable drawable = mTintManager.getDrawable(mCompoundButtonResId = array.getResourceId(0, 0));
            if (drawable != null) {
                setButtonDrawable(drawable);
            }
        }
        array.recycle();
    }


    public void setButtonDrawable() {
        if (skipNextApply()) return;

        resetTintResource(0);
        setSkipNextApply(false);
    }

    public void setButtonDrawable(int resId) {
        if (mCompoundButtonTintResId != resId) {
            resetTintResource(resId);

            if (resId != 0) {
                Drawable drawable = mTintManager.getDrawable(resId);
                setButtonDrawable(
                        drawable != null ? drawable : ContextCompat.getDrawable(mView.getContext(), resId));
            }
        }
    }

    public void setButtonDrawableTintList(int resId, PorterDuff.Mode mode) {
        if (mCompoundButtonTintResId != resId) {
            mCompoundButtonTintResId = resId;
            if (mCompoundButtonTintInfo != null) {
                mCompoundButtonTintInfo.mHasTintList = false;
                mCompoundButtonTintInfo.mTintList = null;
                mCompoundButtonTintInfo.mHasTintMode = false;
                mCompoundButtonTintInfo.mTintMode = null;
            }
            setSupportButtonDrawableTintMode(mode);
            setSupportButtonDrawableTint(resId);
        }
    }


    private void setButtonDrawable(Drawable drawable) {
        if (skipNextApply()) return;

        ((CompoundButton) mView).setButtonDrawable(drawable);
    }

    public boolean setSupportButtonDrawableTint(int resId) {
        if (resId != 0) {
            if (mCompoundButtonTintInfo == null) {
                mCompoundButtonTintInfo = new TintInfo();
            }
            mCompoundButtonTintInfo.mHasTintList = true;
            mCompoundButtonTintInfo.mTintList = mTintManager.getColorStateList(resId);
        }
        return applySupportButtonDrawableTint();
    }

    private void setSupportButtonDrawableTintMode(PorterDuff.Mode mode) {
        if (mCompoundButtonTintResId != 0 && mode != null) {
            if (mCompoundButtonTintInfo == null) {
                mCompoundButtonTintInfo = new TintInfo();
            }
            mCompoundButtonTintInfo.mHasTintMode = true;
            mCompoundButtonTintInfo.mTintMode = mode;
        }
    }

    public boolean applySupportButtonDrawableTint() {
        Drawable buttonDrawable = CompoundButtonCompat.getButtonDrawable((CompoundButton) mView);
        if (buttonDrawable != null && mCompoundButtonTintInfo != null && mCompoundButtonTintInfo.mHasTintList) {
            buttonDrawable = DrawableCompat.wrap(buttonDrawable);
            buttonDrawable = buttonDrawable.mutate();
            if (mCompoundButtonTintInfo.mHasTintList) {
                DrawableCompat.setTintList(buttonDrawable, mCompoundButtonTintInfo.mTintList);
            }
            if (mCompoundButtonTintInfo.mHasTintMode) {
                DrawableCompat.setTintMode(buttonDrawable, mCompoundButtonTintInfo.mTintMode);
            }

            if (buttonDrawable.isStateful()) {
                buttonDrawable.setState(mView.getDrawableState());
            }
            setButtonDrawable(buttonDrawable);
            return true;
        }
        return false;
    }

    private void resetTintResource(int resId/*background resource id*/) {
        mCompoundButtonResId = resId;
        mCompoundButtonTintResId = 0;
        if (mCompoundButtonTintInfo != null) {
            mCompoundButtonTintInfo.mHasTintList = false;
            mCompoundButtonTintInfo.mTintList = null;
        }
    }

    public int getCompoundPaddingLeft(int superValue) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {

            Drawable buttonDrawable = CompoundButtonCompat.getButtonDrawable((CompoundButton) mView);
            if (buttonDrawable != null) {
                superValue += buttonDrawable.getIntrinsicWidth();
            }
        }
        return superValue;
    }

    @Override
    public void tint() {
        if (mCompoundButtonTintResId == 0 || !setSupportButtonDrawableTint(mCompoundButtonTintResId)) {
            Drawable drawable = mTintManager.getDrawable(mCompoundButtonResId);
            if (drawable == null) {
                drawable = mCompoundButtonResId == 0 ? null : ContextCompat.getDrawable(mView.getContext(), mCompoundButtonResId);
            }
            setButtonDrawable(drawable);
        }
    }

    public interface CompoundButtonExtensible {
        void setCompoundButtonTintList(int resId);

        void setCompoundButtonTintList(int resId, PorterDuff.Mode mode);
    }
}

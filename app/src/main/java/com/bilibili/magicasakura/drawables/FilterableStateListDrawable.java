package com.bilibili.magicasakura.drawables;

import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.SparseArray;


public class FilterableStateListDrawable extends StateListDrawable {

    private int currIdx = -1;
    private int childrenCount = 0;
    private SparseArray<ColorFilter> filterMap;

    public FilterableStateListDrawable() {
        super();
        filterMap = new SparseArray<>();
    }

    @Override
    public void addState(int[] stateSet, Drawable drawable) {
        super.addState(stateSet, drawable);
        childrenCount++;
    }

    public void addState(int[] stateSet, Drawable drawable, ColorFilter colorFilter) {
        if (colorFilter == null) {
            addState(stateSet, drawable);
            return;
        }

        int currChild = childrenCount;
        addState(stateSet, drawable);
        filterMap.put(currChild, colorFilter);
    }

    @Override
    public boolean selectDrawable(int idx) {

        boolean result = super.selectDrawable(idx);

        if (getCurrent() != null) {
            currIdx = result ? idx : currIdx;
            setColorFilter(getColorFilterForIdx(currIdx));
        } else {
            currIdx = -1;
            setColorFilter(null);
        }
        return result;
    }

    private ColorFilter getColorFilterForIdx(int idx) {
        return filterMap != null ? filterMap.get(idx) : null;
    }

    @Override
    public ConstantState getConstantState() {
        return super.getConstantState();
    }

}
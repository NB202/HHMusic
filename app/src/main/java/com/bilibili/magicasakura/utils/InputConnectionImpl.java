package com.bilibili.magicasakura.utils;

import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;


public class InputConnectionImpl extends InputConnectionWrapper {

    public InputConnectionImpl(InputConnection target, boolean mutable) {
        super(target, mutable);
    }

    @Override
    public boolean setSelection(int start, int end) {
        if (start < 0 || end < 0) {

            return true;
        }
        return super.setSelection(start, end);
    }
}

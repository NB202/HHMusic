package com.hhmusic.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;


public class AttachFragment extends Fragment {

    public Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }
}

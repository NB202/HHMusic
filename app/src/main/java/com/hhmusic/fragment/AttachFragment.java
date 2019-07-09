package com.hhmusic.fragment;

import android.content.Context;
import androidx.fragment.app.Fragment;


public class AttachFragment extends Fragment {

    public Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }
}

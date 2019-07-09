package com.hhmusic.fragment;

import android.app.Activity;
import androidx.fragment.app.DialogFragment;


public class AttachDialogFragment extends DialogFragment {

    public Activity mContext;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mContext = activity;
    }


}

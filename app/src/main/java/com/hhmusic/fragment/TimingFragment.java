package com.hhmusic.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.hhmusic.R;
import com.hhmusic.service.MusicPlayer;

public class TimingFragment extends AttachDialogFragment implements View.OnClickListener {

    private TextView timing10, timing20, timing30, timing45, timing60, timing90;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.fragment_timing, container);
        timing10 = view.findViewById(R.id.timing_10min);
        timing20 = view.findViewById(R.id.timing_20min);
        timing30 = view.findViewById(R.id.timing_30min);
        timing45 = view.findViewById(R.id.timing_45min);
        timing60 = view.findViewById(R.id.timing_60min);
        timing90 = view.findViewById(R.id.timing_90min);
        timing10.setOnClickListener(this);
        timing20.setOnClickListener(this);
        timing30.setOnClickListener(this);
        timing45.setOnClickListener(this);
        timing60.setOnClickListener(this);
        timing90.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.timing_10min:
                MusicPlayer.timing(10 * 60 * 1000);
                Toast.makeText(mContext, "将在10分钟后停止播放", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
            case R.id.timing_20min:
                MusicPlayer.timing(20 * 60 * 1000);
                Toast.makeText(mContext, "将在20分钟后停止播放", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
            case R.id.timing_30min:
                MusicPlayer.timing(30 * 60 * 1000);
                Toast.makeText(mContext, "将在30分钟后停止播放", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
            case R.id.timing_45min:
                MusicPlayer.timing(45 * 60 * 1000);
                Toast.makeText(mContext, "将在45分钟后停止播放", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
            case R.id.timing_60min:
                MusicPlayer.timing(60 * 60 * 1000);
                Toast.makeText(mContext, "将在60分钟后停止播放", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
            case R.id.timing_90min:
                MusicPlayer.timing(90 * 60 * 1000);
                Toast.makeText(mContext, "将在90分钟后停止播放", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.71);
        int dialogWidth = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.79);
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }

}

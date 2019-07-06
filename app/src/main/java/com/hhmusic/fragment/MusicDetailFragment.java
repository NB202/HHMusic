package com.hhmusic.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hhmusic.R;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.uitl.MusicUtils;


public class MusicDetailFragment extends AttachDialogFragment {
    private TextView title, name, time, qua, size, path;
    private MusicInfo musicInfo;

    public static MusicDetailFragment newInstance(MusicInfo musicInfo) {
        MusicDetailFragment fragment = new MusicDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("musicinfo", musicInfo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams params = getDialog().getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setAttributes(params);
        if (getArguments() != null) {
            musicInfo = getArguments().getParcelable("musicinfo");
        }

        View view = inflater.inflate(R.layout.fragment_music_detail, container);

        title = (TextView) view.findViewById(R.id.music_detail_title);
        name = (TextView) view.findViewById(R.id.music_detail_name);
        time = (TextView) view.findViewById(R.id.music_detail_time);

        size = (TextView) view.findViewById(R.id.music_detail_size);
        path = (TextView) view.findViewById(R.id.music_detail_path);


        title.setText(musicInfo.musicName);
        name.setText(musicInfo.artist + "-" + musicInfo.musicName);
        time.setText(MusicUtils.makeShortTimeString(mContext, musicInfo.duration / 1000));

        size.setText(musicInfo.size / 1000000 + "m");
        path.setText(musicInfo.data);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Override
    public void onStart() {
        super.onStart();

        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.30);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }


}

package com.hhmusic.lrc;

import android.content.Context;

import java.util.List;


public interface ILrcView {

    void init(Context context);


    void setLrcRows(List<LrcRow> lrcRows);


    void seekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser);


    void setLrcScalingFactor(float scalingFactor);


    void reset();
}

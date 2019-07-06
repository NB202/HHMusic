package com.hhmusic.recent;

import android.content.Context;
import android.util.Log;

import com.hhmusic.info.MusicInfo;

import java.util.ArrayList;


public class QueueLoader {

    private static PlayQueueCursor mCursor;

    public static ArrayList<MusicInfo> getQueueSongs(Context context) {

        final ArrayList<MusicInfo> mMusicQueues = new ArrayList<>();
        Log.e("queueloader", "created");
        mCursor = new PlayQueueCursor(context);

        while (mCursor.moveToNext()) {
            MusicInfo music = new MusicInfo();
            music.songId = mCursor.getInt(0);
            music.albumName = mCursor.getString(4);
            music.musicName = mCursor.getString(1);
            music.artist = mCursor.getString(2);
            mMusicQueues.add(music);
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mMusicQueues;
    }
}

package com.hhmusic.service;

import android.os.Parcel;
import android.os.Parcelable;



public class MusicTrack implements Parcelable {


    public static final Creator<MusicTrack> CREATOR = new Creator<MusicTrack>() {
        @Override
        public MusicTrack createFromParcel(Parcel source) {
            return new MusicTrack(source);
        }

        @Override
        public MusicTrack[] newArray(int size) {
            return new MusicTrack[size];
        }
    };
    public long mId;
    public int mSourcePosition;
    public String mTitle;
    public String mAlbum;
    public String mArtist;


    public MusicTrack(long id, int sourcePosition) {
        mId = id;
        mSourcePosition = sourcePosition;

    }

    public MusicTrack(Parcel in) {
        mId = in.readLong();
        mSourcePosition = in.readInt();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeInt(mSourcePosition);


    }

}

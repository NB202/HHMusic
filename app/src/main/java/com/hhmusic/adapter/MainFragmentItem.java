package com.hhmusic.adapter;


public class MainFragmentItem {
    public String title;
    public int count;
    public int avatar;
    public boolean countChanged = true;

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAvatar() {
        return avatar;
    }


    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}

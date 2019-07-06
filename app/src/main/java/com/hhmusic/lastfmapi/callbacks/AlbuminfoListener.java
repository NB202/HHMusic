package com.hhmusic.lastfmapi.callbacks;


import com.hhmusic.lastfmapi.models.LastfmAlbum;

public interface AlbuminfoListener {

    void albumInfoSucess(LastfmAlbum album);

    void albumInfoFailed();

}

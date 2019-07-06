package com.hhmusic.lastfmapi.callbacks;


import com.hhmusic.lastfmapi.models.LastfmArtist;

public interface ArtistInfoListener {

    void artistInfoSucess(LastfmArtist artist);

    void artistInfoFailed();

}

package com.hhmusic.uitl;

import android.provider.MediaStore;


public final class SortOrder {


    public SortOrder() {
    }


    public interface ArtistSortOrder {

        String ARTIST_A_Z = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;


        String ARTIST_Z_A = ARTIST_A_Z + " DESC";


        String ARTIST_NUMBER_OF_SONGS = MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                + " DESC";


        String ARTIST_NUMBER_OF_ALBUMS = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
                + " DESC";
    }


    public interface AlbumSortOrder {

        String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;


        String ALBUM_Z_A = ALBUM_A_Z + " DESC";


        String ALBUM_NUMBER_OF_SONGS = MediaStore.Audio.Albums.NUMBER_OF_SONGS
                + " DESC";


        String ALBUM_ARTIST = MediaStore.Audio.Albums.ARTIST;


        String ALBUM_YEAR = MediaStore.Audio.Albums.FIRST_YEAR + " DESC";

    }


    public interface SongSortOrder {

        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;


        String SONG_Z_A = SONG_A_Z + " DESC";


        String SONG_ARTIST = MediaStore.Audio.Media.ARTIST;


        String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;


        String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";


        String SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC";


        String SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC";


        String SONG_FILENAME = MediaStore.Audio.Media.DATA;
    }


    public interface AlbumSongSortOrder {

        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;


        String SONG_Z_A = SONG_A_Z + " DESC";


        String SONG_TRACK_LIST = MediaStore.Audio.Media.TRACK + ", "
                + MediaStore.Audio.Media.DEFAULT_SORT_ORDER;


        String SONG_DURATION = SongSortOrder.SONG_DURATION;


        String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";


        String SONG_FILENAME = SongSortOrder.SONG_FILENAME;
    }


    public interface ArtistSongSortOrder {

        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;


        String SONG_Z_A = SONG_A_Z + " DESC";


        String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;


        String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";


        String SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC";


        String SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC";


        String SONG_FILENAME = SongSortOrder.SONG_FILENAME;
    }


    public interface ArtistAlbumSortOrder {

        String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;


        String ALBUM_Z_A = ALBUM_A_Z + " DESC";


        String ALBUM_NUMBER_OF_SONGS = MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS
                + " DESC";


        String ALBUM_YEAR = MediaStore.Audio.Artists.Albums.FIRST_YEAR
                + " DESC";
    }

    public interface FolderSortOrder {
        String FOLDER_A_Z = "foler_az";
        String FOLDER_NUMBER = "foler_number";
    }

}

package com.hhmusic.proxy.utils;

import android.os.Environment;

public class Constants {

    public static final String SD_PATH = Environment.getExternalStorageDirectory().getPath() + "/";

    public static final String DOWNLOAD_PATH = SD_PATH + "MusicPlayerTest/BufferFiles/";

    public static final int SD_REMAIN_SIZE = 50 * 1024 * 1024;

    public static final int AUDIO_BUFFER_MAX_LENGTH = 2 * 1024 * 1024;

    public static final int CACHE_FILE_NUMBER = 3;

    public static final int PRECACHE_SIZE = 300 * 1000;

    public final static String CONTENT_RANGE = "Content-Range";
    public final static String CONTENT_LENGTH = "Content-Length";
    public final static String RANGE = "Range";
    public final static String HOST = "Host";
    public final static String USER_AGENT = "User-Agent";

    public final static String RANGE_PARAMS = "bytes=";
    public final static String RANGE_PARAMS_0 = "bytes=0-";
    public final static String CONTENT_RANGE_PARAMS = "bytes ";

    public final static String LINE_BREAK = "\r\n";
    public final static String HTTP_END = LINE_BREAK + LINE_BREAK;
}

package com.hhmusic.proxy.utils;

import android.content.Context;
import android.util.Log;

import com.hhmusic.proxy.db.CacheFileInfoDao;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;

public class RequestDealThread extends Thread {
    private static final String LOG_TAG = RequestDealThread.class.getSimpleName();

    Socket client;
    HttpURLConnection request;

    ProxyFileUtils fileUtils;

    private int originRangeStart;

    private long realRangeStart;

    CacheFileInfoDao cacheDao;
    Context mContext;

    public RequestDealThread(Context context, HttpURLConnection request, Socket client) {
        this.request = request;
        this.client = client;
        cacheDao = CacheFileInfoDao.getInstance();
        mContext = context;
    }

    @Override
    public void run() {
        try {
            fileUtils = ProxyFileUtils.getInstance(mContext, request.getURL().toURI(), true);
            processRequest(request, client);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private int getRangeStart(HttpURLConnection request) {
        String value = request.getRequestProperty(Constants.RANGE);

        if (value != null) {
            return Integer.valueOf(value.substring(value.indexOf("bytes=") + 6, value.indexOf("-")));
        }
        return 0;
    }


    private void sendLocalHeaderAndCache(int rangeStart, int rangeEnd, int fileLength, byte[] audioCache)
            throws IOException {

        String httpString = HttpUtils.genResponseHeader(rangeStart, rangeEnd, fileLength);
        byte[] httpHeader = httpString.getBytes();
        client.getOutputStream().write(httpHeader, 0, httpHeader.length);

        if (audioCache != null && audioCache.length > 0) {
            client.getOutputStream().write(audioCache, 0, audioCache.length);
        }
    }

    private void processRequest(HttpURLConnection request, Socket client) throws IllegalStateException, IOException {
        if (request == null) {
            return;
        }

        try {
            byte[] audioCache = null;

            originRangeStart = getRangeStart(request);
            Log.i(LOG_TAG, "原始请求Range起始值：" + originRangeStart + " 本地缓存长度：" + fileUtils.getLength());

            int cacheFileSize = cacheDao.getFileSize(fileUtils.getFileName());

            if (fileUtils.isEnable() && fileUtils.getLength() == cacheFileSize) {
                audioCache = fileUtils.read(originRangeStart, Constants.AUDIO_BUFFER_MAX_LENGTH);
                sendLocalHeaderAndCache(originRangeStart, cacheFileSize - 1, cacheFileSize, audioCache);
                return;
            }

            if (fileUtils.isEnable() && originRangeStart < fileUtils.getLength()) {
                audioCache = fileUtils.read(originRangeStart, Constants.AUDIO_BUFFER_MAX_LENGTH);
                Log.i(LOG_TAG, "本地已缓存长度（跳过）:" + audioCache.length);

                realRangeStart = fileUtils.getLength();

                request.setRequestProperty(Constants.RANGE, Constants.RANGE_PARAMS + realRangeStart + "-");


            } else {
                realRangeStart = originRangeStart;
            }

            boolean isCacheEnough = audioCache != null && audioCache.length == Constants.AUDIO_BUFFER_MAX_LENGTH;


            if (isCacheEnough && cacheFileSize > 0) {
                sendLocalHeaderAndCache(originRangeStart, cacheFileSize - 1, cacheFileSize, audioCache);
            }

            else {
                HttpURLConnection realResponse = null;

                if (cacheFileSize <= 0) {
                    Log.d(LOG_TAG, "数据库未包含文件大小，发送请求");
                    realResponse = HttpUtils.send(request);
                    if (realResponse == null) {
                        return;
                    }
                    cacheFileSize = getContentLength(realResponse);
                }
                sendLocalHeaderAndCache(originRangeStart, cacheFileSize - 1, cacheFileSize, audioCache);

                if (realResponse == null) {
                    Log.d(LOG_TAG, "缓存不足，发送请求");
                    realResponse = HttpUtils.send(request);
                    if (realResponse == null) {
                        return;
                    }
                }
                Log.d(LOG_TAG, "接收ResponseContent");
                InputStream data = realResponse.getInputStream();
                if (!isCacheEnough) {
                    byte[] buff = new byte[1024 * 40];
                    boolean isPrint = true;
                    int fileLength = 0;
                    int readBytes;
                    while (Thread.currentThread() == MediaPlayerProxy.downloadThread
                            && (readBytes = data.read(buff, 0, buff.length)) != -1) {
                        long fileBufferLocation = fileLength + realRangeStart;
                        fileLength += readBytes;
                        long fileBufferEndLocation = fileLength + realRangeStart;

                        if (fileUtils.getLength() == fileBufferLocation) {
                            fileUtils.write(buff, readBytes);
                        }

                        if (System.currentTimeMillis() / 1000 % 2 == 0) {
                            if (isPrint) {
                                Log.d(LOG_TAG, "Cache Size:" + readBytes + " File Start:" + fileBufferLocation
                                        + "File End:" + fileBufferEndLocation);
                                isPrint = false;
                            }
                        } else {
                            isPrint = true;
                        }
                        client.getOutputStream().write(buff, 0, readBytes);
                    }
                }
            }
        } catch (SocketException e) {
            Log.i(LOG_TAG, "连接被终止", e);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            client.close();
            Log.i(LOG_TAG, "代理关闭");
        }
    }


    private int getContentLength(HttpURLConnection response) {
        int contentLength = 0;
        String range = request.getHeaderField(Constants.RANGE);
        if (range != null) {
            contentLength = Integer.valueOf(range.substring(range.indexOf("-") + 1, range.indexOf("/"))) + 1;
        } else {
            contentLength = request.getContentLength();
        }
        if (contentLength != 0) {
            cacheDao.insertOrUpdate(fileUtils.getFileName(), contentLength);
        }
        return contentLength;
    }
}

package com.hhmusic.proxy.utils;

import android.content.Context;
import android.util.Log;

import com.hhmusic.proxy.db.CacheFileInfoDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

public class ProxyFileUtils {
    private static final String LOG_TAG = ProxyFileUtils.class.getSimpleName();

    private static ProxyFileUtils fileUtilsByMediaPlayer;
    private static ProxyFileUtils fileUtilsByPreLoader;

    public static ProxyFileUtils getInstance(Context context, URI uri, boolean isUseByMediaPlayer) {

        String name = getValidFileName(uri);
        if (name == null || name.length() <= 0) {
            ProxyFileUtils utils = new ProxyFileUtils(context, null);
            utils.setEnableFalse();
            return utils;
        }

        if (!isUseByMediaPlayer) {
            if (fileUtilsByMediaPlayer != null && fileUtilsByMediaPlayer.getFileName().equals(name)) {
                ProxyFileUtils utils = new ProxyFileUtils(context, null);
                utils.setEnableFalse();
                return utils;
            }
        }

        if (isUseByMediaPlayer) {
            if (fileUtilsByPreLoader != null && fileUtilsByPreLoader.getFileName().equals(name)) {
                close(fileUtilsByPreLoader, false);
            }
            close(fileUtilsByMediaPlayer, true);
            fileUtilsByMediaPlayer = new ProxyFileUtils(context, name);
            return fileUtilsByMediaPlayer;
        } else {
            close(fileUtilsByPreLoader, false);
            fileUtilsByPreLoader = new ProxyFileUtils(context, name);
            return fileUtilsByPreLoader;
        }
    }

    private boolean isEnable;

    private FileOutputStream outputStream;
    private RandomAccessFile randomAccessFile;
    private File file;


    private boolean isSdAvaliable() {

        File dir = new File(Constants.DOWNLOAD_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.exists()) {
                return false;
            }
        }

        ProxyUtils.asynRemoveBufferFile(Constants.CACHE_FILE_NUMBER);

        long freeSize = ProxyUtils.getAvailaleSize(Constants.DOWNLOAD_PATH);
        return freeSize > Constants.SD_REMAIN_SIZE;
    }

    private ProxyFileUtils(Context context, String name) {
        if (name == null || name.length() <= 0) {
            isEnable = false;
            return;
        }

        if (!isSdAvaliable()) {
            isEnable = false;
            return;
        }

        try {

            file = new File(context.getCacheDir().getPath() + name);
            if (!file.exists()) {
                File dir = new File(Constants.DOWNLOAD_PATH);
                dir.mkdirs();
                file.createNewFile();
            }
            randomAccessFile = new RandomAccessFile(file, "r");
            outputStream = new FileOutputStream(file, true);
            isEnable = true;
        } catch (IOException e) {
            isEnable = false;
            Log.e(LOG_TAG, "文件操作失败，无SD？", e);
        }
    }

    public String getFileName() {
        return file.getName();
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnableFalse() {
        isEnable = false;
    }

    public int getLength() {
        if (isEnable) {
            return (int) file.length();
        } else {
            return -1;
        }
    }

    public boolean delete() {
        return file.delete();
    }

    public byte[] read(int startPos) {
        if (isEnable) {
            int byteCount = (getLength() - startPos);
            byte[] tmp = new byte[byteCount];
            try {
                randomAccessFile.seek(startPos);
                randomAccessFile.read(tmp);
                return tmp;
            } catch (IOException e) {
                Log.e(LOG_TAG, "缓存读取失败", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public byte[] read(int startPos, int length) {
        if (isEnable) {
            int byteCount = (getLength() - startPos);
            if (byteCount > length) {
                byteCount = length;
            }
            byte[] tmp = new byte[byteCount];
            try {
                randomAccessFile.seek(startPos);
                randomAccessFile.read(tmp);
                return tmp;
            } catch (IOException e) {
                Log.e(LOG_TAG, "缓存读取失败", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public void write(byte[] buffer, int byteCount) {
        if (isEnable) {
            try {
                outputStream.write(buffer, 0, byteCount);
                outputStream.flush();
            } catch (IOException e) {
                Log.e(LOG_TAG, "缓存写入失败", e);
            }
        }
    }

    public static void close(ProxyFileUtils fileUtils, boolean isUseByMediaPlayer) {
        if (isUseByMediaPlayer) {
            if (fileUtilsByMediaPlayer != null && fileUtilsByMediaPlayer == fileUtils) {
                fileUtilsByMediaPlayer.setEnableFalse();
                fileUtilsByMediaPlayer = null;
            }
        } else {
            if (fileUtilsByPreLoader != null && fileUtilsByPreLoader == fileUtils) {
                fileUtilsByPreLoader.setEnableFalse();
                fileUtilsByPreLoader = null;
            }
        }
    }

    public static void deleteFile(String url) {
        URI uri = URI.create(url);
        String name = getValidFileName(uri);
        boolean isSuccess = new File(name).delete();
        if (isSuccess) {
            CacheFileInfoDao.getInstance().delete(name);
        }
    }

    public static boolean isFinishCache(String url) {
        URI uri = URI.create(url);
        String name = getValidFileName(uri);
        File f = new File(name);

        if (!f.exists()) {
            return false;
        }
        return f.length() == CacheFileInfoDao.getInstance().getFileSize(name);
    }


    static protected String getValidFileName(URI uri) {
        String path = uri.getRawPath();
        String name = path.substring(path.lastIndexOf("/"));
        name = name.replace("\\", "");
        name = name.replace("/", "");
        name = name.replace(":", "");
        name = name.replace("*", "");
        name = name.replace("?", "");
        name = name.replace("\"", "");
        name = name.replace("<", "");
        name = name.replace(">", "");
        name = name.replace("|", "");
        name = name.replace(" ", "_"); // 前面的替换会产生空格,最后将其一并替换掉
        return name;
    }
}

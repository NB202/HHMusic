package com.hhmusic.uitl;

import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

import com.sun.mail.smtp.SMTPMessage;
import com.hhmusic.MainApplication;
import com.hhmusic.R;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;


public class CommonUtils {

    private static final String TAG = "CommonUtils";
    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public static String getUniquePsuedoID() {
        String serial = null;

        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();

            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {

            serial = "serial";
        }

        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }


    public static int getAPPVersionCode(Context ctx) {
        int currentVersionCode = 0;
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            String appVersionName = info.versionName;
            currentVersionCode = info.versionCode;
            System.out.println(currentVersionCode + " " + appVersionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentVersionCode;
    }

    public static String getDeviceInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("MODEL = " + Build.MODEL + "\n");
        builder.append("PRODUCT = " + Build.PRODUCT + "\n");
        builder.append("TAGS = " + Build.TAGS + "\n");
        builder.append("CPU_ABI" + Build.CPU_ABI + "\n");
        builder.append("BOARD = " + Build.BOARD + "\n");
        builder.append("BRAND = " + Build.BRAND + "\n");
        builder.append("DEVICE = " + Build.DEVICE + "\n");
        builder.append("DISPLAY = " + Build.DISPLAY + "\n");
        builder.append("ID = " + Build.ID + "\n");
        builder.append("VERSION.RELEASE = " + Build.VERSION.RELEASE + "\n");
        builder.append("Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT + "\n");
        builder.append("VERSION.BASE_OS = " + Build.VERSION.BASE_OS + "\n");
        builder.append("Build.VERSION.SDK = " + Build.VERSION.SDK + "\n");
        builder.append("APP.VERSION = " + getAPPVersionCode(MainApplication.context) + "\n");
        builder.append("\n" + "log:" + "\n");

        return builder.toString();
    }


    public static boolean sendTextMail(String title ,String content)
    {

        Log.d(TAG, "sendTextMail: ===================================");
        Log.d(TAG, "sendTextMail: ===================================");
        Log.d(TAG, "sendTextMail: " + title + content);
        Log.d(TAG, "sendTextMail: ===================================");
        Log.d(TAG, "sendTextMail: ===================================");
        try
        {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", "smtp.163.com");
            props.put("mail.smtp.auth", "true");
            Session session = Session.getInstance(props, null);
            Transport transport = session.getTransport("smtp");

            transport.connect("smtp.163.com", 25, "hhmusic_log@163.com",
                    "hhmusiclog1");
            Message mailMessage = new SMTPMessage(session);
            Address from = new InternetAddress("hhmusic_log@163.com");
            mailMessage.setFrom(from);
            Address to = new InternetAddress("hhmusic_log@163.com");
            mailMessage.setRecipient(Message.RecipientType.TO, to);
            mailMessage.setSubject(title);
            mailMessage.setSentDate(new Date());
            mailMessage.setText(content);
            transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
            return true;
        }
        catch (MessagingException ex)
        {
            ex.printStackTrace();

        }
        return false;
    }


    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }



    public static boolean isJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }




    public static boolean isFlyme() {
        try {
            // Invoke Build.hasSmartBar()
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }


    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }


    public static final String makeLabel(final Context context, final int pluralInt,
                                         final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }



    public static int getActionBarHeight(Context context) {
        int mActionBarHeight;
        TypedValue mTypedValue = new TypedValue();

        context.getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);

        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, context.getResources().getDisplayMetrics());

        return mActionBarHeight;
    }




    public static int getBlackWhiteColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness >= 0.5) {
            return Color.WHITE;
        } else return Color.BLACK;
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}

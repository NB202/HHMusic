package com.hhmusic;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.google.gson.Gson;
import com.hhmusic.permissions.Nammu;
import com.hhmusic.provider.PlaylistInfo;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.PreferencesUtility;
import com.hhmusic.uitl.ThemeHelper;


public class MainApplication extends Application implements ThemeUtils.switchColor {
    public static Context context;

    private static MainApplication sInstance = null;


    private static int MAX_MEM = (int) Runtime.getRuntime().maxMemory() / 4;

    private long favPlaylist = IConstants.FAV_PLAYLIST;
    private static Gson gson;

    public static Gson gsonInstance() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public static Context getInstance() {
       return MainApplication.sInstance;
    }


    private ImagePipelineConfig getConfigureCaches(Context context) {
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                MAX_MEM,
                Integer.MAX_VALUE,
                MAX_MEM,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE / 10);

        Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
            @Override
            public MemoryCacheParams get() {
                return bitmapCacheParams;
            }
        };
        ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true);
        builder.setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams);



        DiskCacheConfig diskSmallCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(context.getApplicationContext().getCacheDir())

                .build();


        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(Environment.getExternalStorageDirectory().getAbsoluteFile())

                .build();


        ImagePipelineConfig.Builder configBuilder = ImagePipelineConfig.newBuilder(context)

                .setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams)
                .setMainDiskCacheConfig(diskCacheConfig);


        return builder.build();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();

        imagePipeline.clearMemoryCaches();


    }


    private void frescoInit() {
        Fresco.initialize(this, getConfigureCaches(this));
    }







    @Override
    public void onCreate() {
        frescoInit();
        super.onCreate();

        sInstance = this;



        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Nammu.init(this);
        }
        ThemeUtils.setSwitchColor(this);



        if (!PreferencesUtility.getInstance(this).getFavriateMusicPlaylist()) {
            PlaylistInfo.getInstance(this).addPlaylist(favPlaylist, getResources().getString(R.string.my_fav_playlist),
                    0, "res:/" + R.mipmap.lay_protype_default, "local");
            PreferencesUtility.getInstance(this).setFavriateMusicPlaylist(true);
        }
    }

    @Override
    public int replaceColorById(Context context, @ColorRes int colorId) {
        if (ThemeHelper.isDefaultTheme(context)) {
            return context.getResources().getColor(colorId);
        }
        String theme = getTheme(context);
        if (theme != null) {
            colorId = getThemeColorId(context, colorId, theme);
        }
        return context.getResources().getColor(colorId);
    }

    @Override
    public int replaceColor(Context context, @ColorInt int originColor) {
        if (ThemeHelper.isDefaultTheme(context)) {
            return originColor;
        }
        String theme = getTheme(context);
        int colorId = -1;
        if (theme != null) {
            colorId = getThemeColor(context, originColor, theme);
        }
        return colorId != -1 ? getResources().getColor(colorId) : originColor;
    }

    private String getTheme(Context context) {
        if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_STORM) {
            return "blue";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_HOPE) {
            return "purple";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_WOOD) {
            return "green";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_LIGHT) {
            return "green_light";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_THUNDER) {
            return "yellow";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_SAND) {
            return "orange";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_FIREY) {
            return "red";
        }
        return null;
    }

    private
    @ColorRes
    int getThemeColorId(Context context, int colorId, String theme) {
        switch (colorId) {
            case R.color.theme_color_primary:
                return context.getResources().getIdentifier(theme, "color", getPackageName());
            case R.color.theme_color_primary_dark:
                return context.getResources().getIdentifier(theme + "_dark", "color", getPackageName());
            case R.color.playbarProgressColor:
                return context.getResources().getIdentifier(theme + "_trans", "color", getPackageName());
        }
        return colorId;
    }

    private
    @ColorRes
    int getThemeColor(Context context, int color, String theme) {
        switch (color) {
            case 0xd20000:
                return context.getResources().getIdentifier(theme, "color", getPackageName());
        }
        return -1;
    }

}
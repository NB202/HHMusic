package com.hhmusic.widget;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.hhmusic.R;
import com.hhmusic.entity.FocusItemInfo;
import com.hhmusic.api.BMA;
import com.hhmusic.api.HttpUtil;
import com.hhmusic.api.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class LoodView extends FrameLayout {
    private static final String TAG = "LoodView";

    private final static int IMAGE_COUNT = 4;

    private final static int TIME_INTERVAL = 3;

    private final static boolean isAutoPlay = true;

    private int[] imageResIds;
    private FPagerAdapter fPagerAdapter;
    private ArrayList<String> imageNet = new ArrayList<>();
    private List<ImageView> imageViewList;
    private List<View> dotViewList;
    private ViewPager viewPager;
    private boolean isFromCache = true;
    private Context mContext;

    private int currentItem = 0;

    private ScheduledExecutorService scheduledExecutorService;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            viewPager.setCurrentItem(currentItem);
        }
    };


    public LoodView(Context context) {
        super(context);
        mContext = context;
    }

    public LoodView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        mContext = context;
    }

    public LoodView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        mContext = context;
        initImageView();
        initUI(context);
        if (isAutoPlay) {
            startPlay();
        }

    }

    public void onDestroy() {
        scheduledExecutorService.shutdownNow();
        scheduledExecutorService = null;
    }


    public void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new LoopTask(), 1, TIME_INTERVAL, TimeUnit.SECONDS);
    }


    private void stopPlay() {
        scheduledExecutorService.shutdown();
    }


    private void initUI(Context context) {
        LayoutInflater.from(context).inflate(R.layout.load_view, this, true);
        for (String imagesID : imageNet) {
            final SimpleDraweeView mAlbumArt = new SimpleDraweeView(context);

            ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                    if (imageInfo == null) {
                        return;
                    }
                    QualityInfo qualityInfo = imageInfo.getQualityInfo();
                    FLog.d("Final image received! " +
                                    "Size %d x %d",
                            "Quality level %d, good enough: %s, full quality: %s",
                            imageInfo.getWidth(),
                            imageInfo.getHeight(),
                            qualityInfo.getQuality(),
                            qualityInfo.isOfGoodEnoughQuality(),
                            qualityInfo.isOfFullQuality());
                }

                @Override
                public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                    //FLog.d("Intermediate image received");
                }

                @Override
                public void onFailure(String id, Throwable throwable) {
                    mAlbumArt.setImageURI(Uri.parse("res:/" + R.drawable.placeholder_disk_210));
                }
            };
            Uri uri = null;
            try {
                uri = Uri.parse(imagesID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (uri != null) {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri).build();

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(mAlbumArt.getController())
                        .setImageRequest(request)
                        .setControllerListener(controllerListener)
                        .build();

                mAlbumArt.setController(controller);
            } else {
                mAlbumArt.setImageURI(Uri.parse("res:/" + R.drawable.placeholder_disk_210));
            }


            mAlbumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViewList.add(mAlbumArt);
        }
        dotViewList.add(findViewById(R.id.v_dot1));
        dotViewList.add(findViewById(R.id.v_dot2));
        dotViewList.add(findViewById(R.id.v_dot3));
        dotViewList.add(findViewById(R.id.v_dot4));

        viewPager = findViewById(R.id.viewPager);
        viewPager.setFocusable(true);
        fPagerAdapter = new FPagerAdapter();
        viewPager.setAdapter(fPagerAdapter);
        viewPager.addOnPageChangeListener(new MyPageChangeListener());
    }

    private void initImageView() {
        imageResIds = new int[]{
                R.mipmap.first,
                R.mipmap.second,
                R.mipmap.third,
                R.mipmap.fourth,
        };

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (NetworkUtils.isConnectInternet(mContext)) {
                    isFromCache = false;
                }

                try {
                    JsonArray rray = HttpUtil.getResposeJsonObject(BMA.focusPic(IMAGE_COUNT), mContext, isFromCache).get("pic").getAsJsonArray();
                    int en = rray.size();
                    //Log.e(TAG, "大小: "+en );
                    Gson gson = new Gson();

                    imageNet.clear();
                    for (int i = 0; i < en; i++) {
                        FocusItemInfo focusItemInfo = gson.fromJson(rray.get(i), FocusItemInfo.class);
                        if (focusItemInfo != null) {
                            imageNet.add(focusItemInfo.getRandpic());
                        } else {
                            imageNet.add("");
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                for (int i = 0; i < IMAGE_COUNT; i++) {
                    imageViewList.get(i).setImageURI(Uri.parse(imageNet.get(i)));
                }
            }
        }.execute();


        for (int i = 0; i < IMAGE_COUNT; i++) {
            imageNet.add("");
        }

        imageViewList = new ArrayList<>();
        dotViewList = new ArrayList<>();
    }

    private class FPagerAdapter extends PagerAdapter {


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(imageViewList.get(position));
            return imageViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViewList.get(position));
        }

        @Override
        public int getCount() {
            return imageViewList.size();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            super.restoreState(state, loader);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(ViewGroup container) {
            super.startUpdate(container);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        boolean isAutoPlay = false;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentItem = position;
            for (int i = 0; i < dotViewList.size(); i++) {
                if (i == position) {
                    dotViewList.get(i).setBackgroundResource(R.mipmap.red_point);
                } else {
                    dotViewList.get(i).setBackgroundResource(R.mipmap.grey_point);
                }
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {

                case 1:
                    isAutoPlay = false;
                    stopPlay();
                    startPlay();
                    break;

                case 2:
                    isAutoPlay = true;
                    break;

                case 0:
                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
                        viewPager.setCurrentItem(0);
                    } else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                    }
                    break;
            }

        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private class LoopTask implements Runnable {
        @Override
        public void run() {
            synchronized (viewPager) {
                currentItem = (currentItem + 1) % imageViewList.size();
                handler.obtainMessage().sendToTarget();

            }

        }
    }
}


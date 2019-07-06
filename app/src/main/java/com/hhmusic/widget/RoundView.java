package com.hhmusic.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hhmusic.R;


public class RoundView extends FrameLayout {
    private View mView;
    private SimpleDraweeView albumView;

    public RoundView(Context context) {
        super(context);
        initView(context);
    }

    public RoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    public static RoundView getView(Context context ,String str){
        RoundView view = new RoundView(context);
        view.setAlbum(str);
        Log.e("uqueue",str);
        return view;
    }

    private void initView(Context context){
        this.setAnimationCacheEnabled(false);
        mView = LayoutInflater.from(context).inflate(R.layout.fragment_roundimage,null);
        albumView = (SimpleDraweeView) mView.findViewById(R.id.sdv);
        addView(mView);

        rp.setRoundAsCircle(true);

        rp.setBorder(Color.BLACK, 6);
        albumView.setHierarchy(hierarchy);
    }

    public void setAlbum(String albumPath){
        if (albumPath == null) {
            albumView.setImageURI(Uri.parse("res:/" + R.drawable.placeholder_disk_play_song));
        } else {
            try {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(albumPath)).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(albumView.getController())
                        .setImageRequest(request)
                        .setControllerListener(controllerListener)
                        .build();
                albumView.setController(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("roundview","setalbum = " + albumPath);

    }


    RoundingParams rp = new RoundingParams();



    GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())

            .setRoundingParams(rp)

            .setFadeDuration(300)

            .build();

    ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {

        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            albumView.setImageURI(Uri.parse("res:/" + R.drawable.placeholder_disk_play_song));
        }
    };


}

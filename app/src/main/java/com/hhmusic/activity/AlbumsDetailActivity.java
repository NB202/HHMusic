package com.hhmusic.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nineoldandroids.view.ViewHelper;
import com.hhmusic.MainApplication;
import com.hhmusic.R;
import com.hhmusic.dialog.LoadAllDownInfos;
import com.hhmusic.fragment.MoreFragment;
import com.hhmusic.fragment.NetMoreFragment;
import com.hhmusic.log.HandlerUtil;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.entity.AlbumInfo;
import com.hhmusic.entity.GeDanGeInfo;
import com.hhmusic.entity.MusicDetailInfo;
import com.hhmusic.api.BMA;
import com.hhmusic.api.HttpUtil;
import com.hhmusic.api.MusicDetailInfoGet;
import com.hhmusic.api.NetworkUtils;
import com.hhmusic.api.RequestThreadPool;
import com.hhmusic.service.MusicPlayer;
import com.hhmusic.uitl.CommonUtils;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.ImageUtils;
import com.hhmusic.uitl.L;
import com.hhmusic.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;


public class AlbumsDetailActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private String albumId;
    private String albumPath, albumName, albumDes;
    private ArrayList<GeDanGeInfo> mList = new ArrayList<GeDanGeInfo>();
    private ArrayList<MusicInfo> adapterList = new ArrayList<>();

    private SimpleDraweeView albumArtSmall;
    private ImageView albumArt;
    private TextView albumTitle, tryAgain;

    private PlaylistDetailAdapter mAdapter;
    private Toolbar toolbar;
    private SparseArray<MusicDetailInfo> sparseArray = new SparseArray<MusicDetailInfo>();
    private FrameLayout loadFrameLayout;
    private int musicCount;
    private Handler mHandler;
    private View loadView;
    private int mFlexibleSpaceImageHeight;
    private ActionBar actionBar;
    private int mActionBarSize;
    private int mStatusSize;
    private FrameLayout headerViewContent;
    private RelativeLayout headerDetail;
    private LoadNetPlaylistInfo mLoadNetList;
    private ObservableRecyclerView recyclerView;
    private String TAG = "AlbumsDetailActivity";
    private boolean d = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (getIntent().getExtras() != null) {
            albumId = getIntent().getStringExtra("albumid");
            albumPath = getIntent().getStringExtra("albumart");
            albumName = getIntent().getStringExtra("albumname");
            albumDes = getIntent().getStringExtra("albumdetail");
        }
        setContentView(R.layout.activity_playlist);
        loadFrameLayout = findViewById(R.id.state_container);

        headerViewContent = findViewById(R.id.headerview);
        headerDetail = findViewById(R.id.headerdetail);

        toolbar = findViewById(R.id.toolbar);
        mHandler = HandlerUtil.getInstance(this);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mActionBarSize = CommonUtils.getActionBarHeight(this);
        mStatusSize = CommonUtils.getStatusHeight(this);


        tryAgain = findViewById(R.id.try_again);

        setUpEverything();

    }

    private void setUpEverything() {
        setupToolbar();
        setHeaderView();
        setAlbumart();
        setList();
        loadAllLists();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.actionbar_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("歌单");
        toolbar.setPadding(0, mStatusSize, 0, 0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setSubtitle(albumDes);

    }

    private void setHeaderView() {
        albumArt = findViewById(R.id.album_art);
        albumTitle = findViewById(R.id.album_title);
        albumArtSmall = findViewById(R.id.playlist_art);
        LinearLayout downAll = headerViewContent.findViewById(R.id.playlist_down);
        downAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoadAllDownInfos(AlbumsDetailActivity.this, mList).execute();
            }
        });

        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAllLists();
            }
        });
    }


    private void setList() {
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setScrollViewCallbacks(AlbumsDetailActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(AlbumsDetailActivity.this));
        recyclerView.setHasFixedSize(false);
        mAdapter = new PlaylistDetailAdapter(AlbumsDetailActivity.this, adapterList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(AlbumsDetailActivity.this, DividerItemDecoration.VERTICAL_LIST));
    }


    protected void updateViews(int scrollY, boolean animated) {

        ViewHelper.setTranslationY(headerViewContent, getHeaderTranslationY(scrollY));

    }

    protected float getHeaderTranslationY(int scrollY) {
        final int headerHeight = headerViewContent.getHeight();
        Log.e("hei", "  " + headerHeight);
        int headerTranslationY = mActionBarSize + mStatusSize - headerHeight;
        if (mActionBarSize + mStatusSize <= -scrollY + headerHeight) {
            headerTranslationY = -scrollY;
        }
        Log.e("headerY", "  " + headerTranslationY);
        return headerTranslationY;
    }


    private void loadAllLists() {


        if (NetworkUtils.isConnectInternet(this)) {
            tryAgain.setVisibility(View.GONE);
            loadView = LayoutInflater.from(this).inflate(R.layout.loading, loadFrameLayout, false);
            loadFrameLayout.addView(loadView);
            mLoadNetList = new LoadNetPlaylistInfo();
            mLoadNetList.execute();

        } else {
            tryAgain.setVisibility(View.VISIBLE);

        }

    }

    AlbumInfo albumInfo;
    class LoadNetPlaylistInfo extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(final Void... unused) {
            try {
                JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Album.albumInfo(albumId + ""));
                JsonArray pArray = jsonObject.get("songlist").getAsJsonArray();
                mHandler.post(showInfo);
                musicCount = pArray.size();

                for (int i = 0; i < musicCount; i++) {
                    GeDanGeInfo geDanGeInfo = MainApplication.gsonInstance().fromJson(pArray.get(i), GeDanGeInfo.class);
                    mList.add(geDanGeInfo);
                    RequestThreadPool.post(new MusicDetailInfoGet(geDanGeInfo.getSong_id(), i, sparseArray));
                }

                int tryCount = 0;
                while (sparseArray.size() != musicCount && tryCount < 1000 && !isCancelled()){
                    tryCount++;
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(sparseArray.size() == musicCount){
                    for (int i = 0; i < mList.size(); i++) {
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.songId = Integer.parseInt(mList.get(i).getSong_id());
                        musicInfo.musicName = mList.get(i).getTitle();
                        musicInfo.artist = sparseArray.get(i).getArtist_name();
                        musicInfo.islocal = false;
                        musicInfo.albumName = sparseArray.get(i).getAlbum_title();
                        musicInfo.albumId = Integer.parseInt(mList.get(i).getAlbum_id());
                        musicInfo.artistId = Integer.parseInt(sparseArray.get(i).getArtist_id());
                        musicInfo.lrc = sparseArray.get(i).getLrclink();
                        musicInfo.albumData = sparseArray.get(i).getPic_radio();
                        adapterList.add(musicInfo);
                    }
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean complete) {

            if (!complete) {
                loadFrameLayout.removeAllViews();
                tryAgain.setVisibility(View.VISIBLE);
            } else {
                loadFrameLayout.removeAllViews();
                recyclerView.setVisibility(View.VISIBLE);
                mAdapter.updateDataSet(adapterList);

            }

        }

        public void cancleTask(){
            cancel(true);
            RequestThreadPool.finish();
        }
    }

    Runnable showInfo = new Runnable() {
        @Override
        public void run() {
            headerDetail.setVisibility(View.VISIBLE);
        }
    };


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadNetList != null){
            mLoadNetList.cancleTask();
        }
    }

    @Override
    public void updateTrack() {
        mAdapter.notifyDataSetChanged();
    }


    private void setAlbumart() {
        albumTitle.setText(albumName);
        albumArtSmall.setImageURI(Uri.parse(albumPath));
        try {
            ImageRequest imageRequest = ImageRequest.fromUri(albumPath);


            imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(albumPath))
                    .setProgressiveRenderingEnabled(true).build();
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            DataSource<CloseableReference<CloseableImage>>
                    dataSource = imagePipeline.fetchDecodedImage(imageRequest, AlbumsDetailActivity.this);

            dataSource.subscribe(new BaseBitmapDataSubscriber() {

                                     @Override
                                     public void onNewResultImpl(@Nullable Bitmap bitmap) {

                                         if (bitmap != null) {
                                             new setBlurredAlbumArt().execute(bitmap);
                                         }
                                     }

                                     @Override
                                     public void onFailureImpl(DataSource dataSource) {


                                     }
                                 },
                    CallerThreadExecutor.getInstance());





        } catch (Exception e) {

        }

    }


    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;

            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], AlbumsDetailActivity.this, 20);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (albumArt.getDrawable() != null) {
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[]{
                                    albumArt.getDrawable(),
                                    result
                            });
                    albumArt.setImageDrawable(td);
                    td.startTransition(200);

                } else {
                    albumArt.setImageDrawable(result);
                }
            }
        }
    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

        updateViews(scrollY, false);

        if (scrollY > 0 && scrollY < mFlexibleSpaceImageHeight - mActionBarSize - mStatusSize) {
            toolbar.setTitle(albumName);
            toolbar.setSubtitle(albumDes);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.toolbar_background));
        }
        if (scrollY == 0) {
            toolbar.setTitle("歌单");
            actionBar.setBackgroundDrawable(null);
        }
        if (scrollY > mFlexibleSpaceImageHeight - mActionBarSize - mStatusSize) {


        }

        float a = (float) scrollY / (mFlexibleSpaceImageHeight - mActionBarSize - mStatusSize);
        headerDetail.setAlpha(1f - a);
        Log.e("alpha", " " + a);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }


    class PlaylistDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private ArrayList<MusicInfo> arraylist;
        private Activity mContext;

        public PlaylistDetailAdapter(Activity context, ArrayList<MusicInfo> mList) {
            this.arraylist = mList;
            this.mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == FIRST_ITEM) {
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_common_item, viewGroup, false));
            } else {
                return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_playlist_detail_item, viewGroup, false));
            }
        }


        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder itemHolder, final int i) {
            if (itemHolder instanceof ItemViewHolder) {
                final MusicInfo localItem = arraylist.get(i - 1);

                if (MusicPlayer.getCurrentAudioId() == localItem.songId) {
                    ((ItemViewHolder) itemHolder).trackNumber.setVisibility(View.GONE);
                    ((ItemViewHolder) itemHolder).playState.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) itemHolder).playState.setImageResource(R.drawable.song_play_icon);
                    ((ItemViewHolder) itemHolder).playState.setImageTintList(R.color.theme_color_primary);
                } else {
                    ((ItemViewHolder) itemHolder).playState.setVisibility(View.GONE);
                    ((ItemViewHolder) itemHolder).trackNumber.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) itemHolder).trackNumber.setText(i + "");
                }
                ((ItemViewHolder) itemHolder).title.setText(localItem.musicName);
                ((ItemViewHolder) itemHolder).artist.setText(localItem.artist);
                ((ItemViewHolder) itemHolder).menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.e("re", "get");
                        HttpUtil.getResposeJsonObject("http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.8.1.0&channel=ppzs&operator=3&method=baidu.ting.artist.item&format=entity&tinguid=1035&artistid=14");

                        if (localItem.islocal) {
                            MoreFragment morefragment = MoreFragment.newInstance(arraylist.get(i - 1),
                                    IConstants.MUSICOVERFLOW);
                            morefragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "music");
                        } else {
                            NetMoreFragment morefragment = NetMoreFragment.newInstance(arraylist.get(i - 1),
                                    IConstants.MUSICOVERFLOW);
                            morefragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "music");
                        }

                    }
                });

            } else if (itemHolder instanceof CommonItemViewHolder) {

                ((CommonItemViewHolder) itemHolder).textView.setText("(共" + arraylist.size() + "首)");

                ((CommonItemViewHolder) itemHolder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }

        }

        @Override
        public int getItemCount() {
            return arraylist == null ? 0 : arraylist.size() + 1;
        }

        public void updateDataSet(ArrayList<MusicInfo> arraylist) {
            this.arraylist = arraylist;
            this.notifyDataSetChanged();
        }

        public class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView textView;
            ImageView select;
            RelativeLayout layout;

            CommonItemViewHolder(View view) {
                super(view);
                this.textView = view.findViewById(R.id.play_all_number);
                this.select = view.findViewById(R.id.select);
                this.layout = view.findViewById(R.id.play_all_layout);
                layout.setOnClickListener(this);
            }

            public void onClick(View v) {

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<Long, MusicInfo> infos = new HashMap<Long, MusicInfo>();
                        int len = arraylist.size();
                        long[] list = new long[len];
                        for (int i = 0; i < len; i++) {
                            MusicInfo info = arraylist.get(i);
                            list[i] = info.songId;
                            infos.put(list[i], info);
                        }

                        if (getAdapterPosition() > -1)
                            MusicPlayer.playAll(infos, list, 0, false);
                    }
                }, 70);

            }

        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            protected TextView title, artist, trackNumber;
            protected ImageView menu;
            TintImageView playState;

            public ItemViewHolder(View view) {
                super(view);
                this.title = view.findViewById(R.id.song_title);
                this.artist = view.findViewById(R.id.song_artist);
                this.trackNumber = view.findViewById(R.id.trackNumber);
                this.menu = view.findViewById(R.id.popup_menu);
                this.playState = view.findViewById(R.id.play_state);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<Long, MusicInfo> infos = new HashMap<Long, MusicInfo>();
                        int len = arraylist.size();
                        long[] list = new long[len];
                        for (int i = 0; i < len; i++) {
                            MusicInfo info = arraylist.get(i);
                            list[i] = info.songId;
                            infos.put(list[i], info);
                        }

                        if (getAdapterPosition() > 0)
                            MusicPlayer.playAll(infos, list, getAdapterPosition() - 1, false);
                    }
                }, 70);
            }

        }
    }
    PlayMusic playMusic;
    public class PlayMusic extends Thread {
        private volatile boolean isInterrupted = false;
        private ArrayList<MusicInfo> arrayList;
        public PlayMusic(ArrayList<MusicInfo> arrayList){
            this.arrayList = arrayList;
        }
        public void interrupt(){
            isInterrupted = true;
            super.interrupt();
        }

        public void run(){
            L.D(d,TAG, " start");
            while(!isInterrupted){
                HashMap<Long, MusicInfo> infos = new HashMap<Long, MusicInfo>();
                int len = arrayList.size();
                long[] list = new long[len];
                for (int i = 0; i < len; i++) {
                    MusicInfo info = arrayList.get(i);
                    list[i] = info.songId;
                    infos.put(list[i], info);
                }
                MusicPlayer.playAll(infos, list, 0, false);

            }
            L.D(d,TAG, "已经终止!");
        }
    }
}

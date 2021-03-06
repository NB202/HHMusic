package com.hhmusic.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nineoldandroids.view.ViewHelper;
import com.hhmusic.MainApplication;
import com.hhmusic.R;
import com.hhmusic.dialog.LoadAllDownInfos;
import com.hhmusic.dialog.LoadAllNeteaseDownInfos;
import com.hhmusic.fragment.MoreFragment;
import com.hhmusic.fragment.NetMoreFragment;
import com.hhmusic.log.HandlerUtil;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.entity.GeDanGeInfo;
import com.hhmusic.entity.MusicDetailInfo;
import com.hhmusic.entity.NeteaseSongList;
import com.hhmusic.entity.NeteaseSongList.ResultBean.TracksBean;
import com.hhmusic.api.API;
import com.hhmusic.api.BMA;
import com.hhmusic.api.HttpUtil;
import com.hhmusic.api.MusicDetailInfoGet;
import com.hhmusic.api.NetworkUtils;
import com.hhmusic.api.RequestThreadPool;
import com.hhmusic.provider.PlaylistInfo;
import com.hhmusic.provider.PlaylistsManager;
import com.hhmusic.service.MusicPlayer;
import com.hhmusic.uitl.CommonUtils;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.ImageUtils;
import com.hhmusic.uitl.L;
import com.hhmusic.widget.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PlaylistActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private String playlsitId;
    private String albumPath, playlistName, playlistDetail;

    private ArrayList<GeDanGeInfo> mList = new ArrayList<>();
    private List<TracksBean> mTracksBeanArrayList = new ArrayList<>();

    private ArrayList<MusicInfo> adapterList = new ArrayList<>();

    private SimpleDraweeView albumArtSmall;
    private ImageView albumArt;
    private TextView playlistTitleView, playlistDetailView;
    private boolean isLocalPlaylist;

    private PlaylistDetailAdapter mAdapter;
    private Toolbar toolbar;
    private SparseArray<MusicDetailInfo> sparseArray = new SparseArray<>();
    private FrameLayout loadFrameLayout;
    private int musicCount;
    private Handler mHandler;
    private View loadView;
    private int mFlexibleSpaceImageHeight;
    private ActionBar actionBar;
    private int mActionBarSize;
    private int mStatusSize;
    private TextView tryAgain;
    private TextView playlistCountView;
    private String playlistCount;
    private FrameLayout headerViewContent;
    private RelativeLayout headerDetail;
    private Context mContext;
    private boolean mCollected;
    private TextView collectText;
    private ImageView collectView;
    private FrameLayout favLayout;
    private LinearLayout share;

    private LoadLocalPlaylistInfo mLoadLocalList;
    private LoadNetPlaylistInfo mLoadNetList;
    private LoadNeteasePlaylistInfo mLoadNeteaseList;

    private ObservableRecyclerView recyclerView;
    private String TAG = "PlaylistActivity";
    private boolean d = true;
    private boolean isFromNetease;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

            if (getIntent().getExtras() != null) {
            isLocalPlaylist = getIntent().getBooleanExtra("islocal", false);
            playlsitId = getIntent().getStringExtra("playlistid");
            albumPath = getIntent().getStringExtra("albumart");
            playlistName = getIntent().getStringExtra("playlistname");
            playlistDetail = getIntent().getStringExtra("playlistDetail");
            playlistCount = getIntent().getStringExtra("playlistcount");
            isFromNetease = getIntent().getBooleanExtra("isFromNetease", false);
            Log.d(TAG, "onCreate: " + getIntent().getExtras().toString());
        }
        mContext = this;
        setContentView(R.layout.activity_playlist);
        loadFrameLayout = findViewById(R.id.state_container);

        headerViewContent = findViewById(R.id.headerview);
        headerDetail = findViewById(R.id.headerdetail);
        favLayout = findViewById(R.id.playlist_fav);

        toolbar = findViewById(R.id.toolbar);
        mHandler = HandlerUtil.getInstance(this);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mActionBarSize = CommonUtils.getActionBarHeight(this);
        mStatusSize = CommonUtils.getStatusHeight(this);


        tryAgain = findViewById(R.id.try_again);
        collectText = findViewById(R.id.playlist_collect_state);
        collectView = findViewById(R.id.playlist_collect_view);
        share = findViewById(R.id.playlist_share);

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
        if (!isLocalPlaylist) {
            toolbar.setSubtitle(playlistDetail);
        }

    }


    private void setHeaderView() {
        albumArt = findViewById(R.id.album_art);
        playlistTitleView = findViewById(R.id.album_title);
        playlistDetailView = findViewById(R.id.album_details);
        albumArtSmall = findViewById(R.id.playlist_art);
        SpannableString spanString;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.index_icn_earphone);
        ImageSpan imgSpan = new ImageSpan(this, b, ImageSpan.ALIGN_BASELINE);
        spanString = new SpannableString("icon");
        spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        playlistCountView = findViewById(R.id.playlist_listen_count);
        playlistCountView.setText(spanString);
        if (playlistCount == null) {
            playlistCount = "0";
        }
        int count = Integer.parseInt(playlistCount);
        if (count > 10000) {
            count = count / 10000;
            playlistCountView.append(" " + count + "万");
        } else {
            playlistCountView.append(" " + playlistCount);
        }
        LinearLayout downAll = headerViewContent.findViewById(R.id.playlist_down);


        downAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFromNetease) {
                    new LoadAllNeteaseDownInfos((Activity) mContext, mTracksBeanArrayList).execute();
                } else {
                    new LoadAllDownInfos((Activity) mContext, mList).execute();
                }

            }
        });

        final LinearLayout addToplaylist = headerViewContent.findViewById(R.id.playlist_collect);
        addToplaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCollected) {
                    collectText.setText("已收藏");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            String albumart = null;
                            for (MusicInfo info : adapterList) {
                                albumart = info.albumData;
                                if (!TextUtils.isEmpty(albumart)) {
                                    break;
                                }
                            }
                            PlaylistInfo.getInstance(mContext).addPlaylist(Long.parseLong(playlsitId), playlistName,
                                    adapterList.size(), albumart, "api");
                            PlaylistsManager.getInstance(mContext).insertLists(mContext, Long.parseLong(playlsitId), adapterList);
                            Intent intent = new Intent(IConstants.PLAYLIST_COUNT_CHANGED);
                            MainApplication.context.sendBroadcast(intent);

                            mCollected = true;
                            return null;
                        }
                    }.execute();

                } else {
                    collectText.setText("收藏");
                    PlaylistInfo.getInstance(mContext).deletePlaylist(Long.parseLong(playlsitId));
                    mCollected = false;
                }

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("http://music.baidu.com/songlist/" + playlsitId));
                shareIntent.setType("html/*");
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.shared_to)));
            }
        });

        if (!isLocalPlaylist)
            headerDetail.setVisibility(View.GONE);


        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAllLists();
            }
        });

        long  val = Long.parseLong(playlsitId);
        if (val == IConstants.FAV_PLAYLIST) {
            favLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setList() {
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setScrollViewCallbacks(PlaylistActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(PlaylistActivity.this));
        recyclerView.setHasFixedSize(true);
        mAdapter = new PlaylistDetailAdapter(PlaylistActivity.this, adapterList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(PlaylistActivity.this, DividerItemDecoration.VERTICAL_LIST));
    }


    protected void updateViews(int scrollY, boolean animated) {

        ViewHelper.setTranslationY(headerViewContent, getHeaderTranslationY(scrollY));

    }

    protected float getHeaderTranslationY(int scrollY) {
        final int headerHeight = headerViewContent.getHeight();
        int headerTranslationY = mActionBarSize + mStatusSize - headerHeight;
        if (mActionBarSize + mStatusSize <= -scrollY + headerHeight) {
            headerTranslationY = -scrollY;
        }
        return headerTranslationY;
    }


    private void loadAllLists() {

        if (isLocalPlaylist) {
            loadView = LayoutInflater.from(this).inflate(R.layout.loading, loadFrameLayout, false);
            loadFrameLayout.addView(loadView);
            mLoadLocalList = new LoadLocalPlaylistInfo();
            mLoadLocalList.execute();
            return;
        }

        if (NetworkUtils.isConnectInternet(this)) {
            tryAgain.setVisibility(View.GONE);
            loadView = LayoutInflater.from(this).inflate(R.layout.loading, loadFrameLayout, false);
            loadFrameLayout.addView(loadView);

            if (isFromNetease) {
                mLoadNeteaseList = new LoadNeteasePlaylistInfo();
                mLoadNeteaseList.execute();
            } else {

                mLoadNetList = new LoadNetPlaylistInfo();
                mLoadNetList.execute();
            }
        } else {
            tryAgain.setVisibility(View.VISIBLE);

        }

    }

    class LoadLocalPlaylistInfo extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(final Void... unused) {
            adapterList = PlaylistsManager.getInstance(mContext).getMusicInfos(Long.parseLong(playlsitId));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadFrameLayout.removeAllViews();
            mAdapter.updateDataSet(adapterList);
        }
    }

    @Override
    public void updateTrack() {
        mAdapter.notifyDataSetChanged();
    }


    class LoadNetPlaylistInfo extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final Void... unused) {
            try {
                JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.GeDan.geDanInfo(playlsitId + ""));
                JsonArray pArray = jsonObject.get("content").getAsJsonArray();

                mCollected = PlaylistInfo.getInstance(mContext).hasPlaylist(Long.parseLong(playlsitId));
                playlistDetail = jsonObject.get("desc").getAsString();
                mHandler.post(showInfo);

                musicCount = pArray.size();
                for (int i = 0; i < musicCount; i++) {
                    GeDanGeInfo geDanGeInfo = MainApplication.gsonInstance().fromJson(pArray.get(i), GeDanGeInfo.class);
                    mList.add(geDanGeInfo);
                    RequestThreadPool.post(new MusicDetailInfoGet(geDanGeInfo.getSong_id(), i, sparseArray));
                }
                int tryCount = 0;
                while (sparseArray.size() != musicCount && tryCount < 1000 && !isCancelled()) {
                    tryCount++;
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                if (sparseArray.size() == musicCount) {
                    for (int i = 0; i < mList.size(); i++) {
                        try {
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

        public void cancleTask() {

            cancel(true);
            RequestThreadPool.finish();
            Log.e(TAG, " cancled task , + thread" + Thread.currentThread().getName());
        }
    }


    class LoadNeteasePlaylistInfo extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final Void... unused) {
            try {
                JsonObject jsonObject = HttpUtil.getNeteaseResposeJsonObject(API.geDanInfo(playlsitId));
                NeteaseSongList songList = MainApplication.gsonInstance().fromJson(jsonObject, NeteaseSongList.class);


                mCollected = PlaylistInfo.getInstance(mContext).hasPlaylist(Long.parseLong(playlsitId));
                playlistDetail = songList.getResult().getDescription();
                mHandler.post(showInfo);

                musicCount = songList.getResult().getTracks().size();
                mTracksBeanArrayList = songList.getResult().getTracks();
                for (int i = 0; i < musicCount; ++i) {
                    try {
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.songId = mTracksBeanArrayList.get(i).getId();
                        musicInfo.musicName = mTracksBeanArrayList.get(i).getName();
                        musicInfo.artist = mTracksBeanArrayList.get(i).getArtists().get(0).getName();
                        musicInfo.islocal = false;
                        musicInfo.albumName = mTracksBeanArrayList.get(i).getAlbum().getName();
                        musicInfo.albumId = mTracksBeanArrayList.get(i).getAlbum().getId();
                        musicInfo.artistId = mTracksBeanArrayList.get(i).getArtists().get(0).getId();
                        if (musicInfo.lrc == null)
                            musicInfo.lrc = API.getLyricqq(musicInfo.songId + "");
                        if (musicInfo.lrc == null)
                            musicInfo.lrc = API.getLyricwy(musicInfo.songId + "");
                        if (musicInfo.lrc == null)
                            musicInfo.lrc = API.getLyrickugou(musicInfo.songId + "");
                        if (musicInfo.lrc == null)
                            musicInfo.lrc = API.getLyrickuwo(musicInfo.songId + "");
                        if (musicInfo.lrc == null)
                            musicInfo.lrc = API.getLyricbaidu(musicInfo.songId + "");
                        musicInfo.albumData = mTracksBeanArrayList.get(i).getAlbum().getBlurPicUrl();
                        adapterList.add(musicInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
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

        void cancleTask() {

            cancel(true);
            RequestThreadPool.finish();
            Log.e(TAG, " cancled task , + thread" + Thread.currentThread().getName());
        }
    }

    Runnable showInfo = new Runnable() {
        @Override
        public void run() {
            playlistDetailView.setText(playlistDetail);
            headerDetail.setVisibility(View.VISIBLE);
            if (mCollected) {
                L.D(d, TAG, "collected");
                collectText.setText("已收藏");
            }
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

        if (mLoadNetList != null) {
            mLoadNetList.cancleTask();
        }
        if (mLoadNeteaseList != null) {
            mLoadNeteaseList.cancleTask();
        }
        if (mLoadLocalList != null) {
            mLoadLocalList.cancel(true);
        }
        super.onDestroy();
    }

    private void setAlbumart() {
        playlistTitleView.setText(playlistName);

        if (albumPath == null) {
            albumArtSmall.setImageResource(R.drawable.placeholder_disk_210);
        } else {
            albumArtSmall.setImageURI(Uri.parse(albumPath));
        }

        try {

            if (isLocalPlaylist && !URLUtil.isNetworkUrl(albumPath)) {
                new setBlurredAlbumArt().execute(ImageUtils.getArtworkQuick(PlaylistActivity.this, Uri.parse(albumPath), 300, 300));
                L.D(d, TAG, "albumpath = " + albumPath);
            } else {

                ImageRequest imageRequest = ImageRequest.fromUri(albumPath);
                CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                        .getEncodedCacheKey(imageRequest);
                BinaryResource resource = ImagePipelineFactory.getInstance()
                        .getMainDiskStorageCache().getResource(cacheKey);
                File file = ((FileBinaryResource) resource).getFile();
                if (file != null)
                    new setBlurredAlbumArt().execute(ImageUtils.getArtworkQuick(file, 300, 300));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;

            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], PlaylistActivity.this, 20);
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
            toolbar.setTitle(playlistName);
            toolbar.setSubtitle(playlistDetail);
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
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.playlit_menu, menu);

        return super.onCreateOptionsMenu(menu);
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

    private PlayMusic mPlay;
    private volatile boolean tryPlaying = false;

    public class PlayMusic extends Thread {
        private volatile boolean isInterrupted = false;
        private ArrayList<MusicInfo> arrayList;
        private int position;

        public PlayMusic(ArrayList<MusicInfo> arrayList, int position) {
            this.arrayList = arrayList;
            this.position = position;
        }

        public void interrupt() {
            isInterrupted = true;
            super.interrupt();
        }

        public void run() {
            L.D(d, TAG, " start");
            tryPlaying = true;
            while (!isInterrupted) {
                HashMap<Long, MusicInfo> infos = new HashMap<Long, MusicInfo>();
                int len = arrayList.size();
                long[] list = new long[len];
                for (int i = 0; i < len; i++) {
                    MusicInfo info = arrayList.get(i);
                    list[i] = info.songId;
                    infos.put(list[i], info);
                }
                MusicPlayer.playAll(infos, list, position, false);
            }
            tryPlaying = false;
            L.D(d, TAG, "已经终止!");
        }
    }
}

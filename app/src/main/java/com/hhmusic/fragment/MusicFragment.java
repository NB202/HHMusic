package com.hhmusic.fragment;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.github.promeg.pinyinhelper.Pinyin;
import com.hhmusic.R;
import com.hhmusic.activity.SelectActivity;
import com.hhmusic.log.HandlerUtil;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.service.MusicPlayer;
import com.hhmusic.uitl.Comparator.MusicComparator;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.MusicUtils;
import com.hhmusic.uitl.PreferencesUtility;
import com.hhmusic.uitl.SortOrder;
import com.hhmusic.widget.DividerItemDecoration;
import com.hhmusic.widget.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MusicFragment extends BaseFragment {
    private Adapter mAdapter;
    private ArrayList<MusicInfo> musicInfos;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private PreferencesUtility mPreferences;
    private FrameLayout frameLayout;
    private View view;
    private boolean isFirstLoad = true;
    private SideBar sideBar;
    private TextView dialogText;
    private HashMap<String, Integer> positionMap = new HashMap<>();
    private boolean isAZSort = true;


    private void loadView() {

        if (view == null && mContext != null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.recylerview, frameLayout, false);

            dialogText = view.findViewById(R.id.dialog_text);
            recyclerView = view.findViewById(R.id.recyclerview);
            layoutManager = new LinearLayoutManager(mContext);
            recyclerView.setLayoutManager(layoutManager);
            mAdapter = new Adapter(null);
            recyclerView.setAdapter(mAdapter);
            recyclerView.setHasFixedSize(true);

            recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));

            sideBar = view.findViewById(R.id.sidebar);
            sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
                @Override
                public void onTouchingLetterChanged(String s) {
                    dialogText.setText(s);
                    sideBar.setView(dialogText);
                    if (positionMap.get(s) != null) {
                        int i = positionMap.get(s);
                        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(i + 1, 0);
                    }

                }
            });
            reloadAdapter();
            Log.e("MusicFragment", "load l");
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            loadView();
        }

    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                sideBar.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.load_framelayout, container, false);
        frameLayout = view.findViewById(R.id.loadframe);
        View loadView = LayoutInflater.from(mContext).inflate(R.layout.loading, frameLayout, false);
        frameLayout.addView(loadView);
        isFirstLoad = true;
        isAZSort = mPreferences.getSongSortOrder().equals(SortOrder.SongSortOrder.SONG_A_Z);

        if(getUserVisibleHint()){
            loadView();
        }

        return view;
    }



    public void reloadAdapter() {
        if (mAdapter == null) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                isAZSort = mPreferences.getSongSortOrder().equals(SortOrder.SongSortOrder.SONG_A_Z);
                ArrayList<MusicInfo> songList = (ArrayList) MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL);

                if (isAZSort) {
                    Collections.sort(songList, new MusicComparator());
                    for (int i = 0; i < songList.size(); i++) {
                        if (positionMap.get(songList.get(i).sort) == null)
                            positionMap.put(songList.get(i).sort, i);
                    }
                }
                mAdapter.updateDataSet(songList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
                if (isAZSort) {
                    recyclerView.addOnScrollListener(scrollListener);
                } else {
                    sideBar.setVisibility(View.INVISIBLE);
                    recyclerView.removeOnScrollListener(scrollListener);
                }
                Log.e("MusicFragment","load t");
                if (isFirstLoad) {
                    Log.e("MusicFragment","load");
                    frameLayout.removeAllViews();

                    ViewGroup p = (ViewGroup) view.getParent();
                    if (p != null) {
                        p.removeAllViewsInLayout();
                    }
                    frameLayout.addView(view);
                    isFirstLoad = false;
                }
            }
        }.execute();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_date:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DATE);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_artist:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_album:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                reloadAdapter();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (mContext != null) {
                musicInfos = (ArrayList<MusicInfo>) MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL);

                for (int i = 0; i < musicInfos.size(); i++) {
                    char c = Pinyin.toPinyin(musicInfos.get(i).musicName.charAt(0)).charAt(0);
                }
                if (musicInfos != null)
                    mAdapter = new Adapter(musicInfos);
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            if (mContext != null)
                recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));

        }

        @Override
        protected void onPreExecute() {

        }
    }

    public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private ArrayList<MusicInfo> mList;
        PlayMusic playMusic;
        Handler handler;

        public Adapter(ArrayList<MusicInfo> list) {

            handler = HandlerUtil.getInstance(mContext);
            mList = list;

        }


        public void updateDataSet(ArrayList<MusicInfo> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == FIRST_ITEM)
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_item, viewGroup, false));

            else {
                return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_musci_common_item, viewGroup, false));
            }
        }


        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;

        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MusicInfo model = null;
            if (position > 0) {
                model = mList.get(position - 1);
            }
            if (holder instanceof ListItemViewHolder) {

                ((ListItemViewHolder) holder).mainTitle.setText(model.musicName);
                ((ListItemViewHolder) holder).title.setText(model.artist);


                if (MusicPlayer.getCurrentAudioId() == model.songId) {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.VISIBLE);
                    ((ListItemViewHolder) holder).playState.setImageResource(R.drawable.song_play_icon);
                    ((ListItemViewHolder) holder).playState.setImageTintList(R.color.theme_color_primary);
                } else {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.GONE);
                }

            } else if (holder instanceof CommonItemViewHolder) {
                ((CommonItemViewHolder) holder).textView.setText("(共" + mList.size() + "首)");

                ((CommonItemViewHolder) holder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SelectActivity.class);
                        intent.putParcelableArrayListExtra("ids", mList);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mContext.startActivity(intent);
                    }
                });

            }
        }

        @Override
        public int getItemCount() {
            return (null != mList ? mList.size() + 1 : 0);
        }


        public class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView textView;
            ImageView select;

            CommonItemViewHolder(View view) {
                super(view);
                this.textView = view.findViewById(R.id.play_all_number);
                this.select = view.findViewById(R.id.select);
                view.setOnClickListener(this);
            }

            public void onClick(View v) {
                if(playMusic != null)
                    handler.removeCallbacks(playMusic);
                if(getAdapterPosition() > -1){
                    playMusic = new PlayMusic(0);
                    handler.postDelayed(playMusic,70);
                }


            }

        }


        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView moreOverflow;
            TextView mainTitle, title;
            TintImageView playState;


            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = view.findViewById(R.id.viewpager_list_toptext);
                this.title = view.findViewById(R.id.viewpager_list_bottom_text);
                this.playState = view.findViewById(R.id.play_state);
                this.moreOverflow = view.findViewById(R.id.viewpager_list_button);


                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition() - 1), IConstants.MUSICOVERFLOW);
                        morefragment.show(getFragmentManager(), "music");
                    }
                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                if(playMusic != null)
                    handler.removeCallbacks(playMusic);
                if(getAdapterPosition() > -1){
                    playMusic = new PlayMusic(getAdapterPosition() - 1);
                    handler.postDelayed(playMusic,70);
                }

            }

        }

        class PlayMusic implements Runnable{
            int position;
            public PlayMusic(int position){
                this.position = position;
            }

            @Override
            public void run() {
                long[] list = new long[mList.size()];
                HashMap<Long, MusicInfo> infos = new HashMap();
                for (int i = 0; i < mList.size(); i++) {
                    MusicInfo info = mList.get(i);
                    list[i] = info.songId;
                    info.islocal = true;
                    info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
                    infos.put(list[i], mList.get(i));
                }
                if (position > -1)
                    MusicPlayer.playAll(infos, list, position, false);
            }
        }
    }


}
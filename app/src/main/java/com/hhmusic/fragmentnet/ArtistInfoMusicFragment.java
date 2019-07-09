package com.hhmusic.fragmentnet;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.hhmusic.R;
import com.hhmusic.fragment.BaseFragment;
import com.hhmusic.fragment.MoreFragment;
import com.hhmusic.handler.HandlerUtil;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.service.MusicPlayer;
import com.hhmusic.uitl.IConstants;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtistInfoMusicFragment extends BaseFragment {
    ArrayList<MusicInfo> mList = new ArrayList<>();
    PlaylistDetailAdapter mAdapter;

    public static Fragment getInstance(ArrayList<MusicInfo> mList) {
        ArtistInfoMusicFragment fragment = new ArtistInfoMusicFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("list", mList);
        fragment.setArguments(bundle);

        return fragment;
    }

    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";
    ObservableRecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        if (getArguments() != null) {
            mList = getArguments().getParcelableArrayList("list");
        }
        Activity parentActivity = getActivity();
        recyclerView = view.findViewById(R.id.scroll);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setHasFixedSize(true);
        mAdapter = new PlaylistDetailAdapter(getActivity(), mList);
        recyclerView.setAdapter(mAdapter);

        if (parentActivity instanceof ObservableScrollViewCallbacks) {

            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_INITIAL_POSITION)) {
                final int initialPosition = args.getInt(ARG_INITIAL_POSITION, 0);
                ScrollUtils.addOnGlobalLayoutListener(recyclerView, new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }


            recyclerView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.root));

            recyclerView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }
        return view;
    }

    @Override
    public void updateTrackInfo() {
        super.updateTrackInfo();
        mAdapter.notifyDataSetChanged();
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
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.artist_header_common_item, viewGroup, false));
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


                        MoreFragment morefragment = MoreFragment.newInstance(arraylist.get(i - 1).songId + "",
                                IConstants.MUSICOVERFLOW);
                        morefragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "music");

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

                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
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
                        MusicPlayer.playAll(infos, list, 0, false);
                    }
                },70);
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
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
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


}

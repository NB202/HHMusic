package com.hhmusic.fragment;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hhmusic.R;
import com.hhmusic.info.AlbumInfo;
import com.hhmusic.service.MusicPlayer;
import com.hhmusic.uitl.Comparator.AlbumComparator;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.MusicUtils;
import com.hhmusic.uitl.PreferencesUtility;
import com.hhmusic.uitl.SortOrder;
import com.hhmusic.widget.DividerItemDecoration;
import com.hhmusic.widget.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class AlbumFragment extends BaseFragment {

    private LinearLayoutManager layoutManager;
    private List<AlbumInfo> mAlbumList = new ArrayList<>();
    private AlbumAdapter mAdapter;
    private RecyclerView recyclerView;
    private PreferencesUtility mPreferences;

    private RecyclerView.ItemDecoration itemDecoration;
    private boolean isAZSort = true;
    private HashMap<String, Integer> positionMap = new HashMap<>();
    private SideBar sideBar;
    private TextView dialogText;

    public static final AlbumFragment newInstance(int title, String message) {
        AlbumFragment f = new AlbumFragment();
        Bundle bdl = new Bundle(2);
        bdl.putInt("EXTRA_TITLE", title);
        bdl.putString("EXTRA_MESSAGE", message);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylerview, container, false);
        isAZSort = mPreferences.getAlbumSortOrder().equals(SortOrder.AlbumSortOrder.ALBUM_A_Z);
        recyclerView = view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        mAdapter = new AlbumAdapter(null);
        recyclerView.setAdapter(mAdapter);
        setItemDecoration();
        dialogText = view.findViewById(R.id.dialog_text);
        sideBar = view.findViewById(R.id.sidebar);
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                dialogText.setText(s);
                sideBar.setView(dialogText);
                Log.e("scrol", "  " + s);
                if (positionMap.get(s) != null) {
                    int i = positionMap.get(s);
                    Log.e("scrolget", "  " + i);
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                }

            }
        });

        reloadAdapter();



        return view;
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
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.album_sort_by, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_number_of_songs:
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                reloadAdapter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setItemDecoration() {
        itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                isAZSort = mPreferences.getAlbumSortOrder().equals(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                Log.e("sort", isAZSort + "");
                List<AlbumInfo> albumList = MusicUtils.queryAlbums(mContext);
                if (isAZSort) {
                    Collections.sort(albumList, new AlbumComparator());
                    for (int i = 0; i < albumList.size(); i++) {
                        if (positionMap.get(albumList.get(i).album_sort) == null)
                            positionMap.put(albumList.get(i).album_sort, i);
                    }
                }
                mAdapter.updateDataSet(albumList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (isAZSort) {
                    recyclerView.addOnScrollListener(scrollListener);
                } else {
                    sideBar.setVisibility(View.INVISIBLE);
                    recyclerView.removeOnScrollListener(scrollListener);
                }
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }


    private class loadAlbums extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (mContext != null)
                mAdapter = new AlbumAdapter(mAlbumList);
            mAlbumList = MusicUtils.queryAlbums(mContext);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            if (mContext != null) {
                setItemDecoration();
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

    public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private List<AlbumInfo> mList;

        public AlbumAdapter(List<AlbumInfo> list) {

            mList = list;
        }


        public void updateDataSet(List<AlbumInfo> list) {
            this.mList = list;
        }



        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.recyclerview_common_item, viewGroup, false));
        }



        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;

        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            AlbumInfo model = mList.get(position);
            ((ListItemViewHolder) holder).title.setText(model.album_name);
            ((ListItemViewHolder) holder).title2.setText(model.number_of_songs + "首" + model.album_artist);
            ((ListItemViewHolder) holder).draweeView.setImageURI(Uri.parse(model.album_art + ""));

            if (MusicPlayer.getArtistName() != null && MusicPlayer.getAlbumName().equals(model.album_name)) {
                ((ListItemViewHolder) holder).moreOverflow.setImageResource(R.drawable.song_play_icon);
                ((ListItemViewHolder) holder).moreOverflow.setImageTintList(R.color.theme_color_primary);
            } else {
                ((ListItemViewHolder) holder).moreOverflow.setImageResource(R.drawable.list_icn_more);
            }

        }


        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }


        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TintImageView moreOverflow;
            SimpleDraweeView draweeView;
            TextView title, title2;

            ListItemViewHolder(View view) {
                super(view);
                this.title = view.findViewById(R.id.viewpager_list_toptext);
                this.title2 = view.findViewById(R.id.viewpager_list_bottom_text);
                this.draweeView = view.findViewById(R.id.viewpager_list_img);
                this.moreOverflow = view.findViewById(R.id.viewpager_list_button);
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition()).album_id + "", IConstants.ALBUMOVERFLOW);
                        morefragment.show(getFragmentManager(), "album");
                    }
                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                AlbumDetailFragment fragment = AlbumDetailFragment.newInstance(mList.get(getAdapterPosition()).album_id, false, null);
                transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.tab_container));
                transaction.add(R.id.tab_container, fragment);
                transaction.addToBackStack(null).commit();
            }

        }
    }

}

package com.hhmusic.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.hhmusic.MainApplication;
import com.hhmusic.R;
import com.hhmusic.info.FolderInfo;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.service.MusicPlayer;
import com.hhmusic.uitl.Comparator.FolderComparator;
import com.hhmusic.uitl.Comparator.FolderCountComparator;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.MusicUtils;
import com.hhmusic.uitl.PreferencesUtility;
import com.hhmusic.uitl.SortOrder;
import com.hhmusic.widget.DividerItemDecoration;
import com.hhmusic.widget.SideBar;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class FolderFragment extends BaseFragment {
    //private List<FolderInfo> folderInfos;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private Adapter mAdapter;
    private PreferencesUtility mPreferences;
    private boolean isAZSort = true;
    private HashMap<String, Integer> positionMap = new HashMap<>();
    private SideBar sideBar;
    private TextView dialogText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylerview, container, false);


        recyclerView = view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new Adapter(null);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        setItemDecoration();
        isAZSort = mPreferences.getFoloerSortOrder().equals(SortOrder.FolderSortOrder.FOLDER_A_Z);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void setItemDecoration() {
        itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

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
                mPreferences.setFolerSortOrder(SortOrder.FolderSortOrder.FOLDER_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_number_of_songs:
                mPreferences.setFolerSortOrder(SortOrder.FolderSortOrder.FOLDER_NUMBER);
                reloadAdapter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                isAZSort = mPreferences.getFoloerSortOrder().equals(SortOrder.FolderSortOrder.FOLDER_A_Z);
                Log.e("sort", "foler" + isAZSort);
                List<FolderInfo> folderList = MusicUtils.queryFolder(mContext);
                for (int i = 0; i < folderList.size(); i++) {
                    List<MusicInfo> albumList = MusicUtils.queryMusic(MainApplication.context, folderList.get(i).folder_path, IConstants.START_FROM_FOLDER);
                    folderList.get(i).folder_count = albumList.size();
                }
                if (isAZSort) {
                    Collections.sort(folderList, new FolderComparator());
                    for (int i = 0; i < folderList.size(); i++) {
                        if (positionMap.get(folderList.get(i).folder_sort) == null)
                            positionMap.put(folderList.get(i).folder_sort, i);
                    }
                } else {
                    Collections.sort(folderList, new FolderCountComparator());
                }
                mAdapter.updateDataSet(folderList);
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


    public class Adapter extends RecyclerView.Adapter<Adapter.ListItemViewHolder> {

        private List<FolderInfo> mList;


        public Adapter(List<FolderInfo> list) {

            mList = list;
        }


        public void updateDataSet(List<FolderInfo> list) {
            this.mList = list;
        }

        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_common_item, viewGroup, false);
            return new ListItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int i) {
            FolderInfo model = mList.get(i);
            holder.Maintitle.setText(model.folder_name);
            holder.title.setText(model.folder_count + "首 " + model.folder_path);
            holder.imageView.setImageResource(R.drawable.list_icn_folder);

            String folder_path = null;
            if (MusicPlayer.getPath() != null && MusicPlayer.getTrackName() != null) {
                folder_path = MusicPlayer.getPath().substring(0, MusicPlayer.getPath().lastIndexOf(File.separator));
            }
            if (folder_path != null && folder_path.equals(model.folder_path)) {
                holder.moreOverflow.setImageResource(R.drawable.song_play_icon);
                holder.moreOverflow.setImageTintList(R.color.theme_color_primary);
            } else {
                holder.moreOverflow.setImageResource(R.drawable.list_icn_more);
            }

        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView imageView;
            TintImageView moreOverflow;
            TextView Maintitle, title;

            ListItemViewHolder(View view) {
                super(view);
                this.Maintitle = view.findViewById(R.id.viewpager_list_toptext);
                this.title = view.findViewById(R.id.viewpager_list_bottom_text);
                this.moreOverflow = view.findViewById(R.id.viewpager_list_button);
                this.imageView = view.findViewById(R.id.viewpager_list_img);
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition()).folder_name, IConstants.FOLDEROVERFLOW);
                        morefragment.show(getFragmentManager(), "music");
                    }
                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                FolderDetailFragment fragment = FolderDetailFragment.newInstance(mList.get(getAdapterPosition()).folder_path, false, null);
                transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.tab_container));
                transaction.add(R.id.tab_container, fragment);
                transaction.addToBackStack(null).commit();
            }

        }
    }
}

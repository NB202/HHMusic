package com.hhmusic.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hhmusic.R;
import com.hhmusic.info.Playlist;
import com.hhmusic.provider.PlaylistInfo;
import com.hhmusic.provider.PlaylistsManager;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.widget.DividerItemDecoration;
import com.hhmusic.widget.DragSortRecycler;

import java.util.ArrayList;




public class PlaylistManagerActivity extends AppCompatActivity implements View.OnClickListener {
    SelectAdapter mAdapter;
    PlaylistInfo playlistInfo;
    ActionBar ab;
    ArrayList<Playlist> playlists;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_manager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("已选择0项");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        playlistInfo = PlaylistInfo.getInstance(this);


        LinearLayout delete = findViewById(R.id.select_del);
        delete.setOnClickListener(this);

        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        new loadSongs().execute("");

    }


    @Override
    public void onClick(View v) {
        final ArrayList<Playlist> selectList = mAdapter.getSelectedItem();
        switch (v.getId()) {
            case R.id.select_del:
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.sure_to_delete_music)).
                        setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < selectList.size(); i++) {
                                    PlaylistInfo.getInstance(PlaylistManagerActivity.this).deletePlaylist(selectList.get(i).id);
                                    PlaylistsManager.getInstance(PlaylistManagerActivity.this).delete(selectList.get(i).id);
                                }
                                new reload().execute();
                                ab.setTitle("已选择0项");
                                dialog.dismiss();
                            }
                        }).
                        setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent();
        intent.setAction(IConstants.PLAYLIST_ITEM_MOVED);
        sendBroadcast(intent);
        finish();
    }

    private class reload extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            playlists = playlistInfo.getPlaylist();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mAdapter.updateDataSet(playlists);
            mAdapter.notifyDataSetChanged();
        }
    }


    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            playlists = playlistInfo.getPlaylist();

            mAdapter = new SelectAdapter(playlists);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(PlaylistManagerActivity.this, DividerItemDecoration.VERTICAL_LIST));
            recyclerView.setAdapter(mAdapter);
            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.select_move);

            dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
                @Override
                public void onItemMoved(int from, int to) {
                    Log.d("queue", "onItemMoved " + from + " to " + to);
                    final Playlist playlist = mAdapter.getMusicAt(from);
                    boolean f = mAdapter.isItemChecked(from);
                    boolean t = mAdapter.isItemChecked(to);
                    mAdapter.removeSongAt(from);
                    mAdapter.setItemChecked(from, t);
                    mAdapter.addPlaylistTo(to, playlist);
                    mAdapter.setItemChecked(to, f);
                    mAdapter.notifyDataSetChanged();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            int length = mAdapter.mList.size();
                            long[] playlists = new long[length];
                            for (int i = 0; i < length; i++) {
                                playlists[i] = mAdapter.mList.get(i).id;
                            }

                            playlistInfo.deletePlaylist(playlists);
                            playlistInfo.addPlaylist(mAdapter.mList);

                        }
                    }, 300);


                }
            });

            recyclerView.addItemDecoration(dragSortRecycler);
            recyclerView.addOnItemTouchListener(dragSortRecycler);
            recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());


        }

        @Override
        protected void onPreExecute() {

        }
    }


    public class SelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList selected;
        private ArrayList<Playlist> mList;
        private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
        private boolean mIsSelectable = false;


        public SelectAdapter(ArrayList<Playlist> list) {
            if (list == null) {
                throw new IllegalArgumentException("model Data must not be null");
            }
            if (list.size() == 0) {
                return;
            }
            list.remove(0);
            mList = list;
        }

        public ArrayList<Playlist> getSelectedItem() {

            ArrayList<Playlist> selectList = new ArrayList<>();
            for (int i = 0; i < mList.size(); i++) {
                if (isItemChecked(i)) {
                    selectList.add(mList.get(i));
                }
            }
            return selectList;
        }


        public void updateDataSet(ArrayList<Playlist> list) {
            list.remove(0);
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_manager_select_item, viewGroup, false);
            return new ListItemViewHolder(itemView);
        }

        private void setItemChecked(int position, boolean isChecked) {
            mSelectedPositions.put(position, isChecked);
        }

        private boolean isItemChecked(int position) {
            return mSelectedPositions.get(position);
        }

        private boolean isSelectable() {
            return mIsSelectable;
        }

        private void setSelectable(boolean selectable) {
            mIsSelectable = selectable;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {
            Playlist playlist = mList.get(i);

            ((ListItemViewHolder) holder).albumArt.setImageURI(Uri.parse(playlist.albumArt));
            ((ListItemViewHolder) holder).mainTitle.setText(playlist.name);
            ((ListItemViewHolder) holder).title.setText(playlist.songCount + "");
            ((ListItemViewHolder) holder).checkBox.setChecked(isItemChecked(i));
            ((ListItemViewHolder) holder).checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isItemChecked(i)) {
                        setItemChecked(i, false);
                    } else {
                        setItemChecked(i, true);
                    }

                    ab.setTitle("已选择" + getSelectedItem().size() + "项");
                }
            });
            ((ListItemViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isItemChecked(i)) {
                        setItemChecked(i, false);
                    } else {
                        setItemChecked(i, true);
                    }
                    notifyItemChanged(i);
                    ab.setTitle("已选择" + getSelectedItem().size() + "项");
                }
            });


        }

        public Playlist getMusicAt(int i) {
            return mList.get(i);
        }

        public void addPlaylistTo(int i, Playlist playlist) {
            mList.add(i, playlist);
        }

        public void removeSongAt(int i) {
            mList.remove(i);
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {

            CheckBox checkBox;
            TextView mainTitle, title;
            ImageView move;
            SimpleDraweeView albumArt;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = view.findViewById(R.id.select_title_main);
                this.title = view.findViewById(R.id.select_title_small);
                this.checkBox = view.findViewById(R.id.select_checkbox);
                this.albumArt = view.findViewById(R.id.playlist_album);
                this.move = view.findViewById(R.id.select_move);

            }

        }
    }
}

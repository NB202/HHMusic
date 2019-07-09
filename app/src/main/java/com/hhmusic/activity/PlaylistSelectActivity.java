package com.hhmusic.activity;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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

import com.hhmusic.R;
import com.hhmusic.dialog.AddPlaylistDialog;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.provider.PlaylistsManager;
import com.hhmusic.service.MediaService;
import com.hhmusic.service.MusicPlayer;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.MusicUtils;
import com.hhmusic.widget.DividerItemDecoration;
import com.hhmusic.widget.DragSortRecycler;

import java.util.ArrayList;
import java.util.HashMap;




public class PlaylistSelectActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<MusicInfo> arrayList;
    private SelectAdapter mAdapter;
    private ActionBar ab;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;
    private PlaylistsManager pManager;
    private long playlistId;
    private LinearLayout nextPlay, addtoPlaylist, delete;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select);

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

        pManager = PlaylistsManager.getInstance(this);

        nextPlay = findViewById(R.id.select_next);
        addtoPlaylist = findViewById(R.id.select_addtoplaylist);
        delete = findViewById(R.id.select_del);
        nextPlay.setOnClickListener(this);
        addtoPlaylist.setOnClickListener(this);
        delete.setOnClickListener(this);

        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        new loadSongs().execute("");

    }


    @Override
    public void onClick(View v) {
        final ArrayList<MusicInfo> selectList = mAdapter.getSelectedItem();
        switch (v.getId()) {

            case R.id.select_next:
                long[] list = new long[selectList.size()];
                HashMap<Long, MusicInfo> infos = new HashMap();
                for (int i = 0; i < mAdapter.getSelectedItem().size(); i++) {
                    MusicInfo info = selectList.get(i);
                    list[i] = selectList.get(i).songId;
                    info.islocal = true;
                    info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
                    infos.put(list[i], selectList.get(i));
                }
                MusicPlayer.playNext(this, infos, list);
                break;
            case R.id.select_addtoplaylist:
                long[] list1 = new long[selectList.size()];
                for (int i = 0; i < mAdapter.getSelectedItem().size(); i++) {
                    list1[i] = selectList.get(i).songId;
                }
                AddPlaylistDialog.newInstance(list1).show(getSupportFragmentManager(), "add");
                Intent intent = new Intent(MediaService.PLAYLIST_CHANGED);
                sendBroadcast(intent);

                break;
            case R.id.select_del:
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.sure_to_delete_music)).
                        setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        for (MusicInfo music : selectList) {

                                            if (MusicPlayer.getCurrentAudioId() == music.songId) {
                                                if (MusicPlayer.getQueueSize() == 0) {
                                                    MusicPlayer.stop();
                                                } else {
                                                    MusicPlayer.next();
                                                }

                                            }
                                            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.songId);
                                            PlaylistSelectActivity.this.getContentResolver().delete(uri, null, null);
                                            PlaylistsManager.getInstance(PlaylistSelectActivity.this).deleteMusic(PlaylistSelectActivity.this,
                                                    music.songId);
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void v) {
                                        mAdapter.updateDataSet();
                                        mAdapter.notifyDataSetChanged();
                                        PlaylistSelectActivity.this.sendBroadcast(new Intent(IConstants.MUSIC_COUNT_CHANGED));
                                    }

                                }.execute();
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
    public void onStop() {
        super.onStop();
        Intent intent = new Intent();
        intent.setAction(IConstants.PLAYLIST_ITEM_MOVED);
        PlaylistSelectActivity.this.sendBroadcast(intent);
        finish();
    }



    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getIntent().getParcelableArrayListExtra("ids") != null) {
                arrayList = getIntent().getParcelableArrayListExtra("ids");
                playlistId = getIntent().getLongExtra("playlistid", -1);

            }
            mAdapter = new SelectAdapter(arrayList);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(PlaylistSelectActivity.this, DividerItemDecoration.VERTICAL_LIST));
            recyclerView.setAdapter(mAdapter);
            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.select_move);

            dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
                @Override
                public void onItemMoved(int from, int to) {
                    Log.d("queue", "onItemMoved " + from + " to " + to);
                    MusicInfo musicInfo = mAdapter.getMusicAt(from);
                    boolean f = mAdapter.isItemChecked(from);
                    boolean t = mAdapter.isItemChecked(to);
                    mAdapter.removeSongAt(from);
                    mAdapter.setItemChecked(from, t);
                    mAdapter.addSongTo(to, musicInfo);
                    mAdapter.setItemChecked(to, f);
                    mAdapter.notifyDataSetChanged();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pManager.delete(playlistId);
                            for (int i = 0; i < mAdapter.mList.size(); i++) {
                                pManager.insert(PlaylistSelectActivity.this, playlistId, mAdapter.mList.get(i).songId, i);

                            }

                        }
                    }, 100);


                    Intent intent = new Intent();
                    intent.setAction(IConstants.PLAYLIST_ITEM_MOVED);
                    PlaylistSelectActivity.this.sendBroadcast(intent);

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
        private ArrayList<MusicInfo> mList;
        private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
        private boolean mIsSelectable = false;


        public SelectAdapter(ArrayList<MusicInfo> list) {
            if (list == null) {
                throw new IllegalArgumentException("model Data must not be null");
            }
            mList = list;
        }

        public ArrayList<MusicInfo> getSelectedItem() {


            ArrayList<MusicInfo> selectList = new ArrayList<>();
            for (int i = 0; i < mList.size(); i++) {
                if (isItemChecked(i)) {
                    selectList.add(mList.get(i));
                }
            }
            return selectList;
        }

        public void updateDataSet() {
            ab.setTitle("已选择0项");
            mList.removeAll(getSelectedItem());
            mSelectedPositions.clear();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_select_item, viewGroup, false);
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
            MusicInfo model = mList.get(i);

            ((ListItemViewHolder) holder).mainTitle.setText(model.musicName);
            ((ListItemViewHolder) holder).title.setText(model.artist);
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

        public MusicInfo getMusicAt(int i) {
            return mList.get(i);
        }

        public void addSongTo(int i, MusicInfo musicInfo) {
            mList.add(i, musicInfo);
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

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = view.findViewById(R.id.select_title_main);
                this.title = view.findViewById(R.id.select_title_small);
                this.checkBox = view.findViewById(R.id.select_checkbox);
                this.move = view.findViewById(R.id.select_move);

            }
        }
    }

}

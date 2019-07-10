package com.hhmusic.fragment;


import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hhmusic.MainApplication;
import com.hhmusic.R;
import com.hhmusic.activity.AlbumsDetailActivity;
import com.hhmusic.activity.ArtistDetailActivity;
import com.hhmusic.adapter.MusicFlowAdapter;
import com.hhmusic.adapter.OverFlowAdapter;
import com.hhmusic.adapter.OverFlowItem;
import com.hhmusic.dialog.AddNetPlaylistDialog;
import com.hhmusic.log.HandlerUtil;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.entity.SearchAlbumInfo;
import com.hhmusic.entity.SearchArtistInfo;
import com.hhmusic.api.BMA;
import com.hhmusic.api.HttpUtil;
import com.hhmusic.provider.PlaylistsManager;
import com.hhmusic.service.MusicPlayer;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.MusicUtils;
import com.hhmusic.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MoreFragment extends AttachDialogFragment {
    private int type;
    private double heightPercent;
    private TextView topTitle;
    private ArrayList<MusicInfo> list = null;
    private MusicFlowAdapter muaicflowAdapter;
    private MusicInfo adapterMusicInfo;
    private OverFlowAdapter commonAdapter;

    private List<OverFlowItem> mlistInfo = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private String args;
    private String musicName, artist, albumId, albumName;
    private Context mContext;
    private Handler mHandler;
    private long playlistId = -1;

    public static MoreFragment newInstance(String id, int startFrom, String albumId, String artistId) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("albumid", albumId);
        args.putString("artistid", artistId);
        args.putInt("type", startFrom);
        fragment.setArguments(args);
        return fragment;
    }


    public static MoreFragment newInstance(String id, int startFrom) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putInt("type", startFrom);
        fragment.setArguments(args);
        return fragment;
    }


    public static MoreFragment newInstance(MusicInfo info, int startFrom) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putParcelable("music", info);
        args.putInt("type", startFrom);
        fragment.setArguments(args);
        return fragment;
    }

    public static MoreFragment newInstance(MusicInfo info, long playlistid) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putParcelable("music", info);
        args.putLong("playlistid", playlistid);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mHandler = HandlerUtil.getInstance(mContext);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams params = getDialog().getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setAttributes(params);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
            args = getArguments().getString("id");
            playlistId = getArguments().getLong("playlistid");
        }

        View view = inflater.inflate(R.layout.more_fragment, container);
        topTitle = view.findViewById(R.id.pop_list_title);
        recyclerView = view.findViewById(R.id.pop_list);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        getList();
        setClick();
        setItemDecoration();
        return view;
    }


    private void setItemDecoration() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void getList() {

        if (type == IConstants.MUSICOVERFLOW) {

            adapterMusicInfo = getArguments().getParcelable("music");
            if(adapterMusicInfo == null){
                adapterMusicInfo = new MusicInfo();
            }
            artist = adapterMusicInfo.artist;
            albumId = adapterMusicInfo.albumId + "";
            albumName = adapterMusicInfo.albumName;
            musicName = adapterMusicInfo.musicName;
            topTitle.setText("歌曲：" + " " + musicName);
            heightPercent = 0.6;
            setMusicInfo();
            muaicflowAdapter = new MusicFlowAdapter(mContext, mlistInfo, adapterMusicInfo);

        } else {
            switch (type) {
                case IConstants.ARTISTOVERFLOW:
                    String artist = args;
                    list = MusicUtils.queryMusic(mContext, artist, IConstants.START_FROM_ARTIST);
                    topTitle.setText("歌曲：" + " " + list.get(0).artist);
                    break;
                case IConstants.ALBUMOVERFLOW:
                    String albumId = args;
                    list = MusicUtils.queryMusic(mContext, albumId, IConstants.START_FROM_ALBUM);
                    topTitle.setText("专辑：" + " " + list.get(0).albumName);
                    break;
                case IConstants.FOLDEROVERFLOW:
                    String folder = args;
                    list = MusicUtils.queryMusic(mContext, folder, IConstants.START_FROM_FOLDER);
                    topTitle.setText("文件夹：" + " " + folder);
                    break;
            }
            setCommonInfo();
            heightPercent = 0.3;
            commonAdapter = new OverFlowAdapter(mContext, mlistInfo, list);

        }

    }

    private void setClick() {
        if (muaicflowAdapter != null) {
            muaicflowAdapter.setOnItemClickListener(new MusicFlowAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, String data) {
                    switch (Integer.parseInt(data)) {
                        case 0:
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (adapterMusicInfo.songId == MusicPlayer.getCurrentAudioId())
                                        return;

                                    long[] ids = new long[1];
                                    ids[0] = adapterMusicInfo.songId;
                                    HashMap<Long, MusicInfo> map = new HashMap<Long, MusicInfo>();
                                    map.put(ids[0], adapterMusicInfo);
                                    MusicPlayer.playNext(mContext, map, ids);
                                }
                            }, 100);

                            dismiss();
                            break;
                        case 1:
                            ArrayList<MusicInfo> musicList = new ArrayList<MusicInfo>();
                            musicList.add(adapterMusicInfo);
                            AddNetPlaylistDialog.newInstance(musicList).show(getFragmentManager(), "add");
                            dismiss();
                            break;
                        case 2:
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + adapterMusicInfo.data));
                            shareIntent.setType("audio/*");
                            mContext.startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.shared_to)));
                            dismiss();
                            break;
                        case 3:
                            new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.sure_to_delete_music)).
                                    setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            try {
                                                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, adapterMusicInfo.songId);
                                                mContext.getContentResolver().delete(uri, null, null);


                                                if (MusicPlayer.getCurrentAudioId() == adapterMusicInfo.songId) {
                                                    if (MusicPlayer.getQueueSize() == 0) {
                                                        MusicPlayer.stop();
                                                    } else {
                                                        MusicPlayer.next();
                                                    }

                                                }

                                                mHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        PlaylistsManager.getInstance(mContext).deleteMusic(mContext, adapterMusicInfo.songId);
                                                        mContext.sendBroadcast(new Intent(IConstants.MUSIC_COUNT_CHANGED));
                                                    }
                                                }, 200);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            dismiss();
                                        }
                                    }).
                                    setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dismiss();
                                        }
                                    }).show();
                            dismiss();
                            break;
                        case 4:

                            if (adapterMusicInfo.islocal) {
                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        ArrayList<SearchArtistInfo> artistResults = new ArrayList<>();
                                        try {

                                            JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Search.searchMerge(adapterMusicInfo.artist, 1, 50)).get("result").getAsJsonObject();
                                            JsonObject artistObject = jsonObject.get("artist_info").getAsJsonObject();
                                            JsonArray artistArray = artistObject.get("artist_list").getAsJsonArray();
                                            for (JsonElement o : artistArray) {
                                                SearchArtistInfo artistInfo = MainApplication.gsonInstance().fromJson(o, SearchArtistInfo.class);
                                                artistResults.add(artistInfo);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                        if (artistResults.size() == 0) {
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(mContext, "没有找到该艺术家", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {
                                            SearchArtistInfo info = artistResults.get(0);
                                            Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                                            intent.putExtra("artistid", info.getArtist_id());
                                            intent.putExtra("artistname", info.getAuthor());
                                            mContext.startActivity(intent);
                                        }
                                        return null;
                                    }
                                }.execute();
                            } else {

                                Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                                intent.putExtra("artistid", adapterMusicInfo.artistId + "");
                                intent.putExtra("artistname", adapterMusicInfo.artist);
                                mContext.startActivity(intent);
                            }
                            dismiss();
                            break;
                        case 5:

                            if (adapterMusicInfo.islocal) {
                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        ArrayList<SearchAlbumInfo> albumResults = new ArrayList<SearchAlbumInfo>();
                                        try {

                                            JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Search.searchMerge(adapterMusicInfo.albumName, 1, 10)).get("result").getAsJsonObject();
                                            JsonObject albumObject = jsonObject.get("album_info").getAsJsonObject();
                                            JsonArray albumArray = albumObject.get("album_list").getAsJsonArray();
                                            for (JsonElement o : albumArray) {
                                                SearchAlbumInfo albumInfo = MainApplication.gsonInstance().fromJson(o, SearchAlbumInfo.class);
                                                albumResults.add(albumInfo);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        if (albumResults.size() == 0) {
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(mContext, "没有找到所属专辑", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {
                                            SearchAlbumInfo info = albumResults.get(0);
                                            Intent intent = new Intent(mContext, AlbumsDetailActivity.class);
                                            intent.putExtra("albumid", info.getAlbum_id());
                                            intent.putExtra("albumart", info.getPic_small());
                                            intent.putExtra("albumname", info.getTitle());
                                            intent.putExtra("albumdetail", info.getAlbum_desc());
                                            mContext.startActivity(intent);
                                        }
                                        return null;
                                    }
                                };
                            } else {

                                Intent intent = new Intent(mContext, AlbumsDetailActivity.class);
                                intent.putExtra("albumid", adapterMusicInfo.albumId + "");
                                intent.putExtra("albumart", adapterMusicInfo.albumData);
                                intent.putExtra("albumname", adapterMusicInfo.albumName);
                                mContext.startActivity(intent);
                            }
                            dismiss();
                            break;
                        case 6:
                            if(adapterMusicInfo.islocal){
                                new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.sure_to_set_ringtone)).
                                        setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Uri ringUri = Uri.parse("file://" + adapterMusicInfo.data);
                                                RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, ringUri);
                                                dialog.dismiss();
                                                Toast.makeText(mContext, getResources().getString(R.string.set_ringtone_successed),
                                                        Toast.LENGTH_SHORT).show();
                                                dismiss();
                                            }
                                        }).
                                        setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }else {

                            }

                            break;
                        case 7:
                            MusicDetailFragment detailFrament = MusicDetailFragment.newInstance(adapterMusicInfo);
                            detailFrament.show(getActivity().getSupportFragmentManager(), "detail");
                            dismiss();
                            break;
                        default:
                            break;
                    }
                }
            });
            recyclerView.setAdapter(muaicflowAdapter);
            return;
        }

        commonAdapter.setOnItemClickListener(new OverFlowAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, String data) {
                switch (Integer.parseInt(data)) {
                    case 0:
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                HashMap<Long, MusicInfo> infos = new HashMap<Long, MusicInfo>();
                                int len = list.size();
                                long[] listid = new long[len];
                                for (int i = 0; i < len; i++) {
                                    MusicInfo info = list.get(i);
                                    listid[i] = info.songId;
                                    infos.put(listid[i], info);
                                }

                                MusicPlayer.playAll(infos, listid, 0, false);
                            }
                        }, 60);
                        dismiss();
                        break;
                    case 1:
                        AddNetPlaylistDialog.newInstance(list).show(getFragmentManager(), "add");

                        dismiss();
                        break;
                    case 2:

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                for (MusicInfo music : list) {

                                    if (MusicPlayer.getCurrentAudioId() == music.songId) {
                                        if (MusicPlayer.getQueueSize() == 0) {
                                            MusicPlayer.stop();
                                        } else {
                                            MusicPlayer.next();
                                        }

                                    }
                                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.songId);
                                    mContext.getContentResolver().delete(uri, null, null);
                                    PlaylistsManager.getInstance(mContext).deleteMusic(mContext, music.songId);
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void v) {
                                mContext.sendBroadcast(new Intent(IConstants.MUSIC_COUNT_CHANGED));
                            }

                        }.execute();



                        dismiss();
                        break;
                }
            }
        });
        recyclerView.setAdapter(commonAdapter);
    }


    private void setMusicInfo() {

        setInfo("下一首播放", R.drawable.lay_icn_next);
        setInfo("收藏到歌单", R.drawable.lay_icn_fav);
        setInfo("分享", R.drawable.lay_icn_share);
        setInfo("删除", R.drawable.lay_icn_delete);
        setInfo("歌手：" + artist, R.drawable.lay_icn_artist);
        setInfo("专辑：" + albumName, R.drawable.lay_icn_alb);
        setInfo("设为铃声", R.drawable.lay_icn_ring);
        setInfo("查看歌曲信息", R.drawable.lay_icn_document);
    }


    private void setCommonInfo() {
        setInfo("播放", R.drawable.lay_icn_play);
        setInfo("收藏到歌单", R.drawable.lay_icn_fav);
        setInfo("删除", R.drawable.lay_icn_delete);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
        mContext = getContext();
    }

    @Override
    public void onStart() {
        super.onStart();

        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * heightPercent);


        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }


    public void setInfo(String title, int id) {

        OverFlowItem information = new OverFlowItem();
        information.setTitle(title);
        information.setAvatar(id);
        mlistInfo.add(information);
    }


}

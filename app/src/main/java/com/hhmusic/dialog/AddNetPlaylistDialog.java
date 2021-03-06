package com.hhmusic.dialog;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hhmusic.MainApplication;
import com.hhmusic.R;
import com.hhmusic.fragment.AttachDialogFragment;
import com.hhmusic.info.MusicInfo;
import com.hhmusic.info.Playlist;
import com.hhmusic.provider.PlaylistInfo;
import com.hhmusic.provider.PlaylistsManager;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.MusicUtils;
import com.hhmusic.widget.DividerItemDecoration;

import java.util.ArrayList;

public class AddNetPlaylistDialog extends AttachDialogFragment {
    private PlaylistInfo playlistInfo;
    private PlaylistsManager playlistsManager;
    private RecyclerView recyclerView;
    private ArrayList<MusicInfo> musics;
    private String author;

    public static AddNetPlaylistDialog newInstance(ArrayList<MusicInfo> list, String author) {
        AddNetPlaylistDialog dialog = new AddNetPlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putString("author", author);
        bundle.putParcelableArrayList("songs", list);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static AddNetPlaylistDialog newInstance(ArrayList<MusicInfo> list) {
        AddNetPlaylistDialog dialog = new AddNetPlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putString("author", "local");
        bundle.putParcelableArrayList("songs", list);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static AddNetPlaylistDialog newInstance(MusicInfo info) {
        ArrayList<MusicInfo> list = new ArrayList<>();
        list.add(info);
        return AddNetPlaylistDialog.newInstance(list);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (getArguments() != null) {
            musics = getArguments().getParcelableArrayList("songs");
            author = getArguments().getString("author");
        }
        playlistInfo = PlaylistInfo.getInstance(mContext);
        playlistsManager = PlaylistsManager.getInstance(mContext);

        View view = inflater.inflate(R.layout.fragment_add_playlist, container);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        LinearLayout linearLayout = view.findViewById(R.id.create_new_playlist);
        recyclerView = view.findViewById(R.id.add_playlist_recyclerview);


        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                alertDialog.setView((mContext.getLayoutInflater().inflate(R.layout.dialog, null)));
                alertDialog.show();
                Window window = alertDialog.getWindow();
                window.setContentView(R.layout.dialog);
                final EditText editText = (window.findViewById(R.id.message));
                editText.requestFocus();
                (window.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                (window.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("addplay","here");
                                String albumart = null;
                                for (MusicInfo info : musics) {
                                    albumart = info.albumData;
                                    if (info.islocal) {
                                        if (albumart.equals(MusicUtils.getAlbumdata(MainApplication.context, info.songId)))
                                            break;
                                    } else if (!TextUtils.isEmpty(albumart)) {
                                        break;
                                    }
                                }
                                long playlistid = editText.getText().hashCode();
                                playlistInfo.addPlaylist(playlistid, editText.getText().toString(),
                                        musics.size(), albumart, author);
                                playlistsManager.insertLists(mContext, playlistid, musics);
                                Intent intent = new Intent(IConstants.PLAYLIST_COUNT_CHANGED);
                                MainApplication.context.sendBroadcast(intent);

                            }
                        }).start();

                        alertDialog.dismiss();
                    }
                });
            }
        });
        ArrayList<Playlist> playlists = playlistInfo.getPlaylist();
        recyclerView.setLayoutManager(layoutManager);
        AddPlaylistAdapter adapter = new AddPlaylistAdapter(playlists);
        recyclerView.setAdapter(adapter);
        //setItemDecoration();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Override
    public void onStart() {
        super.onStart();

        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.65);
        int dialogWidth = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.77);
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }


    private void setItemDecoration() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private class AddPlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<Playlist> playlists;

        public AddPlaylistAdapter(ArrayList<Playlist> p) {
            playlists = p;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.fragment_add_playlist_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Playlist playlist = playlists.get(position);

            ((ViewHolder) holder).title.setText(playlist.name);
            ((ViewHolder) holder).count.setText(playlist.songCount + "");
            Uri uri = Uri.parse(playlist.albumArt);
            ((ViewHolder) holder).imageView.setImageURI(uri);

        }

        @Override
        public int getItemCount() {
            return playlists.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            SimpleDraweeView imageView;
            TextView title, count;

            public ViewHolder(View v) {
                super(v);
                this.imageView = v.findViewById(R.id.add_playlist_img);
                this.title = v.findViewById(R.id.add_playlist_toptext);
                this.count = v.findViewById(R.id.add_playlist_bottom_text);
                v.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                final Playlist playlist = playlists.get(getAdapterPosition());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            playlistsManager.insertLists(mContext, playlist.id, musics);
                            Intent intent = new Intent(IConstants.PLAYLIST_COUNT_CHANGED);
                            mContext.sendBroadcast(intent);
                            dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                }).start();


            }
        }
    }


}

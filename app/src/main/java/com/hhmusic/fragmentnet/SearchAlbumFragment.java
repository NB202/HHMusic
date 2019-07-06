package com.hhmusic.fragmentnet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hhmusic.R;
import com.hhmusic.activity.AlbumsDetailActivity;
import com.hhmusic.fragment.AttachFragment;
import com.hhmusic.entity.SearchAlbumInfo;
import com.hhmusic.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;


public class SearchAlbumFragment extends AttachFragment {

    private LinearLayoutManager layoutManager;
    private List<SearchAlbumInfo> mAlbumList = new ArrayList<>();
    private AlbumAdapter mAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.ItemDecoration itemDecoration;

    public static SearchAlbumFragment newInstance(ArrayList<SearchAlbumInfo> list) {
        SearchAlbumFragment fragment = new SearchAlbumFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("searchAlbum", list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylerview, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new AlbumAdapter(null);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        setItemDecoration();
        loadAlbums();
        return view;
    }



    private void setItemDecoration() {
        itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void loadAlbums() {
        if (mContext == null) {
            return;
        }
        if (getArguments() != null) {
            mAlbumList = getArguments().getParcelableArrayList("searchAlbum");
            mAdapter = new AlbumAdapter(mAlbumList);
            recyclerView.setAdapter(mAdapter);

            setItemDecoration();
        }


    }


    public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<SearchAlbumInfo> mList;

        public AlbumAdapter(List<SearchAlbumInfo> list) {
            mList = list;
        }


        public void updateDataSet(List<SearchAlbumInfo> list) {
            this.mList = list;
        }



        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.recyclerview_common_item, viewGroup, false));
        }



        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SearchAlbumInfo model = mList.get(position);
            ((ListItemViewHolder) holder).title.setText(model.getTitle());
            ((ListItemViewHolder) holder).title2.setText(model.getAuthor());
            ((ListItemViewHolder) holder).draweeView.setImageURI(Uri.parse(model.getPic_small()));//要加“” 弹出println needs a message

        }


        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }


        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView moreOverflow;
            SimpleDraweeView draweeView;
            TextView title, title2;

            ListItemViewHolder(View view) {
                super(view);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title2 = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.draweeView = (SimpleDraweeView) view.findViewById(R.id.viewpager_list_img);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                SearchAlbumInfo model = mList.get(getAdapterPosition());
                Intent intent = new Intent(mContext, AlbumsDetailActivity.class);
                intent.putExtra("albumid", model.getAlbum_id());
                intent.putExtra("albumart", model.getPic_small());
                intent.putExtra("albumname", model.getTitle());
                intent.putExtra("artistname", model.getAuthor());
                mContext.startActivity(intent);
            }

        }
    }


}

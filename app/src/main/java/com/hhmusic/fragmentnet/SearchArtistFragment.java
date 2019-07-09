package com.hhmusic.fragmentnet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hhmusic.R;
import com.hhmusic.activity.ArtistDetailActivity;
import com.hhmusic.fragment.AttachFragment;
import com.hhmusic.entity.SearchArtistInfo;
import com.hhmusic.widget.DividerItemDecoration;

import java.util.ArrayList;


public class SearchArtistFragment extends AttachFragment {

    private ArrayList<SearchArtistInfo> artistInfos;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArtistAdapter mAdapter;
    private RecyclerView.ItemDecoration itemDecoration;

    public static SearchArtistFragment newInstance(ArrayList<SearchArtistInfo> list) {
        SearchArtistFragment fragment = new SearchArtistFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("searchArtist", list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylerview, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ArtistAdapter(null);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        setItemDecoration();
        loadArtists();

        return view;
    }



    private void setItemDecoration() {

        itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }


    private void loadArtists() {

        if (getArguments() != null) {
            artistInfos = getArguments().getParcelableArrayList("searchArtist");
        }
        mAdapter = new ArtistAdapter(artistInfos);
        recyclerView.setAdapter(mAdapter);
        setItemDecoration();

    }


    public class ArtistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<SearchArtistInfo> mList;

        public ArtistAdapter(ArrayList<SearchArtistInfo> list) {
            mList = list;
        }


        public void updateDataSet(ArrayList<SearchArtistInfo> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_common_item, viewGroup, false);
            return new ListItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int i) {
            SearchArtistInfo model = mList.get(i);

            ((ListItemViewHolder) holder).mainTitle.setText(model.getAuthor());
            ((ListItemViewHolder) holder).draweeView.setImageURI(Uri.parse(model.getAvatar_middle()));

        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            SimpleDraweeView draweeView;
            TextView mainTitle, title;
            ImageView moreOverflow;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = view.findViewById(R.id.viewpager_list_toptext);
                this.title = view.findViewById(R.id.viewpager_list_bottom_text);
                this.draweeView = view.findViewById(R.id.viewpager_list_img);
                this.moreOverflow = view.findViewById(R.id.viewpager_list_button);



                view.setOnClickListener(this);

            }


            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                SearchArtistInfo model = mList.get(getAdapterPosition());
                intent.putExtra("artistid", model.getArtist_id());
                intent.putExtra("artistart", model.getAvatar_middle());
                intent.putExtra("artistname", model.getAuthor());
                intent.putExtra("artistUid", model.getTing_uid());
                mContext.startActivity(intent);
            }

        }
    }

}

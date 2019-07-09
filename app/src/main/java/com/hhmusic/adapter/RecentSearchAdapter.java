package com.hhmusic.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hhmusic.R;
import com.hhmusic.fragmentnet.SearchWords;
import com.hhmusic.provider.SearchHistory;

import java.util.ArrayList;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ItemHolder> {
    private Context mContext;
    private ArrayList<String> recentSearches = new ArrayList<>();
    private SearchWords searchWords;

    public RecentSearchAdapter(Context context) {
        mContext = context;
        recentSearches = SearchHistory.getInstance(context).getRecentSearches();
    }

    public void setListenter(SearchWords search) {
        searchWords = search;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recent_search_item, null);
        ItemHolder ml0 = new ItemHolder(v0);
        return ml0;
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {

        itemHolder.title.setText(recentSearches.get(i));
        setOnPopupMenuListener(itemHolder, i);
    }

    @Override
    public void onViewRecycled(ItemHolder itemHolder) {

    }

    @Override
    public int getItemCount() {
        return recentSearches.size();
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchHistory.getInstance(mContext).deleteRecentSearches(recentSearches.get(position));
                recentSearches = SearchHistory.getInstance(mContext).getRecentSearches();
                notifyDataSetChanged();
            }
        });
    }


    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        ImageView menu;

        public ItemHolder(View view) {
            super(view);
            this.title = view.findViewById(R.id.title);
            this.menu = view.findViewById(R.id.menu);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (searchWords != null) {
                searchWords.onSearch(recentSearches.get(getAdapterPosition()));
            }

        }

    }
}






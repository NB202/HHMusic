package com.hhmusic.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.hhmusic.R;
import com.hhmusic.info.MusicInfo;

import java.util.List;


public class OverFlowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private List<OverFlowItem> mList;
    private List<MusicInfo> musicInfos;
    private Context mContext;
    private Context activity;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public OverFlowAdapter(Context activity, List<OverFlowItem> list, List<MusicInfo> info) {
        mList = list;
        musicInfos = info;
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_flow_layout, parent, false);
        ListItemViewHolder vh = new ListItemViewHolder(view);

        view.setOnClickListener(this);
        return vh;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        OverFlowItem minfo = mList.get(position);
        ((ListItemViewHolder) holder).icon.setImageResource(minfo.getAvatar());
        ((ListItemViewHolder) holder).icon.setImageTintList(R.color.theme_color_primary);
        ((ListItemViewHolder) holder).title.setText(minfo.getTitle());

        ((ListItemViewHolder) holder).itemView.setTag(position + "");

    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {

            mOnItemClickListener.onItemClick(v, (String) v.getTag());
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, String data);
    }

    public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TintImageView icon;
        TextView title;

        ListItemViewHolder(View view) {
            super(view);
            this.icon = view.findViewById(R.id.pop_list_view);
            this.title = view.findViewById(R.id.pop_list_item);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }

    }

}

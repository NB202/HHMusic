package com.hhmusic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.hhmusic.R;
import com.hhmusic.info.MusicInfo;

import java.util.List;


public class MusicFlowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private List<OverFlowItem> mList;
    private MusicInfo musicInfo;
    private Context mContext;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public MusicFlowAdapter(Context context, List<OverFlowItem> list, MusicInfo info) {
        mList = list;
        musicInfo = info;
        mContext = context;
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
    public int getItemCount() {
        return mList.size();
    }


    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, String data);
    }

    public class ListItemViewHolder extends RecyclerView.ViewHolder {
        TintImageView icon;
        TextView title;

        ListItemViewHolder(View view) {
            super(view);
            this.icon = (TintImageView) view.findViewById(R.id.pop_list_view);
            this.title = (TextView) view.findViewById(R.id.pop_list_item);

        }


    }

}

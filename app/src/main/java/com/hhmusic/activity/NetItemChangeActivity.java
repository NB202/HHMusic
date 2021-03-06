package com.hhmusic.activity;

import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hhmusic.R;
import com.hhmusic.uitl.PreferencesUtility;
import com.hhmusic.widget.DividerItemDecoration;
import com.hhmusic.widget.DragSortRecycler;

import java.util.ArrayList;



public class NetItemChangeActivity extends AppCompatActivity {
    SelectAdapter mAdapter;
    ActionBar ab;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_netmusic_item);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView defaultPosition = findViewById(R.id.default_item_position);
        defaultPosition.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        defaultPosition.getPaint().setAntiAlias(true);//抗锯齿
        defaultPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesUtility.getInstance(NetItemChangeActivity.this).setItemPostion("推荐歌单 最新专辑 主播电台");
            }
        });

        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        String str = PreferencesUtility.getInstance(this).getItemPosition();
        final String[] strs = str.split(" ");
        ArrayList<String> list = new ArrayList<>();
        for (String st : strs) {
            list.add(st);
        }
        mAdapter = new SelectAdapter(list);

        recyclerView.setAdapter(mAdapter);
        DragSortRecycler dragSortRecycler = new DragSortRecycler();
        dragSortRecycler.setViewHandleId(R.id.move);

        dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
            @Override
            public void onItemMoved(int from, int to) {
                Log.d("queue", "onItemMoved " + from + " to " + to);
                final String str = mAdapter.getMusicAt(from);
                mAdapter.removeSongAt(from);
                mAdapter.addStringTo(to, str);
                mAdapter.notifyDataSetChanged();


                String st = "";
                for (int i = 0; i < mAdapter.strs.size(); i++) {
                    if (i == mAdapter.strs.size() - 1) {
                        st = st + mAdapter.strs.get(i);
                        continue;
                    }
                    st = st + mAdapter.strs.get(i) + " ";
                }
                PreferencesUtility.getInstance(NetItemChangeActivity.this).setItemPostion(st);

            }
        });

        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());
        recyclerView.setHasFixedSize(true);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public class SelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<String> strs = new ArrayList<>();

        public SelectAdapter(ArrayList<String> strs) {
            this.strs = strs;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.net_item_change_recyclerview_item, viewGroup, false);
            return new ListItemViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {
            String str = strs.get(i);
            //设置条目状态
            ((ListItemViewHolder) holder).mainTitle.setText(str);


        }

        public String getMusicAt(int i) {
            return strs.get(i);
        }

        public void addStringTo(int i, String str) {
            strs.add(i, str);
        }

        public void removeSongAt(int i) {
            strs.remove(i);
        }

        @Override
        public int getItemCount() {
            return strs.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {

            TextView mainTitle;
            ImageView move;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = view.findViewById(R.id.text);
                this.move = view.findViewById(R.id.move);

            }

        }
    }
}

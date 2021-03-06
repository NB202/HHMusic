package com.hhmusic.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hhmusic.R;
import com.hhmusic.downmusic.DownService;
import com.hhmusic.downmusic.DownloadDBEntity;
import com.hhmusic.provider.DownFileStore;
import com.hhmusic.uitl.IConstants;
import com.hhmusic.uitl.L;

import java.util.ArrayList;


public class DownFragment extends Fragment {

    private LinearLayout allStart, allStop, clear;
    private ArrayList<DownloadDBEntity> mList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DownLoadAdapter adapter;
    private DownFileStore downFileStore;
    private DownStatus downStatus;
    private int downPosition = -1;
    private String TAG = "DownFragment";
    private boolean d = true;
    public Activity mContext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_down, container, false);


        allStart = view.findViewById(R.id.down_start_all);
        allStop = view.findViewById(R.id.down_pause_all);
        clear = view.findViewById(R.id.down_clear_all);
        setListener();

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DownLoadAdapter(null, null);
        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        reload();

        return view;
    }



    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mContext = activity;
    }


    @Override
    public void onStart() {
        super.onStart();
        downStatus = new DownStatus();
        IntentFilter f = new IntentFilter();
        f.addAction(DownService.TASK_STARTDOWN);
        f.addAction(DownService.UPDATE_DOWNSTAUS);
        f.addAction(DownService.TASKS_CHANGED);
        mContext.registerReceiver(downStatus, new IntentFilter(f));
    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.unregisterReceiver(downStatus);
    }


    private void reload() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                downFileStore = DownFileStore.getInstance(mContext);
                mList = downFileStore.getDownLoadedListAllDowning();
                L.D(d, TAG, " mlist size = " + mList.size());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                adapter.update(mList, DownService.getPrepareTasks());
            }
        }.execute();
    }

    private void setListener() {


        allStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownService.START_ALL_DOWNTASK);
                intent.setPackage(IConstants.PACKAGE);
                mContext.startService(intent);


            }
        });
        allStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownService.PAUSE_ALLTASK);
                intent.setPackage(IConstants.PACKAGE);
                mContext.startService(intent);

            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownService.CANCLE_ALL_DOWNTASK);
                intent.setPackage(IConstants.PACKAGE);
                mContext.startService(intent);
            }
        });
    }


    class DownLoadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList mList;
        private ArrayList<String> currentTaskList;
        private long completed = 0;
        private long totalsize = -1;


        public DownLoadAdapter(ArrayList list, ArrayList<String> currentTaskList) {
            mList = list;
            this.currentTaskList = currentTaskList;
        }

        public void update(ArrayList list, ArrayList<String> currentTaskList) {
            mList = list;
            this.currentTaskList = currentTaskList;
            completed = 0;
            totalsize = -1;
            notifyDataSetChanged();
        }

        public void notifyItem(long completed, long total) {
            // L.D(d,TAG," comleted = " + completed + "  total = " + total);
            this.completed = completed;
            if (total != -1)
                this.totalsize = total;
            notifyItemChanged(downPosition);

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_down_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            boolean isCurrent = false;
            boolean isPreparing = false;
            final DownloadDBEntity task = (DownloadDBEntity) mList.get(position);
            ((ItemViewHolder) holder).title.setText(task.getFileName());

            if (currentTaskList.size() > 0) {
                L.D(d, TAG, "currentlist size = " + currentTaskList.size());
                isCurrent = currentTaskList.get(0).equals(task.getDownloadId());
                if (isCurrent) {
                    downPosition = position;
                }
                if (currentTaskList.contains(task.getDownloadId())) {
                    isPreparing = true;
                }
            }

            if (isCurrent) {
                completed = completed > task.getCompletedSize() ? completed : task.getCompletedSize();
                totalsize = totalsize > task.getTotalSize() ? totalsize : task.getTotalSize();
                if (completed == 0 || totalsize == -1) {
                    ((ItemViewHolder) holder).count.setText("正在计算大小文件大小");
                    ((ItemViewHolder) holder).progressBar.setVisibility(View.GONE);
                } else {
                    ((ItemViewHolder) holder).count.setText((float) (Math.round((float) completed / (1024 * 1024) * 10)) / 10 + "M/" +
                            (float) (Math.round((float) totalsize / (1024 * 1024) * 10)) / 10 + "M");
                    if (((ItemViewHolder) holder).progressBar.getVisibility() != View.VISIBLE)
                        ((ItemViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    if (totalsize > 0)
                        ((ItemViewHolder) holder).progressBar.setProgress((int) (100 * completed / totalsize));
                }
            } else if (isPreparing) {
                ((ItemViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((ItemViewHolder) holder).count.setText(task.getArtist() + "-" + task.getFileName());
            } else {
                ((ItemViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((ItemViewHolder) holder).count.setText("已经暂停，点击继续下载");
            }

            final boolean isPreparing1 = isPreparing;
            ((ItemViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPreparing1) {
                        L.D(d, TAG, "isprepaing");
                        Intent intent = new Intent(DownService.PAUSE_TASK);
                        intent.setPackage(IConstants.PACKAGE);
                        intent.putExtra("downloadid", task.getDownloadId());
                        mContext.startService(intent);
                    } else {
                        L.D(d, TAG, "not isprepaing");
                        Intent intent = new Intent(DownService.RESUME_START_DOWNTASK);
                        intent.setPackage(IConstants.PACKAGE);
                        intent.putExtra("downloadid", task.getDownloadId());
                        mContext.startService(intent);
                    }

                }
            });

            ((ItemViewHolder) holder).clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mContext).setTitle("要清除下载吗")
                            .setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(DownService.CANCLE_DOWNTASK);
                                    intent.putExtra("downloadid", task.getDownloadId());
                                    intent.setPackage(IConstants.PACKAGE);
                                    mContext.startService(intent);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });


        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            SimpleDraweeView draweeView;
            ImageView downloaded, clear;
            TextView title, count, artist;
            ProgressBar progressBar;

            public ItemViewHolder(View itemView) {
                super(itemView);
                draweeView = itemView.findViewById(R.id.down_img);
                title = itemView.findViewById(R.id.down_top_text);
                count = itemView.findViewById(R.id.down_count);
                clear = itemView.findViewById(R.id.down_single_clear);
                artist = itemView.findViewById(R.id.down_artist);
                downloaded = itemView.findViewById(R.id.downloaded);
                progressBar = itemView.findViewById(R.id.down_progress);
                progressBar.setMax(100);

                clear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }
        }


    }

    private class DownStatus extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DownService.UPDATE_DOWNSTAUS:
                    adapter.notifyItem(intent.getLongExtra("completesize", 0), intent.getLongExtra("totalsize", -1));
                    break;
                case DownService.TASK_STARTDOWN:
                    adapter.notifyItem(intent.getLongExtra("completesize", 0), intent.getLongExtra("totalsize", -1));
                    break;
                case DownService.TASKS_CHANGED:
                    reload();
                    break;

            }
        }
    }
}

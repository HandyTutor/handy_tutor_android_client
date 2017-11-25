package com.handy.handy.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.handy.handy.R;
import com.handy.handy.activity.StudyActivity;
import com.handy.handy.Item.VideoListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sekyo on 2017-11-12.
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    private List<VideoListItem> list;
    private int itemLayout;
    private Context context;

    public VideoListAdapter(int itemLayout, Context context){
        this.context = context;
        this.list = new ArrayList<VideoListItem>();
        this.itemLayout = itemLayout;
    }
    public void addItem(VideoListItem videoRecyclerItem){
        list.add(videoRecyclerItem);
        this.notifyItemInserted(list.size() - 1);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout,viewGroup,false);
        return new ViewHolder(view, list);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        VideoListItem item = list.get(position);
        viewHolder.textTitle.setText(item.getTitle());
        viewHolder.textCheck.setText(item.getCheck());
        //viewHolder.img.setBackgroundResource(item.getImage());
        Glide.with(context).
                load("https://img.youtube.com/vi/" + item.getVideoKey() + "/0.jpg").
                apply(new RequestOptions().centerCrop()).
                into(viewHolder.img);
        viewHolder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        public ImageView img;
        public TextView textTitle;
        public TextView textCheck;
        private List<VideoListItem> list;

        public ViewHolder(View itemView, List<VideoListItem> list){
            super(itemView);
            this.list = list;
            itemView.setOnClickListener(this);
            img = (ImageView) itemView.findViewById(R.id.img);
            textTitle = (TextView) itemView.findViewById(R.id.txt_title);
            textCheck = (TextView) itemView.findViewById(R.id.txt_check);
        }

        public void onClick(View v) {

            Intent intent = new Intent(v.getContext() , StudyActivity.class);
            intent.putExtra("video_key", list.get(getPosition()).getVideoKey());
            intent.putExtra("index", getPosition() + 1 + "");
            v.getContext().startActivity(intent);

        }
    }
}

package com.handy.handy.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handy.handy.Item.ScoreListItem;
import com.handy.handy.R;
import com.handy.handy.utils.PronunciationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sekyo on 2017-12-07.
 */

public class ScoreListAdapter extends RecyclerView.Adapter<ScoreListAdapter.ViewHolder> {

    private List<ScoreListItem> list;
    private int itemLayout;
    private Context context;
    private String videoKey;

    public ScoreListAdapter(int itemLayout, Context context, String videoKey){
        this.context = context;
        this.videoKey = videoKey;
        this.itemLayout = itemLayout;
        this.list = new ArrayList<ScoreListItem>();
    }
    public void addItem(ScoreListItem scoreListItem){
        list.add(scoreListItem);
        this.notifyItemInserted(list.size() - 1);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ScoreListItem item = list.get(position);
        viewHolder.script.setText(item.getScript());
        viewHolder.voice.setText(item.getVoice());
        new PronunciationManager(context, viewHolder.pronunciation, item.getScript(), videoKey + position).start();

        viewHolder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView script;
        public TextView voice;
        public TextView pronunciation;
        public TextView similarity;


        public ViewHolder(View itemView){
            super(itemView);
            script = itemView.findViewById(R.id.script_text);
            voice = itemView.findViewById(R.id.voice_text);
            pronunciation = itemView.findViewById(R.id.pronunciation_text);
            similarity = itemView.findViewById(R.id.similarity_text);
        }
    }
}
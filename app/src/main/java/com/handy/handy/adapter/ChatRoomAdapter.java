package com.handy.handy.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handy.handy.Item.ChatBubbleItem;
import com.handy.handy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sekyo on 2017-11-12.
 */
public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    private List<ChatBubbleItem> list;
    private int itemLayout;
    private Context context;

    public ChatRoomAdapter(int itemLayout, Context context){
        this.context = context;
        this.list = new ArrayList<ChatBubbleItem>();
        this.itemLayout = itemLayout;
    }

    public void addItem(ChatBubbleItem chatBubbleItem){
        list.add(chatBubbleItem);
        this.notifyItemInserted(list.size() - 1);
    }
    public void setContent(String content){
        list.get(list.size() - 1).setContent(content);
        this.notifyItemChanged(list.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ChatBubbleItem item = list.get(position);
        viewHolder.content.setText(item.getContent());
        if(list.get(position).isLeft()){
            viewHolder.arrow.setImageResource(R.drawable.speech_bubble_left);
            viewHolder.linearLayout.setGravity(Gravity.LEFT);
            viewHolder.linearLayout.removeAllViews();
            viewHolder.linearLayout.addView(viewHolder.arrow);
            viewHolder.linearLayout.addView(viewHolder.content);
        }
        viewHolder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView content;
        public ImageView arrow;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView){
            super(itemView);

            linearLayout = itemView.findViewById(R.id.chat_room_layout);
            arrow = itemView.findViewById(R.id.chat_bubble_arrow);
            content = itemView.findViewById(R.id.chat_bubble_content);
        }
    }
}
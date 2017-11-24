package com.handy.handy.utils;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.google.android.youtube.player.YouTubePlayer;
import com.handy.handy.Item.VideoItem;
import com.handy.handy.adapter.ChatRoomAdapter;

import java.util.ArrayList;

/**
 * Created by sekyo on 2017-11-22.
 */

public class SubtitleManager extends Thread{
    private VideoItem videoItem;
    private ChatRoomAdapter chatRoomAdapter;
    private YouTubePlayer youTubePlayer;
    private Activity activity;
    private RecyclerView chatRoom;
    private boolean languageSetting = true; // ture = korean, false = English

    public SubtitleManager(VideoItem videoItem, ChatRoomAdapter chatRoomAdapter, YouTubePlayer youTubePlayer, RecyclerView chatRoom, Activity activity){
        this.videoItem = videoItem;
        this.chatRoomAdapter = chatRoomAdapter;
        this.youTubePlayer = youTubePlayer;
        this.chatRoom = chatRoom;
        this.activity = activity;
    }

    public void run(){
        ArrayList<String> subtitles;
        ArrayList subtitleTimes;
        int i = 0;

        if(languageSetting){ // Korean
            subtitles = videoItem.getKrSubtitles();
            subtitleTimes = videoItem.getKrSubtitleTimes();
        } else { // English
            subtitles = videoItem.getEnSubtitles();
            subtitleTimes = videoItem.getEnSubtitleTimes();
        }

        while(i < subtitleTimes.size()){
            if(youTubePlayer.getCurrentTimeMillis() < (int)subtitleTimes.get(i)){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }
    }

    public void playKrSubtitle(){
        languageSetting = true;
        this.start();
    }

    public void playEnSubtitle(){
        languageSetting = false;
        this.start();
    }
    public class ReflectSubtitle implements Runnable{

        @Override
        public void run(){

        }
    }
}

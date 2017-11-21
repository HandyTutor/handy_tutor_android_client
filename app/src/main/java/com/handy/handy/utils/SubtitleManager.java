package com.handy.handy.utils;

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
    private boolean languageSetting = true; // ture = korean, false = English

    public SubtitleManager(VideoItem videoItem, ChatRoomAdapter chatRoomAdapter, YouTubePlayer youTubePlayer){
        this.videoItem = videoItem;
        this.chatRoomAdapter = chatRoomAdapter;
        this.youTubePlayer = youTubePlayer;
    }

    public void run(){
        ArrayList<String> subtitles;
        ArrayList subtitleTimes;

        if(languageSetting){ // Korean
            subtitles = videoItem.getKrSubtitles();
            subtitleTimes = videoItem.getKrSubtitleTimes();
        } else { // English
            subtitles = videoItem.getEnSubtitles();
            subtitleTimes = videoItem.getEnSubtitleTimes();
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
}

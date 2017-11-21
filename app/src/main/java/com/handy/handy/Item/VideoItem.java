package com.handy.handy.Item;

import java.util.ArrayList;

/**
 * Created by sekyo on 2017-11-22.
 */

public class VideoItem {
    private ArrayList krSubtitleTimes;
    private ArrayList usSubtitleTimes;
    private ArrayList<String> krSubtitles;
    private ArrayList<String> usSubtitles;
    
    public ArrayList<String> getKrSubtitles() {
        return krSubtitles;
    }

    public void setKrSubtitles(ArrayList<String> krSubtitles) {
        this.krSubtitles = krSubtitles;
    }

    public ArrayList<String> getUsSubtitles() {
        return usSubtitles;
    }

    public void setUsSubtitles(ArrayList<String> usSubtitles) {
        this.usSubtitles = usSubtitles;
    }

    public ArrayList getKrSubtitleTimes() {
        return krSubtitleTimes;
    }

    public void setKrSubtitleTimes(ArrayList krSubtitleTimes) {
        this.krSubtitleTimes = krSubtitleTimes;
    }

    public ArrayList getUsSubtitleTimes() {
        return usSubtitleTimes;
    }

    public void setUsSubtitleTimes(ArrayList usSubtitleTimes) {
        this.usSubtitleTimes = usSubtitleTimes;
    }
}

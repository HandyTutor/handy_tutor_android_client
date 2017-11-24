package com.handy.handy.Item;

import java.util.ArrayList;

/**
 * Created by sekyo on 2017-11-22.
 */

public class VideoItem {
    private ArrayList krSubtitleTimes;
    private ArrayList enSubtitleTimes;
    private ArrayList<String> krSubtitles;
    private ArrayList<String> enSubtitles;
    private ArrayList subtitleCharacter;

    public ArrayList getSubtitleCharacter() {
        return subtitleCharacter;
    }

    public void setSubtitleCharacter(ArrayList subtitleCharacter) {
        this.subtitleCharacter = subtitleCharacter;
    }

    public ArrayList<String> getKrSubtitles() {
        return krSubtitles;
    }

    public void setKrSubtitles(ArrayList<String> krSubtitles) {
        this.krSubtitles = krSubtitles;
    }

    public ArrayList<String> getEnSubtitles() {
        return enSubtitles;
    }

    public void setEnSubtitles(ArrayList<String> enSubtitles) {
        this.enSubtitles = enSubtitles;
    }

    public ArrayList getKrSubtitleTimes() {
        return krSubtitleTimes;
    }

    public void setKrSubtitleTimes(ArrayList krSubtitleTimes) {
        this.krSubtitleTimes = krSubtitleTimes;
    }

    public ArrayList getEnSubtitleTimes() {
        return enSubtitleTimes;
    }

    public void setEnSubtitleTimes(ArrayList enSubtitleTimes) {
        this.enSubtitleTimes = enSubtitleTimes;
    }
}

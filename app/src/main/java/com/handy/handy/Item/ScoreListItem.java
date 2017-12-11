package com.handy.handy.Item;

/**
 * Created by sekyo on 2017-12-07.
 */

public class ScoreListItem {
    private String script;
    private String voice;
    private String fileName;
    private String similarity;
    private String pronunciation;

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }


    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

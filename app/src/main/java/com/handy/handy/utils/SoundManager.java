package com.handy.handy.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.handy.handy.Config;
import com.handy.handy.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by sekyo on 2017-12-04.
 */

public class SoundManager extends Thread{
    private String fileName;
    private MediaPlayer.OnCompletionListener onCompletionListener;
    private MediaPlayer audioPlay;
    public SoundManager(String fileName, MediaPlayer.OnCompletionListener onCompletionListener){
        this.fileName = fileName;
        this.onCompletionListener = onCompletionListener;
    }
    public void run(){

        Log.v("FUCK","2");
        try {
            String Path_to_file = Environment.getExternalStorageDirectory()+ File.separator+"Naver/"+ fileName +".mp3";
            audioPlay = new MediaPlayer();
            audioPlay.setDataSource(Path_to_file);
            audioPlay.prepare();
            audioPlay.setOnCompletionListener(onCompletionListener);
            audioPlay.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    Log.v("FUCK","3");
                    audioPlay.setVolume(50,50);
                    audioPlay.start();
                }
            });

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

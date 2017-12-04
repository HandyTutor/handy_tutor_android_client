package com.handy.handy.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Environment;

import java.io.File;

/**
 * Created by sekyo on 2017-12-04.
 */

public class SoundManager {

    public static void SoundManager(Context context, int soundID){
        SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION,0);
        final int pool = soundPool.load(context, soundID, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(pool, 20, 20, 1, 0, 1f);
            }
        });

    }

}

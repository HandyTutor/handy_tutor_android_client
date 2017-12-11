package com.handy.handy.utils;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.handy.handy.Config;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by sekyo on 2017-12-07.
 */

public class SimilarityManager extends Thread {
    private Context context;
    private String input1;
    private String input2;
    private TextView textView;

    public SimilarityManager(Context context, TextView textView, String input1, String input2){
        this.context = context;
        this.textView = textView;
        this.input1 = input1;
        this.input2 = input2;
    }
    public void run(){
        Ion.with(context)
                .load(Config.SERVER_ADRESS + "phrase_similarity")
                .setBodyParameter("input1",input1)
                .setBodyParameter("input2",input2)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        textView.setText(result);
                    }
                });
    }
}

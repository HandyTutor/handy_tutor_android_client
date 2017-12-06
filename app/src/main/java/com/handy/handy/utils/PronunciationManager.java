package com.handy.handy.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.widget.TextView;

import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sekyo on 2017-12-07.
 */

public class PronunciationManager extends Thread{
    private static final String accessKey = "be7ea07b-ba8c-4738-93fb-32d7a8d8e03d";    // 발급받은 Access Key
    private static final String languageCode = "english";     // 언어 코드
    private static final String openApiURL = "http://aiopen.etri.re.kr:8000/WiseASR/Pronunciation";
    private String audioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeech/";  // 녹음된 음성 파일 경로
    private String script = null; // 평가 대본private
    private String audioContents = null;
    private Context context = null;
    private TextView textView = null;
    private Gson gson = null;

    public PronunciationManager(Context context, TextView textView, String script, String fileName){
        this.context = context;
        this.textView = textView;
        this.script = script;
        this.audioFilePath += fileName;
    }

    public void run(){
        gson = new Gson();

        final Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();

        byte[] audioBytes = readBytesFromFile(audioFilePath);
        audioContents = android.util.Base64.encodeToString(audioBytes, Base64.NO_WRAP);

        argument.put("language_code", languageCode);
        argument.put("script", script);
        argument.put("audio", audioContents);

        request.put("access_key", accessKey);
        request.put("argument", argument);
        try {
            Ion.with(context)
                    .load(openApiURL)
                    .setByteArrayBody(gson.toJson(request).getBytes("UTF-8"))
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            try{
                                JSONObject jsonObject = new JSONObject(result);
                                jsonObject = new JSONObject(jsonObject.getString("return_object"));
                                textView.setText(jsonObject.getString("score"));
                            } catch (JSONException e1){
                                e1.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static byte[] readBytesFromFile(String filePath) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];
            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }
}

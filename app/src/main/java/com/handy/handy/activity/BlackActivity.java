package com.handy.handy.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.handy.handy.Config;
import com.handy.handy.Item.ChatBubbleItem;
import com.handy.handy.R;
import com.handy.handy.utils.AudioWriterPCM;
import com.handy.handy.utils.NaverTTS;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionResult;

import java.lang.ref.WeakReference;
import java.util.List;

public class BlackActivity extends AppCompatActivity {

    // Naver Recognition에 필요한 변수
    private BlackActivity.RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private java.lang.String mResult;
    private AudioWriterPCM writer;
    // Handle speech recognition Messages.
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady:
                // Now an user can speak.
                writer = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;

            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                //Log.d(Config.TAG,"recoding");
                break;

            case R.id.partialResult:
                // Extract obj property typed with String.
                mResult = (java.lang.String) (msg.obj);
                break;

            case R.id.finalResult:
                // Extract obj property typed with String array.
                // The first element is recognition result for speech.
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();

                mResult = results.get(0);
                break;

            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }
                startRecognition();
                break;

            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                if(!mResult.equals(""))
                    handleFinalResult(mResult);
                else
                    startRecognition();
                break;
        }
    }

    // 음성 인식 최종 결과를 처리
    private void handleFinalResult(String result){
        if(result.contains("안녕")){
            Intent intent = new Intent(getApplicationContext() , MainActivity.class);
            startActivity(intent);
        } else {
            startRecognition();
        }
    }

    // 음성 인식을 시작
    private void startRecognition(){
        if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
            // Start button is pushed when SpeechRecognizer's state is inactive.
            // Run SpeechRecongizer by calling recognize().
            mResult = "";
            naverRecognizer.getSpeechRecognizer().initialize();
            naverRecognizer.recognize(SpeechConfig.LanguageType.KOREAN);
        } else {
            Log.d(Config.TAG, "stop and wait Final Result");

            naverRecognizer.getSpeechRecognizer().stop();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // NOTE : initialize() must be called on start time.
    }

    @Override
    protected void onResume() {
        super.onResume();

        mResult = "";
    }

    @Override
    protected void onStop() {
        super.onStop();
        // NOTE : release() must be called on stop time.
        naverRecognizer.getSpeechRecognizer().release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // NOTE : release() must be called on stop time.
        naverRecognizer.getSpeechRecognizer().release();
        finish();
    }

    // Declare handler for handling SpeechRecognizer thread's Messages.
    static class RecognitionHandler extends Handler {
        private final WeakReference<BlackActivity> mActivity;

        RecognitionHandler(BlackActivity activity) {
            mActivity = new WeakReference<BlackActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BlackActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black);

        // Naver STT 셋팅
        handler = new BlackActivity.RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, Config.NAVER_CLIENT_ID);

        Button button = findViewById(R.id.button)
;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() , MainActivity.class);
                startActivity(intent);
            }
        });
    }
}

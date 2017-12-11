package com.handy.handy.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import com.handy.handy.Config;
import com.handy.handy.Item.ChatBubbleItem;
import com.handy.handy.Item.ScoreListItem;
import com.handy.handy.R;
import com.handy.handy.adapter.ChatRoomAdapter;
import com.handy.handy.adapter.ScoreListAdapter;
import com.handy.handy.utils.AudioWriterPCM;
import com.handy.handy.utils.NaverTTS;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ScoreActivity extends AppCompatActivity {

    // Naver Recognition에 필요한 변수
    private RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private java.lang.String mResult;
    private AudioWriterPCM writer;

    // ChatRoomo 리사이클러 뷰에 필요한 변수
    private RecyclerView chatRoom;
    private ChatRoomAdapter chatRoomAdapter;

    // 말풍선 생성에 필요한 변수
    private boolean chatBubbleFlag = false;
    private ChatBubbleItem chatBubbleItem;

    // 발음 평가를 위한 스크립트
    private ArrayList<String> scripts;
    private ArrayList<String> voices;
    private String videoKey;

    // 스코어 리사이클러 뷰에 필요한 변수
    private RecyclerView scoreList;
    private ScoreListAdapter scoreListAdapter;

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
                if(!mResult.equals("")){
                    if(chatBubbleFlag == false){
                        chatBubbleFlag = true;
                        chatBubbleItem = new ChatBubbleItem(false, mResult);
                        chatRoomAdapter.addItem(chatBubbleItem);
                        chatRoom.scrollToPosition(chatRoomAdapter.getItemCount() - 1);
                    } else {
                        chatRoomAdapter.setContent(mResult);
                    }
                }
                break;

            case R.id.finalResult:
                // Extract obj property typed with String array.
                // The first element is recognition result for speech.
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();

                mResult = results.get(0);
                if(!mResult.equals("")) {
                    chatRoomAdapter.setContent(mResult);
                }
                break;

            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }
                startRecognition();
                break;

            case R.id.clientInactive:
                chatBubbleFlag = false;
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
        if(result.contains("그만")){
            new NaverTTS("오늘의 학습을 종료할게요.", new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    addChatBubble(true, "오늘의 학습을 종료할게요.");
                    Intent intent = new Intent(getApplicationContext() , BlackActivity.class);
                    startActivity(intent);
                }
            }).start();
        } else {
            new NaverTTS("무슨 뜻인지 잘 모르겠어요. 다시 한번 말해주세요.", new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    addChatBubble(true, "무슨 뜻인지 잘 모르겠어요. 다시 한번 말해주세요.");
                    startRecognition();
                }
            }).start();
        }
    }

    private void addChatBubble(boolean isLeft, String content){
        ChatBubbleItem chatBubbleItem = new ChatBubbleItem(isLeft, content);
        chatRoomAdapter.addItem(chatBubbleItem);
        chatRoom.scrollToPosition(chatRoomAdapter.getItemCount() - 1);
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
        private final WeakReference<ScoreActivity> mActivity;

        RecognitionHandler(ScoreActivity activity) {
            mActivity = new WeakReference<ScoreActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ScoreActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private byte[] readBytesFromFile(String filePath) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // 화면 꺼지지 않음
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 발음 평가를 위한 스크립트 로드
        scripts = getIntent().getStringArrayListExtra("scripts");
        voices = getIntent().getStringArrayListExtra("voices");
        videoKey = getIntent().getStringExtra("video_key");

        // 뷰를 할당
        chatRoom = findViewById(R.id.chat_room);

        // Naver STT 셋팅
        handler = new ScoreActivity.RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, Config.NAVER_CLIENT_ID);

        // 채팅 리스트 리사이클러 뷰 셋팅
        chatRoomAdapter = new ChatRoomAdapter(R.layout.chat_bubble,getApplicationContext());
        chatRoom.setAdapter(chatRoomAdapter);
        chatRoom.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatRoom.setItemAnimator(new DefaultItemAnimator());

        // 테스트 셋팅
        scripts = new ArrayList<String>();
        voices = new ArrayList<String>();

        scripts.add("This guy sayas \"hello\" I wanna kill my self");
        scripts.add("I'll be fine. I hope she will be very happy.");
        scripts.add("Why does everyone keep fixating on that");

        voices.add("This guy sayas \"hello\" I wanna kill my self");
        voices.add("I'm okay. I wish she will be very happy.");
        voices.add("I don't know");

        videoKey = "BkmxXpMqfAU";

        // 리스트뷰 예시 셋팅
        scoreList = findViewById(R.id.score_view);
        ArrayList<ScoreListItem> scoreListItems= new ArrayList<ScoreListItem>();

        ScoreListItem scoreListItem = new ScoreListItem();
        scoreListItem.setScript(scripts.get(0));
        scoreListItem.setVoice(voices.get(0));
        scoreListItem.setFileName(videoKey + 0);
        scoreListItem.setPronunciation("BEST");
        scoreListItem.setSimilarity("O");
        scoreListItems.add(scoreListItem);

        scoreListItem = new ScoreListItem();
        scoreListItem.setScript(scripts.get(1));
        scoreListItem.setVoice(voices.get(1));
        scoreListItem.setFileName(videoKey + 1);
        scoreListItem.setPronunciation("BEST");
        scoreListItem.setSimilarity("O");
        scoreListItems.add(scoreListItem);

        scoreListItem = new ScoreListItem();
        scoreListItem.setScript(scripts.get(2));
        scoreListItem.setVoice(voices.get(2));
        scoreListItem.setFileName(videoKey + 2);
        scoreListItem.setPronunciation("BAD");
        scoreListItem.setSimilarity("X");
        scoreListItems.add(scoreListItem);

        // 리사이클러 뷰 셋팅
        scoreListAdapter = new ScoreListAdapter(R.layout.score_list_item,getApplicationContext(),scoreListItems);
        scoreList.setAdapter(scoreListAdapter);
        scoreList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        scoreList.setItemAnimator(new DefaultItemAnimator());


        new NaverTTS("체쩜 결과를 보여드릴게요.", new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                addChatBubble(true, "체점 결과를 보여드릴게요.");
                naverRecognizer.getSpeechRecognizer().initialize();
                startRecognition();
            }
        }).start();

    }
}

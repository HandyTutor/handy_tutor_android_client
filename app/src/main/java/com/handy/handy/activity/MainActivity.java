package com.handy.handy.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;

import com.handy.handy.Config;
import com.handy.handy.Item.ChatBubbleItem;
import com.handy.handy.Item.VideoListItem;
import com.handy.handy.adapter.ChatRoomAdapter;
import com.handy.handy.utils.NaverTTS;
import com.handy.handy.R;
import com.handy.handy.adapter.VideoListAdapter;
import com.handy.handy.utils.AudioWriterPCM;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends Activity {

    // Naver Recognition에 필요한 변수
    private RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private java.lang.String mResult;
    private AudioWriterPCM writer;

    // ChatRoomo, VideoList 리사이클러 뷰에 필요한 변수
    private RecyclerView videoList;
    private VideoListAdapter videoListAdapter;
    private RecyclerView chatRoom;
    private ChatRoomAdapter chatRoomAdapter;

    // 말풍선 생성에 필요한 변수
    private boolean chatBubbleFlag = false;
    private ChatBubbleItem chatBubbleItem;

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
                Log.d(Config.TAG,"recoding");
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
        if(result.contains("시작")){
            new NaverTTS("오늘 학습을 시작할게요.", new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    addChatBubble(true, "오늘 학습을 시작할게요.");
                    Intent intent = new Intent(getApplicationContext() , StudyActivity.class);
                    intent.putExtra("video_key", "BkmxXpMqfAU");
                    intent.putExtra("index", 1);
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
        private final WeakReference<MainActivity> mActivity;

        RecognitionHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 화면 꺼지지 않음
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 뷰를 할당
        videoList = findViewById(R.id.video_list);
        chatRoom = findViewById(R.id.chat_room);

        // Naver STT 셋팅
        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, Config.NAVER_CLIENT_ID);

        // 비디오 리스트 리사이클러 뷰 셋팅
        videoListAdapter = new VideoListAdapter(R.layout.video_list_item,getApplicationContext());
        videoList.setAdapter(videoListAdapter);
        videoList.setLayoutManager(new GridLayoutManager(getApplicationContext(),2));
        videoList.setItemAnimator(new DefaultItemAnimator());

        // 채팅 리스트 리사이클러 뷰 셋팅
        chatRoomAdapter = new ChatRoomAdapter(R.layout.chat_bubble,getApplicationContext());
        chatRoom.setAdapter(chatRoomAdapter);
        chatRoom.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatRoom.setItemAnimator(new DefaultItemAnimator());


        // 예시 비디오 아이템
        /*
        VideoListItem videoListItem;

        videoListItem = new VideoListItem();
        videoListItem.setCheck("완료");
        videoListItem.setTitle("심슨 Scene1");
        videoListItem.setVideoKey("oBQRJf67cAk");
        videoListAdapter.addItem(videoListItem);

        videoListItem = new VideoListItem();
        videoListItem.setCheck("완료");
        videoListItem.setTitle("심슨 Scene2");
        videoListItem.setVideoKey("S65_th5s8hQ");
        videoListAdapter.addItem(videoListItem);

        videoListItem = new VideoListItem();
        videoListItem.setCheck("완료");
        videoListItem.setTitle("위베어스베어 Scene1");
        videoListItem.setVideoKey("TpHDteUxR4c");
        videoListAdapter.addItem(videoListItem);

        videoListItem = new VideoListItem();
        videoListItem.setCheck("미완료");
        videoListItem.setTitle("프렌즈 Scene1");
        videoListItem.setVideoKey("QmJZqzzNCfU");
        videoListAdapter.addItem(videoListItem);

        videoListItem = new VideoListItem();
        videoListItem.setCheck("미완료");
        videoListItem.setTitle("사우스파크 Scene1");
        videoListItem.setVideoKey("hLUr3AHZihU");
        videoListAdapter.addItem(videoListItem);

        videoListItem = new VideoListItem();
        videoListItem.setCheck("미완료");
        videoListItem.setTitle("사우스파크 Scene2");
        videoListItem.setVideoKey("ZrU_tt4R3xY");
        videoListAdapter.addItem(videoListItem);

        videoListItem = new VideoListItem();
        videoListItem.setCheck("미완료");
        videoListItem.setTitle("위베어스베어 Scene2");
        videoListItem.setVideoKey("yk6ORj9QVOk");
        videoListAdapter.addItem(videoListItem);

        videoListItem = new VideoListItem();
        videoListItem.setCheck("미완료");
        videoListItem.setTitle("프렌즈 Scene2");
        videoListItem.setVideoKey("gWSI3hMhxO4");
        videoListAdapter.addItem(videoListItem);
        */

        // 서버에서 비디오 목록을 받아옴
        Ion.with(getApplicationContext())
                .load(Config.SERVER_ADRESS + "video_list")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        try {
                            JSONArray videoList = new JSONArray(result);
                            JSONObject video;

                            // 비디오 리스트 리사이클러뷰에 추가
                            for(int i = 0;i < videoList.length();i++){
                                video = videoList.getJSONObject(i);
                                VideoListItem videoRecyclerItem = new VideoListItem();
                                videoRecyclerItem.setCheck("완료");
                                videoRecyclerItem.setTitle((String)video.get("VIDEO_TITLE"));
                                videoRecyclerItem.setVideoKey((String)video.get("VIDEO_KEY"));
                                videoListAdapter.addItem(videoRecyclerItem);
                            }

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });


        new NaverTTS("안녕하세요. 학습 목록을 보여드릴게요.", new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                addChatBubble(true, "안녕하세요. 학습 목록을 보여드릴게요.");
                naverRecognizer.getSpeechRecognizer().initialize();
                startRecognition();
            }
        }).start();

    }
}

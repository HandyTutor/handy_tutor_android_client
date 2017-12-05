package com.handy.handy.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.handy.handy.Config;
import com.handy.handy.Item.ChatBubbleItem;
import com.handy.handy.Item.Subtitle;
import com.handy.handy.R;
import com.handy.handy.adapter.ChatRoomAdapter;
import com.handy.handy.utils.AudioWriterPCM;
import com.handy.handy.utils.NaverTTS;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StudyActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    // 유튜브 플레이어에 필요한 변수
    private static final int RECOVERY_REQUEST = 1;
    private YouTubePlayerView youTubeView;
    private MyPlayerStateChangeListener playerStateChangeListener;
    private MyPlaybackEventListener playbackEventListener;
    private YouTubePlayer player;
    private String videoKey;

    // Naver Recognition에 필요한 변수
    private StudyActivity.RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private java.lang.String mResult;
    private AudioWriterPCM writer;

    // ChatRoomo 리사이클러 뷰에 필요한 변수
    private RecyclerView chatRoom;
    private ChatRoomAdapter chatRoomAdapter;

    // 말풍선 생성에 필요한 변수
    private boolean chatBubbleFlag = false;
    private ChatBubbleItem chatBubbleItem;

    // 자막 재생을 위한 변수
    private ArrayList<Subtitle> subtitles;
    private int nowSubtitleIndex = 0;

    // 평가에 필요한 스크립트 변수
    private ArrayList<String> voices;
    private ArrayList<String> scripts;

    // 학습 순서 변수 0 = 한글 자막 재생 중, 1 = 영어 자막 재생 중, 2 = 인터랙팅 중
    private int state = 0;

    // Handle speech recognition Messages.
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady:
                // Now an user can speak.
                Log.d(Config.TAG,"clientReady");
                writer = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeech");
                writer.open(videoKey + voices.size());
                break;

            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
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
                voices.add("error");
                break;

            case R.id.clientInactive:
                chatBubbleFlag = false;
                if (writer != null) {
                    writer.close();
                }
                voices.add(mResult);
                player.play();
                break;
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
            naverRecognizer.recognize(SpeechConfig.LanguageType.ENGLISH);
        } else {
            Log.d(Config.TAG, "stop and wait Final Result");

            naverRecognizer.getSpeechRecognizer().stop();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // NOTE : initialize() must be called on start time.
        naverRecognizer.getSpeechRecognizer().initialize();
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
        private final WeakReference<StudyActivity> mActivity;

        RecognitionHandler(StudyActivity activity) {
            mActivity = new WeakReference<StudyActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            StudyActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        chatRoom = findViewById(R.id.chat_room);
        //videoKey = getIntent().getStringExtra("video_key");
        videoKey = "BkmxXpMqfAU";

        // 유튜브 플레이어 셋팅
        youTubeView = findViewById(R.id.youtube_view);
        youTubeView.initialize(Config.YOUTUBE_API_KEY, this);
        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();

        // Naver STT 셋팅
        handler = new StudyActivity.RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, Config.NAVER_CLIENT_ID);

        // 채팅 리스트 리사이클러 뷰 셋팅
        chatRoomAdapter = new ChatRoomAdapter(R.layout.chat_bubble,getApplicationContext());
        chatRoom.setAdapter(chatRoomAdapter);
        chatRoom.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatRoom.setItemAnimator(new DefaultItemAnimator());

        // 사용자의 발화 어레이리스트 초기화
        voices = new ArrayList<String>();

        // 서버에서 자막리스트를 받아옴
        Ion.with(getApplicationContext())
                .load(Config.SERVER_ADRESS + "video")
                .setBodyParameter("video_key",videoKey)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            subtitles = new ArrayList<Subtitle>();
                            scripts = new ArrayList<String>();
                            for(int i = 0;i < jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Subtitle subtitle = new Subtitle();
                                subtitle.setEnglish(jsonObject.getString("ENGLISH"));
                                subtitle.setKorean(jsonObject.getString("KOREAN"));
                                subtitle.setRole(Integer.parseInt(jsonObject.getString("ROLE")));
                                subtitle.setTime(Integer.parseInt(jsonObject.getString("TIME")));
                                subtitles.add(subtitle);
                                if(subtitle.getRole() == 1)
                                    scripts.add(subtitle.getEnglish());
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.player = player;
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);

        if (!wasRestored) {
            player.cueVideo(videoKey); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
        }
        player.play();

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(Config.YOUTUBE_API_KEY, this);
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubeView;
    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        @Override
        public void onPlaying() {
            // Called when playback starts, either due to user action or call to play().
        }

        @Override
        public void onPaused() {
            // Called when playback is paused, either due to user action or call to pause().
        }

        @Override
        public void onStopped() {
            // Called when playback stops for a reason other than being paused.
        }

        @Override
        public void onBuffering(boolean b) {
            // Called when buffering starts or ends.
        }

        @Override
        public void onSeekTo(int i) {
            // Called when a jump in playback position occurs, either
            // due to user scrubbing or call to seekRelativeMillis() or seekToMillis()
        }
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoading() {
            // Called when the player is loading a video
            // At this point, it's not ready to accept commands affecting playback such as play() or pause()
        }

        @Override
        public void onLoaded(String s) {
            // Called when a video is done loading.
            // Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
            //showKrSubtitle();
            startLearning();
        }

        @Override
        public void onAdStarted() {
            // Called when playback of an advertisement starts.
        }

        @Override
        public void onVideoStarted() {
            // Called when playback of the video starts.
        }

        @Override
        public void onVideoEnded() {
            // Called when the video reaches its end.

            if(state == 0){ // 한글 자막 재생 종료
                showEnSubtitle();
                state++;
            } else if (state == 1){ // 영어 자막 재생 종료
                startLearning();
                state++;
            } else if (state == 2){ // 인터랙팅 종료
                Intent intent = new Intent(getApplicationContext() , ScoreActivity.class);
                intent.putExtra("video_key", videoKey);
                intent.putStringArrayListExtra("scripts", scripts);
                intent.putStringArrayListExtra("voices", voices);
                startActivity(intent);
            }
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            // Called when an error occurs.
        }
    }
    private void showKrSubtitle(){
        addChatBubble(true, "한글 자막과 함께 보여드릴게요.");
        new NaverTTS("한글 자막과 함께 보여드릴게요.", new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                player.play();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        ArrayList flag = new ArrayList();
                        for(int j = 0;j < subtitles.size();j++){
                            flag.add(true);
                        }
                        nowSubtitleIndex = 0;
                        while (i < subtitles.size()){
                            if(player.getCurrentTimeMillis() > subtitles.get(i).getTime() && (boolean)flag.get(i)){
                                flag.set(i, false);
                                nowSubtitleIndex = i++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addChatBubble(true,subtitles.get(nowSubtitleIndex).getKorean());
                                        Log.v("FUCK", nowSubtitleIndex + subtitles.get(nowSubtitleIndex).getKorean());
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        }).start();
    }

    private void showEnSubtitle(){
        addChatBubble(true,"영어 자막과 함께 보여드릴게요.");
        new NaverTTS("영어 자막과 함께 보여드릴게요.", new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                player.play();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        ArrayList flag = new ArrayList();
                        for(int j = 0;j < subtitles.size();j++){
                            flag.add(true);
                        }
                        nowSubtitleIndex = 0;
                        while (i < subtitles.size()){
                            if(player.getCurrentTimeMillis() != player.getDurationMillis() && player.getCurrentTimeMillis()> subtitles.get(i).getTime() && (boolean)flag.get(i)){
                                flag.set(i, false);
                                nowSubtitleIndex = i++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addChatBubble(true,subtitles.get(nowSubtitleIndex).getEnglish());
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        }).start();
    }
    private void startLearning(){
        addChatBubble(true,"영상이 정지되면 적절한 대사를 말해주세요.");
        new NaverTTS("영상이 정지되면 적절한 대사를 말해주세요.", new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                player.play();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        ArrayList flag = new ArrayList();
                        for(int j = 0;j < subtitles.size();j++){
                            flag.add(true);
                        }
                        nowSubtitleIndex = 0;
                        while (i < subtitles.size()){
                            if(player.getCurrentTimeMillis() != player.getDurationMillis() && player.getCurrentTimeMillis()> subtitles.get(i).getTime() && (boolean)flag.get(i)){
                                flag.set(i, false);
                                nowSubtitleIndex = i++;
                                if(subtitles.get(i - 1).getRole() == 1){
                                    player.pause();
                                    SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
                                    final int resource = soundPool.load(getApplicationContext(), R.raw.sound, 2);

                                    soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                                        @Override
                                        public void onLoadComplete(SoundPool soundPool, int sampleId,
                                                                   int status) {
                                            soundPool.play(resource, 5, 5, 1, 0, 1f);
                                            startRecognition();
                                        }
                                    });

                                }
                            }
                        }
                    }
                }).start();
            }
        }).start();
    }
}

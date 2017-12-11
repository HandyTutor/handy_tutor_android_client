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
import com.handy.handy.utils.PronunciationManager;
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
    private ArrayList<String> score;

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

                Log.v("FUCK","-1");
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
                Log.v("FUCK","0");
                SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
                final int resource = soundPool.load(getApplicationContext(), R.raw.sound, 2);

                Log.v("FUCK","1");
                soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId,
                                               int status) {
                        Log.v("FUCK","2");
                        soundPool.play(resource, 5, 5, 1, 0, 1f);
                        Log.v("FUCK","3");
                        Toast.makeText(getApplicationContext(), score.get(voices.size()), Toast.LENGTH_LONG).show();
                    }
                });
                Log.v("FUCK","4");
                voices.add(mResult);

                player.play();
                Log.v("FUCK","5");
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
            naverRecognizer.getSpeechRecognizer().initialize();
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
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.player = player;
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
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
            if(state == 0){
                //showKrSubtitle();
                startLearning();
                state++;
            }
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

            if(state == 1){ // 한글 자막 재생 종료
                //showEnSubtitle();

                Intent intent = new Intent(getApplicationContext() , ScoreActivity.class);
                intent.putExtra("video_key", videoKey);
                intent.putStringArrayListExtra("scripts", scripts);
                intent.putStringArrayListExtra("voices", voices);
                startActivity(intent);

                state++;
            } else if (state == 2){ // 영어 자막 재생 종료
                startLearning();
                state++;
            } else if (state == 3){ // 인터랙팅 종료
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
                                            Log.v("FUCK","-2");
                                            startRecognition();
                                        }
                                    });

                                    Log.v("FUCK","5");
                                }
                            }
                        }
                    }
                }).start();
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int sync = 300;
        chatRoom = findViewById(R.id.chat_room);
        //videoKey = getIntent().getStringExtra("video_key");
        videoKey = "QmJZqzzNCfU";

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

        // 예시 리스트 아이템
        /*
        subtitles = new ArrayList<Subtitle>();
        Subtitle subtitle;
        subtitle = new Subtitle();
        subtitle.setKorean("안녕");
        subtitle.setEnglish("Hi");
        subtitle.setTime(2200 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("쟤가 저렇게 인사하면 콱 죽여버리고 싶어");
        subtitle.setEnglish("This guy sayas \"hello\" I wanna kill my self");
        subtitle.setTime(4600 + sync);
        subtitle.setRole(1);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("괜찮아요 오빠?");
        subtitle.setEnglish("Are you okay sweetie?");
        subtitle.setTime(8700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("누군가 내 목에 손을 집어넣고 내장을 잡아채서");
        subtitle.setEnglish("I feel like someone grabbed my small intenstine");
        subtitle.setTime(9700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("입 밖으로 꺼내서 목에 돌돌 마는 기분이야");
        subtitle.setEnglish("pulled it out of my mouth and tied it around my neck");
        subtitle.setTime(12700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("쿠키 먹을래?");
        subtitle.setEnglish("Cookie? ");
        subtitle.setTime(15900 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("캐롤이 오늘 짐을 가져갔대");
        subtitle.setEnglish("Carol moved her stuff out today");
        subtitle.setTime(19700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("커피 타줄게. 고마워");
        subtitle.setEnglish("Let me get you some coffee. Thanks");
        subtitle.setTime(22700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("그만 내 아우라를 없앨 생각 말아줘");
        subtitle.setEnglish("No No don't! Stop cleansing my aura");
        subtitle.setTime(28700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("그냥 내 아우라를 그냥 두란말야 알겠어?");
        subtitle.setEnglish("Just leave my aura alone okay?");
        subtitle.setTime(33700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("난 괜찮아 솔직히. 캐롤이 행복햇으면 좋겠어");
        subtitle.setEnglish("I'll be fine. I hope she will be very happy.");
        subtitle.setTime(37700 + sync);
        subtitle.setRole(1);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("아닐걸");
        subtitle.setEnglish("No you don't");
        subtitle.setTime(41700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("당연히 아니지 날 버리고 간 매정한 여잔데");
        subtitle.setEnglish("No I don't. To hell with her. She left me!");
        subtitle.setTime(42400+ sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("캐롤이 레즈비언인거 정말 몰랐어?");
        subtitle.setEnglish("And you never knew she was a lesbian");
        subtitle.setTime(45700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("그래 몰랐다");
        subtitle.setEnglish("No! Okay?");
        subtitle.setTime(51700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("왜 항상 그걸 묻는 거지?");
        subtitle.setEnglish("Why does everyone keep fixating on that");
        subtitle.setTime(53700 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        subtitle = new Subtitle();
        subtitle.setKorean("자기도 몰랐다는데 내가 어떻게 알았겠어?");
        subtitle.setEnglish("She didn't know. How should I know");
        subtitle.setTime(58200 + sync);
        subtitle.setRole(0);
        subtitles.add(subtitle);

        score = new ArrayList<String>();
        score.add("발음: BEST\n의미: O");
        score.add("발음: BEST\n의미: O");
        score.add("발음: BAD\n의미: X");
        */

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
}

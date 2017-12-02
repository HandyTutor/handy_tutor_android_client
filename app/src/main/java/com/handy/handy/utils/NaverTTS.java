package com.handy.handy.utils;


import android.media.MediaPlayer;
import android.os.Environment;

import com.handy.handy.Config;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NaverTTS extends Thread{
    private String inputText = null;
    MediaPlayer.OnCompletionListener onCompletionListener;

    public NaverTTS(String inputText, MediaPlayer.OnCompletionListener onCompletionListener){
        this.inputText = inputText;
        this.onCompletionListener = onCompletionListener;
    }
    public void run(){

        try {
            String text = URLEncoder.encode(inputText, "UTF-8"); // 13자
            String apiURL = "https://openapi.naver.com/v1/voice/tts.bin";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", Config.NAVER_CLIENT_ID);
            con.setRequestProperty("X-Naver-Client-Secret", Config.NAVER_CLIENT_SECRET);
            // post request
            String postParams = "speaker=jinho&speed=0&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;

            if(responseCode==200) { // 정상 호출
                InputStream is = con.getInputStream();

                int read = 0;
                byte[] bytes = new byte[1024];

                // 디렉토리 생성
                File dir = new File(Environment.getExternalStorageDirectory()+"/", "Naver");
                if(!dir.exists()){
                    dir.mkdirs();
                }

                String tempname = "naverttstemp"; //하나의 파일명으로 덮어쓰기 하자.
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "Naver/" + tempname + ".mp3");

                f.createNewFile();
                OutputStream outputStream = new FileOutputStream(f);
                while ((read =is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                is.close();

                // mp3파일 재생
                String Path_to_file = Environment.getExternalStorageDirectory()+File.separator+"Naver/"+tempname+".mp3";
                MediaPlayer audioPlay = new MediaPlayer();
                audioPlay.setDataSource(Path_to_file);
                audioPlay.prepare();
                audioPlay.setOnCompletionListener(onCompletionListener);
                audioPlay.start();


            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println(response.toString());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

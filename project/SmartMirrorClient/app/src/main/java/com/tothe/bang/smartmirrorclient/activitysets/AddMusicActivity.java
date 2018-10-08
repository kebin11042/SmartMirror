package com.tothe.bang.smartmirrorclient.activitysets;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.tothe.bang.smartmirrorclient.R;
import com.tothe.bang.smartmirrorclient.datasets.UserClass;
import com.tothe.bang.smartmirrorclient.fragmentsets.MusicFragment;
import com.tothe.bang.smartmirrorclient.viewsets.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by BANG on 2016-03-28.
 */
public class AddMusicActivity extends Activity implements TextWatcher, View.OnClickListener{

    //link url : https://soundcloud.com/k2nblog26/crush-dont-forget-feat-taeyeon
    //응답
    //result = ok, fail, curl_fail, not_streamable

    private final UserClass userClass = LoginedActivity.getUserClass();

    private String strSoundcloudShareURL;       //사운드 클라우드에서 공유한 링크

    private String soundcloudTitle;
    private String soundcloudStreamUrl;
    private String soundcloudArtworkUrl;

    private ImageButton ibtnBack;

    private ImageView imgvSoundcloudArtwork;
    private TextView txtvSoundcloudTitle;

    private EditText edtxSoundcloudUrl;
    private EditText edtxSubject;

    private Button btnPlay;
    private Button btnOk;

    private boolean isSoundcloud;
    private boolean isUploadEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isSoundcloud = false;
        isUploadEnable = false;

        try {
            strSoundcloudShareURL = getIntent().getExtras().getString("soundcloudURL");
        }
        catch (NullPointerException e){
            strSoundcloudShareURL = null;
        }

        setContentView(R.layout.activity_add_music);

        Init();
    }

    public void Init(){
        ibtnBack = (ImageButton) findViewById(R.id.ibtnBack);

        imgvSoundcloudArtwork = (ImageView) findViewById(R.id.imgvAddMusicSoundcloudArtwork);
        txtvSoundcloudTitle = (TextView) findViewById(R.id.txtvAddMusicSoundcloudTitle);

        edtxSoundcloudUrl = (EditText) findViewById(R.id.edtxAddMusicSoundcloudLink);
        edtxSubject = (EditText) findViewById(R.id.edtxAddMusicSubject);

        btnPlay = (Button) findViewById(R.id.btnAddMusicPlay);
        btnOk = (Button) findViewById(R.id.btnAddMusicOk);

        edtxSoundcloudUrl.addTextChangedListener(this);

        ibtnBack.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnOk.setOnClickListener(this);

        if(strSoundcloudShareURL != null){
            String[] subSoundcloudsShareURL = strSoundcloudShareURL.split("\n");
            int index = -1;
            for(int i=0;i<subSoundcloudsShareURL.length;i++){
                if(subSoundcloudsShareURL[i].contains("https://soundcloud.com")){
                    index = i;
                }
            }

            if(index != -1){
                edtxSoundcloudUrl.setText(subSoundcloudsShareURL[index]);
                TaskGetSoundcloud taskGetSoundcloud = new TaskGetSoundcloud(edtxSoundcloudUrl.getText().toString());
                taskGetSoundcloud.execute("");
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        if(count > 0){
//            String strSoundcloudUrl = s.toString();
//            if(strSoundcloudUrl.contains("https://soundcloud.com/")){
//                btnPlay.setBackgroundResource(R.drawable.selector_th_green);
//                playEnable = true;
//            }
//            else{
//                btnPlay.setBackgroundColor(Color.parseColor("#9D9FA6"));
//                playEnable = false;
//            }
//        }
//        else{
//            playEnable = false;
//        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        //Log.i("AddMusic", "afterTextChanged Editable = " + s.toString());
        isUploadEnable = false;

        String strSoundcloudUrl = s.toString();
        if(strSoundcloudUrl.contains("https://soundcloud.com/")){
            btnPlay.setBackgroundResource(R.drawable.selector_th_green);
            isSoundcloud = true;
        }
        else{
            btnPlay.setBackgroundColor(Color.parseColor("#9D9FA6"));
            isSoundcloud = false;
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == ibtnBack.getId()){
            finish();
        }
        else if(v.getId() == btnPlay.getId()){
            if(isSoundcloud){
                TaskGetSoundcloud taskGetSoundcloud = new TaskGetSoundcloud(edtxSoundcloudUrl.getText().toString());
                taskGetSoundcloud.execute("");
            }
            else{
                Toast.makeText(AddMusicActivity.this, "soundcloud 링크 url이 아닌것 같네요", Toast.LENGTH_SHORT).show();
            }
        }
        else if(v.getId() == btnOk.getId()){
            if(isUploadEnable){
                String strSubject = edtxSubject.getText().toString();
                if(!strSubject.isEmpty()){
                    String soundcloud_url = edtxSoundcloudUrl.getText().toString();

                    TaskInsertMusic taskInsertMusic = new TaskInsertMusic(strSubject, soundcloud_url);
                    taskInsertMusic.execute("");
                }
                else{
                    Toast.makeText(AddMusicActivity.this, "제목을 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(AddMusicActivity.this, "음악 링크를 먼저 확인해 주세요", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class TaskGetSoundcloud extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/get_soundcloud.php";

        private String music_link_url;

        private LoadingDialog loadingDialog;

        public TaskGetSoundcloud(String music_link_url) {
            this.music_link_url = music_link_url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(AddMusicActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("music_link_url", this.music_link_url)
                    .build();

            Request request = new Request.Builder()
                    .url(strUrl)
                    .post(requestBody)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();

                if(response.isSuccessful()){
                    strResponse = response.body().string();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return strResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //Log.i("InsertMusic", s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){
                    isUploadEnable = true;
                    loadingDialog.dismissWithAnimation();

                    soundcloudTitle = jsonObject.getString("soundcloud_title");
                    soundcloudStreamUrl = jsonObject.getString("soundcloud_stream_url");
                    soundcloudArtworkUrl = jsonObject.getString("soundcloud_artwork_url");

                    Picasso.with(AddMusicActivity.this).load(soundcloudArtworkUrl).fit().into(imgvSoundcloudArtwork);
                    txtvSoundcloudTitle.setText(soundcloudTitle);
                }
                else if(result.equals("curl_fail")){
                    //올바른 링크가 아닌것 같네요
                    loadingDialog.dismissWithAnimation();
                }
                else if(result.equals("not_streamable")){
                    //스트림 음악을 지원하지 않네요
                    loadingDialog.dismissWithAnimation();
                }
                else{
                    //응답 오류
                    loadingDialog.dismissWithAnimation();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                loadingDialog.dismissWithAnimation();
            }
        }
    }

    public class TaskInsertMusic extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/insert_music.php";

        private String music_subject;
        private String music_link_url;

        private LoadingDialog loadingDialog;

        public TaskInsertMusic(String music_subject, String music_link_url) {
            this.music_subject = music_subject;
            this.music_link_url = music_link_url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(AddMusicActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_id", userClass.getId())
                    .addFormDataPart("music_subject", music_subject)
                    .addFormDataPart("music_link_url", music_link_url)
                    .build();

            Request request = new Request.Builder()
                    .url(strUrl)
                    .post(requestBody)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();

                if(response.isSuccessful()){
                    strResponse = response.body().string();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return strResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //Log.i("insertMusic", s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){
                    loadingDialog.dismissWithAnimation();
                    Toast.makeText(AddMusicActivity.this, "완료!", Toast.LENGTH_SHORT).show();
                    MusicFragment.musicFragment.onRefresh();
                    finish();
                }
                else if(result.equals("curl_fail")){
                    //올바른 링크가 아닌것 같네요
                    loadingDialog.dismissWithAnimation();
                }
                else if(result.equals("not_streamable")){
                    //스트림 음악을 지원하지 않네요
                    loadingDialog.dismissWithAnimation();
                }
                else if(result.equals("fail")){
                    //실패
                    loadingDialog.dismissWithAnimation();
                }
                else{
                    //응답 오류
                    loadingDialog.dismissWithAnimation();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                loadingDialog.dismissWithAnimation();
            }
        }
    }
}

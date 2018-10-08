package com.tothe.bang.smartmirrorclient.fragmentsets;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.tothe.bang.smartmirrorclient.R;
import com.tothe.bang.smartmirrorclient.activitysets.AddMusicActivity;
import com.tothe.bang.smartmirrorclient.activitysets.LoginedActivity;
import com.tothe.bang.smartmirrorclient.datasets.MusicClass;
import com.tothe.bang.smartmirrorclient.datasets.UserClass;
import com.tothe.bang.smartmirrorclient.viewsets.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by BANG on 2016-03-27.
 */
public class MusicFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener{

    public static MusicFragment musicFragment;

    private final UserClass userClass = LoginedActivity.getUserClass();

    private ArrayList<MusicClass> arrMusicClass;
    private int currentPage;
    private int totalPage;
    private int totalCnt;

    private MediaPlayer mediaPlayer;
    private MusicClass musicClassPlaying;   //현재 재생중인 최근 음악 클래스

    private ViewGroup rootView;

    private SwipeRefreshLayout swlayMusic;
    private RecyclerView recyclerView;

    private FrameLayout layNoResult;

    private AdapterMusic adapterMusic;

    private FloatingActionButton fabAdd;

    private ImageView imgvPlaying;
    private TextView txtvPlayingSubject;
    private ImageButton ibtnPlaying;

    private ProgressBar progressBar;

    public static MusicFragment create(){
        MusicFragment fragment = new MusicFragment();

        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        musicFragment = MusicFragment.this;

        arrMusicClass = null;
        totalPage = 0;
        totalCnt = 0;
        currentPage = 1;

        adapterMusic = null;
        musicClassPlaying = null;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_music, container, false);

        Init();

        return rootView;
    }

    public void Init(){
        layNoResult = (FrameLayout) rootView.findViewById(R.id.layMusicNoResult);

        swlayMusic = (SwipeRefreshLayout) rootView.findViewById(R.id.swlayMusic);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyvMusic);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        fabAdd = (FloatingActionButton) rootView.findViewById(R.id.fabMusicAdd);

        imgvPlaying = (ImageView) rootView.findViewById(R.id.imgvMusicPlaying);
        txtvPlayingSubject = (TextView) rootView.findViewById(R.id.txtvMusicPlayingSubject);
        ibtnPlaying = (ImageButton) rootView.findViewById(R.id.ibtnMusicPlaying);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progbMusicPlaying);

        fabAdd.setOnClickListener(this);
        ibtnPlaying.setOnClickListener(this);

        swlayMusic.setOnRefreshListener(this);

        swlayMusic.setRefreshing(true);
        TaskMusicList taskMusicList = new TaskMusicList();
        taskMusicList.execute("");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == fabAdd.getId()){
            Intent intent = new Intent(getActivity(), AddMusicActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == ibtnPlaying.getId()){
            if(musicClassPlaying != null){
                if(mediaPlayer != null){
                    if(mediaPlayer.isPlaying()){
                        ibtnPlaying.setImageResource(R.drawable.img_play);
                        mediaPlayer.pause();
                    }
                    else{
                        ibtnPlaying.setImageResource(R.drawable.img_pause);
                        mediaPlayer.start();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mediaPlayer != null){
            mediaPlayer.release();
        }
    }

    @Override
    public void onRefresh() {
        if(!swlayMusic.isRefreshing()){
            swlayMusic.setRefreshing(true);
        }

        currentPage = 1;
        arrMusicClass = null;

        TaskMusicList taskMusicList = new TaskMusicList();
        taskMusicList.execute("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class AdapterMusic extends RecyclerView.Adapter<AdapterMusic.ViewHolderMusic> {

        @Override
        public ViewHolderMusic onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_music, viewGroup, false);

            return new ViewHolderMusic(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolderMusic viewHolderMusic, int i) {
            final MusicClass musicClass = arrMusicClass.get(i);

            if(!musicClass.getThumbnail_url().equals("")){
                Picasso.with(getActivity()).load(musicClass.getThumbnail_url()).fit().into(viewHolderMusic.imgvThumbnail);
            }
            else{
                viewHolderMusic.imgvThumbnail.setImageResource(R.drawable.img_soundcloud);
                viewHolderMusic.imgvThumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

            viewHolderMusic.txtvSubject.setText(musicClass.getSubject());
            viewHolderMusic.txtvUrl.setText(musicClass.getLink_url());
            viewHolderMusic.txtvCreated.setText(musicClass.getCreated());

            if(i == ( arrMusicClass.size() - 1) ) {
                if(currentPage < totalPage){
                    currentPage++;

                    swlayMusic.setRefreshing(true);
                    TaskMusicList taskMusicList = new TaskMusicList();
                    taskMusicList.execute("");
                }
            }

            //클릭시 음악 재생
            viewHolderMusic.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //현재 누른 음악이 재생되고 있는 음악과 같지 않다면...
                    if(musicClass != musicClassPlaying){
                        musicClassPlaying = musicClass;

                        txtvPlayingSubject.setText(musicClassPlaying.getSubject());
                        if(!musicClassPlaying.getThumbnail_url().equals("")){
                            Picasso.with(getActivity()).load(musicClassPlaying.getThumbnail_url()).fit().into(imgvPlaying);
                        }
                        else{
                            imgvPlaying.setImageResource(R.drawable.img_soundcloud);
                            imgvPlaying.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        }

                        TaskPlayMusic taskPlayMusic = new TaskPlayMusic();
                        taskPlayMusic.execute("");
                    }
                }
            });

            //롱클릭시 음악 삭제
            viewHolderMusic.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("삭제하시겠습니까?")
                            .setCancelText("취소")
                            .setConfirmText("확인")
                            .showCancelButton(true)
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    TaskDeleteMusic taskDeleteMusic = new TaskDeleteMusic(musicClass.getId());
                                    taskDeleteMusic.execute("");
                                }
                            })
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            }).show();

                    //현재 삭제할 음악이 재생중인지 파악할 필요가 있음.
                    if(musicClass == musicClassPlaying){

                    }

                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrMusicClass.size();
        }

        public class ViewHolderMusic extends RecyclerView.ViewHolder {

            protected ImageView imgvThumbnail;
            protected TextView txtvSubject;
            protected TextView txtvUrl;
            protected TextView txtvCreated;

            public ViewHolderMusic(View itemView) {
                super(itemView);

                imgvThumbnail = (ImageView) itemView.findViewById(R.id.imgvMusicViewThumbnail);
                txtvSubject = (TextView) itemView.findViewById(R.id.txtvMusicViewSubject);
                txtvUrl = (TextView) itemView.findViewById(R.id.txtvMusicViewUrl);
                txtvCreated = (TextView) itemView.findViewById(R.id.txtvMusicViewCreated);
            }
        }
    }


    public class TaskMusicList extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/get_music_list.php";

        public TaskMusicList() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_id", userClass.getId())
                    .addFormDataPart("currentPage", currentPage + "")
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

            //Log.i("MusicList", s);

            if(swlayMusic.isRefreshing()){
                swlayMusic.setRefreshing(false);
            }

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){
                    totalPage = jsonObject.getInt("totalPage");
                    totalCnt = jsonObject.getInt("totalCnt");

                    if (arrMusicClass == null){
                        arrMusicClass = new ArrayList<MusicClass>();
                    }

                    if(totalPage != 0){
                        layNoResult.setVisibility(View.GONE);
                        JSONArray jsonArrayMusicList = jsonObject.getJSONArray("music_list");
                        for(int i=0;i<jsonArrayMusicList.length();i++){
                            JSONObject jsonObjectMusicList = jsonArrayMusicList.getJSONObject(i);
                            String id = jsonObjectMusicList.getString("id");
                            String subject = jsonObjectMusicList.getString("subject");
                            String link_url = jsonObjectMusicList.getString("link_url");
                            String stream_url = jsonObjectMusicList.getString("stream_url");
                            String thumbnail_url = jsonObjectMusicList.getString("thumbnail_url");

                            MusicClass musicClass = new MusicClass();
                            musicClass.setId(id);
                            musicClass.setSubject(subject);
                            musicClass.setLink_url(link_url);
                            musicClass.setStream_url(stream_url);
                            musicClass.setThumbnail_url(thumbnail_url);

                            arrMusicClass.add(musicClass);
                        }
                    }
                    else{
                        layNoResult.setVisibility(View.VISIBLE);
                    }

                    if(adapterMusic == null){
                        adapterMusic = new AdapterMusic();
                        recyclerView.setAdapter(adapterMusic);
                    }
                    adapterMusic.notifyDataSetChanged();
                }
                else{
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setTitleText("노래 리스트 오류!");
                    sweetAlertDialog.setContentText("새로고침을 시도해주세요");
                    sweetAlertDialog.setConfirmText("확인");
                    sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                    sweetAlertDialog.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText("노래 리스트 오류!");
                sweetAlertDialog.setContentText("새로고침을 시도해주세요");
                sweetAlertDialog.setConfirmText("확인");
                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
                sweetAlertDialog.show();
            }
        }
    }

    public class TaskDeleteMusic extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/delete_music.php";

        private LoadingDialog loadingDialog;
        private String music_id;

        public TaskDeleteMusic(String music_id) {
            this.music_id = music_id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(getActivity());
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("music_id", music_id)
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

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){
                    loadingDialog.setTitleText("삭제 완료");
                    loadingDialog.setContentText("삭제를 완료하였습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();

                            currentPage = 1;
                            arrMusicClass = null;
                            TaskMusicList taskMusicList = new TaskMusicList();
                            taskMusicList.execute("");
                        }
                    });
                }
                else{
                    loadingDialog.setTitleText("삭제 실패!!");
                    loadingDialog.setContentText("다시한번 시도해 주세요");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
                loadingDialog.dismissWithAnimation();
            }
        }
    }

    public class TaskPlayMusic extends AsyncTask<String, Integer, String> {

        public TaskPlayMusic() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ibtnPlaying.setOnClickListener(null);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }

            try {
                mediaPlayer.release();

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                mediaPlayer.setDataSource(musicClassPlaying.getStream_url() + "?client_id=532f9c69b70212f0c23fe71bc29b2605");

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        progressBar.setVisibility(View.GONE);
                        ibtnPlaying.setOnClickListener(MusicFragment.this);
                        ibtnPlaying.setImageResource(R.drawable.img_pause);
                        mp.start();
                    }
                });

                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return strResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }
}

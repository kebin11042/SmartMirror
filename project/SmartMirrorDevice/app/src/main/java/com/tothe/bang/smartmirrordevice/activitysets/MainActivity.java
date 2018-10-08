package com.tothe.bang.smartmirrordevice.activitysets;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.tothe.bang.smartmirrordevice.R;
import com.tothe.bang.smartmirrordevice.datasets.MemoClass;
import com.tothe.bang.smartmirrordevice.datasets.MusicClass;
import com.tothe.bang.smartmirrordevice.datasets.SmartDeviceClass;
import com.tothe.bang.smartmirrordevice.datasets.UserClass;
import com.tothe.bang.smartmirrordevice.datasets.VoiceClass;
import com.tothe.bang.smartmirrordevice.datasets.WeatherClass;
import com.tothe.bang.smartmirrordevice.gcmsets.QuickstartPreferences;
import com.tothe.bang.smartmirrordevice.gcmsets.RegistrationIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity
        implements
            CameraBridgeViewBase.CvCameraViewListener2,
            RecognitionListener,
            MediaPlayer.OnCompletionListener {

    private final String SPEECH_TAG = "SPEECH";

    private Typeface typefaceLane;
    private Typeface typefaceNanum;

    private static MainActivity mainActivity;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private UserClass userClass;    //연결된 유저 정보

    private ArrayList<MusicClass> arrMusic; //연결된 유저 음악 클래스

    private ArrayList<MemoClass> arrMemo;   //연결된

    private ArrayList<WeatherClass> arrWeatherClass;

    //////////////////////////////////////////GCM 변수////////////////////////////////////
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;   //GCM 푸시알림
    private static final String TAG = "MainActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    /////////////////////////////////////////////////////////////////////

    ////////////////////음악관련 변수/////////////////////////////////////////////////
    private int currPlayIndex;
    private MediaPlayer mediaPlayer;
    private MusicClass musicClassPlaying;   //현재 재생중인
    private boolean isPlaying;              //현재 음악이 재생 중인지
    private boolean justStop;               //음성 인식을 위한 잠시 멈춤

    /////////////////////////////////////////////////////////////////////////////////////////////////
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 198, 80, 98);
    private static final Scalar SMILE_RECT_COLOR     = new Scalar(80, 80, 255, 255);

    private CameraBridgeViewBase cameraBridgeViewBase;  //카메라 뷰

    private File mCascadeFileFace, mCascadeFileEye;
    private CascadeClassifier cascadeClassifierFace, cascadeClassifierEye;

    //이미지 파일
    private Mat mGray;
    private Mat mRgba;

    private int notDispCnt;     //디스플레이 여부 카운터
    private boolean currDisp;   //얼굴인식 여부

    private boolean isWink; //윙크 인식 모드 여부
    private int winkCnt;    //윙크 횟수 카운터
    private int recyWink;   //윙크 인식 모드 주기
    private final int T_RECY_WINK_CNT = 5; //윙크 인식 모드 주기 쓰레스 홀드
    private final int T_WINK_CNT = 5;  //윙크 횟수 카운터 쓰레스홀드

    private int gcCnt;  //gc 실행 여부 카운터

    //카메라뷰 콜백 매니져
    private final BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            //성공이라면
            if(status == LoaderCallbackInterface.SUCCESS){

                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);

                mCascadeFileFace = new File(cascadeDir, "lbpcascade_frontalface.xml");
                mCascadeFileEye = new File(cascadeDir, "haarcascade_eye.xml");

                try {
                    InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);

                    FileOutputStream os = new FileOutputStream(mCascadeFileFace);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }

                    is = getResources().openRawResource(R.raw.haarcascade_eye);
                    os = new FileOutputStream(mCascadeFileEye);
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }

                    is.close();
                    os.close();

                    cascadeClassifierFace = new CascadeClassifier(mCascadeFileFace.getAbsolutePath());
                    if (cascadeClassifierFace.empty()) {
                        cascadeClassifierFace = null;
                    }
                    cascadeClassifierEye = new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
                    if (cascadeClassifierEye.empty()) {
                       cascadeClassifierEye = null;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


                //카메라 뷰 가능하게
                cameraBridgeViewBase.enableView();
            }
            else{
                super.onManagerConnected(status);
            }
        }

    };

    ///////
    //음성인식
    private Intent intentVoice;                     //음성인식 인텐트
    private SpeechRecognizer speechRecognizer;      //
    private boolean isSpeech;                       //음성인식 중 여부
    private TaskVoice taskVoice;                    //음성인식 비동기 쓰레드
    //볼륨조절
    private AudioManager audioManager;
    private int nMaxVolum;


    ///////////////////////////////////뷰셋////////////////////////////////////
    //디스플레이 레이아웃
    private FrameLayout layDisplay;
    //날짜 정보 뷰
    private TextView txtvYear;
    private TextView txtvMonthDay;
    private TextView txtvDayofWeek;
    //날씨 정보 뷰
    private LinearLayout layWeather;
    private LinearLayout[] layWeathers;
    private TextView txtvAddr;
    private ImageView[] imgvWeathers;
    private TextView[] txtvWeathers;
    //안녕 메시지
    private TextView txtvHello;
    //음성인식
    private LinearLayout layVoice;
    private TextView txtvVoiceText;
    //노래
    private LinearLayout layMusic;
    private ImageView imgvMusicThumbnail;
    private TextView txtvMusicSubject;
    //메모
    private LinearLayout layMemo;
    private LinearLayout layMemoContents;
    //GCM
    private LinearLayout layKakao;
    private LinearLayout layMMS;
    private LinearLayout layFacebook;
    //음성인식 명령어
    private LinearLayout layManual;
    private TextView txtvManualContents;
    private final String strManualContents =
            "노래 - 노래를 시작합니다\n" +
            "다음 - 다음 음악을 재생합니다\n" +
            "이전 - 이전 음악을 재생합니다\n" +
            "그만 - 재생중인 노래를 정지합니다\n" +
            "재생 - 정지한 노래를 다시 재생합니다\n" +
            "소리 키워 - 소리를 키웁니다\n" +
            "소리 줄여 - 소리를 줄입니다\n" +
            "알림 지워 - 알림 메시지를 모두 지웁니다";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;
        //
        ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getLargeMemoryClass();

        notDispCnt = 0;
        currDisp = false;

        gcCnt = 0;

        isWink = false;
        isSpeech = false;

        //음악관련 변수
        isPlaying = false;

        //미디어 플레이어
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        arrMemo = null;

        //화면 키기 유지하기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //볼륨조절 초기화
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        nMaxVolum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        Log.i("audio", "max vol = " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        //음성인식 인텐트 초기화
        InitVoice();

        //GCM
        getInstanceIdToken();
        registBroadcastReceiver();

        //글씨체 적용
        typefaceLane = Typeface.createFromAsset(getAssets(), "lane_na_r.ttf.mp3");
        typefaceNanum = Typeface.createFromAsset(getAssets(), "nanum_ul.ttf.mp3");

        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        setGlobalFont(root, typefaceLane);

        Init();

    }

    public void setGlobalFont(ViewGroup root, Typeface typeface) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof TextView)
                ((TextView)child).setTypeface(typeface);
            else if (child instanceof ViewGroup)
                setGlobalFont((ViewGroup)child, typeface);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_READY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_GENERATING));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, baseLoaderCallback);
        }
        else{
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }

        if(speechRecognizer != null){
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }

        if(mediaPlayer != null){
            mediaPlayer.release();
        }
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    //뷰초기화
    public void Init(){
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.jcvMain);
        cameraBridgeViewBase.setVisibility(View.VISIBLE);
        //cameraBridgeViewBase.setCameraIndex(1);             //셀카 모드
        cameraBridgeViewBase.setCvCameraViewListener(this); //리스너 등록

        layDisplay = (FrameLayout) findViewById(R.id.layMainDisplay);

        txtvYear = (TextView) findViewById(R.id.txtvMainYear);
        txtvMonthDay = (TextView) findViewById(R.id.txtvMainMonthDay);
        txtvDayofWeek = (TextView) findViewById(R.id.txtvMainDayofWeek);

        layWeather = (LinearLayout) findViewById(R.id.layMainWeather);
        txtvAddr = (TextView) findViewById(R.id.txtvAddr);
        layWeathers = new LinearLayout[5];
        layWeathers[0] = (LinearLayout) findViewById(R.id.layWeatherMorning);
        layWeathers[1] = (LinearLayout) findViewById(R.id.layWeatherNoon);
        layWeathers[2] = (LinearLayout) findViewById(R.id.layWeatherEve);
        layWeathers[3] = (LinearLayout) findViewById(R.id.layWeatherNight);
        layWeathers[4] = (LinearLayout) findViewById(R.id.layWeatherNext);
        imgvWeathers = new ImageView[5];
        imgvWeathers[0] = (ImageView) findViewById(R.id.imgvMorning);
        imgvWeathers[1] = (ImageView) findViewById(R.id.imgvNoon);
        imgvWeathers[2] = (ImageView) findViewById(R.id.imgvEve);
        imgvWeathers[3] = (ImageView) findViewById(R.id.imgvNight);
        imgvWeathers[4] = (ImageView) findViewById(R.id.imgvNext);
        txtvWeathers = new TextView[5];
        txtvWeathers[0] = (TextView) findViewById(R.id.txtvMorningTemp);
        txtvWeathers[1] = (TextView) findViewById(R.id.txtvNoonTemp);
        txtvWeathers[2] = (TextView) findViewById(R.id.txtvEveTemp);
        txtvWeathers[3] = (TextView) findViewById(R.id.txtvNightTemp);
        txtvWeathers[4] = (TextView) findViewById(R.id.txtvNextTemp);

        txtvHello = (TextView) findViewById(R.id.txtvMainUserHello);

        layVoice = (LinearLayout) findViewById(R.id.layMainVoice);
        txtvVoiceText = (TextView) findViewById(R.id.txtvVoiceText);

        layMusic = (LinearLayout) findViewById(R.id.layMainMusic);
        imgvMusicThumbnail = (ImageView) findViewById(R.id.imgvMusicThumbnail);
        txtvMusicSubject = (TextView) findViewById(R.id.txtvMusicSubject);

        layMemo = (LinearLayout) findViewById(R.id.layMainMemo);
        layMemoContents = (LinearLayout) findViewById(R.id.layMainMemoContents);

        layKakao = (LinearLayout) findViewById(R.id.layKakao);
        layMMS = (LinearLayout) findViewById(R.id.layMMS);
        layFacebook = (LinearLayout) findViewById(R.id.layFacebook);

        layManual = (LinearLayout) findViewById(R.id.layMainMenual);
        txtvManualContents = (TextView) findViewById(R.id.txtvManualContents);
        txtvManualContents.setText(strManualContents);

        InitNanumType();
    }

    public void InitNanumType() {
        txtvHello.setTypeface(typefaceNanum);

        txtvDayofWeek.setTypeface(typefaceNanum);

        txtvVoiceText.setTypeface(typefaceNanum);

        setGlobalFont(layVoice, typefaceNanum);
        setGlobalFont(layMemo, typefaceNanum);
        setGlobalFont(layKakao, typefaceNanum);
        setGlobalFont(layManual, typefaceNanum);
    }

    //음성인식 초기화
    public void InitVoice() {
        intentVoice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentVoice.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intentVoice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
    }

    public void setManualView() {
        isSpeech = true;
        layManual.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeInUp)
                .duration(350)
                .withListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                        YoYo.with(Techniques.FadeOutDown)
                                .delay(7500)
                                .duration(350)
                                .withListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                                        isSpeech = false;
                                        layDisplay.setVisibility(View.GONE);
                                        animation.removeAllListeners();
                                    }

                                    @Override
                                    public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {
                                    }
                                })
                                .playOn(layManual);
                    }

                    @Override
                    public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {
                    }
                })
                .playOn(layManual);
    }

    //안녕하세요 텍스트뷰 애니메이션으로 뿌려주는 메소드
    public void setUserMsgView(String strMsg, int color){
        txtvHello.setText(strMsg);
        txtvHello.setVisibility(View.VISIBLE);
        txtvHello.setTextColor(color);
        txtvHello.setAlpha(0f);
        txtvHello.animate().setDuration(2000).alpha(1f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                //켜지는 애니메이션이 끝나면 꺼지는 애니메이션 실행
                animation.removeAllListeners();
                txtvHello.animate().setStartDelay(3000).setDuration(1500).alpha(0f).start();
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        }).start();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
//        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {

//        mRgba.release();
        if(mGray != null){
            mGray.release();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

//        mRgba = inputFrame.rgba();
//        Mat m = new Mat();
//        Core.normalize(inputFrame.gray(), m);
//        Imgproc.equalizeHist(inputFrame.gray(), m);

        //음성 인식일 때 할 필요가 없기 때문에
        if(!isSpeech){
            mGray = inputFrame.gray();
            Imgproc.equalizeHist(mGray, mGray);
            //Imgproc.blur(mGray, mGray, new Size(3,3));

//            Core.transpose(mGray, mGray);
//            Core.flip(mGray, mGray, -1);

            MatOfRect faces = new MatOfRect();
            MatOfRect eyes = new MatOfRect();


            if(cascadeClassifierFace != null){
                cascadeClassifierFace.detectMultiScale(mGray, faces, 1.1, 5, 0, new Size(150, 150), new Size());
            }
            Rect[] facesArray = faces.toArray();

            //인식된 얼굴이 있을 때
            if(facesArray.length > 0){
                currDisp = true;
                notDispCnt = 0;

                for(int i=0;i<facesArray.length;i++){

                    //눈의 특성 상 얼굴의 60% 위에 위치하고 있음
                    int nEyeHeight = (int) Math.round(facesArray[i].height * 0.6);
                    Mat mEye = new Mat(mGray.submat(facesArray[i]), new Rect(0, 0, facesArray[i].width, nEyeHeight));

                    if(cascadeClassifierEye != null){
                        cascadeClassifierEye.detectMultiScale(mEye, eyes, 1.3, 5, 0, new Size(65, 65), new Size());
                    }

                    Rect[] eyesArray = eyes.toArray();

                    //양쪽 눈 다 떳을 때
                    if(eyesArray.length == 2){
                        if(isWink){
                            winkCnt--;
                            recyWink++;
                        }
                    }
                    //윙크임
                    else if(eyesArray.length == 1){
                        if(isWink){
                            winkCnt++;
                            recyWink++;
                        }
                        //윙크 발동
                        else{
                            isWink = true;
                            winkCnt = 0;
                            recyWink = 0;
                        }
                    }
                    //그 외의 ex) 3개 인지, 0개 인지
                    else{
                        if(isWink){
                            winkCnt--;
                            recyWink++;
                        }
                    }

//                    for(Rect eye : eyes.toArray()){
//                        Point tl = new Point(eye.tl().x + facesArray[i].tl().x, eye.tl().y + facesArray[i].tl().y);
//                        Point br = new Point(tl.x + eye.width, tl.y + eye.height);
//                        Imgproc.rectangle(mRgba, tl, br, SMILE_RECT_COLOR, 3);
//                    }
//
//                    Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
                }
            }
            //인식된 얼굴이 없을 때
            else{
                currDisp = false;

                if(layDisplay.getVisibility() == View.VISIBLE){
                    notDispCnt++;
                }

                //윙크 발동 중에 얼굴이 없을 경우 윙크 카운트 낮추기
                if(isWink){
                    winkCnt--;
                    recyWink++;
                }
            }

            mGray.release();
            //Log.i("Camera", "faceCnt = " + facesArray.length);

            TaskDisplay taskDisplay = new TaskDisplay();
            taskDisplay.execute("");

            gcCnt++;
            if(gcCnt > 500){
                System.gc();
                System.runFinalization();
                gcCnt = 0;
            }

            if(recyWink >= T_RECY_WINK_CNT && isWink){
                isWink = false;
                if(winkCnt >= T_WINK_CNT && userClass != null){
                    //Log.i("WINK", "윙크 발동!!!!!");
                    isSpeech = true;

                    taskVoice = new TaskVoice();
                    taskVoice.execute();
                }
            }
        }


        return null;
    }

    //////////////////////////////////////GCM///////////////////////////////////////////////////////////////////
    /**
     * Google Play Service를 사용할 수 있는 환경이지를 체크한다.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(MainActivity.this, "해당 기기는 푸시알림 기능을 지원하지 않습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     * LocalBroadcast 리시버를 정의한다. 토큰을 획득하기 위한 READY, GENERATING, COMPLETE 액션에 따라 UI에 변화를 준다.
     */
    public void registBroadcastReceiver(){
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();


                if(action.equals(QuickstartPreferences.REGISTRATION_READY)){

                } else if(action.equals(QuickstartPreferences.REGISTRATION_GENERATING)){

                } else if(action.equals(QuickstartPreferences.REGISTRATION_COMPLETE)){
                    String token = intent.getStringExtra("token");
                    TaskUpdateToken taskUpdateToken = new TaskUpdateToken(token);
                    taskUpdateToken.execute("");
                }

            }
        };
    }

    public void setVisibleKakaoView() {
        layKakao.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.BounceInUp)
                .duration(550)
                .playOn(layKakao);
    }

    public void setGoneKakaoView() {
        YoYo.with(Techniques.FadeOutUp)
                .duration(350)
                .playOn(layKakao);
    }

    public void setVisibleMMSView() {
        layMMS.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.BounceInUp)
                .duration(550)
                .playOn(layMMS);
    }

    public void setGoneMMSView() {
        YoYo.with(Techniques.FadeOutUp)
                .duration(350)
                .playOn(layMMS);
    }

    public void setVisibleFacebookView() {
        layFacebook.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.BounceInUp)
                .duration(550)
                .playOn(layFacebook);
    }

    public void setGoneFacebookView() {
        YoYo.with(Techniques.FadeOutUp)
                .duration(350)
                .playOn(layFacebook);
    }

    //////////////////////////////////////음성인식////////////////////////////////////////////////////////////////

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(SPEECH_TAG, "onReadyForSpeech()");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(SPEECH_TAG, "onBeginningOfSpeech()");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(SPEECH_TAG, "onRmsChanged()");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(SPEECH_TAG, "onBufferReceived()");
    }

    @Override
    public void onEndOfSpeech() {

        Log.i(SPEECH_TAG, "onEndOfSpeech");

        if(justStop) {
            mediaPlayer.start();
            isPlaying = true;
            justStop = false;
        }

        layVoice.setVisibility(View.GONE);
        isSpeech = false;
    }

    @Override
    public void onError(int error) {
        Log.i(SPEECH_TAG, "onError() => errorNo : " + error);

        if(error != 7) {
            if(justStop) {
                mediaPlayer.start();
                isPlaying = true;
                justStop = false;
            }

            layVoice.setVisibility(View.GONE);
            isSpeech = false;

            speechRecognizer.cancel();
        }
    }

    @Override
    public void onResults(Bundle results) {

        String key = SpeechRecognizer.RESULTS_RECOGNITION;
        ArrayList<String> arrResults = results.getStringArrayList(key);
        if(arrResults != null){
            String[] strResults = new String[arrResults.size()];
            arrResults.toArray(strResults);
            //Log.i("onResult()", strResults[0]);

            layVoice.setVisibility(View.GONE);
            //Toast.makeText(MainActivity.this, strResults[0], Toast.LENGTH_SHORT).show();

            Log.i(SPEECH_TAG, "onResults() => result : " + strResults);

            executeVoiceResult(strResults[0]);
        }
        else{
            speechRecognizer.cancel();
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {}

    @Override
    public void onEvent(int eventType, Bundle params) {}

    //////음성인식 성공 후 판단 메소드
    public void executeVoiceResult(String strVoiceResult){

        //볼륨 업
        if(VoiceClass.isVolumUp(strVoiceResult)) {
            int currVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            currVol += 3;
            if(currVol >= nMaxVolum) {
                currVol = nMaxVolum;
                setUserMsgView("최대 볼륨입니다.", Color.parseColor("#FF98AD"));
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currVol, 0);

        }
        //볼륨 다운
        else if(VoiceClass.isVolumDown(strVoiceResult)) {
            int currVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            currVol -= 3;
            if(currVol <= 0) {
                currVol = 0;
                setUserMsgView("최저 볼륨입니다.", Color.parseColor("#FF98AD"));
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currVol, 0);

        }
        else if(VoiceClass.isRemoveMsg(strVoiceResult)) {
            setGoneKakaoView();
            setGoneFacebookView();
            setGoneMMSView();
        }
        //음성인식 명령어
        else if(VoiceClass.isManual(strVoiceResult)) {
            setManualView();
        }
        //노래 시작인가?
        else if(VoiceClass.isMusicStart(strVoiceResult)){
            if(arrMusic != null){
                if(arrMusic.size() != 0) {
                    if(layMusic.getVisibility() == View.GONE){
                        layMusic.setAlpha(0);
                        layMusic.setVisibility(View.VISIBLE);
                        layMusic.animate().alpha(1).setDuration(1500).start();
                    }

                    currPlayIndex = 0;
                    musicClassPlaying = arrMusic.get(currPlayIndex);
                    setMusicViews(musicClassPlaying);

                    TaskPlayMusic taskPlayMusic = new TaskPlayMusic();
                    taskPlayMusic.execute();
                }
                else {
                    setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
                }
            }
            else{
                setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
            }
        }
        //그만
        else if(VoiceClass.isMusicStop(strVoiceResult)) {

            if(arrMusic != null){
                if(arrMusic.size() != 0) {
                    if(mediaPlayer != null) {
                        if(mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            isPlaying = false;

                            layMusic.setVisibility(View.GONE);
                        }
                    }
                }
                else {
                    setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
                }
            }
            else{
                setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
            }
        }
        //다시 재생
        else if(VoiceClass.isMusicPlay(strVoiceResult)) {

            if(arrMusic != null){
                if(arrMusic.size() != 0) {
                    if(mediaPlayer != null) {
                        if(!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                            isPlaying = true;

                            if(layMusic.getVisibility() == View.GONE){
                                layMusic.setAlpha(0);
                                layMusic.setVisibility(View.VISIBLE);
                                layMusic.animate().alpha(1).setDuration(1500).start();
                            }
                        }
                    }
                }
                else {
                    setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
                }
            }
            else{
                setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
            }
        }
        //다음 음악
        else if(VoiceClass.isMusicNext(strVoiceResult)) {

            if(arrMusic != null){
                if(arrMusic.size() != 0) {
                    if(mediaPlayer != null) {
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.stop();
                        }

                        currPlayIndex++;
                        if(currPlayIndex >= arrMusic.size()) {
                            currPlayIndex = 0;
                        }
                        musicClassPlaying = arrMusic.get(currPlayIndex);

                        setMusicViews(musicClassPlaying);

                        TaskPlayMusic taskPlayMusic = new TaskPlayMusic();
                        taskPlayMusic.execute();
                    }
                }
                else {
                    setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
                }
            }
            else{
                setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
            }
        }
        //이전 음악
        else if(VoiceClass.isMusicPrev(strVoiceResult)) {

            if(arrMusic != null){
                if(arrMusic.size() != 0) {
                    if(mediaPlayer != null) {
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.stop();
                        }

                        currPlayIndex--;
                        if(currPlayIndex < 0) {
                            currPlayIndex = arrMusic.size() - 1;
                        }
                        musicClassPlaying = arrMusic.get(currPlayIndex);

                        setMusicViews(musicClassPlaying);

                        TaskPlayMusic taskPlayMusic = new TaskPlayMusic();
                        taskPlayMusic.execute();
                    }
                }
                else {
                    setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
                }
            }
            else{
                setUserMsgView("노래를 먼저 등록해 주세요", Color.parseColor("#AC5580"));
            }
        }
    }

    public void setMusicViews(MusicClass musicClass){
        if(!musicClass.getThumbnail_url().equals("")){
            Picasso.with(MainActivity.this).load(musicClass.getThumbnail_url()).fit().into(imgvMusicThumbnail);
        }
        else{
            imgvMusicThumbnail.setImageResource(R.drawable.img_soundcloud);
            imgvMusicThumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        txtvMusicSubject.setText(musicClass.getSubject());
    }



    /////////////////////////////////////////////////////////미디어 플레이어

    //자동 다음곡 재생을 위한 알고리즘
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(arrMusic != null) {
            if(arrMusic.size() != 0){
                //마지막 곡이라면
                if(currPlayIndex == ( arrMusic.size() - 1 ) ){
                    currPlayIndex = 0;
                }
                else {
                    currPlayIndex++;
                }

                musicClassPlaying = arrMusic.get(currPlayIndex);

                setMusicViews(musicClassPlaying);

                TaskPlayMusic taskPlayMusic = new TaskPlayMusic();
                taskPlayMusic.execute();
            }
        }
    }




    ///////////////////////////////////////////////////////비동기 쓰레드///////////////////

    //디스플레이 비동기 쓰레드
    public class TaskDisplay extends AsyncTask<String, Integer, String> {

        public TaskDisplay() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            setViewDate();

            if(layDisplay.getVisibility() == View.GONE){
                if(currDisp){
                    layDisplay.setAlpha(0f);
                    layDisplay.setVisibility(View.VISIBLE);
                    layDisplay.animate().setDuration(1500).alpha(1f).start();

                    TaskLoadInfo taskLoadInfo = new TaskLoadInfo();
                    taskLoadInfo.execute("");
                }
            }
            else if (layDisplay.getVisibility() == View.VISIBLE) {
                if(notDispCnt > 300){
                    layWeather.setVisibility(View.GONE);
                    layDisplay.setVisibility(View.GONE);
                }
            }
        }

        //날짜 뷰 세팅 메소드
        public void setViewDate(){
            Calendar calendar = Calendar.getInstance();
            String strYear = calendar.get(Calendar.YEAR) + "";
            String strMonth = String.format("%02d", (calendar.get(Calendar.MONTH) + 1));
            String strDay = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));

            int nDayofWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String strDayofWeek = "";
            if(nDayofWeek == 1){
                strDayofWeek = "일요일";
            }
            else if(nDayofWeek == 2){
                strDayofWeek = "월요일";
            }
            else if(nDayofWeek == 3){
                strDayofWeek = "화요일";
            }
            else if(nDayofWeek == 4){
                strDayofWeek = "수요일";
            }
            else if(nDayofWeek == 5){
                strDayofWeek = "목요일";
            }
            else if(nDayofWeek == 6){
                strDayofWeek = "금요일";
            }
            else if(nDayofWeek == 7){
                strDayofWeek = "토요일";
            }

            txtvYear.setText(strYear);
            txtvMonthDay.setText(strMonth + "/" + strDay);
            txtvDayofWeek.setText(strDayofWeek);
        }
    }

    //음성인식 뷰
    public class TaskVoice extends AsyncTask<String, Integer, String> {

        public TaskVoice() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(isPlaying && mediaPlayer != null) {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    justStop = true;
                }
            }

            layVoice.setAlpha(0);
            layVoice.setVisibility(View.VISIBLE);
            layVoice.animate().setDuration(1300).alpha(1f).start();

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
            speechRecognizer.setRecognitionListener(MainActivity.this);
            speechRecognizer.startListening(intentVoice);
        }
    }

    //서버와 연결 정보를 얻는다
    public class TaskLoadInfo extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/get_info_by_device.php";

        public TaskLoadInfo() {

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
                    .addFormDataPart("device_serial_number", SmartDeviceClass.getSerialNumber())
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
            //Log.i("LoadInfo", s);

            userClass = null;
            arrMusic = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){
                    //사용자 정보 세팅
                    JSONObject jsonObjectUser = jsonObject.getJSONObject("user_info");
                    String user_id = jsonObjectUser.getString("id");
                    String user_name = jsonObjectUser.getString("name");
                    String user_addr = jsonObjectUser.getString("addr");

                    userClass = new UserClass();
                    userClass.setId(user_id);
                    userClass.setName(user_name);
                    userClass.setAddr(user_addr);

                    arrWeatherClass = new ArrayList<WeatherClass>();
                    for(int i=0;i<5;i++) {
                        WeatherClass weatherClass = new WeatherClass();
                        if(i == 0) {
                            try {
                                //날씨 정보 세팅
                                JSONObject jsonObjectWeatherMorning = jsonObject.getJSONObject("weather_morning");
                                String wm_temp = jsonObjectWeatherMorning.getDouble("temp") + "";
                                String wm_icon = jsonObjectWeatherMorning.getString("icon");

                                weatherClass = new WeatherClass();
                                weatherClass.setTemp(wm_temp + "'C");
                                weatherClass.setIcon(wm_icon);
                            }
                            catch (JSONException e){

                                weatherClass = new WeatherClass();
                                weatherClass.setTemp("정보 없음");
                                weatherClass.setIcon("0");
                            }
                        }
                        else if(i == 1) {
                            try {
                                JSONObject jsonObjectWeatherNoon = jsonObject.getJSONObject("weather_noon");
                                String wn_temp = jsonObjectWeatherNoon.getDouble("temp") + "";
                                String wn_icon = jsonObjectWeatherNoon.getString("icon");

                                weatherClass = new WeatherClass();
                                weatherClass.setTemp(wn_temp + "'C");
                                weatherClass.setIcon(wn_icon);
                            }
                            catch (JSONException e) {
                                weatherClass = new WeatherClass();
                                weatherClass.setTemp("정보 없음");
                                weatherClass.setIcon("0");
                            }
                        }
                        else if(i == 2) {
                            try {
                                JSONObject jsonObjectWeatherEve = jsonObject.getJSONObject("weather_eve");
                                String we_temp = jsonObjectWeatherEve.getDouble("temp") + "";
                                String we_icon = jsonObjectWeatherEve.getString("icon");

                                weatherClass = new WeatherClass();
                                weatherClass.setTemp(we_temp + "'C");
                                weatherClass.setIcon(we_icon);
                            }
                            catch (JSONException e) {
                                weatherClass = new WeatherClass();
                                weatherClass.setTemp("정보 없음");
                                weatherClass.setIcon("0");
                            }
                        }
                        else if(i == 3) {
                            try {
                                JSONObject jsonObjectWeatherNight = jsonObject.getJSONObject("weather_night");
                                String wn_temp = jsonObjectWeatherNight.getDouble("temp") + "";
                                String wn_icon = jsonObjectWeatherNight.getString("icon");

                                weatherClass = new WeatherClass();
                                weatherClass.setTemp(wn_temp + "'C");
                                weatherClass.setIcon(wn_icon);
                            }
                            catch (JSONException e) {
                                weatherClass = new WeatherClass();
                                weatherClass.setTemp("정보 없음");
                                weatherClass.setIcon("0");
                            }
                        }
                        else if(i == 4) {
                            try {
                                JSONObject jsonObjectWeatherNext = jsonObject.getJSONObject("weather_next");
                                String wn_temp = jsonObjectWeatherNext.getDouble("temp") + "";
                                String wn_icon = jsonObjectWeatherNext.getString("icon");

                                weatherClass = new WeatherClass();
                                weatherClass.setTemp(wn_temp + "'C");
                                weatherClass.setIcon(wn_icon);
                            }
                            catch (JSONException e) {
                                weatherClass = new WeatherClass();
                                weatherClass.setTemp("정보 없음");
                                weatherClass.setIcon("0");
                            }
                        }

                        arrWeatherClass.add(weatherClass);
                    }

                    try {
                        arrMusic = new ArrayList<MusicClass>();
                        JSONArray jsonArraMusic = jsonObject.getJSONArray("music_list");
                        for(int i=0;i<jsonArraMusic.length();i++){
                            JSONObject jsonObjectMusic = jsonArraMusic.getJSONObject(i);
                            String music_id = jsonObjectMusic.getString("id");
                            String subject = jsonObjectMusic.getString("subject");
                            String link_url = jsonObjectMusic.getString("link_url");
                            String thumbnail_url = jsonObjectMusic.getString("thumbnail_url");
                            String stream_url = jsonObjectMusic.getString("stream_url");
                            String created = jsonObjectMusic.getString("created");

                            MusicClass musicClass = new MusicClass();
                            musicClass.setId(music_id);
                            musicClass.setSubject(subject);
                            musicClass.setLink_url(link_url);
                            musicClass.setThumbnail_url(thumbnail_url);
                            musicClass.setStream_url(stream_url);
                            musicClass.setCreated(created);

                            arrMusic.add(musicClass);
                        }
                    }
                    catch (JSONException e) {

                    }


                    try {
                        arrMemo = new ArrayList<MemoClass>();
                        JSONArray jsonArrayMemo = jsonObject.getJSONArray("memo_list");
                        for(int i=0;i<jsonArrayMemo.length();i++){
                            JSONObject jsonObjectMemo = jsonArrayMemo.getJSONObject(i);
                            String memo_id = jsonObjectMemo.getString("id");
                            String subject = jsonObjectMemo.getString("subject");
                            String date = jsonObjectMemo.getString("date");
                            String created = jsonObjectMemo.getString("created");

                            MemoClass memoClass = new MemoClass();
                            memoClass.setId(memo_id);
                            memoClass.setSubject(subject);
                            memoClass.setDate(date);
                            memoClass.setCreated(created);

                            arrMemo.add(memoClass);

                            setMemoView();
                        }
                    }
                    catch (JSONException e) {

                    }

                    //뷰 세팅
                    setUserHelloView("안녕하세요 " + user_name + "님!!!");
                    setWeatherView();

                }
                else if(result.equals("not_connected")){

                    setUserHelloView("안녕하세요!!!");
                }
                else if(result.equals("fail")){

                    setUserHelloView("연결상태가 좋지 않습니다...");
                }
                else{
                }

            } catch (JSONException e) {
                e.printStackTrace();

                setUserMsgView("서버 오류 입니다!!", Color.parseColor("#AC5580"));
            }
        }

        //안녕하세요 텍스트뷰 애니메이션으로 뿌려주는 메소드
        public void setUserHelloView(String strHello){
            txtvHello.setText(strHello);
            txtvHello.setVisibility(View.VISIBLE);
            txtvHello.setTextColor(Color.WHITE);
            txtvHello.setAlpha(0f);
            txtvHello.animate().setDuration(2000).alpha(1f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    //켜지는 애니메이션이 끝나면 꺼지는 애니메이션 실행
                    animation.removeAllListeners();
                    txtvHello.animate().setStartDelay(3000).setDuration(1500).alpha(0f).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }

        //날씨 뷰 세팅 메소드
        public void setWeatherView(){
            layWeather.setVisibility(View.VISIBLE);

            txtvAddr.setText(userClass.getAddr());

            int index = -1;
            for(int i=0;i<arrWeatherClass.size();i++) {
                WeatherClass weatherClass = arrWeatherClass.get(i);

                layWeathers[i].setVisibility(View.GONE);
                txtvWeathers[i].setText(weatherClass.getTemp());
                imgvWeathers[i].setImageResource(weatherClass.getIconDrawableId());
                if(!weatherClass.getIcon().equals("0")) {
                    if(index == -1){
                        index = i;
                    }
                }
            }

            if(index != -1){
                layWeathers[index].setVisibility(View.VISIBLE);
                layWeathers[index + 1].setVisibility(View.VISIBLE);
            }
        }

        //
        public void setMemoView(){
            if(arrMemo != null ) {
                if(arrMemo.size() == 0) {
                    layMemo.setVisibility(View.GONE);
                }
                else{
                    layMemo.setVisibility(View.VISIBLE);

                    layMemoContents.removeAllViews();
                    TextView[] txtvMemo = new TextView[arrMemo.size()];
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    for(int i=0;i<arrMemo.size();i++) {
                        txtvMemo[i] = new TextView(MainActivity.this);
                        txtvMemo[i].setText("- " + arrMemo.get(i).getSubject());
                        txtvMemo[i].setLayoutParams(layoutParams);
                        txtvMemo[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        txtvMemo[i].setTextColor(Color.WHITE);
                        txtvMemo[i].setTypeface(typefaceNanum);

                        layMemoContents.addView(txtvMemo[i]);
                    }
                }
            }
            else {
                layMemo.setVisibility(View.GONE);
            }
        }
    }

    //스마트 거울 기기 GCM 토큰 업데이트
    //서버와 연결
    public class TaskUpdateToken extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/update_device_token.php";

        private String strToken;

        public TaskUpdateToken(String strToken) {
            this.strToken = strToken;
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
                    .addFormDataPart("device_gcm_token", strToken)
                    .addFormDataPart("device_serial_number", SmartDeviceClass.getSerialNumber())
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
            //Log.i("UPDATE_GCM_RES", s);

            try {
                JSONObject jsonObject = new JSONObject(s);

                String result = jsonObject.getString("result");
                if(result.equals("ok")) {

                }
                else{

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //음악 준비 비동기로
    public class TaskPlayMusic extends AsyncTask<String, Integer, String> {

        public TaskPlayMusic() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

                        mp.start();
                        isPlaying = true;
                    }
                });

                mediaPlayer.setOnCompletionListener(MainActivity.this);

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
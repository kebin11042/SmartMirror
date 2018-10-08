package com.tothe.bang.smartmirrorclient.activitysets;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tothe.bang.smartmirrorclient.R;
import com.tothe.bang.smartmirrorclient.datasets.UserClass;
import com.tothe.bang.smartmirrorclient.viewsets.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends Activity implements View.OnClickListener{

    // SHA1 = 5E:7F:BF:4A:1C:7A:CD:C0:D9:A7:46:5B:DF:CD:7C:91:C2:FD:04:ED
    private static MainActivity mainActivity;

    private String strSoundcloudShareURL;

    private Button btnLogin;
    private Button btnJoin;

    //자동 로그인에 사용되는
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        try {
            strSoundcloudShareURL = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
            //Log.i("MaindAc", "soundcloudURL = " + strSoundcloudShareURL);
        }
        catch (NullPointerException e){
            strSoundcloudShareURL = null;
        }


        preferences = getSharedPreferences("user", MODE_PRIVATE);
        editor = preferences.edit();
        String mode = preferences.getString("mode", "null");

        //일반 회원
        if(mode.equals("normal")){
            String email = preferences.getString("email", "null");
            String password = preferences.getString("password", "null");

            TaskLogin taskLogin = new TaskLogin(email, password);
            taskLogin.execute("");
        }
        else if(mode.equals("facebook")){
            String facebook_id = preferences.getString("facebook_id", "null");

            TaskLoginFacebook taskLoginFacebook = new TaskLoginFacebook(facebook_id);
            taskLoginFacebook.execute("");
        }

        Init();
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public void Init(){
        btnLogin = (Button) findViewById(R.id.btnMainLogin);
        btnJoin = (Button) findViewById(R.id.btnMainJoin);

        btnLogin.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btnLogin.getId()){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == btnJoin.getId()){
            Intent intent = new Intent(MainActivity.this, JoinActivity.class);
            startActivity(intent);
        }
    }


    public class TaskLogin extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/login_normal.php";

        private String user_email;
        private String user_password;

        private LoadingDialog loadingDialog;

        public TaskLogin(String user_email, String user_password) {
            this.user_email = user_email;
            this.user_password = user_password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(MainActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();


            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_email", user_email)
                    .addFormDataPart("user_password", user_password)
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

//            Log.i("login", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){

                    JSONObject jsonObjectUserInfo = jsonObject.getJSONObject("user_info");

                    String user_id = jsonObjectUserInfo.getString("id");
                    String device_id = jsonObjectUserInfo.getString("device_id");
                    String name = jsonObjectUserInfo.getString("name");
                    String lat = jsonObjectUserInfo.getString("lat");
                    String lng = jsonObjectUserInfo.getString("lng");
                    String addr = jsonObjectUserInfo.getString("addr");

                    final UserClass userClass = new UserClass();
                    userClass.setEmail(user_email);
                    userClass.setId(user_id);
                    userClass.setName(name);
                    userClass.setLat(lat);
                    userClass.setLng(lng);
                    userClass.setAddr(addr);

                    //Log.i("user_info", "lat = " + lat + ", addr = " + addr + ", devie_id =" + device_id);

                    userClass.getDeviceClass().setId(device_id);

                    try {
                        JSONObject jsonObjectDeviceInfo = jsonObject.getJSONObject("device_info");
                        String device_serial_number = jsonObjectDeviceInfo.getString("serial_number");
                        userClass.getDeviceClass().setSerial_Number(device_serial_number);
                    }
                    catch (JSONException e) {
                        //Log.i("LoginActivity", "연결된 기기 정보 없음");
                    }

                    if(lat.equals("") || lng.equals("") || addr.equals("")){
                        loadingDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE);
                        loadingDialog.setTitleText("위치정보 설정");
                        loadingDialog.setContentText("서비스를 사용하기 전에 위치정보를 설정해 주세요");
                        loadingDialog.setConfirmText("확인");
                        loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                loadingDialog.dismissWithAnimation();
                                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                                intent.putExtra("userClass", userClass);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else{
                        loadingDialog.dismissWithAnimation();

                        editor.clear();
                        editor.apply();
                        editor.putString("mode", "normal");
                        editor.putString("email", user_email);
                        editor.putString("password", user_password);
                        editor.apply();


                        Intent intent = new Intent(MainActivity.this, LoginedActivity.class);
                        intent.putExtra("userClass", userClass);

                        if(strSoundcloudShareURL != null){
                            intent.putExtra("soundcloudURL", strSoundcloudShareURL);
                        }

                        startActivity(intent);
                        finish();
                    }
                }
                else{
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("로그인 실패");
                    loadingDialog.setContentText("정보를 다시 확인해 주세요");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            loadingDialog.dismissWithAnimation();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
                loadingDialog.dismiss();
            }
        }
    }

    public class TaskLoginFacebook extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/login_facebook.php";

        private String user_facebook_id;

        private LoadingDialog loadingDialog;

        public TaskLoginFacebook(String user_facebook_id) {
            this.user_facebook_id = user_facebook_id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(MainActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();


            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_facebook_id", user_facebook_id)
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

            //Log.i("login", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){

                    JSONObject jsonObjectUserInfo = jsonObject.getJSONObject("user_info");

                    String user_id = jsonObjectUserInfo.getString("id");
                    String user_email = jsonObjectUserInfo.getString("email");
                    String device_id = jsonObjectUserInfo.getString("device_id");
                    String name = jsonObjectUserInfo.getString("name");
                    String lat = jsonObjectUserInfo.getString("lat");
                    String lng = jsonObjectUserInfo.getString("lng");
                    String addr = jsonObjectUserInfo.getString("addr");

                    final UserClass userClass = new UserClass();
                    userClass.setEmail(user_email);
                    userClass.setId(user_id);
                    userClass.setName(name);
                    userClass.setLat(lat);
                    userClass.setLng(lng);
                    userClass.setAddr(addr);
                    userClass.setFacebook_id(user_facebook_id);

                    //Log.i("user_info", "lat = " + lat + ", addr = " + addr + ", devie_id =" + device_id);

                    userClass.getDeviceClass().setId(device_id);

                    try {
                        JSONObject jsonObjectDeviceInfo = jsonObject.getJSONObject("device_info");
                        String device_serial_number = jsonObjectDeviceInfo.getString("serial_number");
                        userClass.getDeviceClass().setSerial_Number(device_serial_number);
                    }
                    catch (JSONException e) {
                        //Log.i("LoginActivity", "연결된 기기 정보 없음");
                    }

                    if(lat.equals("") || lng.equals("") || addr.equals("")){
                        loadingDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE);
                        loadingDialog.setTitleText("위치정보 설정");
                        loadingDialog.setContentText("서비스를 사용하기 전에 위치정보를 설정해 주세요");
                        loadingDialog.setConfirmText("확인");
                        loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                loadingDialog.dismissWithAnimation();
                                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                                intent.putExtra("mode", 0);
                                intent.putExtra("userClass", userClass);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else{
                        loadingDialog.dismissWithAnimation();

                        preferences.edit().clear();
                        preferences.edit().apply();
                        preferences.edit().putString("mode", "facebook");
                        preferences.edit().putString("facebook_id", user_facebook_id);
                        preferences.edit().apply();

                        Intent intent = new Intent(MainActivity.this, LoginedActivity.class);
                        intent.putExtra("userClass", userClass);

                        if(strSoundcloudShareURL != null){
                            intent.putExtra("soundcloudURL", strSoundcloudShareURL);
                        }

                        startActivity(intent);
                        finish();
                    }
                }
                else{
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("로그인 실패");
                    loadingDialog.setContentText("정보를 다시 확인해 주세요");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            loadingDialog.dismissWithAnimation();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
                loadingDialog.dismiss();
            }
        }
    }
}

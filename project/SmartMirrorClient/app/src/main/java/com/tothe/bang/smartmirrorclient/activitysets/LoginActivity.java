package com.tothe.bang.smartmirrorclient.activitysets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by BANG on 2016-03-25.
 */
public class LoginActivity extends Activity implements View.OnClickListener{

    private CallbackManager callbackManager;

    private ImageButton ibtnBack;

    private EditText edtxEmail;
    private EditText edtxPassword;

    private Button btnLogin;
    private LinearLayout layFacebook;

    //자동 로그인에 사용되는
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //페이스북 sdk 초기화
        FacebookSdk.sdkInitialize(getApplicationContext());
        //페이스북 콜백 매니져 정의
        callbackManager = CallbackManager.Factory.create();
        //페이스북 로그인 메니져에 콜백 매니져 등록
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //로그인 성공 후 처리
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("facebook", "페이스북 로그인 성공");

                //사용자 어쎄스 토큰값 얻기
                AccessToken accessToken = loginResult.getAccessToken();
                //페이스북 유저 고유 아이디 얻기
                String facebook_id = accessToken.getUserId();

                TaskLoginFacebook taskLoginFacebook = new TaskLoginFacebook(facebook_id);
                taskLoginFacebook.execute("");

            }

            @Override
            public void onCancel() {
                Log.i("facebook", "페이스북 로그인 취소");
            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
                Log.i("facebook", "페이스북 로그인 에러");
            }
        });

//        Intent intent = getIntent();
//        Log.i("LoginAc", "getDataString = " + getIntent().getExtras().get(Intent.EXTRA_TEXT));

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

    public void Init(){
        ibtnBack = (ImageButton) findViewById(R.id.ibtnBack);

        edtxEmail = (EditText) findViewById(R.id.edtxLoginEmail);
        edtxPassword = (EditText) findViewById(R.id.edtxLoginPassword);

        btnLogin = (Button) findViewById(R.id.btnLoginLogin);
        layFacebook = (LinearLayout) findViewById(R.id.layLoginFacebook);

        ibtnBack.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        layFacebook.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == ibtnBack.getId()){
            finish();
        }
        else if(v.getId() == btnLogin.getId()){
            String strEmail = edtxEmail.getText().toString();
            String strPassword = edtxPassword.getText().toString();

            if(isEmail(strEmail)){

                TaskLogin taskLogin = new TaskLogin(strEmail, strPassword);
                taskLogin.execute("");
            }
            else{
                Toast.makeText(this, "올바른 이메일 형식이 아닙니다", Toast.LENGTH_SHORT).show();
            }
        }
        else if(v.getId() == layFacebook.getId()){
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }
    }

    //이메일 양식 검사 함수
    public boolean isEmail(String email) {
        if (email==null) return false;
        boolean b = Pattern.matches("[\\w\\~\\-\\.]+@[\\w\\~\\-]+(\\.[\\w\\~\\-]+)+", email.trim());
        return b;
    }

    //암호화
    public String SHA256(String str){
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //페이스북 액티비티 리설트 등록
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public class TaskLogin extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/login_normal.php";

        private String user_email;
        private String user_password;

        private LoadingDialog loadingDialog;

        public TaskLogin(String user_email, String user_password) {
            this.user_email = user_email;
            this.user_password = SHA256(user_password);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(LoginActivity.this);
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

            //Log.i("login", s);
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

                    if(lat.equals("") || lng.equals("") || addr.equals("")){
                        loadingDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE);
                        loadingDialog.setTitleText("위치정보 설정");
                        loadingDialog.setContentText("서비스를 사용하기 전에 위치정보를 설정해 주세요");
                        loadingDialog.setConfirmText("확인");
                        loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                loadingDialog.dismissWithAnimation();
                                Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
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

                        Intent intent = new Intent(LoginActivity.this, LoginedActivity.class);
                        intent.putExtra("userClass", userClass);
                        startActivity(intent);

                        finish();

                        if(MainActivity.getMainActivity() != null) {
                            MainActivity.getMainActivity().finish();
                        }
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

            loadingDialog = new LoadingDialog(LoginActivity.this);
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
                                Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
                                intent.putExtra("mode", 0);
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
                        editor.putString("mode", "facebook");
                        editor.putString("facebook_id", user_facebook_id);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, LoginedActivity.class);
                        intent.putExtra("userClass", userClass);
                        startActivity(intent);

                        finish();

                        if(MainActivity.getMainActivity() != null) {
                            MainActivity.getMainActivity().finish();
                        }
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
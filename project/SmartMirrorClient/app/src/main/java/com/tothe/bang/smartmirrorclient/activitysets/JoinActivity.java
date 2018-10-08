package com.tothe.bang.smartmirrorclient.activitysets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by BANG on 2016-03-27.
 */
public class JoinActivity extends Activity implements View.OnClickListener{

    private CallbackManager callbackManager;

    private ImageButton ibtnBack;

    private EditText edtxEmail;
    private EditText edtxName;
    private EditText edtxPassword;
    private EditText edtxPasswordCon;

    private Button btnJoin;
    private LinearLayout layFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join);

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("facebook", "페이스북 로그인 성공");

                //사용자 어쎄스 토큰값 얻기
                final AccessToken accessToken = loginResult.getAccessToken();
                //페이스북 유저 고유 아이디 얻기
                final String facebook_id = accessToken.getUserId();
                //이름과 이메일을 알기 위해
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                Log.i("facebook", response.toString());
                                // {Response:  responseCode: 200, graphObject: {"id":"961086880674070","email":"kebin1104@nate.com","name":"방윤환"}, error: null}

                                if(response.toString().contains("email")){
                                    try {

                                        String email = object.getString("email");
                                        String name = object.getString("name");

                                        TaskJoinFacebook taskJoinFacebook = new TaskJoinFacebook(facebook_id, email, name);
                                        taskJoinFacebook.execute("");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(JoinActivity.this, SweetAlertDialog.ERROR_TYPE);
                                    sweetAlertDialog.setTitleText("페이스북");
                                    sweetAlertDialog.setContentText("email 정보 제공에 동의하셔야 합니다");
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
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });

        Init();
    }

    public void Init(){
        ibtnBack = (ImageButton) findViewById(R.id.ibtnBack);

        edtxEmail = (EditText) findViewById(R.id.edtxJoinEmail);
        edtxName = (EditText) findViewById(R.id.edtxJoinName);
        edtxPassword = (EditText) findViewById(R.id.edtxJoinPassword);
        edtxPasswordCon = (EditText) findViewById(R.id.edtxJoinPasswordCon);

        btnJoin = (Button) findViewById(R.id.btnJoinJoin);

        layFacebook = (LinearLayout) findViewById(R.id.layJoinFacebook);

        ibtnBack.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
        layFacebook.setOnClickListener(this);
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
    public void onClick(View v) {
        if(v.getId() == ibtnBack.getId()){
            finish();
        }
        else if(v.getId() == btnJoin.getId()){
            String strEmail = edtxEmail.getText().toString();
            String strName = edtxName.getText().toString();
            String strPassword = edtxPassword.getText().toString();
            String strPasswordCon = edtxPasswordCon.getText().toString();

            if(isEmail(strEmail)){
                if(strName.isEmpty() || strPassword.isEmpty() || strPasswordCon.isEmpty()){
                    Toast.makeText(this, "비어있는 란이 있습니다", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(strPassword.equals(strPasswordCon)){
                        TaskJoin taskJoin = new TaskJoin(strEmail, strPassword, strName);
                        taskJoin.execute("");
                    }
                    else{
                        Toast.makeText(this, "비밀번호를 다시 확인해 주세요", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else{
                Toast.makeText(this, "올바른 이베일 형식이 아닙니다", Toast.LENGTH_SHORT).show();
            }
        }
        else if(v.getId() == layFacebook.getId()){
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public class TaskJoin extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/join_normal.php";

        private String user_email;
        private String user_password;
        private String user_name;

        private LoadingDialog loadingDialog;

        public TaskJoin(String user_email, String user_password, String user_name) {
            this.user_email = user_email;
            this.user_password = SHA256(user_password);
            this.user_name = user_name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(JoinActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();


            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_email", user_email)
                    .addFormDataPart("user_password", user_password)
                    .addFormDataPart("user_name", user_name)
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
                    loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    loadingDialog.setTitleText("회원가입 완료");
                    loadingDialog.setContentText("가입하신 계정으로 로그인 해주세요");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            loadingDialog.dismissWithAnimation();
                            finish();
                        }
                    });
                }
                else if(result.equals("overlap")){
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("회원가입 실패");
                    loadingDialog.setContentText("이메일 중복 입니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            loadingDialog.dismissWithAnimation();
                        }
                    });
                }
                else{
                    loadingDialog.dismiss();
                    Toast.makeText(JoinActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(JoinActivity.this, "서버 오류 입니다", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        }
    }

    public class TaskJoinFacebook extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/join_facebook.php";

        private String user_facebook_id;
        private String user_email;
        private String user_name;

        private LoadingDialog loadingDialog;

        public TaskJoinFacebook(String user_facebook_id, String user_email, String user_name) {
            this.user_facebook_id = user_facebook_id;
            this.user_email = user_email;
            this.user_name = user_name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(JoinActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();


            RequestBody requestBody = new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_facebook_id", user_facebook_id)
                    .addFormDataPart("user_email", user_email)
                    .addFormDataPart("user_name", user_name)
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

                //페이스북으로 받아온 email로 이미지 일반회원가입 되어 있는 사람이 있을 수도 있다.
                if(result.equals("ok")){
                    loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    loadingDialog.setTitleText("회원가입 완료");
                    loadingDialog.setContentText("가입하신 계정으로 로그인 해주세요");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            loadingDialog.dismissWithAnimation();
                            finish();
                        }
                    });
                }
                else if(result.equals("facebook_overlap")){
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("회원가입 실패");
                    loadingDialog.setContentText("이미 페이스북 계정으로 가입하신 회원입니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            loadingDialog.dismissWithAnimation();
                        }
                    });
                }
                else if(result.equals("email_overlap")){
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("회원가입 실패");
                    loadingDialog.setContentText("이미 가입하신 이메일주소가 있습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            loadingDialog.dismissWithAnimation();
                        }
                    });
                }
                else{
                    loadingDialog.dismiss();
                    Toast.makeText(JoinActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(JoinActivity.this, "서버 오류 입니다", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        }
    }
}

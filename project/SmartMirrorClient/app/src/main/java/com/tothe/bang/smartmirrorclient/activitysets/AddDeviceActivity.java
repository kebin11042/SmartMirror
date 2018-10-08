package com.tothe.bang.smartmirrorclient.activitysets;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by BANG on 2016-05-03.
 */
public class AddDeviceActivity extends Activity implements View.OnClickListener{

    private final UserClass userClass = LoginedActivity.getUserClass();

    private ImageButton ibtnBack;

    private EditText edtxDeviceSerialNumber;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_device);

        Init();
    }

    public void Init(){
        ibtnBack = (ImageButton) findViewById(R.id.ibtnBack);

        edtxDeviceSerialNumber = (EditText) findViewById(R.id.edtxAddDeviceSerialNumber);

        btnOk = (Button) findViewById(R.id.btnAddDeviceOk);

        ibtnBack.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == ibtnBack.getId()) {
            finish();
        }
        else if(v.getId() == btnOk.getId()){
            String device_serial_number = edtxDeviceSerialNumber.getText().toString();
            if(device_serial_number.isEmpty()){
                Toast.makeText(AddDeviceActivity.this, "입력창이 비어있습니다", Toast.LENGTH_SHORT).show();
            }
            else{
                TaskInsertDevice taskInsertDevice = new TaskInsertDevice(device_serial_number);
                taskInsertDevice.execute("");
            }
        }
    }



    public class TaskInsertDevice extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/insert_device.php";

        private String device_serial_number;

        private LoadingDialog loadingDialog;

        public TaskInsertDevice(String device_serial_number) {
            this.device_serial_number = device_serial_number;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(AddDeviceActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_id", userClass.getId())
                    .addFormDataPart("device_serial_number", device_serial_number)
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

            //Log.i("AddDeviceAc", s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){
                    loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    loadingDialog.setTitleText("기기 등록");
                    loadingDialog.setContentText("스마트 거울 기기 등록에 성공하였습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            finish();
                        }
                    });
                }
                else if(result.equals("not_exist")){
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("기기 등록");
                    loadingDialog.setContentText("일치하는 스마트 거울 기기가 없습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                }
                else if(result.equals("overlap")){
                    String user_name = jsonObject.getString("user_name");

                    loadingDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                    loadingDialog.setTitleText("기기 등록");
                    loadingDialog.setContentText(user_name + " 님께서 등록하신 기기입니다. 갱신하시겠습니까?");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setCancelText("취소");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            TaskInsertDeviceForced taskInsertDeviceForced = new TaskInsertDeviceForced(device_serial_number);
                            taskInsertDeviceForced.execute("");
                        }
                    });
                    loadingDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                }
                else {
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("기기 등록");
                    loadingDialog.setContentText("스마트 거울 기기등록에 실패하였습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class TaskInsertDeviceForced extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/insert_device_forced.php";

        private String device_serial_number;

        private LoadingDialog loadingDialog;

        public TaskInsertDeviceForced(String device_serial_number) {
            this.device_serial_number = device_serial_number;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(AddDeviceActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_id", userClass.getId())
                    .addFormDataPart("device_serial_number", device_serial_number)
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

            //Log.i("AddDeviceAc", s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){
                    loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    loadingDialog.setTitleText("기기 등록");
                    loadingDialog.setContentText("스마트 거울 기기 등록에 성공하였습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            finish();
                        }
                    });
                }
                else {
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("기기 등록");
                    loadingDialog.setContentText("스마트 거울 기기등록에 실패하였습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
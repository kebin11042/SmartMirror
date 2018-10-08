package com.tothe.bang.smartmirrorclient.activitysets;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tothe.bang.smartmirrorclient.R;
import com.tothe.bang.smartmirrorclient.datasets.UserClass;
import com.tothe.bang.smartmirrorclient.fragmentsets.MemoFragment;
import com.tothe.bang.smartmirrorclient.viewsets.LoadingDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by BANG on 2016-03-28.
 */
public class AddMemoActivity extends Activity implements View.OnClickListener{

    private UserClass userClass;

    private String memo_subject;
    private int nYear, nMonth, nDay;

    private ImageButton ibtnBack;

    private EditText edtxMemoSubject;
    private TextView txtvMemoDate;

    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userClass = (UserClass) getIntent().getExtras().getSerializable("userClass");

        setContentView(R.layout.activity_add_memo);

        Init();
    }

    public void Init() {
        ibtnBack = (ImageButton) findViewById(R.id.ibtnBack);

        edtxMemoSubject = (EditText) findViewById(R.id.edtxAddMemoSubject);
        txtvMemoDate = (TextView) findViewById(R.id.txtvAddMemoDate);

        btnOk = (Button) findViewById(R.id.btnAddMemoOk);

        ibtnBack.setOnClickListener(this);
        txtvMemoDate.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == ibtnBack.getId()) {
            finish();
        }
        else if(v.getId() == txtvMemoDate.getId()) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
//                            Log.i("datePickerDlg", "i = " + i + ", i1 = " + i1 + ", i2 = " + i2);
                            nYear = i;
                            nMonth = i1;
                            nDay = i2;

                            txtvMemoDate.setText(nYear + "년 " + (nMonth+1) + "월 " + nDay + "일");
                        }
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.setTitle("날짜 선택");
            datePickerDialog.setAccentColor(Color.parseColor("#606ACA"));
            datePickerDialog.setThemeDark(true);
            datePickerDialog.vibrate(false);
            datePickerDialog.show(getFragmentManager(), "DatepickerDlg");
        }
        else if(v.getId() == btnOk.getId()) {
            memo_subject = edtxMemoSubject.getText().toString();
            if(!memo_subject.isEmpty()){
                String strDate = txtvMemoDate.getText().toString();
                if(!strDate.equals("날짜를 선택해 주세요")) {
                    //실행
                    TaskInsertMemo taskInsertMemo = new TaskInsertMemo();
                    taskInsertMemo.execute();
                }
                else {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AddMemoActivity.this, SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setTitleText("날짜 선택");
                    sweetAlertDialog.setContentText("날짜를 선택해 주세요");
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
            else{
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AddMemoActivity.this, SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText("메모");
                sweetAlertDialog.setContentText("메모를 입력해 주세요");
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


    public class TaskInsertMemo extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/insert_memo.php";

        private String memo_date;

        private LoadingDialog loadingDialog;

        public TaskInsertMemo() {
            memo_date = nYear + "-" + (nMonth+1) + "-" + nDay;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(AddMemoActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_id", userClass.getId())
                    .addFormDataPart("memo_subject", memo_subject)
                    .addFormDataPart("memo_date", memo_date)
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
                    loadingDialog.setTitleText("메모 등록");
                    loadingDialog.setContentText("메모 등록에 성공하였습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();

                            if(MemoFragment.getMemoFragment() != null) {
                                MemoFragment.getMemoFragment().onRefresh();
                            }

                            finish();
                        }
                    });
                }
                else {
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    loadingDialog.setTitleText("메모 등록");
                    loadingDialog.setContentText("메모 등록에 실패하였습니다");
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

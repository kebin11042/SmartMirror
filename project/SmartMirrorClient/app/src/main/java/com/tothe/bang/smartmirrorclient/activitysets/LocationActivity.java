package com.tothe.bang.smartmirrorclient.activitysets;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.tothe.bang.smartmirrorclient.R;
import com.tothe.bang.smartmirrorclient.datasets.LocationClass;
import com.tothe.bang.smartmirrorclient.datasets.UserClass;
import com.tothe.bang.smartmirrorclient.viewsets.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by BANG on 2016-03-28.
 */
public class LocationActivity extends Activity implements View.OnClickListener{

    private int mode;   //0이면 로그인 후, 1이면 바꾸는 모드

    private UserClass userClass;

    private ArrayList<LocationClass> arrLocation;


    private ImageButton ibtnBack;

    private EditText edtxAddr;
    private ImageButton ibtnSearch;

    private RecyclerView recyclerView;
    private AdapterLocation adapterLocation;

    private TextView txtvNoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getIntent().getExtras().getInt("mode");
        if(mode == 0){
            userClass = (UserClass) getIntent().getExtras().getSerializable("userClass");
        }
        else{
            userClass = LoginedActivity.getUserClass();
        }

        setContentView(R.layout.activity_location);

        Init();
    }

    public void Init(){
        ibtnBack = (ImageButton) findViewById(R.id.ibtnBack);

        edtxAddr = (EditText) findViewById(R.id.edtxLocationAddr);
        ibtnSearch = (ImageButton) findViewById(R.id.ibtnLocationSearch);

        recyclerView = (RecyclerView) findViewById(R.id.recyvLocation);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapterLocation = new AdapterLocation();
        recyclerView.setAdapter(adapterLocation);

        txtvNoResult = (TextView) findViewById(R.id.txtvLocationNoResult);

        ibtnBack.setOnClickListener(this);
        ibtnSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == ibtnBack.getId()){
            finish();
        }
        else if(v.getId() == ibtnSearch.getId()) {
            String strAddr = edtxAddr.getText().toString();
            if(!strAddr.isEmpty()){
                TaskFindAddr taskFindAddr = new TaskFindAddr(strAddr);
                taskFindAddr.execute("");
            }
        }
    }


    public class AdapterLocation extends RecyclerView.Adapter<AdapterLocation.ViewHolderLocation> {

        @Override
        public ViewHolderLocation onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_location, viewGroup, false);

            return new ViewHolderLocation(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolderLocation viewHolderLocation, int i) {
            final LocationClass locationClass = arrLocation.get(i);

            viewHolderLocation.txtvAddr.setText(locationClass.getTotalLocalName());
            viewHolderLocation.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(LocationActivity.this, SweetAlertDialog.NORMAL_TYPE);
                    sweetAlertDialog.setTitleText("위치 설정");
                    sweetAlertDialog.setContentText("위치를 설정 하시겠습니까?");
                    sweetAlertDialog.setConfirmText("확인");
                    sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            TaskSetLocation taskSetLocation = new TaskSetLocation(locationClass.getLat(), locationClass.getLng(), locationClass.getTotalLocalName());
                            taskSetLocation.execute("");
                        }
                    });
                    sweetAlertDialog.setCancelText("취소");
                    sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                    sweetAlertDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            if(arrLocation == null){
                return 0;
            }
            else{
                return arrLocation.size();
            }
        }

        public class ViewHolderLocation extends RecyclerView.ViewHolder {

            protected TextView txtvAddr;

            public ViewHolderLocation(View itemView) {
                super(itemView);

                txtvAddr = (TextView) itemView.findViewById(R.id.txtvLocationViewAddr);
            }
        }
    }



    public class TaskFindAddr extends AsyncTask<String, Integer, String> {

        private String strUrl = "https://apis.daum.net/local/geo/addr2coord?output=json&apikey=ec91a04fd58c983b32f3bbc63b3c7c4a";

        private LoadingDialog loadingDialog;

        public TaskFindAddr(String strSearch) {
            try {
                strSearch = URLEncoder.encode(strSearch, "utf-8");
                strUrl = strUrl + "&q=" + strSearch;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(LocationActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(strUrl)
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

            loadingDialog.dismissWithAnimation();

            //Log.i("location", s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject jsonObjectChannel = jsonObject.getJSONObject("channel");

                JSONArray jsonArrayItem = jsonObjectChannel.getJSONArray("item");

                arrLocation = new ArrayList<LocationClass>();

                for(int i=0;i<jsonArrayItem.length();i++){
                    JSONObject jsonObjectItem = jsonArrayItem.getJSONObject(i);

                    String lat = jsonObjectItem.getDouble("lat") + "";
                    String lng = jsonObjectItem.getDouble("lng") + "";
                    String title = jsonObjectItem.getString("title");
                    String[] localName = new String[3];
                    localName[0] = jsonObjectItem.getString("localName_1");
                    localName[1] = jsonObjectItem.getString("localName_2");
                    localName[2] = jsonObjectItem.getString("localName_3");

                    LocationClass locationClass = new LocationClass();
                    locationClass.setTitle(title);
                    locationClass.setLat(lat);
                    locationClass.setLng(lng);
                    locationClass.setLocalName(localName);

                    arrLocation.add(locationClass);
                }

                if(arrLocation.size() == 0){
                    txtvNoResult.setVisibility(View.VISIBLE);
                }
                else{
                    txtvNoResult.setVisibility(View.GONE);
                }

                if(adapterLocation == null){
                    adapterLocation = new AdapterLocation();
                    recyclerView.setAdapter(adapterLocation);
                }

                adapterLocation.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class TaskSetLocation extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/setlocation.php";

        private String user_lat, user_lng;
        private String user_addr;

        private LoadingDialog loadingDialog;

        public TaskSetLocation(String user_lat, String user_lng, String user_addr) {
            this.user_lat = user_lat;
            this.user_lng = user_lng;
            this.user_addr = user_addr;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog(LocationActivity.this);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody =  new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("user_id", userClass.getId())
                    .addFormDataPart("user_lat", user_lat)
                    .addFormDataPart("user_lng", user_lng)
                    .addFormDataPart("user_addr", user_addr)
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

           // Log.i("location", s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){

                    userClass.setLat(user_lat);
                    userClass.setLng(user_lng);
                    userClass.setAddr(user_addr);

                    Log.i("location", "user_addr = " + userClass.getAddr());

                    loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    loadingDialog.setTitleText("위치 설정");
                    loadingDialog.setContentText("위시 설정이 완료되었습니다");
                    loadingDialog.setConfirmText("확인");
                    loadingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            if(mode == 0){

                                Intent intent = new Intent(LocationActivity.this, LoginedActivity.class);
                                intent.putExtra("userClass", userClass);
                                startActivity(intent);
                            }
                            else{
                                finish();
                            }
                        }
                    });
                }
                else if(result.equals("fail")){
                    loadingDialog.dismissWithAnimation();
                }
                else{
                    loadingDialog.dismissWithAnimation();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                loadingDialog.dismissWithAnimation();
            }
        }
    }

}

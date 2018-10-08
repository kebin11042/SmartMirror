package com.tothe.bang.smartmirrorclient.fragmentsets;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tothe.bang.smartmirrorclient.R;
import com.tothe.bang.smartmirrorclient.activitysets.AddMemoActivity;
import com.tothe.bang.smartmirrorclient.activitysets.LoginedActivity;
import com.tothe.bang.smartmirrorclient.datasets.MemoClass;
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
public class MemoFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener{

    private final UserClass userClass = LoginedActivity.getUserClass();
    private ArrayList<MemoClass> arrMemoClass;

    private static MemoFragment memoFragment;

    private int currentPage;
    private int totalPage;
    private int totalCnt;

    private ViewGroup rootView;

    private SwipeRefreshLayout swlayMemo;
    private RecyclerView recyclerView;

    private FrameLayout layNoResult;

    private AdapterMemo adapterMemo;

    private FloatingActionButton fabAdd;

    public static MemoFragment getMemoFragment() {
        return memoFragment;
    }

    public static MemoFragment create(){
        MemoFragment fragment = new MemoFragment();

        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        totalPage = 0;
        totalCnt = 0;
        currentPage = 1;

        adapterMemo = null;
        arrMemoClass = null;

        memoFragment = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_memo, container, false);

        Init();

        TaskMemoList taskMemoList = new TaskMemoList();
        taskMemoList.execute();

        return rootView;
    }

    public void Init(){

        swlayMemo = (SwipeRefreshLayout) rootView.findViewById(R.id.swlayMemo);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyvMemo);

        layNoResult = (FrameLayout) rootView.findViewById(R.id.layMemoNoResult);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapterMemo = new AdapterMemo();
        recyclerView.setAdapter(adapterMemo);

        fabAdd = (FloatingActionButton) rootView.findViewById(R.id.fabMemoAdd);


        fabAdd.setOnClickListener(this);

        swlayMemo.setOnRefreshListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == fabAdd.getId()){
            Intent intent = new Intent(getActivity(), AddMemoActivity.class);
            intent.putExtra("userClass", userClass);
            startActivity(intent);
        }
    }

    @Override
    public void onRefresh() {
        arrMemoClass = null;
        currentPage = 1;

        TaskMemoList taskMemoList = new TaskMemoList();
        taskMemoList.execute();
    }

    ////////////////////어댑터
    public class AdapterMemo extends RecyclerView.Adapter<AdapterMemo.ViewHolderMemo> {

        @Override
        public ViewHolderMemo onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_memo, viewGroup, false);

            return new ViewHolderMemo(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolderMemo viewHolderMemo, int i) {

            final MemoClass memoClass = arrMemoClass.get(i);

            viewHolderMemo.txtvSubject.setText(memoClass.getSubject());
            viewHolderMemo.txtvDate.setText(memoClass.getDate());

            viewHolderMemo.itemView.setOnLongClickListener(new View.OnLongClickListener() {
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
                                    TaskDeleteMemo taskDeleteMemo = new TaskDeleteMemo(memoClass.getId());
                                    taskDeleteMemo.execute();
                                }
                            })
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            })
                            .show();

                    return true;
                }
            });

            if(i == ( arrMemoClass.size() - 1) ) {
                if(currentPage < totalPage){
                    currentPage++;

                    swlayMemo.setRefreshing(true);
                    TaskMemoList taskMusicList = new TaskMemoList();
                    taskMusicList.execute("");
                }
            }
        }

        @Override
        public int getItemCount() {
            if(arrMemoClass != null){
                return arrMemoClass.size();
            }

            return 0;
        }

        public class ViewHolderMemo extends RecyclerView.ViewHolder {

            protected TextView txtvSubject;
            protected TextView txtvDate;

            public ViewHolderMemo(View itemView) {
                super(itemView);

                txtvSubject = (TextView) itemView.findViewById(R.id.txtvMemoSubject);
                txtvDate = (TextView) itemView.findViewById(R.id.txtvMemoDate);
            }
        }
    }




    ////////////////////////////비동기 쓰레드
    public class TaskMemoList extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/get_memo_list.php";

        public TaskMemoList() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!swlayMemo.isRefreshing()){
                swlayMemo.setRefreshing(true);
            }
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

            if(swlayMemo.isRefreshing()){
                swlayMemo.setRefreshing(false);
            }

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");

                if(result.equals("ok")){
                    totalPage = jsonObject.getInt("totalPage");
                    totalCnt = jsonObject.getInt("totalCnt");

                    if (arrMemoClass == null){
                        arrMemoClass = new ArrayList<MemoClass>();
                    }

                    if(totalPage != 0){
                        layNoResult.setVisibility(View.GONE);
                        JSONArray jsonArrayMusicList = jsonObject.getJSONArray("memo_list");
                        for(int i=0;i<jsonArrayMusicList.length();i++){
                            JSONObject jsonObjectMusicList = jsonArrayMusicList.getJSONObject(i);
                            String id = jsonObjectMusicList.getString("id");
                            String subject = jsonObjectMusicList.getString("subject");
                            String date = jsonObjectMusicList.getString("date");
                            String created = jsonObjectMusicList.getString("created");

                            MemoClass memoClass = new MemoClass();
                            memoClass.setId(id);
                            memoClass.setSubject(subject);
                            memoClass.setDate(date);
                            memoClass.setCreated(created);

                            arrMemoClass.add(memoClass);
                        }
                    }
                    else{
                        layNoResult.setVisibility(View.VISIBLE);
                    }

                    if(adapterMemo == null){
                        adapterMemo = new AdapterMemo();
                        recyclerView.setAdapter(adapterMemo);
                    }
                    adapterMemo.notifyDataSetChanged();
                }
                else{
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setTitleText("메모 리스트 오류!");
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
                sweetAlertDialog.setTitleText("메모 리스트 오류!");
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

    public class TaskDeleteMemo extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/delete_memo.php";

        private LoadingDialog loadingDialog;
        private String memo_id;

        public TaskDeleteMemo(String music_id) {
            this.memo_id = music_id;
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
                    .addFormDataPart("memo_id", memo_id)
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
                            arrMemoClass = null;
                            TaskMemoList taskMemoList = new TaskMemoList();
                            taskMemoList.execute();
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
}

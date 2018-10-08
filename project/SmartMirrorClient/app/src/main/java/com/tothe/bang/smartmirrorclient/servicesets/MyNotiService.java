package com.tothe.bang.smartmirrorclient.servicesets;

import android.os.AsyncTask;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tothe.bang.smartmirrorclient.activitysets.LoginActivity;
import com.tothe.bang.smartmirrorclient.activitysets.LoginedActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by BANG on 2016-03-08.
 */
public class MyNotiService extends NotificationListenerService {

    private static MyNotiService myNotiService;

    private String user_id;

    public static final String EXTRA_TITLE = "android.title";
    public static final String EXTRA_TEXT = "android.text";
    public static final String EXTRA_SUB_TEXT = "android.subText";
    public static final String EXTRA_LARGE_ICON = "android.largeIcon";

    @Override
    public void onCreate() {
        Log.i("onCreate()", "NotificationListenerService start!!!!");
        myNotiService = this;
    }

    public static MyNotiService getMyNotiService() {
        return myNotiService;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Bundle bundle = sbn.getNotification().extras;

        String packName = sbn.getPackageName();

//        String notificationTitle = bundle.getString(Notification.EXTRA_TITLE);
//        //int notificationIcon = bundle.getInt(Notification.EXTRA_SMALL_ICON);
//        //Bitmap notificationLargeIcon = ((Bitmap) bundle.getParcelable(Notification.EXTRA_LARGE_ICON));
//        CharSequence notificationText = bundle.getCharSequence(Notification.EXTRA_TEXT);
//        CharSequence notificationSubText = bundle.getCharSequence(Notification.EXTRA_SUB_TEXT);
//
//        if(packName != null){
//            Log.i("onNotiPosted", "packName : " + packName);
//        }
//        if(notificationTitle != null){
//            Log.i("onNotiPosted", "Title : " + notificationTitle);
//        }
//        if(notificationText != null){
//            Log.i("onNotiPosted", "Text : " + notificationText.toString());
//        }
//        if(notificationSubText != null){
//            Log.i("onNotiPosted", "SubText : " + notificationSubText.toString());
//        }

        if(packName != null) {

            try {
                if(user_id == null) {
                    user_id = LoginedActivity.getUserClass().getId();
                }

                //Log.i("MyNotiService", "packName = " + packName);
                if(packName.contains("com.kakao.talk")) {
                    //카카오톡 메시지 알림이 왔을 때
                    TaskGcmSend taskGcmSend = new TaskGcmSend("received", "kakao");
                    taskGcmSend.execute();
                }
                else if(packName.contains("mms")) {
                    TaskGcmSend taskGcmSend = new TaskGcmSend("received", "mms");
                    taskGcmSend.execute();
                }
                else if(packName.contains("com.facebook")) {
                    TaskGcmSend taskGcmSend = new TaskGcmSend("received", "facebook");
                    taskGcmSend.execute();
                }
            }
            catch (NullPointerException e) {
                Log.i("MyNoti", e.toString());
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Bundle bundle = sbn.getNotification().extras;
//        Log.i("onNotiRemoved", bundle.toString());

        Bundle bundle = sbn.getNotification().extras;

        String packName = sbn.getPackageName();

//        String notificationTitle = bundle.getString(Notification.EXTRA_TITLE);
//        //int notificationIcon = bundle.getInt(Notification.EXTRA_SMALL_ICON);
//        //Bitmap notificationLargeIcon = ((Bitmap) bundle.getParcelable(Notification.EXTRA_LARGE_ICON));
//        CharSequence notificationText = bundle.getCharSequence(Notification.EXTRA_TEXT);
//        CharSequence notificationSubText = bundle.getCharSequence(Notification.EXTRA_SUB_TEXT);
//
//        if(packName != null){
//            Log.i("onNotiPosted", "packName : " + packName);
//        }
//        if(notificationTitle != null){
//            Log.i("onNotiPosted", "Title : " + notificationTitle);
//        }
//        if(notificationText != null){
//            Log.i("onNotiPosted", "Text : " + notificationText.toString());
//        }
//        if(notificationSubText != null){
//            Log.i("onNotiPosted", "SubText : " + notificationSubText.toString());
//        }

        if(packName != null) {

            try {
                if(user_id == null) {
                    user_id = LoginedActivity.getUserClass().getId();
                }

                if(packName.contains("com.kakao.talk")) {
                    //카카오톡 메시지 알림이 지워졌을 때
                    TaskGcmSend taskGcmSend = new TaskGcmSend("removed", "kakao");
                    taskGcmSend.execute();
                }
                else if(packName.contains("mms")) {
                    TaskGcmSend taskGcmSend = new TaskGcmSend("removed", "mms");
                    taskGcmSend.execute();
                }
                else if(packName.contains("com.facebook")) {
                    TaskGcmSend taskGcmSend = new TaskGcmSend("removed", "facebook");
                    taskGcmSend.execute();
                }
            }
            catch (NullPointerException e) {
                Log.i("MyNoti", e.toString());
            }
        }
    }

    @Override
    public void onDestroy() {
        try {
            if(user_id != null){
                //Log.i("MyNotiService", "user_id = " + user_id);
            }
            else{
                //Log.i("MyNotiService", "user_id is NULL!! ");
                user_id = LoginedActivity.getUserClass().getId();
            }

            TaskGcmSend taskGcmSend = new TaskGcmSend("removed", "all");
            taskGcmSend.execute();

            Log.i("onDestroy()", "NotificationListenerService destroy!!!!");
            myNotiService = null;
        }
        catch (NullPointerException e) {
            Log.i("MyNotiDes", e.toString());
        }

        super.onDestroy();
    }



    ///

    public class TaskGcmSend extends AsyncTask<String, Integer, String> {

        private final String strUrl = "http://kebin1104.dothome.co.kr/SSM/gcm_send.php";

        private String gcm_mode;
        private String gcm_contents;

        public TaskGcmSend(String gcm_mode, String gcm_contents) {
            this.gcm_mode = gcm_mode;
            this.gcm_contents = gcm_contents;
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
                    .addFormDataPart("user_id", user_id)
                    .addFormDataPart("gcm_mode", gcm_mode)
                    .addFormDataPart("gcm_contents", gcm_contents)
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

            //Log.i("GCM SEND", s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.tothe.bang.smartmirrordevice.gcmsets;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.tothe.bang.smartmirrordevice.activitysets.MainActivity;


/**
 * Created by BANG on 2016-02-21.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     *
     * @param from SenderID 값을 받아온다.
     * @param data Set형태로 GCM으로 받은 데이터 payload이다.
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
//
//        /*
//            'title' :
//            'message' :
//            'msg_type': alarm.msg_type,
//            'event_id': alarm.event_id,
//            'event_comment_id': alarm.event_comment_id,
//            'notice_id': alarm.notice_id,
//            'member_id': alarm.member_id,
//            'comment_type': alarm.comment_type,
//            'badge': cnt
//        * */
//
//        String title = data.getString("title");
//        String message = data.getString("message");
//        String badge = data.getString("badge");
//        int not_read = 0;
//        if(badge != null){
//            not_read = Integer.parseInt(badge);
//        }
//
//        Log.d(TAG, "data " + data.toString());
////        Log.d(TAG, "Title: " + title);
////        Log.d(TAG, "Message: " + message);
//
//        // GCM으로 받은 메세지를 디바이스에 알려주는 sendNotification()을 호출한다.
////        sendNotification(title, message, not_read);
//        sendNotification(title, message, not_read);

        try{
            String gcm_mode = data.getString("gcm_mode");
            String gcm_contents = data.getString("gcm_contents");

            if(MainActivity.getMainActivity() != null) {
                //나타내기
                if(gcm_mode.equals("received")){
                    if(gcm_contents.equals("kakao")) {
                        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getMainActivity().setVisibleKakaoView();
                            }
                        });
                    }

                    else if(gcm_contents.equals("mms")) {
                        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getMainActivity().setVisibleMMSView();
                            }
                        });
                    }

                    else if(gcm_contents.equals("facebook")) {
                        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getMainActivity().setVisibleFacebookView();
                            }
                        });
                    }
                }
                //지우기
                else if(gcm_mode.equals("removed")) {
                    if(gcm_contents.equals("kakao")) {
                        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getMainActivity().setGoneKakaoView();
                            }
                        });
                    }
                    else if(gcm_contents.equals("mms")) {
                        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getMainActivity().setGoneMMSView();
                            }
                        });
                    }
                    else if(gcm_contents.equals("facebook")) {
                        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getMainActivity().setGoneFacebookView();
                            }
                        });
                    }
                    else if(gcm_contents.equals("all")) {
                        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getMainActivity().setGoneKakaoView();
                                MainActivity.getMainActivity().setGoneMMSView();
                                MainActivity.getMainActivity().setGoneFacebookView();
                            }
                        });
                    }
                }
            }
        }
        catch (NullPointerException e) {

        }
    }


    /**
     * 실제 디바에스에 GCM으로부터 받은 메세지를 알려주는 함수이다. 디바이스 Notification Center에 나타난다.
     * @param title
     * @param message
     */
//    private void sendNotification(String title, String message, int not_read) {
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.push_big_icon);
//
//        if (bitmapDrawable != null) {
//            Bitmap bitmap = bitmapDrawable.getBitmap();
//
//            //현재 실행중이라면 굳이
//            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningTaskInfo> arrTaskInfo = activityManager.getRunningTasks(9999);
//
//            Intent intent = null;
//
//            for(int i=0;i<arrTaskInfo.size();i++){
////                Log.i("runningTasks", arrTaskInfo.get(i).topActivity.getPackageName());
////                Log.i("runningTasks", arrTaskInfo.get(i).topActivity.getClassName());
//                if(arrTaskInfo.get(i).topActivity.getClassName().contains("com.example.bang.unboxbeta")){
////                    intent = getPackageManager().getLaunchIntentForPackage(arrTaskInfo.get(i).topActivity.getClassName());
//                    intent = new Intent();
//                    intent.setClassName(this, arrTaskInfo.get(i).topActivity.getClassName());
//                }
//            }
//
//
//            if(intent == null){
//                intent = new Intent(this, FirstActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            }
//            else{
//                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            }
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                    PendingIntent.FLAG_ONE_SHOT);
//
//            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.drawable.push_small_icon_v03)
//                    .setColor(Color.parseColor("#0088CC"))          //현재 임의의 색임
//                    .setLargeIcon(bitmap)
//                    .setContentTitle("unbox")
//                    .setContentText(message)
//                    .setAutoCancel(true)
//                    .setSound(defaultSoundUri)
//                    .setContentIntent(pendingIntent);
//
//            NotificationManager notificationManager =
//                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//        }
//    }
}

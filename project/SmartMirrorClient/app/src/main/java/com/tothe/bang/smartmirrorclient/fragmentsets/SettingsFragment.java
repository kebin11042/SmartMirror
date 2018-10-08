package com.tothe.bang.smartmirrorclient.fragmentsets;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tothe.bang.smartmirrorclient.R;
import com.tothe.bang.smartmirrorclient.activitysets.AddDeviceActivity;
import com.tothe.bang.smartmirrorclient.activitysets.LocationActivity;
import com.tothe.bang.smartmirrorclient.activitysets.LoginedActivity;
import com.tothe.bang.smartmirrorclient.activitysets.MainActivity;
import com.tothe.bang.smartmirrorclient.datasets.UserClass;

/**
 * Created by BANG on 2016-03-27.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener{

    private final UserClass userClass = LoginedActivity.getUserClass();

    private ViewGroup rootView;

    private TextView txtvEmail;
    private TextView txtvName;
    private TextView txtvDeviceInfo;

    private TextView txtvLocation;
    private TextView txtvDevice;
    private TextView txtvNoti;
    private TextView txtvLogout;

    //자동 로그인에 사용되는
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static SettingsFragment create(){
        SettingsFragment fragment = new SettingsFragment();

        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getActivity().getSharedPreferences("user", getActivity().MODE_PRIVATE);
        editor = preferences.edit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_settings, container, false);

        Init();

        return rootView;
    }

    public void Init(){
        txtvEmail = (TextView) rootView.findViewById(R.id.txtvSettingsEmail);
        txtvName = (TextView) rootView.findViewById(R.id.txtvSettingsName);
        txtvDeviceInfo = (TextView) rootView.findViewById(R.id.txtvSettingsDeviceInfo);

        txtvLocation = (TextView) rootView.findViewById(R.id.txtvSettingsLocation);
        txtvDevice = (TextView) rootView.findViewById(R.id.txtvSettingsDevice);
        txtvNoti = (TextView) rootView.findViewById(R.id.txtvSettingsNoti);
        txtvLogout = (TextView) rootView.findViewById(R.id.txtvSettingsLogout);

        txtvLocation.setOnClickListener(this);
        txtvDevice.setOnClickListener(this);
        txtvNoti.setOnClickListener(this);
        txtvLogout.setOnClickListener(this);

        txtvEmail.setText(userClass.getEmail());
        txtvName.setText(userClass.getName());
    }

    @Override
    public void onResume() {
        super.onResume();

        //Log.i("Settings", "user_addr = " + userClass.getAddr());
        if(userClass.getDeviceClass().getSerial_Number() == null) {
            txtvDeviceInfo.setText("연결된 기기가 없습니다");
        }
        else {
            txtvDeviceInfo.setText(userClass.getDeviceClass().getSerial_Number());
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == txtvLocation.getId()){
            Intent intent = new Intent(getActivity(), LocationActivity.class);
            intent.putExtra("mode", 1);
            intent.putExtra("userClass", userClass);
            startActivity(intent);
        }
        else if(v.getId() == txtvDevice.getId()){
            Intent intent = new Intent(getActivity(), AddDeviceActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == txtvNoti.getId()) {
            //알림 읽어올때 설정 해줘야 함
            Intent intentSettings = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intentSettings);
        }
        else if(v.getId() == txtvLogout.getId()) {
            //자동 로그인 정보 다 지우기
            editor.clear();
            editor.apply();

            getActivity().finish();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            getActivity().startActivity(intent);
        }
    }
}

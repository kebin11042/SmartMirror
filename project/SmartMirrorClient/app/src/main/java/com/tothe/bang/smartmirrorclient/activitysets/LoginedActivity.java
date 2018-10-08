package com.tothe.bang.smartmirrorclient.activitysets;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.tothe.bang.smartmirrorclient.R;
import com.tothe.bang.smartmirrorclient.datasets.UserClass;
import com.tothe.bang.smartmirrorclient.fragmentsets.MemoFragment;
import com.tothe.bang.smartmirrorclient.fragmentsets.MusicFragment;
import com.tothe.bang.smartmirrorclient.fragmentsets.SettingsFragment;
import com.tothe.bang.smartmirrorclient.servicesets.MyNotiService;

/**
 * Created by BANG on 2016-03-27.
 */
public class LoginedActivity extends FragmentActivity implements View.OnClickListener, ViewPager.OnPageChangeListener{

    private static UserClass userClass;
    private String strSoundcloudShareURL;

    private final int FRAGMENT_CNT = 3; //프래그 먼트 화면 개수

    private Fragment[] fragments;

    private ImageButton[] ibtnTabs;
    private View vIndicator;

    private ViewPager vpagerLogined;
    private PagerAdapterLogined pagerAdapterLogined;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userClass = (UserClass) getIntent().getExtras().getSerializable("userClass");

        try{
            strSoundcloudShareURL = getIntent().getExtras().getString("soundcloudURL");
            Log.i("Logined", "URL = " + strSoundcloudShareURL);
        }
        catch (NullPointerException e) {
            strSoundcloudShareURL = null;
        }

        //프래그먼트 생성
        setFragments();

        setContentView(R.layout.activity_logined);

        Init();

        //공유기능으로 추가한 것이라고 생각하고 노래 추가 화면을 띄운다
        if(strSoundcloudShareURL != null){
            Intent intent = new Intent(LoginedActivity.this, AddMusicActivity.class);
            intent.putExtra("soundcloudURL", strSoundcloudShareURL);
            startActivity(intent);

            vpagerLogined.setCurrentItem(1);
        }

        if(MyNotiService.getMyNotiService() != null) {
            MyNotiService.getMyNotiService().setUser_id(userClass.getId());
        }
    }

    public static UserClass getUserClass(){
        return userClass;
    }

    public void setFragments(){
        fragments = new Fragment[FRAGMENT_CNT];
        fragments[0] = MemoFragment.create();
        fragments[1] = MusicFragment.create();
        fragments[2] = SettingsFragment.create();
    }

    public void InitTabs(){
        ibtnTabs = new ImageButton[FRAGMENT_CNT];

        ibtnTabs[0] = (ImageButton) findViewById(R.id.ibtnLoginedTab1);
        ibtnTabs[1] = (ImageButton) findViewById(R.id.ibtnLoginedTab2);
        ibtnTabs[2] = (ImageButton) findViewById(R.id.ibtnLoginedTab3);

        for (ImageButton ibtnTab : ibtnTabs) {
            ibtnTab.setOnClickListener(this);
        }
    }

    public void InitIndicator(){
        vIndicator = findViewById(R.id.vLoginedIndicator);

        vIndicator.getLayoutParams().width = getResources().getDisplayMetrics().widthPixels / 3;
    }

    public void Init(){
        InitTabs();
        InitIndicator();

        vpagerLogined = (ViewPager) findViewById(R.id.vpagerLogined);

        pagerAdapterLogined = new PagerAdapterLogined(getSupportFragmentManager());
        vpagerLogined.setAdapter(pagerAdapterLogined);

        int scale = (int) getResources().getDisplayMetrics().density;
        int pagerMargin = scale * 4;

        vpagerLogined.setPageMargin(pagerMargin);
        vpagerLogined.setPageMarginDrawable(R.drawable.pagermargin_d_grey);

        vpagerLogined.addOnPageChangeListener(this);
        vpagerLogined.setOffscreenPageLimit(3);
    }

    public void setTabs(int position){
        if(position == 0){
            ibtnTabs[0].setImageResource(R.drawable.img_tab_memo_wh);
            ibtnTabs[1].setImageResource(R.drawable.img_tab_music_grey);
            ibtnTabs[2].setImageResource(R.drawable.img_tab_settings_grey);
        }
        else if(position == 1){
            ibtnTabs[0].setImageResource(R.drawable.img_tab_memo_grey);
            ibtnTabs[1].setImageResource(R.drawable.img_tab_music_wh);
            ibtnTabs[2].setImageResource(R.drawable.img_tab_settings_grey);
        }
        else{
            ibtnTabs[0].setImageResource(R.drawable.img_tab_memo_grey);
            ibtnTabs[1].setImageResource(R.drawable.img_tab_music_grey);
            ibtnTabs[2].setImageResource(R.drawable.img_tab_settings_wh);
        }

        setIndicator(position);
    }

    public void setIndicator(int position){
        int x = getResources().getDisplayMetrics().widthPixels / 3;
        x = x * position;

        vIndicator.animate().setDuration(250).translationX(x).start();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == ibtnTabs[0].getId()){
            setTabs(0);
            vpagerLogined.setCurrentItem(0, true);
        }
        else if(v.getId() == ibtnTabs[1].getId()){
            setTabs(1);
            vpagerLogined.setCurrentItem(1, true);
        }
        else if(v.getId() == ibtnTabs[2].getId()){
            setTabs(2);
            vpagerLogined.setCurrentItem(2, true);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setTabs(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //뷰페이져 어댑터 클래스
    public class PagerAdapterLogined extends FragmentPagerAdapter {

        public PagerAdapterLogined(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }
}

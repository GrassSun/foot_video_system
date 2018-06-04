package com.example.dh.football_video_system;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.view.ViewPager;


import android.app.Dialog;
import android.app.Activity;
import android.widget.Button;
import android.widget.Spinner;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.Display;

import static android.graphics.Color.GREEN;

public class MainActivity extends AppCompatActivity {

    private ViewPager mVp;
    private SharedPreferences sp ;
    private Boolean login;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            login = sp.getBoolean("login",false);
            if (login) {
                switch (item.getItemId()) {
                    case R.id.navigation_video:
                        mVp.setCurrentItem(0);
                        return true;
                    case R.id.navigation_assignment:
                        mVp.setCurrentItem(1);
                        return true;
                    case R.id.navigation_user:
                        mVp.setCurrentItem(2);
                        return true;
//                case R.id.navigation_user:
//                    mVp.setCurrentItem(3);
//                    return true;
                }
            }
            else
            {
                logout();
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_main);
        sp = this.getSharedPreferences("User", Context.MODE_PRIVATE);
        login = sp.getBoolean("login",false);
        if(!login)
        {
            logout();
        }
        mVp = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(mVp);
        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                navigation.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        BottomAdapter adapter = new BottomAdapter(getSupportFragmentManager());
        adapter.addFragment(new CheckFragment());
        adapter.addFragment(new AssignFragment());
//        adapter.addFragment(new Fragment());
        adapter.addFragment(new UserFragment());
        viewPager.setAdapter(adapter);
    }
    private void logout() {
        SharedPreferences.Editor edit = sp.edit();
        //通过editor对象写入数据
        edit.putBoolean("login", false);
        edit.commit();
        ActivityCollector.finishAll();
        Intent intent=new Intent(this, LoginActivity.class);
        this.startActivity(intent);
    }

}

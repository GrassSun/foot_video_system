package com.example.dh.football_video_system;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class CheckFragment extends Fragment {
    private LinearLayout publicVideoLayout;
    private LinearLayout privateVideoLayout;
    private Spinner weekdaySpinner;

    private static String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    private static String[] data_of_Spinner;
    private static String[] year_of_Spinner;
    private static String[] month_of_Spinner;
    private static String[] day_of_Spinner;
    private SharedPreferences sp ;
    String username;
    String password;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i("in check","0");
        View view = inflater.inflate(R.layout.check, null);
        publicVideoLayout = (LinearLayout)view.findViewById(R.id.publicVideoLinearLayout);
        privateVideoLayout = (LinearLayout)view.findViewById(R.id.privateVideoLinearLayout);
        Log.i("in check","1");
        data_of_Spinner = new String[7];
        year_of_Spinner = new String[7];
        month_of_Spinner = new String[7];
        day_of_Spinner = new String[7];
        sp = this.getActivity().getSharedPreferences("User", Context.MODE_PRIVATE);
        username = sp.getString("username","Null");
        password = sp.getString("password", "Null");
        Log.i("in check","2");
        for(int i = 0; i < 7; i++)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,-i);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH)+1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int weekday = calendar.get(Calendar.DAY_OF_WEEK)-1;
            Log.i("weekday in check", String.valueOf(weekday));
            data_of_Spinner[weekday] = weekdays[weekday]+"   "+String.valueOf(month)+"/"+String.valueOf(day)+"/"+String.valueOf(year);
            year_of_Spinner[weekday] = String.valueOf(year);
            month_of_Spinner[weekday] = String.valueOf(month);
            day_of_Spinner[weekday] = String.valueOf(day);
        }


        weekdaySpinner = (Spinner) view.findViewById(R.id.weekday_spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.my_spinner, data_of_Spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        weekdaySpinner.setAdapter(adapter);
        weekdaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //选取时候的操作
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    final String year = year_of_Spinner[position];
                    final String month = month_of_Spinner[position];
                    final String day = day_of_Spinner[position];
                    check_video(year, month, day, username, password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //没被选取时的操作
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
//        {
//            for(int i = 0; i < 5; i++)
//            {
//                Uri uri = Uri.parse("http://166.111.68.66:28080/video/test.flv");
//                CardView test_card = new CardView(this.getActivity());
//                test_card.setCardElevation(20);
//                LinearLayout testLinearLayout = new LinearLayout(this.getActivity());
//                testLinearLayout.setOrientation(LinearLayout.VERTICAL);
//                LinearLayout.LayoutParams x = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);
//                x.weight = 0;
//
//                final VideoView videoView = new VideoView(this.getActivity());
//                videoView.setLayoutParams(x);
//                LinearLayout.LayoutParams c = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                c.weight = 1;
//                String txt = "game name: test"+String.valueOf(i);
//                TextView test = new TextView(this.getActivity());
//                test.setLayoutParams(c);
//                test.setText(txt);
//                test.setGravity(Gravity.CENTER);
////                MediaController mediaController = new MediaController(test_card.getContext());
////                videoView.setMediaController(mediaController);
//                videoView.setVideoURI(uri);
//                test_card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));
//                videoView.seekTo(100);
//                videoView.start();
//                videoView.pause();
//                testLinearLayout.addView(test);
//                testLinearLayout.addView(videoView);
//                test_card.addView(testLinearLayout);
//                test_card.setBackgroundColor(0xCCCCCCCC);
//                testLinearLayout.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if(videoView.isPlaying())
//                        {
//                            videoView.pause();
//                        }
//                        else
//                        {
//                            videoView.start();
//                        }
//
//                    }
//                });
//                publicVideoLayout.addView(test_card);
//            }
//
//        }
        return view;
    }

    private void check_video(final String year, final String month, final String day, final String username, final String password)
    {
        final android.os.Handler handler = new android.os.Handler() {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle b = msg.getData();
                String msg_string = b.getString("msg");
                present_videos(msg_string);
            }

        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                //urlConnection请求服务器，验证
                try {
                    BufferedReader br = null;
                    //1：url对象
                    URL url = new URL("http://166.111.68.66:28000/"+"ask_for_videos"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "year=" + URLEncoder.encode(year) +"&month=" + URLEncoder.encode(month)+ "&day=" + URLEncoder.encode(day)+"&username="
                            +URLEncoder.encode(username)+"&password="+URLEncoder.encode(password);

                    //设置conn可以写请求的内容
                    httpconn.setDoOutput(true);
                    httpconn.getOutputStream().write(body.getBytes());

                    //4响应码
                    int stat = httpconn.getResponseCode();
                    String msg = "";
                    if (stat == 200) {
                        br = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
                        msg = br.readLine();
                        Bundle b = new Bundle();
                        b.putString("msg", msg);
                        Message m = new Message();
                        m.setData(b);
                        handler.sendMessage(m);
                    } else {
                        msg = "请求失败";
                    }
                    Log.i("post",msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void present_videos(String msg_string){
        int status = 0;
        Log.i("present_videos",msg_string);
        try {
            JSONObject jsonObject = new JSONObject(msg_string);
            status = jsonObject.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(status == 200)
        {
            publicVideoLayout.removeAllViews();
            privateVideoLayout.removeAllViews();
            String public_videos;
            String private_videos;

            try {
                JSONObject jsonObject = new JSONObject(msg_string);
                public_videos = jsonObject.getString("public");
                private_videos = jsonObject.getString("private");
                JSONObject public_videos_object = new JSONObject(public_videos);
                JSONObject private_videos_object = new JSONObject(private_videos);
                int public_videos_num = public_videos_object.getInt("video_num");
                String public_video_name = public_videos_object.getString("video_name");
                String public_video_url = public_videos_object.getString("video_url");
                String public_video_owner = public_videos_object.getString("video_owner");
                String public_video_team = public_videos_object.getString("video_team");
                String public_video_location = public_videos_object.getString("video_location");
                JSONObject public_video_url_object = new JSONObject(public_video_url);
                JSONObject public_video_name_object = new JSONObject(public_video_name);
                JSONObject public_video_owner_object = new JSONObject(public_video_owner);
                JSONObject public_video_team_object = new JSONObject(public_video_team);
                JSONObject public_video_location_object = new JSONObject(public_video_location);


                int private_videos_num = private_videos_object.getInt("video_num");
                String private_video_name = private_videos_object.getString("video_name");
                String private_video_url = private_videos_object.getString("video_url");
                String private_video_owner = private_videos_object.getString("video_owner");
                String private_video_team = private_videos_object.getString("video_team");
                String private_video_location = private_videos_object.getString("video_location");
                JSONObject private_video_url_object = new JSONObject(private_video_url);
                JSONObject private_video_name_object = new JSONObject(private_video_name);
                JSONObject private_video_owner_object = new JSONObject(private_video_owner);
                JSONObject private_video_team_object = new JSONObject(private_video_team);
                JSONObject private_video_location_object = new JSONObject(private_video_location);
                for(int i = 0; i < public_videos_num; i++)
                {
                    Uri uri = Uri.parse("http://"+public_video_url_object.getString(String.valueOf(i))+"_0.flv");
                    CardView test_card = new CardView(this.getActivity());
                    test_card.setCardElevation(20);
                    LinearLayout testLinearLayout = new LinearLayout(this.getActivity());
                    testLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams x = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600);
                    x.weight = 0;
                    final VideoView videoView = new VideoView(this.getActivity());
                    videoView.setLayoutParams(x);
                    LinearLayout.LayoutParams c = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    c.weight = 1;
                    String txt = getContext().getString(R.string.game_name)+':'+public_video_name_object.getString(String.valueOf(i))+'\n';
                    txt = txt+getContext().getString(R.string.game_location)+':'+public_video_location_object.getString(String.valueOf(i))+'\n';
                    txt = txt+getContext().getString(R.string.game_owner)+':'+public_video_owner_object.getString(String.valueOf(i))+getContext().getString(R.string.team_name)+':'+public_video_team_object.getString(String.valueOf(i))+"\n";
                    txt = txt+getContext().getString(R.string.game_hilight)+':';
                    TextView test = new TextView(this.getActivity());
                    test.setLayoutParams(c);
                    test.setText(txt);
                    TextPaint paint = test.getPaint();
                    paint.setFakeBoldText(true);
                    test.setGravity(Gravity.CENTER);


//                MediaController mediaController = new MediaController(test_card.getContext());
//                videoView.setMediaController(mediaController);
                    videoView.setVideoURI(uri);
                    test_card.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600));
                    videoView.seekTo(100);
                    videoView.start();
                    videoView.pause();
                    testLinearLayout.addView(test);
                    testLinearLayout.addView(videoView);
                    test_card.addView(testLinearLayout);
                    test_card.setBackgroundColor(Color.TRANSPARENT);
                    testLinearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(videoView.isPlaying()) {
                                videoView.pause();
                            }
                            else {
                            videoView.start();
                            }
                        }
                    });
                    publicVideoLayout.addView(test_card);
                }
                if(public_videos_num == 0)
                {
                    LinearLayout.LayoutParams c = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    c.weight = 1;
                    String txt = getContext().getString(R.string.no_video_error);
                    TextView test = new TextView(this.getActivity());
                    test.setLayoutParams(c);
                    test.setText(txt);
                    TextPaint paint = test.getPaint();
                    paint.setFakeBoldText(true);
                    test.setGravity(Gravity.CENTER);
                    publicVideoLayout.addView(test);
                }
                for(int i = 0; i < private_videos_num; i++)
                {
                    Uri uri = Uri.parse("http://"+private_video_url_object.getString(String.valueOf(i))+"_0.flv");
                    CardView test_card = new CardView(this.getActivity());
                    test_card.setCardElevation(20);
                    LinearLayout testLinearLayout = new LinearLayout(this.getActivity());
                    testLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams x = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600);
                    x.weight = 0;
                    final VideoView videoView = new VideoView(this.getActivity());
                    videoView.setLayoutParams(x);
                    LinearLayout.LayoutParams c = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    c.weight = 1;
                    String txt = getContext().getString(R.string.game_name)+':'+private_video_name_object.getString(String.valueOf(i))+'\n';
                    txt = txt+getContext().getString(R.string.game_location)+':'+private_video_location_object.getString(String.valueOf(i))+'\n';
                    txt = txt+getContext().getString(R.string.game_owner)+':'+private_video_owner_object.getString(String.valueOf(i))+getContext().getString(R.string.team_name)+':'+private_video_team_object.getString(String.valueOf(i))+"\n";
                    txt = txt+getContext().getString(R.string.game_hilight)+':';
                    TextView test = new TextView(this.getActivity());
                    test.setLayoutParams(c);
                    test.setText(txt);
                    TextPaint paint = test.getPaint();
                    paint.setFakeBoldText(true);
                    test.setGravity(Gravity.CENTER);
//                MediaController mediaController = new MediaController(test_card.getContext());
//                videoView.setMediaController(mediaController);
                    videoView.setVideoURI(uri);
                    test_card.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600));
                    videoView.seekTo(100);
                    videoView.start();
                    videoView.pause();
                    testLinearLayout.addView(test);
                    testLinearLayout.addView(videoView);
                    test_card.addView(testLinearLayout);
                    test_card.setBackgroundColor(Color.TRANSPARENT);
                    testLinearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(videoView.isPlaying()) {
                                videoView.pause();
                            }
                            else {
                                videoView.start();
                            }
                        }
                    });
                    privateVideoLayout.addView(test_card);
                }
                if(private_videos_num == 0)
                {
                    LinearLayout.LayoutParams c = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    c.weight = 1;
                    String txt = getContext().getString(R.string.no_video_error);
                    TextView test = new TextView(this.getActivity());
                    test.setLayoutParams(c);
                    test.setText(txt);
                    TextPaint paint = test.getPaint();
                    paint.setFakeBoldText(true);
                    test.setGravity(Gravity.CENTER);
                    privateVideoLayout.addView(test);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(status == 301)
        {
            Toast.makeText(this.getActivity(),R.string.lack_parameter_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 202)
        {
            Toast.makeText(this.getActivity(),R.string.data_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 203)
        {
            Toast.makeText(this.getActivity(),R.string.no_video_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 300)
        {
            Toast.makeText(this.getActivity(),R.string.login_failed_error,Toast.LENGTH_SHORT).show();
            logout();
        }
    }
    private void logout() {
        SharedPreferences.Editor edit = sp.edit();
        //通过editor对象写入数据
        edit.putBoolean("login", false);
        edit.commit();
        ActivityCollector.finishAll();
        Intent intent=new Intent(this.getActivity(), LoginActivity.class);
        startActivity(intent);
    }
}

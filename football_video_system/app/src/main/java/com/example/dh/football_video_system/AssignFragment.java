package com.example.dh.football_video_system;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.Calendar;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;


public class AssignFragment extends Fragment {
    private LinearLayout orderLinearLayout;
    private Spinner weekdaySpinner;
    private static String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    private static String[] data_of_Spinner;
    private static String[] year_of_Spinner;
    private static String[] month_of_Spinner;
    private static String[] day_of_Spinner;
    private static final String[] Status = {"已经预约", "正在录制", "已完成"};
    private static final String[] Limit = {"私人", "仅队伍", "公共"};
    private SharedPreferences sp;
    private Button orderButton;
    private Button refreshButton;
    private String username;
    private String password;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.assignmet, null);

        orderLinearLayout = (LinearLayout) view.findViewById(R.id.orderLinearLayout);
        orderButton = (Button) view.findViewById(R.id.orderButton);
        refreshButton = (Button) view.findViewById(R.id.refreshButton);
        data_of_Spinner = new String[7];
        year_of_Spinner = new String[7];
        month_of_Spinner = new String[7];
        day_of_Spinner = new String[7];
        sp = this.getActivity().getSharedPreferences("User", Context.MODE_PRIVATE);
        username = sp.getString("username", "Null");
        password = sp.getString("password", "Null");

        for (int i = 0; i < 7; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            Log.i("weekday", String.valueOf(weekday));
            data_of_Spinner[weekday] = weekdays[weekday] + "   " + String.valueOf(month) + "/" + String.valueOf(day) + "/" + String.valueOf(year);
            year_of_Spinner[weekday] = String.valueOf(year);
            month_of_Spinner[weekday] = String.valueOf(month);
            day_of_Spinner[weekday] = String.valueOf(day);
        }
//        ImageView testImageView[];
//        testImageView = new ImageView[5];
//        for (int i = 0; i < 5; i++)
//        {
//            testImageView[i] = new ImageView(this.getActivity());
//            LinearLayout.LayoutParams x = new LinearLayout.LayoutParams(250, 250);
//            x.weight = 0;
//            testImageView[i].setLayoutParams(x);
//        }
//
//        testImageView[0].setImageDrawable(getResources().getDrawable(R.drawable.football_0));
//        testImageView[1].setImageDrawable(getResources().getDrawable(R.drawable.football_1));
//        testImageView[2].setImageDrawable(getResources().getDrawable(R.drawable.football_2));
//        testImageView[3].setImageDrawable(getResources().getDrawable(R.drawable.football_3));
//        testImageView[4].setImageDrawable(getResources().getDrawable(R.drawable.football_0));
//        for(int i = 0; i < 5; i++)
//        {
//            CardView test_card = new CardView(this.getActivity());
//            test_card.setCardElevation(20);
//            test_card.setContentPadding(10,10,10,10);
//            test_card.setPadding(10,10,10,10);
//            test_card.setRadius(10);
//            test_card.setPreventCornerOverlap(true);
//            LinearLayout.LayoutParams c = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            c.weight = 1;
//            test_card.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//            LinearLayout testLinearLayout = new LinearLayout(this.getActivity());
//            testLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//
//            String txt = "game name: test"+String.valueOf(i)+"\n";
//            txt = txt + "time: 8:30 -- 9:30\n";
//            txt = txt + "limit: " + Limit[0] + "\n";
//            txt = txt + "team: Liquid\n";
//            txt = txt + "owner: dinghao\n";
//            txt = txt + "phone: 18773318889\n";
//            txt = txt + "email: 56223640@qq.com\n";
//            TextView test = new TextView(this.getActivity());
//            test.setLayoutParams(c);
//            test.setText(txt);
//            test.setGravity(Gravity.CENTER);
//            testLinearLayout.addView(testImageView[i]);
//            testLinearLayout.addView(test);
//            test_card.addView(testLinearLayout);
//            test_card.setBackgroundColor(0xCCCCCCCC);
//            orderLinearLayout.addView(test_card);
//        }

        //SPinner
        weekdaySpinner = (Spinner) view.findViewById(R.id.weekday_spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.my_spinner, data_of_Spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptOrder();
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int position = weekdaySpinner.getSelectedItemPosition();
                    final String year = year_of_Spinner[position];
                    final String month = month_of_Spinner[position];
                    final String day = day_of_Spinner[position];
                    orderLinearLayout.removeAllViews();
                    check_shedule(year, month, day);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        weekdaySpinner.setAdapter(adapter);
        weekdaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //选取时候的操作
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    orderLinearLayout.removeAllViews();
                    final String year = year_of_Spinner[position];
                    final String month = month_of_Spinner[position];
                    final String day = day_of_Spinner[position];
                    Log.i("date",year+'/'+month+'/'+day);
                    check_shedule(year,month,day);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //没被选取时的操作
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }

    private void attemptOrder()
    {
        int position = weekdaySpinner.getSelectedItemPosition();
        final String year = year_of_Spinner[position];
        final String month = month_of_Spinner[position];
        final String day = day_of_Spinner[position];
        OrderDialog orderDialog = new OrderDialog(getActivity(), username, password, year, month, day);
        orderDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(final DialogInterface dialog) {
                try {
                    orderLinearLayout.removeAllViews();
                    check_shedule(year, month,day);
                } catch (Exception e) {
                        e.printStackTrace();
                }
            }
        });
        orderDialog.show();
    }


    private void check_shedule(final String year, final String month, final String day) throws Exception {
        final android.os.Handler handler = new android.os.Handler() {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle b = msg.getData();
                String msg_string = b.getString("msg");
                try {
                    refresh_table(msg_string);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                //urlConnection请求服务器，验证
                try {
                    BufferedReader br = null;
                    //1：url对象
                    URL url = new URL("http://166.111.68.66:28000/"+"check_schedule"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "year=" + URLEncoder.encode(year) + "&month=" + URLEncoder.encode(month)+"&day="
                            +URLEncoder.encode(day)+"&username="+URLEncoder.encode(username)+"&password="+URLEncoder.encode(password);

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
                        Log.i("post",msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void refresh_table(String msg) throws JSONException{
        Log.i("msg", msg);
        if(msg != null) {
            JSONObject jsonObject = new JSONObject(msg);
            int status = jsonObject.getInt("status");
            CardView orderCards[];
            TextView orderTexts[];
            LinearLayout orderHorizontalLinearLayout[];
            ImageView orderImageView[];
            orderImageView = new ImageView[4];
            for (int i = 0; i < 4; i++)
            {
                orderImageView[i] = new ImageView(this.getActivity());
                LinearLayout.LayoutParams x = new LinearLayout.LayoutParams(250, 250);
                x.weight = 0;
                orderImageView[i].setLayoutParams(x);
            }
            orderImageView[0].setImageDrawable(getResources().getDrawable(R.drawable.football_0));
            orderImageView[1].setImageDrawable(getResources().getDrawable(R.drawable.football_1));
            orderImageView[2].setImageDrawable(getResources().getDrawable(R.drawable.football_2));
            orderImageView[3].setImageDrawable(getResources().getDrawable(R.drawable.football_3));
            if(status == 200)
            {
                int order_num = jsonObject.getInt("order_num");
                orderTexts = new TextView[order_num];
                orderCards = new CardView[order_num];
                orderHorizontalLinearLayout = new LinearLayout[order_num];
                String order_name = jsonObject.getString("order_name");
                JSONObject order_name_object = new JSONObject(order_name);
                String order_start_hour = jsonObject.getString("order_start_hour");
                JSONObject order_start_hour_object = new JSONObject(order_start_hour);
                String order_start_minute = jsonObject.getString("order_start_minute");
                JSONObject order_start_minute_object = new JSONObject(order_start_minute);
                String order_end_hour = jsonObject.getString("order_end_hour");
                JSONObject order_end_hour_object = new JSONObject(order_end_hour);
                String order_end_minute = jsonObject.getString("order_end_minute");
                JSONObject order_end_minute_object = new JSONObject(order_end_minute);
                String order_location = jsonObject.getString("order_location");
                JSONObject order_location_object = new JSONObject(order_location);
                String order_limit = jsonObject.getString("order_limit");
                JSONObject order_limit_object = new JSONObject(order_limit);
                String order_team = jsonObject.getString("order_team");
                JSONObject order_team_object = new JSONObject(order_team );
                String order_owner = jsonObject.getString("order_owner");
                JSONObject order_owner_object = new JSONObject(order_owner);
                String order_owner_phone = jsonObject.getString("order_owner_phone");
                JSONObject order_owner_phone_object = new JSONObject(order_owner_phone);
                String order_owner_email = jsonObject.getString("order_owner_email");
                JSONObject order_owner_email_object = new JSONObject(order_owner_email);
                for (int i = 0; i < order_num; i++)
                {
                    String txt = getContext().getString(R.string.game_name)+':' + order_name_object.getString(String.valueOf(i))+"\n";
                    txt = txt + getContext().getString(R.string.time)+':' + order_start_hour_object.getString(String.valueOf(i))+":"+order_start_minute_object.getString(String.valueOf(i))+
                            " -- "+ order_end_hour_object.getString(String.valueOf(i))+":"+order_end_minute_object.getString(String.valueOf(i))+"\n";
                    txt = txt + getContext().getString(R.string.game_location)+':'+ order_location_object.getString(String.valueOf(i))+"\n";
                    txt = txt + getContext().getString(R.string.limit)+':'+ Limit[order_limit_object.getInt(String.valueOf(i))]+"\n";
                    txt = txt + getContext().getString(R.string.team_name)+':'+ order_team_object.getString(String.valueOf(i))+"\n";
                    txt = txt + getContext().getString(R.string.game_owner)+':'+order_owner_object.getString(String.valueOf(i))+"\n";
                    txt = txt + getContext().getString(R.string.prompt_phone)+':'+order_owner_phone_object.getString(String.valueOf(i))+"\n";
                    txt = txt + getContext().getString(R.string.prompt_email)+':'+order_owner_email_object.getString(String.valueOf(i))+"\n";
                    orderTexts[i] = new TextView(this.getActivity());
                    orderTexts[i].setText(txt);
                    TextPaint paint = orderTexts[i].getPaint();
                    paint.setFakeBoldText(true);
                    LinearLayout.LayoutParams c = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    c.weight = 1;
                    orderTexts[i].setLayoutParams(c);

                    orderHorizontalLinearLayout[i] = new LinearLayout(this.getActivity());
                    orderHorizontalLinearLayout[i].setOrientation(LinearLayout.HORIZONTAL);
                    orderHorizontalLinearLayout[i].addView(orderImageView[i%4]);
                    orderHorizontalLinearLayout[i].addView(orderTexts[i]);
                    orderCards[i] = new CardView(this.getActivity());
                    orderCards[i].setCardElevation(20);
                    orderCards[i].setContentPadding(10,10,10,10);
                    orderCards[i].setPadding(10,10,10,10);
                    orderCards[i].setRadius(10);
                    orderCards[i].setPreventCornerOverlap(true);
                    orderCards[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    orderCards[i].addView(orderHorizontalLinearLayout[i]);
                    orderCards[i].setBackgroundColor(Color.TRANSPARENT);
                    orderLinearLayout.addView(orderCards[i]);
                }
            }
            else if(status ==  301)
            {
                Toast.makeText(this.getActivity(),R.string.lack_parameter_error, Toast.LENGTH_SHORT).show();
            }
            else if(status == 300)
            {
                Toast.makeText(this.getActivity(),R.string.login_failed_error, Toast.LENGTH_SHORT).show();
                Logout();
            }
            else if(status == 201)
            {
                Toast.makeText(this.getActivity(),R.string.data_error, Toast.LENGTH_SHORT).show();
            }
            else if(status == 202)
            {
                Toast.makeText(this.getActivity(),R.string.no_order_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void Logout() {
        SharedPreferences.Editor edit = sp.edit();
        //通过editor对象写入数据
        edit.putBoolean("login", false);
        edit.commit();
        ActivityCollector.finishAll();
        Intent intent=new Intent(this.getActivity(), LoginActivity.class); startActivity(intent);
    }
}

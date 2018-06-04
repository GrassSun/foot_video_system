package com.example.dh.football_video_system;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OrderDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;

    private Button mButtonOder;
    private EditText mLocation;
    private EditText mGameName;

    private Spinner mSpinnerLimit;
    private Spinner mSpinnerStartHour;
    private Spinner mSpinnerStartMinute;
    private Spinner mSpinnerEndHour;
    private Spinner mSpinnerEndMinute;

    private Spinner mSpinnerTeam;

    public String username;
    public String password;
    private String year;
    private String month;
    private String day;
    private SharedPreferences sp ;

    private static String[] Limits = {"私人", "仅队伍", "公共"};
    private String[] Hour;
    private String[] Minute;
    private static String[] Teams = {"无"};
    public OrderDialog(Activity context, String _username, String _password,  String _year, String _month,String _day)
    {
        super(context);
        this.context = context;
        username = _username;
        password = _password;
        day = _day;
        year = _year;
        month = _month;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定布局
        this.setContentView(R.layout.order_dialog);
        mGameName = (EditText) findViewById(R.id.game_name);
        mLocation = (EditText) findViewById(R.id.game_location);
        mButtonOder = (Button)findViewById(R.id.order_button);
        mSpinnerLimit = (Spinner)findViewById(R.id.spinner_limit);
        mSpinnerTeam = (Spinner)findViewById(R.id.spinner_team);
        mSpinnerStartHour = (Spinner)findViewById(R.id.spinner_start_hour);
        mSpinnerStartMinute = (Spinner)findViewById(R.id.spinner_start_minute);
        mSpinnerEndHour = (Spinner)findViewById(R.id.spinner_end_hour);
        mSpinnerEndMinute = (Spinner)findViewById(R.id.spinner_end_minute);
        final ArrayAdapter<String> adapter_limit = new ArrayAdapter<String>(context, R.layout.my_spinner, Limits);
        adapter_limit.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpinnerLimit.setAdapter(adapter_limit);
        final ArrayAdapter<String> adapter_team = new ArrayAdapter<String>(context, R.layout.my_spinner, Teams);
        adapter_team.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpinnerTeam.setAdapter(adapter_team);
        Hour = new String[10];
        Minute = new String[60];
        for(int i = 0; i < 10; i++)
        {
           Hour[i] = String.valueOf(i+8);
        }
        final ArrayAdapter<String> adapter_hour = new ArrayAdapter<String>(context, R.layout.my_spinner, Hour);
        adapter_hour.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpinnerStartHour.setAdapter(adapter_hour);
        mSpinnerEndHour.setAdapter(adapter_hour);
        for (int i = 0; i < 60; i++)
        {
            Minute[i] = String.valueOf(i);
        }
        final ArrayAdapter<String> adapter_minute = new ArrayAdapter<String>(context, R.layout.my_spinner, Minute);
        adapter_minute.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpinnerStartMinute.setAdapter(adapter_minute);
        mSpinnerEndMinute.setAdapter(adapter_minute);
        /*
         * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置, 可以直接调用getWindow(),表示获得这个Activity的Window
         * 对象,这样这可以以同样的方式改变这个Activity的属性.
         */
        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        // p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.9); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);
        sp = this.getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        // 根据id在布局中找到控件对象
        mButtonOder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                attempt_order();
            }
        });
        get_User_Info(username);
        this.setCancelable(true);
    }

    private void attempt_order()
    {
        int limit = mSpinnerLimit.getSelectedItemPosition();
        View focusView = null;
        boolean cancel = false;
        String team = mSpinnerTeam.getSelectedItem().toString();
        String game_name = mGameName.getText().toString();
        String location = mLocation.getText().toString();
        String start_hour = mSpinnerStartHour.getSelectedItem().toString();
        String start_minute = mSpinnerStartMinute.getSelectedItem().toString();
        String end_hour = mSpinnerEndHour.getSelectedItem().toString();
        String end_minute = mSpinnerEndMinute.getSelectedItem().toString();
        int start_time = Integer.parseInt(start_hour)*60+Integer.parseInt(start_minute);
        int end_time = Integer.parseInt(end_hour)*60+Integer.parseInt(end_minute);
        if(TextUtils.isEmpty(game_name)) {
            mGameName.setError(getContext().getString(R.string.error_field_required));
            focusView = mGameName;
            cancel = true;
        }
        else if(TextUtils.isEmpty(location)) {
            mLocation.setError(getContext().getString(R.string.error_field_required));
            focusView = mLocation;
            cancel = true;
        }
        else if(start_time >= end_time)
        {
            Toast.makeText(context,R.string.error_order_time, Toast.LENGTH_SHORT).show();
            focusView = mSpinnerStartHour;
            cancel = true;
        }
        else {
            Log.i("query data:",game_name+'/'+location+'/'+year+'/'+month+'/'+day+'/'+start_hour+'/'+start_minute+'/'+end_hour+'/'+end_minute+'/'+String.valueOf(limit)+'/'+team);
            handle_record(game_name, location, year, month, day, start_hour, start_minute, end_hour, end_minute,username, password, String.valueOf(limit), team);
        }
        if(cancel) {
            focusView.requestFocus();
        }
    }

    private void handle_record(final String game_name, final String location,final String year, final String month, final String day, final String start_hour, final String start_minute, final String end_hour, final String end_minute, final String username, final String password,final String limit, final String team)
    {
        final android.os.Handler handler = new android.os.Handler() {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle b = msg.getData();
                String msg_string = b.getString("msg");
                int status = 0;
                try {
                    JSONObject jsonObject = new JSONObject(msg_string);
                    status = jsonObject.getInt("status");
                    handle_status(status);
                    handle_status(status);
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
                    URL url = new URL("http://166.111.68.66:28000/"+"order_record"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "year="+ URLEncoder.encode(year)+ "&month=" + URLEncoder.encode(month) + "&day=" + URLEncoder.encode(day)+"&username="
                            +URLEncoder.encode(username)+"&password="+URLEncoder.encode(password)+"&limit="
                            +URLEncoder.encode(limit)+"&team="+URLEncoder.encode(team)+"&game_name="
                            +URLEncoder.encode(game_name)+"&location=" +URLEncoder.encode(location)+"&start_hour="
                            +URLEncoder.encode(start_hour)+"&start_minute=" +URLEncoder.encode(start_minute)+"&end_hour="
                            +URLEncoder.encode(end_hour)+"&end_minute=" +URLEncoder.encode(end_minute);

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

    public void handle_status(int status){
        if(status == 200)
        {
            Toast.makeText(context,R.string.order_succeeded, Toast.LENGTH_SHORT).show();
            dismiss();
        }
        else if(status == 201)
        {
            Toast.makeText(context,R.string.order_conflict_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 202)
        {
            Toast.makeText(context,R.string.data_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 203)
        {
            Toast.makeText(context,R.string.order_right_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 300)
        {
            Toast.makeText(context,R.string.login_failed,Toast.LENGTH_SHORT).show();
            logout();
        }
        else if(status == 301)
        {
            Toast.makeText(context,R.string.lack_parameter_error,Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context,R.string.error_unkown, Toast.LENGTH_SHORT).show();
        }

    }

    public void get_User_Info(final String username)
    {
        final android.os.Handler handler = new android.os.Handler() {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle b = msg.getData();
                String msg_string = b.getString("msg");
                handle_msg(msg_string);
            }

        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                //urlConnection请求服务器，验证
                try {
                    BufferedReader br = null;
                    //1：url对象
                    URL url = new URL("http://166.111.68.66:28000/"+"User_Private_Info"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "username=" +URLEncoder.encode(username)+"&password="+URLEncoder.encode(password);
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

    private void handle_msg(String msg_string){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(msg_string);
            int status = jsonObject.getInt("status");
            if(status == 200)
            {
                Log.i("status","200");
                String team = jsonObject.getString("team");
                jsonObject = new JSONObject(team);
                int team_num = jsonObject.getInt("team_num");
                String team_name = jsonObject.getString("team_name");
                JSONObject team_name_object = new JSONObject(team_name);
                Teams = new String[team_num];
                for (int i = 0; i < team_num; i++)
                {
                    Teams[i] = new String(team_name_object.getString(String.valueOf(i)));
                }
                final ArrayAdapter<String> adapter_team = new ArrayAdapter<String>(context, R.layout.my_spinner, Teams);
                adapter_team.setDropDownViewResource(android.R.layout.simple_spinner_item);
                mSpinnerTeam.setAdapter(adapter_team);

            }
            else if(status == 201)
            {
                Toast.makeText(context,R.string.data_error, Toast.LENGTH_SHORT).show();
            }
            else if(status == 301)
            {
                Toast.makeText(context,R.string.lack_parameter_error, Toast.LENGTH_SHORT).show();
            }
            else if(status == 300)
            {
                Toast.makeText(context,R.string.login_failed, Toast.LENGTH_SHORT).show();
                logout();
            }
            else
            {
                Toast.makeText(context,R.string.error_unkown, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void logout() {
        SharedPreferences.Editor edit = sp.edit();
        //通过editor对象写入数据
        edit.putBoolean("login", false);
        edit.commit();
        ActivityCollector.finishAll();
        Intent intent=new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }
}

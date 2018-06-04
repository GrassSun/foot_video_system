package com.example.dh.football_video_system;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
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

public class OrderListDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;
    private TableLayout tableLayout;
    private TableRow[] orderTableRow;
    private TextView[] nameTextView;
    private TextView[] idTextView;
    private TextView[] dateTextView;
    private TextView[] locationTextView;
    private TextView[] teamTextView;
    private Button[] deleteButton;
    private String username;
    private String password;
    private SharedPreferences sp ;
    public OrderListDialog(Activity context, String _username, String _password)
    {
        super(context);
        this.context = context;
        username = _username;
        password = _password;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定布局
        this.setContentView(R.layout.order_list_dialog);
        /*
         * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置, 可以直接调用getWindow(),表示获得这个Activity的Window
         * 对象,这样这可以以同样的方式改变这个Activity的属性.
         */
        tableLayout = (TableLayout)findViewById(R.id.order_list_table);
        Window dialogWindow = this.getWindow();
        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        // p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);
        // 根据id在布局中找到控件对象
        this.setCancelable(true);
        sp = this.getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        get_User_Info(username);
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
                String order = jsonObject.getString("order");
                jsonObject = new JSONObject(order);
                int order_num = jsonObject.getInt("order_num");
                String order_name = jsonObject.getString("order_name");
                String order_id = jsonObject.getString("order_id");
                String order_year = jsonObject.getString("order_year");
                String order_month = jsonObject.getString("order_month");
                String order_day = jsonObject.getString("order_day");
                String order_location = jsonObject.getString("order_location");
                String order_start_hour = jsonObject.getString("order_start_hour");
                String order_start_minute = jsonObject.getString("order_start_minute");
                String order_team = jsonObject.getString("order_team");
                JSONObject order_name_object = new JSONObject(order_name);
                JSONObject order_id_object = new JSONObject(order_id);
                JSONObject order_year_object = new JSONObject(order_year);
                JSONObject order_month_object = new JSONObject(order_month);
                JSONObject order_day_object = new JSONObject(order_day);
                JSONObject order_location_object = new JSONObject(order_location);
                JSONObject order_start_hour_object = new JSONObject(order_start_hour);
                JSONObject order_start_minute_object = new JSONObject(order_start_minute);
                JSONObject order_team_object = new JSONObject(order_team);
                orderTableRow = new TableRow[order_num];
                nameTextView = new TextView[order_num];
                idTextView = new TextView[order_num];
                locationTextView = new TextView[order_num];
                teamTextView = new TextView[order_num];
                dateTextView = new TextView[order_num];
                deleteButton = new Button[order_num];
                for (int i = 0; i < order_num; i++)
                {
                    orderTableRow[i] = new TableRow(this.getContext());

                    String date = order_year_object.getString(String.valueOf(i))+'/'+order_month_object.getString(String.valueOf(i))
                            +'/'+order_day_object.getString(String.valueOf(i))+' '+order_start_hour_object.getString(String.valueOf(i))
                            +':'+order_start_minute_object.getString(String.valueOf(i));
                    dateTextView[i] = new TextView(this.getContext());
                    dateTextView[i].setText(date);
                    dateTextView[i].setTextSize(20);
                    dateTextView[i].setGravity(Gravity.CENTER);
                    orderTableRow[i].addView(dateTextView[i]);

                    String id = order_id_object.getString(String.valueOf(i));
                    idTextView[i] = new TextView(this.getContext());
                    idTextView[i].setText(id);
                    idTextView[i].setTextSize(20);
                    idTextView[i].setGravity(Gravity.CENTER);
                    orderTableRow[i].addView(idTextView[i]);

                    String name = order_name_object.getString(String.valueOf(i));
                    nameTextView[i] = new TextView(this.getContext());
                    nameTextView[i].setText(name);
                    nameTextView[i].setTextSize(20);
                    nameTextView[i].setGravity(Gravity.CENTER);
                    orderTableRow[i].addView(nameTextView[i]);

                    String location = order_location_object.getString(String.valueOf(i));
                    locationTextView[i] = new TextView(this.getContext());
                    locationTextView[i].setText(location);
                    locationTextView[i].setTextSize(20);
                    locationTextView[i].setGravity(Gravity.CENTER);
                    orderTableRow[i].addView(locationTextView[i]);

                    String team = order_team_object.getString(String.valueOf(i));
                    teamTextView[i] = new TextView(this.getContext());
                    teamTextView[i].setText(location);
                    teamTextView[i].setTextSize(20);
                    teamTextView[i].setGravity(Gravity.CENTER);
                    orderTableRow[i].addView(teamTextView[i]);

                    deleteButton[i] = new Button(this.getContext());
                    deleteButton[i].setText(getContext().getString(R.string.delete_order));
                    final int x = i;
                    deleteButton[i].setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            attemp_remove_order(x);
                        }
                    });
                    orderTableRow[i].addView(deleteButton[i]);

                    tableLayout.addView(orderTableRow[i]);
                }
            }
            else if(status == 201)
            {
                Toast.makeText(context,R.string.not_exist_error, Toast.LENGTH_SHORT).show();
            }
            else if(status == 300)
            {
                Toast.makeText(context,R.string.login_failed, Toast.LENGTH_SHORT).show();
                logout();
            }
            else if(status == 301)
            {
                Toast.makeText(context,R.string.lack_parameter_error, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(context,R.string.error_unkown, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void attemp_remove_order(int x)
    {
        String id = idTextView[x].getText().toString();
        remove_order(username, password, id);
    }

    public void remove_order(final String username, final String password, final String id)
    {
        final android.os.Handler handler = new android.os.Handler() {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle b = msg.getData();
                String msg_string = b.getString("msg");
                handle_status(msg_string);
            }

        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                //urlConnection请求服务器，验证
                try {
                    BufferedReader br = null;
                    //1：url对象
                    URL url = new URL("http://166.111.68.66:28000/"+"cancel_record"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "username=" +URLEncoder.encode(username)+"&password="+URLEncoder.encode(password)+"&order_id="+URLEncoder.encode(id);
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

    private void handle_status(String msg_string){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(msg_string);
            int status = jsonObject.getInt("status");
            if(status == 200)
            {
                Toast.makeText(context,R.string.cancel_succeeded, Toast.LENGTH_SHORT).show();
                dismiss();
            }
            else if(status == 201)
            {
                Toast.makeText(context,R.string.not_exist_error, Toast.LENGTH_SHORT).show();
            }
            else if(status == 300)
            {
                Toast.makeText(context,R.string.login_failed, Toast.LENGTH_SHORT).show();
                logout();
            }
            else if(status == 301)
            {
                Toast.makeText(context,R.string.lack_parameter_error, Toast.LENGTH_SHORT).show();
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

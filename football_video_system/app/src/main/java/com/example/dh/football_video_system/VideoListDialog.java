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

public class VideoListDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;
    private TableLayout tableLayout;
    private TableRow[] videoTableRow;
    private TextView[] nameTextView;
    private TextView[] dateTextView;
    private TextView[] urlTextView;
//    private Button[] operationButton;
    private String username;
    private String password;
    private SharedPreferences sp ;
    public VideoListDialog(Activity context, String _username, String _password)
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
        this.setContentView(R.layout.video_list_dialog);
        /*
         * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置, 可以直接调用getWindow(),表示获得这个Activity的Window
         * 对象,这样这可以以同样的方式改变这个Activity的属性.
         */
        tableLayout = (TableLayout)findViewById(R.id.video_list_table);
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
                String video = jsonObject.getString("video");
                jsonObject = new JSONObject(video);
                int video_num = jsonObject.getInt("video_num");
                String video_name = jsonObject.getString("video_name");
                String video_year = jsonObject.getString("video_year");
                String video_month = jsonObject.getString("video_month");
                String video_day = jsonObject.getString("video_day");
                String video_url = jsonObject.getString("video_url");
                JSONObject video_name_object = new JSONObject(video_name);
                JSONObject video_year_object = new JSONObject(video_year);
                JSONObject video_month_object = new JSONObject(video_month);
                JSONObject video_day_object = new JSONObject(video_day);
                JSONObject video_url_object = new JSONObject(video_url);
                videoTableRow = new TableRow[video_num];
                nameTextView = new TextView[video_num];
                dateTextView = new TextView[video_num];
                urlTextView = new TextView[video_num];
               // operationButton = new Button[video_num];
                for (int i = 0; i < video_num; i++)
                {
                    videoTableRow[i] = new TableRow(this.getContext());
                    String name = video_name_object.getString(String.valueOf(i));
                    String date = video_year_object.getString(String.valueOf(i))+'/'+video_month_object.getString(String.valueOf(i))+'/'+video_day_object.getString(String.valueOf(i));
                    String url = video_url_object.getString(String.valueOf(i));

                    nameTextView[i] = new TextView(this.getContext());
                    nameTextView[i].setText(name+"  ");
                    nameTextView[i].setTextSize(20);
                    nameTextView[i].setGravity(Gravity.CENTER);
                    videoTableRow[i].addView(nameTextView[i]);

                    dateTextView[i] = new TextView(this.getContext());
                    dateTextView[i].setText(date+"  ");
                    dateTextView[i].setTextSize(20);
                    dateTextView[i].setGravity(Gravity.CENTER);
                    videoTableRow[i].addView(dateTextView[i]);

                    urlTextView[i] = new TextView(this.getContext());
                    urlTextView[i].setText(url+"  ");
                    urlTextView[i].setTextSize(20);
                    urlTextView[i].setGravity(Gravity.CENTER);
                    videoTableRow[i].addView(urlTextView[i]);

//                    operationButton[i] = new Button(this.getContext());
//                    operationButton[i].setText(getContext().getString(R.string.edit_video));
//                    final int x = i;
//                    operationButton[i].setOnClickListener(new View.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(View v) {
//                            attemp_edit_video(x);
//                        }
//                    });

                    tableLayout.addView(videoTableRow[i]);
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

//    private void attemp_edit_video(final int x)
//    {
//        String game_name = nameTextView[x].getText().toString();
//        String game_location = dateTextView[x].getText().toString();
//        String game_url = urlTextView[x].getText().toString();
//        String username = this.username;
//        String password = this.password;
//        //TODO start the activity to edit video
//    }

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

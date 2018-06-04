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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AddTeammateDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;

    private Button mButtonAddTeammate;
    String username;
    String password;
    String team;
    private EditText mNewUsername;
    private TextView mTeam;
    private SharedPreferences sp ;

    public AddTeammateDialog(Activity context, String _username, String _password, String _team)
    {
        super(context);
        username = _username;
        password = _password;
        team = _team;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定布局
        this.setContentView(R.layout.add_teammate_dialog);
        mButtonAddTeammate = (Button)findViewById(R.id.add_teammate);
        mNewUsername = (EditText)findViewById(R.id.new_teammate_username);
        mTeam= (TextView) findViewById(R.id.team);
        mTeam.setText(team);
        Window dialogWindow = this.getWindow();
        sp = this.getContext().getSharedPreferences("User", Context.MODE_PRIVATE);

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        // p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);
        // 根据id在布局中找到控件对象
        mButtonAddTeammate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                attempt_add_teammate();

            }
        });
        this.setCancelable(true);
    }

    private void attempt_add_teammate() {
        View focusView = null;
        boolean cancel = false;
        String new_teammate_username = mNewUsername.getText().toString();

        if (TextUtils.isEmpty(new_teammate_username)) {
            mNewUsername.setError(context.getString(R.string.error_field_required));
            focusView = mNewUsername;
            cancel = true;
        } else {
            add_teammate(username, password, team, new_teammate_username);
        }
        if (cancel)
        {
            focusView.requestFocus();
        }
    }

    public void add_teammate(final String username, final String password, final String team, final String new_teammate_username)
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
                    URL url = new URL("http://166.111.68.66:28000/"+"Add_Teammates"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "username=" +URLEncoder.encode(username)+"&password="+URLEncoder.encode(password)
                            +"&team="+URLEncoder.encode(team)+"&new_member="+URLEncoder.encode(new_teammate_username);

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
            Toast.makeText(context,R.string.teammate_added, Toast.LENGTH_SHORT).show();
            dismiss();
        }
        else if(status == 300)
        {
            Toast.makeText(context,R.string.login_failed_error,Toast.LENGTH_SHORT).show();
            dismiss();
            logout();
        }
        else if(status == 301)
        {
            Toast.makeText(context,R.string.lack_parameter_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 201)
        {
            Toast.makeText(context,R.string.not_exist_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 202)
        {
            Toast.makeText(context,R.string.priorit_error,Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context,R.string.error_unkown, Toast.LENGTH_SHORT).show();
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

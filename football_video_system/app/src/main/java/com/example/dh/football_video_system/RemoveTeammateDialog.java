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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RemoveTeammateDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;

    private Button mButtonDelete;
    private TextView mPhone;
    private TextView mEmail;
    private String username;
    private String password;
    private Spinner mTeammate;
    private String team;
    private String team_members[] = {"无"};
    private String team_phones[] = {"无"};
    private String team_mails[] = {"无"};
    private SharedPreferences sp ;

    public RemoveTeammateDialog(Activity context, String _username, String _password, String _team)
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
        this.setContentView(R.layout.remove_teammate_dialog);
        mButtonDelete = (Button)findViewById(R.id.delete_member);
        mTeammate = (Spinner) findViewById(R.id.team_member);
        mPhone = (TextView)findViewById(R.id.phone);
        mEmail = (TextView)findViewById(R.id.email);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.my_spinner, team_members);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mTeammate.setAdapter(adapter);
        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        // p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);
        // 根据id在布局中找到控件对象
        mButtonDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String member = mTeammate.getSelectedItem().toString();
                delete_member(username, password, team, member);
            }
        });
        get_Team_Info(team);
        sp = this.getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        this.setCancelable(true);
    }

    public void get_Team_Info(final String team)
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
                    URL url = new URL("http://166.111.68.66:28000/"+"Team_Info"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "team=" +URLEncoder.encode(team)+"&username=" +URLEncoder.encode(username)+"&password="+URLEncoder.encode(password);
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
            Log.i("member", msg_string);
            jsonObject = new JSONObject(msg_string);
            int status = jsonObject.getInt("status");
            if(status == 200)
            {
                String member = jsonObject.getString("member");
                jsonObject = new JSONObject(member);
                int member_num = jsonObject.getInt("member_num");
                String member_name = jsonObject.getString("member_name");
                String member_phone = jsonObject.getString("member_phone");
                String member_email = jsonObject.getString("member_email");
                team_members = new String[member_num];
                team_mails = new String[member_num];
                team_phones = new String[member_num];
                JSONObject member_name_object = new JSONObject(member_name);
                JSONObject member_phone_object = new JSONObject(member_phone);
                JSONObject member_email_object = new JSONObject(member_email);
                for (int i = 0; i < member_num; i++)
                {
                    team_members[i] = new String(member_name_object.getString(String.valueOf(i)));
                    team_mails[i] = new String(member_email_object.getString(String.valueOf(i)));
                    team_phones[i] = new String(member_phone_object.getString(String.valueOf(i)));
                }
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.my_spinner, team_members);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                mTeammate.setAdapter(adapter);
                mTeammate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    //选取时候的操作
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        mPhone.setText("phone:"+team_phones[position]);
                        mEmail.setText("email:"+team_mails[position]);
                    }
                    //没被选取时的操作
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
            else if(status == 201)
            {
                Toast.makeText(context,R.string.not_exist_error,Toast.LENGTH_SHORT).show();
            }
            else if(status == 202)
            {
                Toast.makeText(context,R.string.priorit_error,Toast.LENGTH_SHORT).show();
            }
            else if(status == 301)
            {
                Toast.makeText(context,R.string.lack_parameter_error,Toast.LENGTH_SHORT).show();
            }
            else if(status == 300)
            {
                Toast.makeText(context,R.string.login_failed,Toast.LENGTH_SHORT).show();
                logout();
            }
            else
            {
                Toast.makeText(context,R.string.error_unkown,Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }


    public void delete_member(final String username, final String password, final String team, final String member)
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
                    URL url = new URL("http://166.111.68.66:28000/"+"Remove_Teammates"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "username=" +URLEncoder.encode(username)+"&password="+URLEncoder.encode(password)
                            +"&team="+URLEncoder.encode(team)+"&remove_member="+URLEncoder.encode(member);

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
            Toast.makeText(context,R.string.teammate_deleted, Toast.LENGTH_SHORT).show();
            dismiss();
        }
        else if(status == 300)
        {
            Toast.makeText(context,R.string.login_failed,Toast.LENGTH_SHORT).show();
            logout();
        }
        else if(status == 201)
        {
            Toast.makeText(context,R.string.not_exist_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 301)
        {
            Toast.makeText(context,R.string.lack_parameter_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 202)
        {
            Toast.makeText(context,R.string.priorit_error,Toast.LENGTH_SHORT).show();
        }
        else if(status == 203)
        {
            Toast.makeText(context,R.string.captain_deleted_error,Toast.LENGTH_SHORT).show();
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

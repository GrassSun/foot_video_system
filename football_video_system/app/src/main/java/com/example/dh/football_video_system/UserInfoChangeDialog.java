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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UserInfoChangeDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;

    private Button mButtonModify;
    String username;
    String password;
    private EditText mPhone;
    private EditText mEmail;
    private SharedPreferences sp ;

    public UserInfoChangeDialog(Activity context, String _username, String _password)
    {
        super(context);
        username = _username;
        password = _password;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定布局
        this.setContentView(R.layout.user_info_change_dialog);
        mButtonModify = (Button)findViewById(R.id.modifyUserInfo);
        mPhone = (EditText)findViewById(R.id.phone);
        mEmail = (EditText)findViewById(R.id.email);
        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        // p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);
        // 根据id在布局中找到控件对象
        mButtonModify.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                attempt_change();

            }
        });
        sp = this.getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        this.setCancelable(true);
    }

    public void attempt_change()
    {

        View focusView = null;
        boolean cancel = false;
        String phone = mPhone.getText().toString();
        String email = mEmail.getText().toString();

        if(TextUtils.isEmpty(phone))
        {
            mPhone.setError(getContext().getString(R.string.error_field_required));
            focusView = mPhone;
            cancel = true;
        }
        else if(TextUtils.isEmpty(email))
        {
            mEmail.setError(getContext().getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        }
        else if(!email.contains("@"))
        {
            mEmail.setError(getContext().getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }
        else
        {
            user_info_change(username, password, phone, email);
        }
        if (cancel)
        {
            focusView.requestFocus();
        }

    }

    public void user_info_change(final String username, final String password, final String phone, final String email)
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
                    URL url = new URL("http://166.111.68.66:28000/"+"User_Info_Change"+"/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "username=" +URLEncoder.encode(username)+"&password="+URLEncoder.encode(password)
                            +"&phone="+URLEncoder.encode(phone)+"&email="+URLEncoder.encode(email);

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
            Toast.makeText(context,R.string.user_info_changed, Toast.LENGTH_SHORT).show();
            dismiss();
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

    private void logout() {
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("login", true);
        edit.commit();
        ActivityCollector.finishAll();
        Intent intent=new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }
}

package com.example.dh.football_video_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.graphics.Color.GREEN;

public class RegisterActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;
    private EditText mConfirm;
    private EditText mPhone;
    private EditText mEmail;
    private Button mRegisterButton;
    private Button mBackButton;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mUsername = (EditText)findViewById(R.id.username);
        mPassword = (EditText)findViewById(R.id.password);
        mConfirm = (EditText)findViewById(R.id.confirm);
        mPhone = (EditText)findViewById(R.id.phone);
        mEmail = (EditText)findViewById(R.id.email);
        mRegisterButton = (Button)findViewById(R.id.register);
        mBackButton = (Button)findViewById(R.id.back_to_login);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterActivity.this, LoginActivity.class); startActivity(intent);
            }
        });
    }

    private void attemptRegister()
    {
        mUsername.setError(null);
        mPassword.setError(null);
        // Store values at the time of the login attempt.
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        String confirm = mConfirm.getText().toString();
        String email = mEmail.getText().toString();
        String phone = mPhone.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_field_required));
            focusView = mPassword;
            cancel = true;
        }
        else if (!isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        }
        else if(!isEmailValid(email))
        {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }

        if(!confirm.equals(password))
        {
            mConfirm.setError(getString(R.string.error_wrong_confirm));
            focusView = mConfirm;
            cancel = true;
        }

        if(TextUtils.isEmpty(email))
        {
            mPhone.setError(getString(R.string.error_field_required));
            focusView = mPhone;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            register(username, password,confirm,phone,email);
        }
    }

    private boolean isPasswordValid(String password) {
        return (password.length() > 7 && password.length() < 17);
    }

    private boolean isEmailValid(String email)
    {
        return email.contains("@");
    }

    private void register(final String username, final String password,final String confirm, final String phone,final String email)
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
                    URL url = new URL("http://166.111.68.66:28000/register/");

                    //2;url.openconnection
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();

                    //3设置请求参数
                    httpconn.setRequestMethod("POST");
                    httpconn.setConnectTimeout(10 * 1000);
                    //请求头的信息
                    String body = "username=" + username + "&password=" + password +"&email="+email+"&confirm="+confirm+"&phone="+phone;

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

    private void handle_status(int status)
    {
        if(status == 200)
        {
            Toast.makeText(this,R.string.register_suceeded, Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(RegisterActivity.this, LoginActivity.class); startActivity(intent);
        }
        else if(status == 201)
        {
            Toast.makeText(this,R.string.error_existed_username,Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this,R.string.error_unkown,Toast.LENGTH_SHORT).show();
        }
    }
}

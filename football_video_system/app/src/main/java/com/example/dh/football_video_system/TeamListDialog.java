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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TeamListDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;
    private TableLayout tableLayout;
    private TableRow[] teamTableRow;
    private TextView[] nameTextView;
    private TextView[] captainTextView;
    private LinearLayout[] buttonLayout;
    private Button[] addButton;
    private Button[] deleteButton;
    private String username;
    private String password;
    private SharedPreferences sp ;
    public TeamListDialog(Activity context, String _username, String _password)
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
        this.setContentView(R.layout.team_list_dialog);
        /*
         * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置, 可以直接调用getWindow(),表示获得这个Activity的Window
         * 对象,这样这可以以同样的方式改变这个Activity的属性.
         */
        tableLayout = (TableLayout)findViewById(R.id.team_list_table);
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
                String team = jsonObject.getString("team");
                jsonObject = new JSONObject(team);
                int team_num = jsonObject.getInt("team_num");
                String team_name = jsonObject.getString("team_name");
                String team_captain = jsonObject.getString("team_captain");
                JSONObject team_name_object = new JSONObject(team_name);
                JSONObject team_captain_object = new JSONObject(team_captain);
                teamTableRow = new TableRow[team_num];
                nameTextView = new TextView[team_num];
                captainTextView = new TextView[team_num];
                buttonLayout = new LinearLayout[team_num];
                addButton = new Button[team_num];
                deleteButton = new Button[team_num];
                for (int i = 0; i < team_num; i++)
                {
                    teamTableRow[i] = new TableRow(this.getContext());
                    String name = team_name_object.getString(String.valueOf(i));
                    String captain = team_captain_object.getString(String.valueOf(i));

                    nameTextView[i] = new TextView(this.getContext());
                    nameTextView[i].setText(name);
                    nameTextView[i].setTextSize(20);
                    nameTextView[i].setGravity(Gravity.CENTER);
                    teamTableRow[i].addView(nameTextView[i]);

                    captainTextView[i] = new TextView(this.getContext());
                    captainTextView[i].setText(captain);
                    captainTextView[i].setTextSize(20);
                    captainTextView[i].setGravity(Gravity.CENTER);
                    teamTableRow[i].addView(captainTextView[i]);

                    buttonLayout[i] = new LinearLayout(this.getContext());
                    buttonLayout[i].setOrientation(LinearLayout.HORIZONTAL);
                    addButton[i] = new Button(this.getContext());
                    addButton[i].setText(getContext().getString(R.string.add_teammate));
                    deleteButton[i] = new Button(this.getContext());
                    deleteButton[i].setText(getContext().getString(R.string.delete_teammate));
                    buttonLayout[i].addView(addButton[i]);
                    buttonLayout[i].addView(deleteButton[i]);
                    teamTableRow[i].addView(buttonLayout[i]);
                    tableLayout.addView(teamTableRow[i]);
                    final int x = i;
                    addButton[i].setOnClickListener(new View.OnClickListener()
                     {
                        @Override
                        public void onClick(View v) {
                        attemp_add_teammate(x);
                        }
                    });
                    deleteButton[i].setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            attemp_remove_teammate(x);
                        }
                    });
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

    private void attemp_add_teammate(int x)
    {
        String team = nameTextView[x].getText().toString();
        String captain = captainTextView[x].getText().toString();
        if (!captain.equals(username))
        {
            Toast.makeText(context,R.string.priorit_error, Toast.LENGTH_SHORT).show();
        }
        else
        {
            AddTeammateDialog addTeammateDialog = new AddTeammateDialog(context, username, password, team);
            addTeammateDialog.show();
        }
    }

    private void attemp_remove_teammate(int x)
    {
        String team = nameTextView[x].getText().toString();
        String captain = captainTextView[x].getText().toString();
        if (!captain.equals( username))
        {
            Toast.makeText(context,R.string.priorit_error, Toast.LENGTH_SHORT).show();
        }
        else
        {
            RemoveTeammateDialog removeTeammateDialog = new RemoveTeammateDialog(context, username, password, team);
            removeTeammateDialog.show();
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

package com.example.dh.football_video_system;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
public class UserFragment extends Fragment {

    TextView mUsername;
    Button modifyUserInfo;
    Button modifyPassword;
    Button createTeam;
    Button modifyTeamMember;
    Button logout;
    Button checkMyVideo;
    Button checkMyOrder;
    String username;
    String password;
    private SharedPreferences sp ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.user, null);
        sp = this.getActivity().getSharedPreferences("User", Context.MODE_PRIVATE);
        username = sp.getString("username","Null");
        password = sp.getString("password", "Null");
        mUsername = (TextView)view.findViewById(R.id.username);
        Drawable drawable_username = getResources().getDrawable(R.drawable.football_3);
        drawable_username.setBounds(0, 0, 200, 200);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        mUsername.setCompoundDrawables(drawable_username, null, null, null);
        mUsername.setText(username);
        modifyUserInfo = (Button)view.findViewById(R.id.modifyUserInfo);
        Drawable drawable_user_info = getResources().getDrawable(R.drawable.modify_user_info);
        drawable_user_info.setBounds(0, 0, 100, 100);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        modifyUserInfo.setCompoundDrawables(null, drawable_user_info, null, null);

        modifyPassword = (Button)view.findViewById(R.id.modifyPassword);
        Drawable drawable_user_password = getResources().getDrawable(R.drawable.modify_password);
        drawable_user_password.setBounds(0, 0, 100, 100);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        modifyPassword.setCompoundDrawables(null, drawable_user_password, null, null);

        createTeam = (Button)view.findViewById(R.id.creatTeam);
        Drawable drawable_create_team = getResources().getDrawable(R.drawable.create_team);
        drawable_create_team.setBounds(0, 0, 100, 100);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        createTeam.setCompoundDrawables(null, drawable_create_team, null, null);

        modifyTeamMember = (Button)view.findViewById(R.id.modifyTeamMember);
        Drawable drawable_team_manage = getResources().getDrawable(R.drawable.team_manage);
        drawable_team_manage.setBounds(0, 0, 100, 100);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        modifyTeamMember.setCompoundDrawables(null, drawable_team_manage, null, null);

        checkMyVideo = (Button)view.findViewById(R.id.checkMyVideo);
        Drawable drawable_video = getResources().getDrawable(R.drawable.video_icon);
        drawable_video.setBounds(0, 0, 100, 100);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        checkMyVideo.setCompoundDrawables(null, drawable_video, null, null);

        checkMyOrder = (Button)view.findViewById(R.id.checkMyOrder);
        Drawable drawable_order = getResources().getDrawable(R.drawable.order_list);
        drawable_order.setBounds(0, 0, 100, 100);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        checkMyOrder.setCompoundDrawables(null, drawable_order, null, null);
        logout = (Button)view.findViewById(R.id.logout);
        Drawable drawable_logout = getResources().getDrawable(R.drawable.logout);
        drawable_logout.setBounds(0, 0, 100, 100);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        logout.setCompoundDrawables(null, drawable_logout, null, null);



        modifyPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                password_change();
            }
        });
        modifyUserInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                user_info_change();
            }
        });
        createTeam.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                create_team();
            }
        });
        logout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        checkMyVideo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                check_my_video();
            }
        });
        checkMyOrder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                check_my_order();
            }
        });
        modifyTeamMember.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                modify_team_member();
            }
        });
        return view;
    }

    private void password_change()
    {
        PasswordChangeDialog passwordChangeDialog = new PasswordChangeDialog(this.getActivity(), username, password);
        passwordChangeDialog.show();
    }

    private void user_info_change()
    {
        UserInfoChangeDialog userInfoChangeDialog = new UserInfoChangeDialog(this.getActivity(), username, password);
        userInfoChangeDialog.show();
    }

    private void create_team()
    {
        CreateTeamDialog createTeamDialog = new CreateTeamDialog(this.getActivity(), username, password);
        createTeamDialog.show();

    }
    private void check_my_video()
    {
//        VideoListDialog videoListDialog = new VideoListDialog(this.getActivity(), username,password);
//        videoListDialog.show();
        String username = this.username;
        String password = this.password;
        //TODO START THE INTENT MADE BY SUNWEIJUN
    }
    private void check_my_order()
    {
        OrderListDialog orderListDialog = new OrderListDialog(this.getActivity(), username,password);
        orderListDialog.show();
    }

    private void modify_team_member()
    {
        TeamListDialog teamListDialog = new TeamListDialog(this.getActivity(),username,password);
        teamListDialog.show();
    }

    private void logout()
    {
        SharedPreferences.Editor edit = sp.edit();
        //通过editor对象写入数据
        edit.putBoolean("login", false);
        edit.commit();
        ActivityCollector.finishAll();
        Intent intent=new Intent(this.getActivity(), LoginActivity.class);
        startActivity(intent);
    }
}

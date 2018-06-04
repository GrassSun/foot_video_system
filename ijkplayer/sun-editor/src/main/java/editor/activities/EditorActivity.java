package editor.activities;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONObject;

import markoperator.MarkMatch;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import editor.ijk.demo.R;
import tv.danmaku.ijk.media.example.application.Settings;
import tv.danmaku.ijk.media.example.fragments.TracksFragment;
import tv.danmaku.ijk.media.example.widget.media.AndroidMediaController;

import editor.widget.EditorView;

public class EditorActivity extends AppCompatActivity implements TracksFragment.ITrackHolder {
    private static final String TAG = "EditorActivity";

    private Context mContext;
    private MarkMatch mMarkMatch;

    private AndroidMediaController mMediaController;
    private FrameLayout viewSwitch;
    private EditorView mVideoView;
    private TextView mToastTextView;
    private TableLayout mHudView;
    private TableLayout mInfoMiddleView;
    private DrawerLayout mDrawerLayout;
    private ViewGroup mRightDrawer;

    private Settings mSettings;
    private boolean mBackPressed;

    public static Intent newIntent(Context context, String markString) {
        Intent intent = new Intent(context, editor.activities.EditorActivity.class);
        intent.putExtra("markMatch", markString);
        return intent;
    }

    public static void intentTo(Context context, MarkMatch markMatch) {
        if( markMatch.size()!= 0)
            context.startActivity(newIntent(context, markMatch.getString()));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // init UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("预览");
        setSupportActionBar(toolbar);

        mSettings = new Settings(this);

        mContext = getApplicationContext();

        Intent intent = getIntent();

        // handle arguments
        String markString = intent.getStringExtra("markMatch");
        try {
            mMarkMatch = new MarkMatch(new JSONObject(markString));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(mMarkMatch.size() == 0) {
            finish();
            return;
        }

        viewSwitch = (FrameLayout) findViewById(R.id.color_overlay_video);

        ActionBar actionBar = getSupportActionBar();
        mMediaController = new AndroidMediaController(this, true);
        mMediaController.setSupportActionBar(actionBar);

        mToastTextView = (TextView) findViewById(R.id.toast_text_view);

        mHudView = (TableLayout) findViewById(R.id.hud_view);
        mHudView.setVisibility(View.INVISIBLE);
        mInfoMiddleView = (TableLayout) findViewById(R.id.middle_info_view);
        //mInfoMiddleView.setVisibility(View.INVISIBLE);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRightDrawer = (ViewGroup) findViewById(R.id.right_drawer);

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoView = (EditorView) findViewById(R.id.editor_view);
        mVideoView.setViewSwitch(viewSwitch);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setSubView(mHudView);

        // prefer mVideoPath
        if (mMarkMatch != null) {
            mVideoView.setMarkMatch(mMarkMatch);
        } else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }

        mVideoView.start();

    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;
        onStop();
        finish();
        //super.onBackPressed();
    }

    @Override
    protected void onStop() {

        if (mBackPressed) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
            IjkMediaPlayer.native_profileEnd();
        }

        super.onStop();
    }

    @Override
    public void onDestroy() {
        if(!mBackPressed) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
            IjkMediaPlayer.native_profileEnd();
        }

        super.onDestroy();
    }

    private boolean stopStatus;

    @Override
    public void onPause() {

        stopStatus = mVideoView.isPlaying();
        mVideoView.pause();

        super.onPause();
    }

    @Override
    public void onResume() {

        if(stopStatus)
            mVideoView.start();

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add("退出");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 0) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        if (mVideoView == null)
            return null;

        return mVideoView.getTrackInfo();
    }

    @Override
    public void selectTrack(int stream) {
        mVideoView.selectTrack(stream);
    }

    @Override
    public void deselectTrack(int stream) {
        mVideoView.deselectTrack(stream);
    }

    @Override
    public int getSelectedTrack(int trackType) {
        if (mVideoView == null)
            return -1;

        return mVideoView.getSelectedTrack(trackType);
    }

    public void setSpeed(float speed) {
        mVideoView.setSpeed(speed);
    }
}
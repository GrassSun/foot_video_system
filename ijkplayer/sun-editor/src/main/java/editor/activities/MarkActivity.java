package editor.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import editor.ijk.demo.R;
import editor.widget.MarkView;
import editor.widget.ScreenSizeUtils;
import file.FileHelper;
import markoperator.MarkMatch;
import markoperator.MarkSegment;
import tv.danmaku.ijk.media.example.application.Settings;
import tv.danmaku.ijk.media.example.fragments.TracksFragment;
import tv.danmaku.ijk.media.example.widget.media.AndroidMediaController;
import tv.danmaku.ijk.media.example.widget.media.MeasureHelper;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

public class MarkActivity extends AppCompatActivity implements TracksFragment.ITrackHolder {
    private static final String TAG = "MarkActivity";
    public static final int MARK_FINISHED = 1013;
    public static final int MARK_UNFINISHED = 1002;

    private Context mContext;
    private String mVideoPath;
    private String mVideoSource;
    private String markMode;
    private MarkMatch markMatch;

    private AndroidMediaController mMediaController;
    private MarkView mVideoView;
    private TextView mToastTextView;
    private TableLayout mHudView;
    private DrawerLayout mDrawerLayout;
    private ViewGroup mRightDrawer;
    private ImageView imageButton;
    private int imageFlag;
    private int index = 0;

    private Settings mSettings;
    private boolean mBackPressed;

    private ArrayList<String> neighbour;
    private String direct;
    private int directIndex;

    final String[] speedList = {"0.5", "1.0", "1.5", "2.0"};


    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
        Intent intent = new Intent(context, MarkActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        return intent;
    }

    private Dialog dialog;
    private int segmentStart;
    private int segmentEnd;

    private Dialog switchDialog;
    private ArrayList<ArrayList<String> > sourceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(MARK_UNFINISHED);
        setContentView(R.layout.activity_mark);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        index = 0;
        mContext = getApplicationContext();

        mSettings = new Settings(this);

        Intent intent = getIntent();

        // handle arguments
        mVideoPath = intent.getStringExtra("videoPath");
        mVideoSource = intent.getStringExtra("videoSource");
        markMode = intent.getStringExtra("markMode");
        direct = intent.getStringExtra("direct");
        directIndex = Integer.parseInt(intent.getStringExtra("index"));
        neighbour = new ArrayList<>();

        FileHelper fileHelper = FileHelper.getInstance(getApplicationContext());

        sourceList = fileHelper.parseVideoSource(mVideoSource);

        if(direct.equals("north")) {
            neighbour = sourceList.get(0);
        } else if(direct.equals("middle")) {
            neighbour = sourceList.get(1);
            } else if(direct.equals("south")) {
            neighbour = sourceList.get(2);
        }

        // init UI

        String title;
        try {
            JSONObject json = new JSONObject(mVideoSource);
            title = json.getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
            title = "标记片段";
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.mark_toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        mMediaController = new AndroidMediaController(this, true);
        mMediaController.setSupportActionBar(actionBar);

        mToastTextView = (TextView) findViewById(R.id.mark_toast_text_view);
        mHudView = (TableLayout) findViewById(R.id.mark_hud_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.mark_drawer_layout);
        mRightDrawer = (ViewGroup) findViewById(R.id.mark_right_drawer);

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        imageButton = (ImageView) findViewById(R.id.mark_button);
        imageFlag = 0;

        markMatch = new MarkMatch();
        markMatch.setVideoSource(mVideoSource);

        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                imageFlag ^= 1;
                if(imageFlag == 0) {

                    imageButton.setImageResource(R.drawable.mark_image);
                    segmentEnd = mVideoView.getCurrentPosition();
                    if(segmentStart >= segmentEnd) {
                        Snackbar.make(mVideoView, "开始时间点不能晚于结束时间点", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    setDialog();

                    subtitleEdit.setText("");
                    timeSpin.setSelection(0);
                    speedSpin.setSelection(1);
                    typeSpin.setSelection(5);

                    dialog.show();
                    mVideoView.pause();
                } else {
                    imageButton.setImageResource(R.drawable.mark_image_right);
                    segmentStart = mVideoView.getCurrentPosition();
                }
            }
        });

        mHudView.setVisibility(View.INVISIBLE);

        mVideoView = (MarkView) findViewById(R.id.mark_video_view);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setHudView(mHudView);
        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }
        mVideoView.start();
        Snackbar.make(mVideoView, "点击左侧按钮以开始mark片段,再次点击结束mark", Snackbar.LENGTH_LONG).show();
    }

    private Spinner typeSpin;
    private Spinner timeSpin;
    private EditText subtitleEdit;
    private Spinner speedSpin;

    private void setDialog() {
        dialog = new Dialog(this, R.style.NormalDialogStyle);
        View view = View.inflate(this, R.layout.dialog_mark, null);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.23f));
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(this).getScreenWidth() * 0.9f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;

        dialog.setCanceledOnTouchOutside(false);

        typeSpin = (Spinner) view.findViewById(R.id.type_spinner);
        timeSpin = (Spinner) view.findViewById(R.id.time_spinner);
        speedSpin = (Spinner) view.findViewById(R.id.speed_spinner);
        subtitleEdit = (EditText) view.findViewById(R.id.mark_subtitle);
        subtitleEdit.setHint("输入字幕");

        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subtitleEdit.setText("");
                dialog.dismiss();
                mVideoView.start();
                Snackbar.make(mVideoView, "标记取消,再次点击按钮以开始标记新片段", Snackbar.LENGTH_LONG).show();
            }
        });

        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int timePrevious = (timeSpin.getSelectedItemPosition()) * 5000;
                int kind = typeSpin.getSelectedItemPosition();
                int speedPosition = speedSpin.getSelectedItemPosition();

                if(kind == 5) {
                    Snackbar.make(v, "请选择片段类型", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(segmentStart > timePrevious)
                    segmentStart -= timePrevious;
                else segmentStart = 1;

                ++index;

                MarkSegment segment = new MarkSegment(neighbour.get(directIndex), segmentStart,
                        segmentEnd);

                String prefix, type;
                type = typeSpin.getSelectedItem().toString();
                if(direct.equals("north"))
                    prefix = getString(R.string.football_door_north);
                else if(direct.equals("south"))
                    prefix = getString(R.string.football_door_south);
                else
                    prefix = getString(R.string.football_door_middle);
                prefix = " " + prefix;

                segment.setValue("subtitle", subtitleEdit.getText().toString());
                segment.setValue("type", String.valueOf(kind));
                segment.setValue("name", "片段" + String.valueOf(index) + prefix + ":" + type);
                segment.setValue("speed", speedList[speedPosition]);
                markMatch.addSegment(segment);

                if(markMode.equals("all")) {

                    for (int i = 0; i < neighbour.size(); ++i) {

                        if (i == directIndex) {
                            continue;
                        }

                        ++index;

                        segment = new MarkSegment(neighbour.get(i), segmentStart,
                                segmentEnd);
                        segment.setValue("subtitle", subtitleEdit.getText().toString());
                        segment.setValue("type", String.valueOf(kind));
                        segment.setValue("name", "片段" + String.valueOf(index) + prefix + ":" + type);
                        segment.setValue("speed", speedList[speedPosition]);
                        markMatch.addSegment(segment);
                    }
                }

                dialog.dismiss();
                mVideoView.start();
                Snackbar.make(mVideoView, "标记结束,再次点击按钮以开始标记新片段", Snackbar.LENGTH_SHORT).show();
            }
        });

        final String[] timeSelect = {"开头提前0s", "开头提前5s", "开头提前10s", "开头提前15s", "开头提前20s"};
        final String[] typeSelect = {"进球" , "射门", "点球", "任意球", "其它", "请选择类型"};
        final String[] speedSelect = {"0.5倍速", "1.0倍速", "1.5倍速", "2.0倍速"};

        ArrayAdapter<String> timeSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, timeSelect);

        timeSpin.setAdapter(timeSpinnerAdapter);

        ArrayAdapter<String> typeSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, typeSelect);

        typeSpin.setAdapter(typeSpinnerAdapter);

        ArrayAdapter<String> speedSpinnerAdapter = new ArrayAdapter<String>(this, android.R
                .layout.simple_spinner_item, speedSelect);

        speedSpin.setAdapter(speedSpinnerAdapter);
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
        getMenuInflater().inflate(R.menu.menu_mark, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle_ratio) {
            int aspectRatio = mVideoView.toggleAspectRatio();
            String aspectRatioText = MeasureHelper.getAspectRatioText(this, aspectRatio);
            mToastTextView.setText(aspectRatioText);
            mMediaController.showOnce(mToastTextView);
            return true;
        } else if (id == R.id.action_toggle_player) {
            int player = mVideoView.togglePlayer();
            String playerText = MarkView.getPlayerText(this, player);
            mToastTextView.setText(playerText);
            mMediaController.showOnce(mToastTextView);
            return true;
        } else if (id == R.id.action_toggle_render) {
            int render = mVideoView.toggleRender();
            String renderText = MarkView.getRenderText(this, render);
            mToastTextView.setText(renderText);
            mMediaController.showOnce(mToastTextView);
            return true;
        } else if (id == R.id.action_show_info) {
            mVideoView.showMediaInfo();
        } else if (id == R.id.action_show_tracks) {
            if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.right_drawer);
                if (f != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.remove(f);
                    transaction.commit();
                }
                mDrawerLayout.closeDrawer(mRightDrawer);
            } else {
                Fragment f = TracksFragment.newInstance();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.right_drawer, f);
                transaction.commit();
                mDrawerLayout.openDrawer(mRightDrawer);
            }
        }

        if(id == R.id.finish_mark) {
            Intent intent = new Intent();
            intent.putExtra("markMatch", markMatch.getString());
            setResult(MARK_FINISHED, intent);
            mBackPressed = true;
            onStop();
            finish();
        } else if(id == R.id.preview_mark) {
            if(markMatch.size() != 0) {
                mVideoView.pause();
                Intent intent = EditorActivity.newIntent(mContext, markMatch.getString());
                startActivityForResult(intent, MARK_UNFINISHED);
            }
        } else if(id == R.id.switch_mark) {
            setSwitchDialog();
            switchDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSwitchDialog() {
        switchDialog = new Dialog(this, R.style.NormalDialogStyle);
        View view = View.inflate(this, R.layout.dialog_spinner, null);
        switchDialog.setContentView(view);
        switchDialog.setCanceledOnTouchOutside(true);
        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.23f));
        Window dialogWindow = switchDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(this).getScreenWidth() * 0.9f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        switchDialog.setCanceledOnTouchOutside(false);

        ArrayList<String> switchVideo = new ArrayList<>();

        ArrayAdapter<String> switchSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, switchVideo);

        for(int i = 0; i < neighbour.size(); ++i) {
            if( i == directIndex)
                switchVideo.add("视角" + String.valueOf(i) + "(当前视角)");
            else
                switchVideo.add("视角" + String.valueOf(i));
        }

        final Spinner spinner = (Spinner) view.findViewById(R.id.match_spinner);

        spinner.setAdapter(switchSpinnerAdapter);

        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoView.pause();
                switchDialog.dismiss();
            }
        });

        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = spinner.getSelectedItemPosition();

                if(position != directIndex) {

                    directIndex = position;
                    mVideoView.switchVideoPath(neighbour.get(position));

                }

                switchDialog.dismiss();
            }
        });

        switchDialog.hide();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MARK_UNFINISHED) {
            mVideoView.start();
        }
    }
}


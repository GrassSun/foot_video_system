package editor.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import editor.ijk.demo.R;
import editor.widget.ScreenSizeUtils;
import editor.widget.SegmentAdapter;
import file.FileHelper;
import markoperator.MarkMatch;
import markoperator.MarkSegment;

public class ModifySegmentActivity extends AppCompatActivity {

    public final static int SEGMENT_PREVIEW = 0;
    public final static int SEGMENT_MARK = SEGMENT_PREVIEW + 1;
    public final static int SEGMENT_ADD = SEGMENT_MARK + 1;
    public final static int SEGMENT_MODIFY = SEGMENT_ADD + 1;
    public final static int SEGMENT_REMOVE = SEGMENT_MODIFY + 1;
    public final static int SEGMENT_UP = SEGMENT_REMOVE + 1;
    public final static int SEGMENT_DOWN = SEGMENT_UP + 1;
    public final static int SEGMENT_FINISH = SEGMENT_DOWN + 1;

    public final static int RESULT_FINISH = 1002;

    private Context mContext;
    private MarkMatch match;
    private ListView operatorListView;
    private ListView segmentListView;
    private SegmentAdapter mAdapter;
    private Dialog segmentInputDialog;
    private FileHelper fileHelper;
    private ArrayList<String> pathNorth, pathSouth, pathMiddle, path, pathSelect;

    private EditText segmentNameEdit;
    private EditText subtitleEdit;
    private EditText startEdit;
    private EditText endEdit;
    private Spinner typeSpin;
    private Spinner sourceSpin;
    private Spinner speedSpin;
    private int dialogType;
    final float[] speedList = {0.5f, 1.0f, 1.5f, 2.0f};
    final String[] speedString = {"0.5", "1.0", "1.5", "2.0"};

    private Dialog sourceSpinnerDialog;
    private Spinner sourceSpinner;
    private ArrayList<String> sourceSelect;
    private String mVideoSource;

    public static Intent newIntent(Context context, MarkMatch markMatch) {
        Intent intent = new Intent(context, ModifySegmentActivity.class);
        intent.putExtra("markMatch", markMatch.getString());
        return intent;
    }

    private int getVideoSourceID(String source) {
        for(int i = 0; i < path.size(); ++i)
            if(path.get(i).equals(source))
                return i;
        return path.size();
    }

    private int getSpeedID(float speed) {
        for(int i = 0; i < speedList.length; ++i)
            if(Math.abs(speed - speedList[i]) < 0.01f)
                return i;
        return 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_segment);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("修改片段");
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();

        String markString = getIntent().getStringExtra("markMatch");
        try {
            JSONObject json = new JSONObject(markString);
            match = new MarkMatch(json);

        } catch (Exception e) {
            e.printStackTrace();
            match = new MarkMatch();
        }

        operatorListView = (ListView) findViewById(R.id.operate_list);
        ArrayList<String> operator = new ArrayList<>();
        operator.add("预览");
        operator.add("标记段落");
        operator.add("添加");
        operator.add("修改");
        operator.add("删除");
        operator.add("上移");
        operator.add("下移");
        operator.add("完成");

        setVideoPath();

        operatorListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout
                .simple_list_item_1, operator));

        operatorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                dialogType = position;
                if(position == SEGMENT_PREVIEW) {
                    EditorActivity.intentTo(mContext, match);
                } else if(position == SEGMENT_ADD) {

                    setDialog();

                    mAdapter.setChose(-1);
                    segmentNameEdit.setText("");
                    subtitleEdit.setText("");
                    segmentNameEdit.setHint("输入名称以区分不同视频段");
                    subtitleEdit.setHint("输入字幕");
                    speedSpin.setSelection(1);
                    sourceSpin.setSelection(0);
                    typeSpin.setSelection(0);
                    startEdit.setText("");
                    endEdit.setText("");
                    segmentInputDialog.show();


                } else if(position == SEGMENT_MODIFY) {
                    int chosen = mAdapter.getChosen();
                    if(chosen == -1) {
                        return;
                    }

                    setDialog();

                    MarkSegment segment = match.mark.get(chosen);

                    segmentNameEdit.setText(segment.getName());
                    subtitleEdit.setText(segment.getSubtitle());
                    subtitleEdit.setHint("输入字幕");
                    segmentNameEdit.setHint("输入名称以区分不同视频段");
                    sourceSpin.setSelection(getVideoSourceID(segment.getVideoPath()));
                    speedSpin.setSelection(getSpeedID(segment.getSpeed()));
                    typeSpin.setSelection(segment.getType());
                    startEdit.setText(floatFormat(segment.getSt() * 0.001f));
                    endEdit.setText(floatFormat(segment.getEd() * 0.001f));
                    segmentInputDialog.show();

                } else if(position == SEGMENT_REMOVE) {
                    int chosen = mAdapter.getChosen();
                    if(chosen == -1) {
                        return;
                    }
                    MarkSegment seg = (MarkSegment)mAdapter.getItem(chosen);
                    mAdapter.remove(seg);
                    match.mark.remove(chosen);
                    mAdapter.setChose(-1);
                    mAdapter.notifyDataSetChanged();

                } else if(position == SEGMENT_FINISH) {
                    Intent intent = new Intent();
                    intent.putExtra("markMatch", match.getString());
                    setResult(RESULT_FINISH, intent);
                    finish();
                } else if(position == SEGMENT_UP) {
                    int chosen = mAdapter.getChosen();
                    if(chosen == 0)
                        return;
                    MarkSegment seg1 = match.mark.get(chosen), seg0 = match.mark.get(chosen - 1);
                    match.mark.remove(chosen);
                    match.mark.remove(chosen - 1);
                    match.mark.add(chosen - 1, seg1);
                    match.mark.add(chosen, seg0);
                    reloadMatchAdapter();
                    mAdapter.setChose(chosen - 1);

                } else if(position == SEGMENT_DOWN) {
                    int chosen = mAdapter.getChosen();
                    if(chosen + 1 == match.mark.size())
                        return;
                    MarkSegment seg1 = match.mark.get(chosen), seg0 = match.mark.get(chosen + 1);
                    match.mark.remove(chosen);
                    match.mark.remove(chosen);
                    match.mark.add(chosen, seg1);
                    match.mark.add(chosen, seg0);
                    reloadMatchAdapter();
                    mAdapter.setChose(chosen + 1);
                } else if(position == SEGMENT_MARK) {
                    setMarkDialog();

                    sourceSpinnerDialog.show();
                }
            }
        });

        segmentListView = (ListView) findViewById(R.id.segment_list);

        ArrayList<MarkSegment> segmentList = new ArrayList<>();

        for(int i = 0; i < match.mark.size(); ++i) {
            MarkSegment seg = new MarkSegment(match.mark.get(i).getJson());
            segmentList.add(seg);
        }


        mAdapter= new SegmentAdapter(this, R.layout.segment_item, segmentList);

        segmentListView.setAdapter(mAdapter);
        segmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int chosen = mAdapter.getChosen();

                if(chosen == position) {
                    mAdapter.setChose(-1);
                    mAdapter.notifyDataSetChanged();
                    return;
                }

                mAdapter.setChose(position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setVideoPath() {
        fileHelper = FileHelper.getInstance(mContext);

        mVideoSource = match.getVideoSource();
        ArrayList<ArrayList<String> > temp = fileHelper.parseVideoSource(mVideoSource);

        path = new ArrayList<>();
        pathSelect = new ArrayList<>();

        pathNorth = temp.get(0);
        pathMiddle = temp.get(1);
        pathSouth = temp.get(2);

        for(int i = 0; i < pathNorth.size(); ++i)
            path.add(pathNorth.get(i));
        for(int i = 0; i < pathMiddle.size(); ++i)
            path.add(pathMiddle.get(i));
        for(int i = 0; i < pathSouth.size(); ++i)
            path.add(pathSouth.get(i));

        String doorNorth = getString(R.string.football_door_north);
        String doorSouth = getString(R.string.football_door_south);
        String doorMiddle = getString(R.string.football_door_middle);

        for(int i = 0; i < pathNorth.size(); ++ i)
            pathSelect.add(doorNorth + "：角度" + String.valueOf(i) );


        for(int i = 0; i < pathMiddle.size(); ++ i)
            pathSelect.add(doorMiddle + "：角度" + String.valueOf(i) );


        for(int i = 0; i < pathSouth.size(); ++ i)
            pathSelect.add(doorSouth + "：角度" + String.valueOf(i) );

        pathSelect.add("其它");
    }

    private String floatFormat(float value) {
        String res = new DecimalFormat("#00.00").format(value);
        return res;
    }

    private void setDialog() {
        segmentInputDialog = new Dialog(this, R.style.WhiteDialogStyle);
        View view = View.inflate(this, R.layout.dialog_segment_input, null);
        segmentInputDialog.setContentView(view);
        segmentInputDialog.setCanceledOnTouchOutside(true);
        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.23f));
        Window dialogWindow = segmentInputDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(this).getScreenWidth() * 0.9f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;

        segmentInputDialog.setCanceledOnTouchOutside(false);

        typeSpin = (Spinner) view.findViewById(R.id.type_spinner);
        speedSpin = (Spinner) view.findViewById(R.id.speed_spinner);
        sourceSpin = (Spinner) view.findViewById(R.id.source_spinner);
        subtitleEdit = (EditText) view.findViewById(R.id.segment_subtitle);
        segmentNameEdit = (EditText) view.findViewById(R.id.segment_name);
        startEdit = (EditText) view.findViewById(R.id.start_seconds);
        endEdit = (EditText) view.findViewById(R.id.end_seconds);

        final String[] typeSelect = {"进球" , "射门", "点球", "任意球", "其它"};

        ArrayAdapter<String> typeSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, typeSelect);
        typeSpin.setAdapter(typeSpinnerAdapter);

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, pathSelect);
        sourceSpin.setAdapter(sourceSpinnerAdapter);

        final String[] speedSelect = {"0.5倍速", "1.0倍速", "1.5倍速", "2.0倍速"};
        ArrayAdapter<String> speedSpinnerAdapter = new ArrayAdapter<String>(this, android.R
                .layout.simple_spinner_item, speedSelect);
        speedSpin.setAdapter(speedSpinnerAdapter);

        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                segmentInputDialog.dismiss();
            }
        });

        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pathId = sourceSpin.getSelectedItemPosition();
                if(sourceSpin.getSelectedItemId() >= path.size()) {
                    segmentInputDialog.dismiss();
                    return;
                }
                String startText = startEdit.getText().toString();
                String endText = endEdit.getText().toString();

                if(startText.equals("") || endText.equals("")) {
                    segmentInputDialog.dismiss();
                    return;
                }

                int start = (int) Float.parseFloat(startText) * 1000;
                int end = (int) Float.parseFloat(endText) * 1000;
                if(start >= end) {
                    Snackbar.make(v, "开始时间不能晚于结束时间", Snackbar.LENGTH_LONG);
                    return;
                }

                MarkSegment segment = new MarkSegment(path.get(pathId), start, end);
                segment.setValue("type", String.valueOf(typeSpin.getSelectedItemPosition()));
                segment.setValue("subtitle", subtitleEdit.getText().toString());
                segment.setValue("speed", speedString[speedSpin.getSelectedItemPosition()]);
                segment.setValue("name", segmentNameEdit.getText().toString());

                if(dialogType == SEGMENT_ADD) {

                    match.mark.add(segment);

                } else if(dialogType == SEGMENT_MODIFY) {
                    int chosen = mAdapter.getChosen();

                    match.mark.set(chosen, segment);
                }

                reloadMatchAdapter();

                segmentInputDialog.dismiss();
            }
        });
        segmentInputDialog.hide();
    }

    private void setMarkDialog() {

        sourceSpinnerDialog = new Dialog(this, R.style.NormalDialogStyle);
        View view = View.inflate(this, R.layout.dialog_video_source_spinner, null);
        sourceSpinnerDialog.setContentView(view);
        sourceSpinner = (Spinner) view.findViewById(R.id.video_source_spinner);
        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);

        sourceSelect = new ArrayList<>();
        for(int i = 0; i + 1 < pathSelect.size(); ++i)
            sourceSelect.add(pathSelect.get(i));

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, sourceSelect);
        sourceSpinner.setAdapter(sourceSpinnerAdapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceSpinnerDialog.dismiss();
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = sourceSpinner.getSelectedItemPosition();
                String videoPath = path.get(position);

                Intent mIntent = MarkActivity.newIntent(mContext, videoPath, "");

                String direct;
                int index;
                if(position < pathNorth.size() ) {
                    direct = "north";
                    index = position;
                } else if(position < pathNorth.size() + pathMiddle.size() ) {
                    direct = "middle";
                    index = position - pathNorth.size();
                } else {
                    direct = "south";
                    index = position - pathNorth.size() - pathSouth.size();
                }

                mIntent.putExtra("videoSource", mVideoSource);

                mIntent.putExtra("direct", direct);
                mIntent.putExtra("index", String.valueOf(index));
                mIntent.putExtra("markMode", "single");

                sourceSpinnerDialog.dismiss();

                startActivityForResult(mIntent, MarkActivity.MARK_UNFINISHED);

            }
        });

        sourceSpinnerDialog.hide();
    }

    public void reloadMatchAdapter() {
        mAdapter.clear();

        for(int i = 0; i < match.mark.size(); ++i) {
            JSONObject json = match.mark.get(i).getJson();
            mAdapter.add(new MarkSegment(json));
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == MarkActivity.MARK_FINISHED ) {

            String markString = data.getStringExtra("markMatch");
            try {
                MarkMatch matchAdd = new MarkMatch(new JSONObject(markString));
                for(int i = 0; i < matchAdd.size(); ++i)
                    match.mark.add(matchAdd.getSegment(i));
                reloadMatchAdapter();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

package editor.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import editor.ijk.demo.R;
import editor.widget.MarkMatchAdapter;
import file.FileHelper;
import markoperator.MarkMatch;

public class EditorOperator extends AppCompatActivity {

    public static final int EDITOR_EDITING = 1001;
    public static final int EDITOR_NEWMATCH = 1002;
    public static final int EDITOR_CALL_FOR_VIDEO_SOURCE = 1003;

    public static final int OPERATE_EDITING_ACTUAL = 0;
    public static final int OPERATE_EDITING_SIMPLE_TEST = OPERATE_EDITING_ACTUAL + 1;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, EditorOperator.class);
        return intent;
    }

    public static void intentTo(Context context) {
        context.startActivity(newIntent(context));
    }

    private Context mContext;
    private ArrayList<MarkMatch> matchList;
    private MarkMatchAdapter markAdapter;

    private Dialog sourceSpinnerDialog;
    private Spinner sourceSpinner;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_operator);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("编辑集锦");
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mContext = getApplicationContext();
        FileHelper fileHelper = FileHelper.getInstance(mContext);

/*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "生成测试文件", Snackbar.LENGTH_LONG).show();
                fileHelper.makeTestMarkMatch();
            }
        });
*/
        final ListView matchListView = (ListView) findViewById(R.id.match_list);

        matchList = fileHelper.getMatchList();

        markAdapter= new MarkMatchAdapter(this, R.layout.mark_match_item, matchList);

        matchListView.setAdapter(markAdapter);

        matchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int chosen = markAdapter.getChosen();
                if(position == chosen) {
                    markAdapter.setChose(-1);
                    markAdapter.notifyDataSetChanged();
                    MarkMatch match = (MarkMatch) markAdapter.getItem(chosen);
                    EditorActivity.intentTo(mContext, match);
                    return;
                }
                markAdapter.setChose(position);
                markAdapter.notifyDataSetChanged();
            }
        });

        initButton();
/*
        infoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String op = operator.get(position);
                int chosen = mAdapter.getChosen();
                if(position == OPERATE_EDITING_ACTUAL) {

                    if(chosen == -1)
                        return;

                    MarkMatch match = (MarkMatch) mAdapter.getItem(chosen);

                    fileHelper.makeMediaFile(match);

                } else if(position == OPERATE_EDITING_SIMPLE_TEST) {
                    MySimpleTest simpleTest = MySimpleTest.getInstance(mContext);
                    simpleTest.testJson();
                }
            }
        });
*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initButton() {
        Button playButton = (Button) findViewById(R.id.operate_button_play);
        Button addButton = (Button) findViewById(R.id.operate_button_new);
        Button removeButton = (Button) findViewById(R.id.operate_button_remove);
        Button editButton = (Button) findViewById(R.id.operate_button_edit);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chosen = markAdapter.getChosen();
                if(chosen == -1)
                    return;
                MarkMatch match = (MarkMatch) markAdapter.getItem(chosen);
                EditorActivity.intentTo(mContext, match);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*setSpinnerDialog();
                sourceSpinnerDialog.show();*/
                Intent intent = new Intent(getApplicationContext(), MatchSelectActivity.class);
                EditorOperator.this.startActivityForResult(intent, EDITOR_CALL_FOR_VIDEO_SOURCE);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chosen = markAdapter.getChosen();

                if(chosen == -1)
                    return;

                MarkMatch markMatch = (MarkMatch) markAdapter.getItem(chosen);

                String title = markMatch.getName();

                FileHelper.getInstance(mContext).removeMatchFile(title);

                markAdapter.setChose(-1);
                markAdapter.remove(markMatch);
                markAdapter.notifyDataSetChanged();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chosen = markAdapter.getChosen();

                if(chosen == -1)
                    return;

                MarkMatch markMatch = (MarkMatch) markAdapter.getItem(chosen);
                Intent intent = EditingActivity.newIntent(getApplicationContext(), markMatch);
                startActivityForResult(intent, EDITOR_EDITING);
            }
        });
    }


    private void setSpinnerDialog() {
        sourceSpinnerDialog = new Dialog(this, R.style.NormalDialogStyle);
        View view = View.inflate(this, R.layout.dialog_video_source_spinner, null);
        sourceSpinnerDialog.setContentView(view);
        sourceSpinner = (Spinner) view.findViewById(R.id.video_source_spinner);
        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);

        FileHelper fileHelper = FileHelper.getInstance(getApplicationContext());

        final ArrayList<String> sourceList = fileHelper.getSourceList();

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, sourceList);
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
                Intent intent = NewMatchActivity.newIntent(mContext);
                intent.putExtra("videoSource", sourceList.get(position));
                startActivityForResult(intent, EDITOR_NEWMATCH);
                sourceSpinnerDialog.dismiss();
            }
        });

        sourceSpinnerDialog.hide();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        markAdapter.setChose(-1);
        markAdapter.clear();

        ArrayList<MarkMatch> matchList = FileHelper.getInstance(mContext).getMatchList();
        for (int i = 0; i < matchList.size(); ++i)
            markAdapter.add(matchList.get(i));
        markAdapter.notifyDataSetChanged();

        if(requestCode == EDITOR_CALL_FOR_VIDEO_SOURCE) {
            if(resultCode == MatchSelectActivity.SELECT_ACCEPT) {

                String videoSource = data.getStringExtra("videoSource");
                Intent intent = NewMatchActivity.newIntent(mContext);
                intent.putExtra("videoSource", videoSource);
                startActivityForResult(intent, EDITOR_NEWMATCH);
            }

        }
    }

}

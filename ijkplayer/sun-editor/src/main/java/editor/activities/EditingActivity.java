package editor.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import editor.ijk.demo.R;
import editor.widget.OperateAdapter;
import editor.widget.OperateItem;
import file.FileHelper;
import markoperator.MarkMatch;

public class EditingActivity extends AppCompatActivity {

    public static Intent newIntent(Context context, MarkMatch markMatch) {
        Intent intent = new Intent(context, EditingActivity.class);
        intent.putExtra("match", markMatch.getString());
        return intent;
    }

    private Context mContext;
    private MarkMatch match;
    private Intent mIntent;

    private OperateAdapter mAdapter;
    private ArrayList<OperateItem> operateList;
    private ListView editingList;
    private Dialog textInputDialog;
    //private Dialog sourceSpinnerDialog;
    private Dialog bgmDialog;
    private int inputType;

    public static final int INPUT_PREVIEW = 0;
    public static final int INPUT_TITLE = INPUT_PREVIEW + 1;
    public static final int INPUT_AUTHOR = INPUT_TITLE + 1;
    public static final int INPUT_MATCH = INPUT_AUTHOR + 1;
    public static final int INPUT_SEGMENT = INPUT_MATCH + 1;
    public static final int INPUT_BGM = INPUT_SEGMENT + 1;
    public static final int INPUT_FINISH = INPUT_BGM + 1;

    public static final int INPUT_START_SEGMENT = 1001;
    public static final int INPUT_RESULT_FINISH = 1002;
    public static final int INPUT_RESULT_CANCEL = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("保存信息");
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mContext = getApplicationContext();
        mIntent = getIntent();

        String markString = mIntent.getStringExtra("match");
        try {
            match = new MarkMatch(new JSONObject(markString));
        } catch (JSONException e) {
            e.printStackTrace();
            match = new MarkMatch();
        }

        String title;
        try {
            JSONObject json = new JSONObject(match.getVideoSource());
            title = json.getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
            title = "未选择比赛";
        }

        operateList = new ArrayList<>();
        operateList.add(new OperateItem("预览", "点此预览"));
        operateList.add(new OperateItem("视频名称", match.getName()));
        operateList.add(new OperateItem("作者", match.getAuthor()));
        operateList.add(new OperateItem("比赛", title));
        operateList.add(new OperateItem("修改集锦片段", "点击修改"));
        File bgm = new File(match.getBgm());
        operateList.add(new OperateItem("背景音乐", bgm.getName()));
        operateList.add(new OperateItem("完成", "保存并退出"));

        mAdapter= new OperateAdapter(this, R.layout.operate_item, operateList);

        editingList = (ListView) findViewById(R.id.editing_list);
        editingList.setAdapter(mAdapter);

        editingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inputType = position;
                if(position == INPUT_TITLE || position == INPUT_AUTHOR) {
                    setDialog();
                    textInputDialog.show();
                } else if(position == INPUT_MATCH) {
                    //setSpinnerDialog();
                    //sourceSpinnerDialog.show();

                } else if(position == INPUT_SEGMENT) {
                    Intent intent = ModifySegmentActivity.newIntent(getApplicationContext(), match);
                    startActivityForResult(intent, INPUT_START_SEGMENT);

                } else if(position == INPUT_FINISH) {
                    if(!match.getName().equals("")) {
                        FileHelper fileHelper = FileHelper.getInstance(getApplicationContext());
                        fileHelper.writeMatchFile(match);
                        setResult(INPUT_RESULT_FINISH);
                        finish();
                    } else {
                        Snackbar.make(editingList, "视频名称不能为空！", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                } else if(position == INPUT_PREVIEW) {
                    EditorActivity.intentTo(mContext, match);
                } else if(position == INPUT_BGM) {
                    setBgmDialog();
                    bgmDialog.show();
                }
            }

        });

        Button finishButton = (Button) findViewById(R.id.editing_finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!match.getName().equals("")) {
                    FileHelper fileHelper = FileHelper.getInstance(getApplicationContext());
                    fileHelper.writeMatchFile(match);
                    setResult(INPUT_RESULT_FINISH);
                    finish();
                } else {
                    Snackbar.make(editingList, "视频名称不能为空！", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private EditText textInput;

    private void setDialog() {
        textInputDialog = new Dialog(this, R.style.WhiteDialogStyle);
        View view = View.inflate(this, R.layout.dialog_text_input, null);
        textInputDialog.setContentView(view);
        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        textInput = (EditText) view.findViewById(R.id.dialog_text_input);
        textInput.setText("");

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textInputDialog.dismiss();
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textInput.getText().toString();
                if(inputType == INPUT_TITLE || inputType == INPUT_AUTHOR) {
                    OperateItem item = (OperateItem)mAdapter.getItem(inputType);
                    item.operateInfo = text;
                    mAdapter.notifyDataSetChanged();
                    if(inputType == INPUT_TITLE)
                        match.setName(text);
                    else if(inputType == INPUT_AUTHOR)
                        match.setAuthor(text);
                    textInputDialog.dismiss();
                }
            }
        });

        textInputDialog.hide();
    }

    private Spinner sourceSpinner;
/*
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
                match.setVideoSource(sourceList.get(position));
                sourceSpinnerDialog.dismiss();
            }
        });

        sourceSpinnerDialog.hide();
    }
*/
    private void setBgmDialog() {
        bgmDialog = new Dialog(this, R.style.NormalDialogStyle);
        View view = View.inflate(this, R.layout.dialog_video_source_spinner, null);
        bgmDialog.setContentView(view);
        sourceSpinner = (Spinner) view.findViewById(R.id.video_source_spinner);
        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);

        FileHelper fileHelper = FileHelper.getInstance(getApplicationContext());

        final ArrayList<String> pathList = fileHelper.getMusicList();
        ArrayList<String> tempList = new ArrayList<>();
        for(int i = 0; i < pathList.size(); ++i) {
            File file = new File(pathList.get(i));
            tempList.add(file.getName());
        }

        final ArrayList<String> sourceList = tempList;

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, sourceList);
        sourceSpinner.setAdapter(sourceSpinnerAdapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bgmDialog.dismiss();
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = sourceSpinner.getSelectedItemPosition();
                String bgm = pathList.get(position);
                match.setBgm(bgm);
                bgmDialog.dismiss();

                OperateItem item = (OperateItem) mAdapter.getItem(inputType);
                item.operateInfo = sourceList.get(position);
                mAdapter.notifyDataSetChanged();

            }
        });

        bgmDialog.hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
        }
        if(id == R.id.save_mark) {
            if(!match.getName().equals("")) {
                FileHelper fileHelper = FileHelper.getInstance(getApplicationContext());
                fileHelper.writeMatchFile(match);
            } else {
                Snackbar.make(editingList, "视频名称不能为空！", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        } else if(id == R.id.quit_mark) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == INPUT_START_SEGMENT && resultCode == ModifySegmentActivity.RESULT_FINISH) {
            String markString = data.getStringExtra("markMatch");
            try {
                JSONObject json = new JSONObject(markString);
                MarkMatch newMatch = new MarkMatch(json);
                match = newMatch;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

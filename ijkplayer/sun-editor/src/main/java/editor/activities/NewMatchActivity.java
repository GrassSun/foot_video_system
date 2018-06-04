package editor.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

public class NewMatchActivity extends AppCompatActivity {

    public final static int NEWMATCH_PREVIEW = 0;
    public final static int NEWMATCH_QUIT = NEWMATCH_PREVIEW + 1;
    public final static int NEWMATCH_FINISH = NEWMATCH_QUIT + 1;
    public final static int NEWMATCH_RESULT_CANCEL = 1001;
    public final static int NEWMATCH_MARK = 1002;
    public final static int NEWMATCH_NEXT_STEP = 1003;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, NewMatchActivity.class);
        return intent;
    }

    private Context mContext;
    private MarkMatch match;

    private ArrayList<String> northList, middleList, southList;
    private String direct, mVideoSource;
    private int directIndex;
    private Dialog sourceSpinDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_match);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mContext = getApplicationContext();
        Intent mIntent = getIntent();
        match = new MarkMatch();

        mVideoSource = mIntent.getStringExtra("videoSource");
        match.setVideoSource(mVideoSource);

        FileHelper fileHelper = FileHelper.getInstance(mContext);

        ArrayList<ArrayList<String> > temp = fileHelper.parseVideoSource(mVideoSource);

        northList = temp.get(0);
        middleList = temp.get(1);
        southList = temp.get(2);

        FrameLayout north = (FrameLayout) findViewById(R.id.new_match_north);
        FrameLayout middle = (FrameLayout) findViewById(R.id.new_match_middle);
        FrameLayout south = (FrameLayout) findViewById(R.id.new_match_south);

        north.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direct = "north";
                setSourceDialog();
                sourceSpinDialog.show();
            }
        });

        middle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direct = "middle";
                setSourceDialog();
                sourceSpinDialog.show();
            }
        });

        south.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direct = "south";
                setSourceDialog();
                sourceSpinDialog.show();
            }
        });

        Button nextStep = (Button) findViewById(R.id.new_match_next_step);
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  EditingActivity.newIntent(mContext, match);

                startActivityForResult(intent, NEWMATCH_NEXT_STEP);
            }
        });

        Button preview = (Button) findViewById(R.id.new_match_button_play);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorActivity.intentTo(mContext, match);
            }
        });

        Button cancel = (Button) findViewById(R.id.new_match_button_remove);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(NEWMATCH_RESULT_CANCEL);
                finish();
            }
        });

        ImageView redPoint1, redPoint2, redPoint3;

        redPoint1 = (ImageView) findViewById(R.id.small_red_point_1);
        redPoint2 = (ImageView) findViewById(R.id.small_red_point_2);
        redPoint3 = (ImageView) findViewById(R.id.small_red_point_3);
        redPoint1.setVisibility(View.INVISIBLE);
        redPoint2.setVisibility(View.INVISIBLE);
        redPoint3.setVisibility(View.INVISIBLE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Spinner sourceSpinner;
    private ArrayList<String> sourceList;
    private ArrayList<String> sourceSelect;

    private void setSourceDialog() {

        sourceSpinDialog = new Dialog(this, R.style.NormalDialogStyle);
        View view = View.inflate(this, R.layout.dialog_video_source_spinner, null);
        sourceSpinDialog.setContentView(view);
        sourceSpinner = (Spinner) view.findViewById(R.id.video_source_spinner);
        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);

        if(direct.equals("north")) {
            sourceList = northList;
        } else if(direct.equals("middle")) {
            sourceList = middleList;
        } else {
            sourceList = southList;
        }

        sourceSelect = new ArrayList<>();

        for(int i = 0; i < sourceList.size(); ++i)
            sourceSelect.add("角度" + String.valueOf(i));

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, sourceSelect);
        sourceSpinner.setAdapter(sourceSpinnerAdapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceSpinDialog.dismiss();
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directIndex = sourceSpinner.getSelectedItemPosition();
                String videoPath = sourceList.get(directIndex);

                Intent intent = MarkActivity.newIntent(mContext, videoPath, "");
                intent.putExtra("videoSource", mVideoSource);
                intent.putExtra("direct", direct);
                intent.putExtra("index", String.valueOf(directIndex));
                intent.putExtra("markMode", "all");

                startActivityForResult(intent, NEWMATCH_MARK);
                sourceSpinDialog.dismiss();
            }
        });

        sourceSpinDialog.hide();
    }

    @Override

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NEWMATCH_MARK && resultCode == MarkActivity.MARK_FINISHED) {
            String markString = data.getStringExtra("markMatch");
            MarkMatch newMatch;

            try {
                JSONObject json = new JSONObject(markString);
                newMatch = new MarkMatch(json);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            if(newMatch.mark != null) {
                match.mark.addAll(newMatch.mark);
            }

            ImageView redPoint;

            if(direct.equals("north")) {
                redPoint = (ImageView) findViewById(R.id.small_red_point_1);
            } else if(direct.equals("middle")) {
                redPoint = (ImageView) findViewById(R.id.small_red_point_2);
            } else
                redPoint = (ImageView) findViewById(R.id.small_red_point_3);

            redPoint.setVisibility(View.VISIBLE); //RED
        } else if(requestCode == NEWMATCH_NEXT_STEP) {
            if(resultCode == EditingActivity.INPUT_RESULT_FINISH) {
                finish();
            }
        }
    }

}

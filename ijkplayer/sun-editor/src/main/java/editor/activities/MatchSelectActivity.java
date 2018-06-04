package editor.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import editor.ijk.demo.R;
import editor.widget.MatchAdapter;
import file.FileHelper;

public class MatchSelectActivity extends AppCompatActivity {

    public static int SELECT_CANCEL = 1001;
    public static int SELECT_ACCEPT = 1002;

    private Context mContext;
    private ListView matchListView;
    private MatchAdapter mAdapter;
    private ArrayList<String> matchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("选择比赛");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mContext = getApplicationContext();
        setResult(SELECT_CANCEL);

        matchListView = (ListView) findViewById(R.id.match_select_list);

        matchList = new ArrayList<>();

        mAdapter = new MatchAdapter(this, R.layout.match_item, matchList);

        matchListView.setAdapter(mAdapter);

        matchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = (String) mAdapter.getItem(position);

                Intent intent = new Intent();
                intent.putExtra("videoSource", data);
                setResult(SELECT_ACCEPT, intent);
                finish();
            }
        });


        getMatchList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            setResult(SELECT_CANCEL);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getMatchList() {
        JSONObject json;
        FileHelper fileHelper = FileHelper.getInstance(mContext);
        fileHelper.makeVideoSource();

        ArrayList<String> url = fileHelper.getUrlList();
        json = new JSONObject();
        JSONArray north = new JSONArray();
        JSONArray south = new JSONArray();
        JSONArray middle = new JSONArray();

        north.put(url.get(0));
        north.put(url.get(2));
        south.put(url.get(1));
        south.put(url.get(3));
        middle.put(url.get(2));
        middle.put(url.get(4));


        try {
            json.put("north", north);
            json.put("south", south);
            json.put("middle", middle);

            json.put("title", "教工比赛");
            json.put("time", "2018-1-1");
            mAdapter.add(json.toString());

            json.put("title", "学生比赛");
            json.put("time", "2018-2-1");
            mAdapter.add(json.toString());
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}

package editor.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import editor.ijk.demo.R;
import file.FileHelper;
import markoperator.MarkMatch;

/**
 * Created by sunweijun on 18-3-30.
 */

public class MatchAdapter extends ArrayAdapter {
    private int resourceId;


    public MatchAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        String match = (String) getItem(position);
        String time;
        String title;
        JSONObject json = null;
        try {
           json = new JSONObject(match);
           time = json.getString("time");
           title = json.getString("title");
        } catch (JSONException e) {
           e.printStackTrace();
           time = "加载中";
           title = "加载中";
       }

        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            holder = new ViewHolder();
            holder.matchTitle = (TextView) view.findViewById(R.id.match_select_title);
            holder.matchTime = (TextView) view.findViewById(R.id.match_select_time);
            holder.image = (ImageView) view.findViewById(R.id.match_image);
            view.setTag(holder);

        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.image.setImageResource(R.drawable.video_match);

        holder.matchTitle.setText(title);
        holder.matchTime.setText(time);

        return view;
    }

    public class ViewHolder {
        public TextView matchTitle;
        public TextView matchTime;
        public ImageView image;
    }
}

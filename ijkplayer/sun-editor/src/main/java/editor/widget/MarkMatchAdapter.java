package editor.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import editor.ijk.demo.R;
import markoperator.MarkMatch;

/**
 * Created by sunweijun on 18-5-30.
 */


public class MarkMatchAdapter extends ArrayAdapter {
    private int resourceId;

    private int chosen;

    public MarkMatchAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        resourceId = resource;
        chosen = -1;
    }

    public int getChosen() {
        return chosen;
    }

    public void setChose(int _chosen) {
        chosen = _chosen;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        MarkMatch match = (MarkMatch) getItem(position);

        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.match_name);
            holder.image = (ImageView) view.findViewById(R.id.match_image);
            view.setTag(holder);

        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.text.setText(match.getName());

        if( position == chosen) {
            holder.image.setImageResource(R.drawable.video_match);
            holder.text.setTextColor(Color.parseColor("#FF3333")); // red
        }
        else {
            holder.image.setImageResource(R.drawable.video_match);
            holder.text.setTextColor(Color.parseColor("#000000")); // black
        }

        return view;
    }

    public class ViewHolder {
        public ImageView image;
        public TextView text;
    }
}
package editor.widget;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import editor.ijk.demo.R;
import markoperator.MarkMatch;
import markoperator.MarkSubTitle;

/**
 * Created by sunweijun on 18-3-30.
 */

public class SubtitleAdapter extends ArrayAdapter {
    private int resourceId;

    private int chosen;

    public SubtitleAdapter(Context context, int resource, List objects) {
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

        MarkSubTitle match = (MarkSubTitle) getItem(position);

        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.subtitle_name);
            holder.image = (ImageView) view .findViewById(R.id.subtitle_image);
            view.setTag(holder);

        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.text.setText(match.getText());

        if( position == chosen) {
            holder.image.setImageResource(R.drawable.ic_check_box_black_40dp);
            holder.text.setTextColor(Color.parseColor("#FF3333")); // red
        }
        else {
            holder.image.setImageResource(R.drawable.ic_check_box_outline_blank_black_40dp);
            holder.text.setTextColor(Color.parseColor("#000000")); // black
        }

        return view;
    }

    public class ViewHolder {
        public ImageView image;
        public TextView text;
    }
}

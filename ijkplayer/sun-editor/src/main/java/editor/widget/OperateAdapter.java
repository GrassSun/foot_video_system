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

/**
 * Created by sunweijun on 18-4-20.
 */

public class OperateAdapter extends ArrayAdapter {
    private int resourceId;

    public OperateAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder holder = null;
        OperateItem item = (OperateItem) getItem(position);

        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            holder = new ViewHolder();
            holder.operate = (TextView) view.findViewById(R.id.operate_name);
            holder.info = (TextView) view.findViewById(R.id.operate_info);
            view.setTag(holder);

        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.operate.setText(item.operateName);
        holder.info.setText(item.operateInfo);

        return view;
    }

    public class ViewHolder {
        public TextView operate = null;
        public TextView info = null;
    }
}

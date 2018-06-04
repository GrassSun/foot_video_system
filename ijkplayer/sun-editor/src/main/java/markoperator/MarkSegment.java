package markoperator;

import org.json.JSONObject;

/**
 * Created by sunweijun on 18-3-13.
 */

public class MarkSegment {

    public static final int TYPE_GOAL = 0;
    public static final int TYPE_SHOOT = 1;
    public static final int TYPE_PENALTY = 2;
    public static final int TYPE_FREE = 3;
    public static final int TYPE_NOTHING = 4;

    private JSONObject content = null;
    private long start, end;
    private float speed;
    private String videoPath = null;
    private String name;
    private String subtitle = null;
    private int type = TYPE_NOTHING;

    public MarkSegment() {
        videoPath = "";
        content = new JSONObject();
    }

    public MarkSegment(String _videoPath, long _start, long _end) {
        content = new JSONObject();
        videoPath = _videoPath;
        start = _start;
        end = _end;
        speed = 1f;
        type = TYPE_NOTHING;
        subtitle = "";
        name = "";
        try {
            content.put("videoPath", videoPath);
            content.put("start", String.valueOf(start));
            content.put("end", String.valueOf(end));
            content.put("speed", String.valueOf(speed));
            content.put("type", String.valueOf(type));
            content.put("subtitle", subtitle);
            content.put("name", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MarkSegment(JSONObject _content) {
        content = _content;
        try {
            start = Long.parseLong(content.getString("start"));
            end = Long.parseLong(content.getString("end"));
            speed = Float.parseFloat(content.getString("speed"));
            videoPath = content.getString("videoPath");
            subtitle = content.getString("subtitle");
            name = content.getString("name");
            type = Integer.parseInt(content.getString("type"));
        } catch (Exception e) {
            e.printStackTrace();
            start = -1;
            end = -1;
            speed = -1f;
            videoPath = "";
            subtitle = "";
            name = "";
            type = TYPE_NOTHING;
        }
    }


    public void setValue(String key, String value)
    {
        if(key.equals("begin_time")) {
            start = Integer.parseInt(value);
        } else if(key.equals("end_time")) {
            end = Integer.parseInt(value);
        }
        else if(key.equals("speed")) {
            speed = Float.parseFloat(value);
        } else if(key.equals("type")) {
            type = Integer.parseInt(value);
        } else if(key.equals("subtitle")) {
            subtitle = value;
        } else if(key.equals("name")) {
            name = value;
        }

        try {
            content.put(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean has(String key) {
        return content.has(key);
    }

    public String opt(String key) {
        return content.optString(key);
    }

    public JSONObject getJson() {
        return content;
    }

    public String getName() {
        return name;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public long getSt() {
        return start;
    }

    public long getEd() {
        return end;
    }

    public int getType() {
        return type;
    }

    public float getSpeed() {
        return speed;
    }

    public String getSubtitle() {
        return subtitle;
    }
}

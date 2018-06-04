package markoperator;

import org.json.JSONObject;

/**
 * Created by sunweijun on 18-3-22.
 */

public class MarkSubTitle {
    private long start;
    private long end;
    private String text;

    public MarkSubTitle() {
        start = 0;
        end = 0;
        text = null;
    }

    public MarkSubTitle(JSONObject jsonObject) {
        text = jsonObject.optString("text");
        start = Long.parseLong(jsonObject.optString("start"));
        end = Long.parseLong(jsonObject.optString("end"));
    }

    public MarkSubTitle(String _text, long _start, long _end) {
        text = _text;
        start = _start;
        end = _end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }

    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("text", text);
            json.put("start", String.valueOf(start));
            json.put("end", String.valueOf(end));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}

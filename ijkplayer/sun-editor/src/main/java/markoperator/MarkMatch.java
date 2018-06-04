package markoperator;

import com.google.android.exoplayer.text.Subtitle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sunweijun on 18-3-13.
 */

public class MarkMatch {

    public ArrayList<MarkSegment> mark = null;
    public ArrayList<MarkSubTitle> sub = null;
    private String name = null;
    private String videoSource = null;
    private String author = null;
    private String bgm = null;

    public MarkMatch() {
        mark = new ArrayList<>();
        sub = new ArrayList<>();
        name = "";
        videoSource = "";
        author = "";
        bgm = "";
    }

    public MarkMatch(JSONObject _content) {
        JSONObject content = _content;
        mark = new ArrayList<>();
        sub = new ArrayList<>();
        name = content.optString("name");
        videoSource = content.optString("videoSource");
        author = content.optString("author");
        bgm = content.optString("bgm");

        JSONArray jsonArray = content.optJSONArray("match");
        for(int i = 0; i < jsonArray.length(); ++i) {
            MarkSegment segment = new MarkSegment(jsonArray.optJSONObject(i));
            mark.add(segment);
        }

        JSONArray jsonArray0 = content.optJSONArray("subTitle");

        for(int i = 0; i < jsonArray0.length(); ++i) {
            MarkSubTitle markSubTitle = new MarkSubTitle(jsonArray0.optJSONObject(i));
            sub.add(markSubTitle);
        }
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public void setVideoSource(String _videoSource) {
        videoSource = _videoSource;
    }

    public String getVideoSource() {
        return videoSource;
    }

    public void setAuthor(String _author) {
        author = _author;
    }

    public String getAuthor() {
        return author;
    }

    public void setBgm(String _bgm) {
        bgm = _bgm;
    }

    public String getBgm() {
        return bgm;
    }

    public String getString() {
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < mark.size(); ++i)
            jsonArray.put(mark.get(i).getJson());

        JSONArray jsonArray0 = new JSONArray();
        for(int i = 0; i < sub.size(); ++i)
            jsonArray0.put(sub.get(i).getJson());


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("match", jsonArray);
            jsonObject.put("subTitle", jsonArray0);
            jsonObject.put("name", name);
            jsonObject.put("videoSource", videoSource);
            jsonObject.put("author", author);
            jsonObject.put("bgm", bgm);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "error";
        }
    }

    public void addSegment(MarkSegment markSegment) {
        mark.add(markSegment);
    }

    public void addSubTitle(MarkSubTitle subTitle) {
        sub.add(subTitle);
    }

    public void addSegment(int i, MarkSegment markSegment) {
        mark.add(i, markSegment);
    }

    public MarkSegment getSegment(int i) {
        return mark.get(i);
    }

    public String getSubTitle(long current) {
        for(int i = 0; i < sub.size(); ++i) {
            if(sub.get(i).getStart() <= current && current <= sub.get(i).getEnd())
                return sub.get(i).getText();
        }
        return "";
    }

    public int size() {
        return mark.size();
    }
}

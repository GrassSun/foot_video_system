package file;

import android.graphics.Paint;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by sunweijun on 18-5-10.
 */

public class FFmpegExecuteListener extends ExecuteBinaryResponseHandler {

    private FileHelper recver = null;
    private JSONObject json;
    private FFmpeg ffmpeg = null;
    private int current = -1;
    private ArrayList<String> cmdList = null;

    public void setFileHelper(FileHelper _helper) {
        recver = _helper;
    }

    private void killRunningProcess() {
        if(ffmpeg != null)
        {
            if(ffmpeg.isFFmpegCommandRunning())
                ffmpeg.killRunningProcesses();
        }
    }

    public synchronized void setFFmpeg(FFmpeg _ffmpeg) {
        killRunningProcess();
        ffmpeg = _ffmpeg;
    }

    public synchronized void setCmdList(ArrayList<String> tempList) {
        killRunningProcess();
        cmdList = tempList;
        executeCmdList();
    }

    private void executeCmdList(String cmd) {
        try {
            ffmpeg.execute(cmd.split(" "), this);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void executeCmdList() {
        current = 0;
        executeCmdList(cmdList.get(current));
    }

    public FFmpegExecuteListener() {
        json = new JSONObject();
    }

    public void put(String key, String value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void put(String key, int value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(String message) {
        put("message", message);
        put("status", "onSuccess");
        recver.receiveMessage(json);
    }

    @Override
    public void onProgress(String message) {
        put("message", message);
        put("status", "onProgress");
        //recver.receiveMessage(json);
    }

    @Override
    public void onFailure(String message) {
        put("message", message);
        put("status", "onFailure");
        recver.receiveMessage(json);
    }

    @Override
    public void onStart() {
        put("status", "onStart");
        put("message", "START");
        recver.receiveMessage(json);
    }

    @Override
    public void onFinish() {
        if(json.optString("status").equals("onSuccess")) {
            ++current;

            put("status", "onFinish");
            put("message", "finish a command!");
            put("progress", String.valueOf((float)current / cmdList.size() ));
            recver.receiveMessage(json);

            if(current < cmdList.size()) {
                executeCmdList(cmdList.get(current));
            } else {
                put("message", "finish");
                put("status", "finish");
                recver.receiveMessage(json);
            }
        }
    }

}

package file;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import markoperator.MarkMatch;
import markoperator.MarkSegment;

/**
 * Created by sunweijun on 18-3-13.
 */

public class FileHelper {

    private Context mContext = null;
    private ContentResolver mContentResolver = null;
    private FFmpeg ffmpeg = null;
    private String username = "csy";
    private String password = "123456789";
    private static FileHelper mFileHelper = null;

    private FileHelper(Context _context) {
        mContext = _context;
        mContentResolver = mContext.getContentResolver();
    }

    public synchronized static FileHelper getInstance(Context mContext) {
        if(mFileHelper == null)
            mFileHelper = new FileHelper(mContext);
        return mFileHelper;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public File getSDCard() {
        return Environment.getExternalStorageDirectory();
    }

    public File getFilesDir() {
        return mContext.getFilesDir();
    }

    public File getCacheDir() {
        return mContext.getCacheDir();
    }

    public File getTestDir() {
        File file = new File(getSDCard().getAbsolutePath() + "/TEST");
        file.mkdirs();
        return file;
    }
/*
    public File getMusicDir() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return file;
    }
*/
    public File getVideoCacheDir() {
        File file = new File(getTestDir().getAbsolutePath() + "/videoCache");
        file.mkdirs();
        return file;
    }

    FFmpegExecuteListener ffmpegResponseListener = null;


    private ArrayList<String> getCommandString(MarkSegment segment, String output) {
        ArrayList<String> list = new ArrayList<>();

        String video = segment.getVideoPath();
        float start = segment.getSt() * 0.001f;
        float end = segment.getEd() * 0.001f;
        float duration = end - start;
        String ss = String.valueOf(start);
        String tt = String.valueOf(duration);

        File tempFile1 = new File(getVideoCacheDir().getAbsolutePath() + "/" + "temp1.mp4");
        File tempFile2 = new File(getVideoCacheDir().getAbsolutePath() + "/" + "temp2.mp4");
        File outputFile = new File(getVideoCacheDir().getAbsolutePath() + "/" + output);

        String cmd;
        cmd = ""+ "-ss " +  ss + ' '
                + "-t " + tt + ' '
                + "-accurate_seek" + ' '
                + "-i " + video + ' '
                + "-vcodec h264 -acodec aac -strict -2 -y "
                + tempFile1.getAbsolutePath();

        list.add(cmd);

        float speed = segment.getSpeed();

        float pts = 1 / segment.getSpeed();

        cmd = ""+ "-i " + tempFile1.getAbsolutePath() + ' '
                + "-an -filter:v setpts=" + String.valueOf(pts) + "*PTS" + ' '
                + "-y " + outputFile.getAbsolutePath();

        if(duration <= 2 * 3.0f) {

            list.add(cmd);

        } else {

            cmd = ""+ "-i " + tempFile1.getAbsolutePath() + ' '
                    + "-an -filter:v setpts=" + String.valueOf(pts) + "*PTS" + ' '
                    + "-y " + tempFile2.getAbsolutePath();

            list.add(cmd);

            cmd = ""+ "-i " + tempFile2.getAbsolutePath() + ' '
                    + "-an -filter:v fade=t=in:st=0:d=3.0" + ','
                    + "fade=t=out:st=" + String.valueOf(duration / speed - 3.0) + ":d=3.0" + ' '
                    + "-y " + outputFile.getAbsolutePath();

            list.add(cmd);

        }

        return list;
    }

    public void runnableMake(MarkMatch match) {

        // make video file
        // start
        String prefix = "output_of_media_segment_";
        String videoPath = getVideoCacheDir() + "/" + "video.mp4";
        String audioPath = getVideoCacheDir() + "/" + "audio.mp3";
        String totalPath = getVideoCacheDir() + "/" + "total.mp4";

        String bgm = match.getBgm();

        ArrayList<String> segmentFileName = new ArrayList<>();
        ArrayList<String> cmdList = new ArrayList<>();

        float duration = 0f;

        for(int i = 0; i < match.size(); ++i) {
            MarkSegment segment = match.getSegment((i));
            duration += ((float) segment.getEd() - segment.getSt()) / segment.getSpeed();
            String fileName = prefix + String.valueOf(i) + ".mp4";
            segmentFileName.add(fileName);
            ArrayList<String> tempList = getCommandString(segment, fileName);
            cmdList.addAll(tempList);
        }

        String data = "";

        for(int i = 0; i < match.size(); ++i) {
            data = data + "file" + " '" + getVideoCacheDir() + "/" + segmentFileName.get(i) +
                    "' " +
                    "" +
                    "\n";
        }

        File videoList = new File(getVideoCacheDir().getAbsolutePath() + "/videolist.txt");
        writeFileString(videoList, data);

        String cmd;
        String tempVideo = bgm.equals("")?totalPath:videoPath;
        cmd = ""+ "-f concat "
                + "-safe 0 "
                + "-i " + videoList.getAbsolutePath() + ' '
                + "-c copy -y "
                + tempVideo;
        cmdList.add(cmd);

        //make video file
        //end

        //make audio file
        //start

        float remain = 0f, step = 0f;
        int repeat = 0;

        if(!bgm.equals("")) {
            String bgmPath = getTestDir().getAbsolutePath() + "/" + bgm + ".mp3";
            String remainPath = getVideoCacheDir() + "/" + "remain.mp3";

            MediaPlayer musicPlayer = new MediaPlayer();
            try {
                musicPlayer.setDataSource(bgmPath);
                musicPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            step = musicPlayer.getDuration();
            musicPlayer.release();

            repeat = (int) (duration / step);
            remain = duration - repeat * step;
            data = "";

            for(int i = 0; i < repeat; ++i) {
                data = data + "file" + " '" + bgmPath +
                        "' " +
                        "" +
                        "\n";
            }

            File audioList = new File(getVideoCacheDir().getAbsolutePath() + "/audiolist.txt");

            if(remain > 1) {

                cmd = ""+ "-ss 0" + ' '
                        + "-t " + String.valueOf(remain / 1000) + ' '
                        + "-accurate_seek" + ' '
                        + "-i " + bgmPath + ' '
                        + "-y "
                        + remainPath;

                cmdList.add(0, cmd);

                data = data + "file" + " '" + remainPath +
                        "' " +
                        "" +
                        "\n";
            }

            writeFileString(audioList, data);

            cmd = ""+ "-f concat "
                    + "-safe 0 "
                    + "-i " + audioList.getAbsolutePath() + ' '
                    + "-c copy -y "
                    + audioPath;
            cmdList.add(cmd);
            cmd = ""+ "-i " + audioPath + ' '
                    + "-i " + videoPath + ' '
                    + "-strict -2 -y" + ' '
                    + totalPath;
            cmdList.add(cmd);
        }
        //end
        //make audio file

        try {
            ffmpeg =  FFmpeg.getInstance(mContext);
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}
                @Override
                public void onFailure() {}
                @Override
                public void onSuccess() {}
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        ffmpegResponseListener = new FFmpegExecuteListener();
        ffmpegResponseListener.setFileHelper(this);
        ffmpegResponseListener.setFFmpeg(ffmpeg);
        ffmpegResponseListener.setCmdList(cmdList);

    }


    public void makeMediaFile(MarkMatch match) {

        final MarkMatch data = match;

        new Thread(new Runnable() {
            @Override
            public void run() {
                runnableMake(data);
            }
        }).start();
    }



    public String getFileString(File file) {
        String res = "";

        try {

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while( (line = br.readLine()) != null)
                res += line + "\n";
            br.close();
            fr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public void writeFileString(File file, String data) {
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(data);
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeMatchFile(String title) {
        String path = getMatchDir().getAbsolutePath() + "/" + title + ".json";
        File file = new File(path);
        if(file.exists() && file.isFile()) {
            file.delete();
        }
    }

    public void writeMatchFile(MarkMatch markMatch) {
        String data = markMatch.getString();
        try {
            String path = getMatchDir().getAbsolutePath();
            path += '/' + markMatch.getName() + ".json";
            File file = new File(path);
            writeFileString(file, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public File getSourceDir() {
        String path = getFilesDir().getAbsolutePath() + "/videoSource";
        File sourceDir = new File(path);
        if(!sourceDir.exists())
            sourceDir.mkdirs();
        return sourceDir;
    }


    public void writeVideoSource(String name, JSONObject json) {
        String path = getSourceDir().getAbsolutePath();
        File file = new File(path + '/' + name + ".txt");
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            String data = json.toString();
            bw.write(data);
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ArrayList<String> > parseVideoSource(String data) {
        ArrayList<ArrayList<String> > res = new ArrayList<>();
        ArrayList<String> north, middle, south;
        north = new ArrayList<>();
        middle = new ArrayList<>();
        south = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(data);
            JSONArray jsonNorth = json.getJSONArray("north");
            JSONArray jsonMiddle = json.getJSONArray("middle");
            JSONArray jsonSouth = json.getJSONArray("south");

            for(int i = 0; i < jsonNorth.length(); ++i)
                north.add(jsonNorth.getString(i));

            for(int i = 0; i < jsonMiddle.length(); ++i)
                middle.add(jsonMiddle.getString(i));

            for(int i = 0; i < jsonSouth.length(); ++i)
                south.add(jsonSouth.getString(i));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        res.add(north);
        res.add(middle);
        res.add(south);
        return res;
    }

    public ArrayList<String> getSourceList() {
        ArrayList<String> res = new ArrayList<>();
        File sourceDir = getSourceDir();
        File[] mList = sourceDir.listFiles();
        for(int i = 0; i < mList.length; ++i) {
            String filename = mList[i].getName();
            int dot = filename.lastIndexOf('.');
            if(dot < 0 || dot >= filename.length())
                continue;
            String text = filename.substring(dot + 1);
            if(!text.equals("txt"))
                continue;
            text = filename.substring(0, dot);
            res.add(text);
        }
        return res;
    }

    public ArrayList<String> getMusicList() {
        ArrayList<String> res = new ArrayList<>();/*
        File musicDir = getMusicDir();
        File[] mList = musicDir.listFiles();
        for(int i = 0; i < mList.length; ++i) {
            String filename = mList[i].getName();
            int dot = filename.lastIndexOf('.');
            if(dot < 0 || dot >= filename.length())
                continue;
            String text = filename.substring(dot + 1);
            if(!text.equals("mp3"))
                continue;
            text = filename.substring(0, dot);
            res.add(text);
        }*/

        Cursor c = null;
        try {
            c = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));// 路径
                File file = new File(path);
                if(file.exists() && path.contains(".mp3"))
                    res.add(path);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return res;
    }

    private ArrayList<String> urlList;

    private ArrayList<String> nameList;

    public void markTestArrayList() {
        urlList = new ArrayList<>();
        nameList = new ArrayList<>();

        FileHelper fileHelper = FileHelper.getInstance(mContext);

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8");
        nameList.add("flv0");



//        urlList.add("http://166.111.68.66:28080/video/test.flv/");
//        nameList.add("flv0");

        for(int i = 1; i <= 4; ++i) {

            String test = fileHelper.getTestDir().getAbsolutePath()
                    + "/test" + String.valueOf(i) + ".mp4";

            urlList.add(test);
            nameList.add("TEST" + String.valueOf(i));
        }

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8");
        nameList.add("bipbop basic master playlist");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8");
        nameList.add("bipbop basic 400x300 @ 232 kbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8");
        nameList.add("bipbop basic 640x480 @ 650 kbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8");
        nameList.add("bipbop basic 640x480 @ 1 Mbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear4/prog_index.m3u8");
        nameList.add("bipbop basic 960x720 @ 2 Mbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8");
        nameList.add("bipbop basic 22.050Hz stereo @ 40 kbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8");
        nameList.add("bipbop advanced master playlist");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear1/prog_index.m3u8");
        nameList.add("bipbop advanced 416x234 @ 265 kbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear2/prog_index.m3u8");
        nameList.add("bipbop advanced 640x360 @ 580 kbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear3/prog_index.m3u8");
        nameList.add("bipbop advanced 960x540 @ 910 kbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear4/prog_index.m3u8");
        nameList.add("bipbop advanced 1289x720 @ 1 Mbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear5/prog_index.m3u8");
        nameList.add("bipbop advanced 1920x1080 @ 2 Mbps");

        urlList.add("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear0/prog_index.m3u8");
        nameList.add("bipbop advanced 22.050Hz stereo @ 40 kbps");
    }

    public ArrayList<String> getUrlList() {
        return urlList;
    }

    public ArrayList<String> getNameList() {
        return nameList;
    }

    public void makeVideoSource() {
        markTestArrayList();

        ArrayList<String> url = getUrlList();

        JSONObject list = new JSONObject();
        JSONArray north = new JSONArray();
        JSONArray south = new JSONArray();
        JSONArray middle = new JSONArray();

        north.put(url.get(1));
        north.put(url.get(2));
        south.put(url.get(1));
        south.put(url.get(3));
        middle.put(url.get(2));
        middle.put(url.get(4));

        try {
            list.put("north", north);
            list.put("south", south);
            list.put("middle", middle);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeVideoSource("match1", list);

    }

    public void makeTestMarkMatch() {

        makeVideoSource();
        markTestArrayList();
        ArrayList<String> url = getUrlList();
        ArrayList<String> name = getNameList();
/*
        MarkMatch matchTemp;
        matchTemp = new MarkMatch();
        matchTemp.addSegment(new MarkSegment(url.get(1), 1, 3000));
        matchTemp.addSegment(new MarkSegment(url.get(1), 7000, 10000));
        matchTemp.setName("mark1");
        matchTemp.setVideoSource("match1");
        matchTemp.addSubTitle(new MarkSubTitle("text1", 1, 3000));
        matchTemp.addSubTitle(new MarkSubTitle("text2", 3000, 6000));
        writeMatchFile(matchTemp);

        matchTemp = new MarkMatch();
        matchTemp.addSegment(new MarkSegment(url.get(1), 1, 3000));
        matchTemp.addSegment(new MarkSegment(url.get(1), 7000, 10000));
        matchTemp.setVideoSource("match1");
        matchTemp.setName("mark2");
        matchTemp.addSubTitle(new MarkSubTitle("text1", 1, 3000));
        matchTemp.addSubTitle(new MarkSubTitle("text2", 3000, 6000));
        writeMatchFile(matchTemp);*/
    }

    public File getMatchDir() {
        String path = getFilesDir().getAbsolutePath() + "/match";
        File matchDir = new File(path);
        if(!matchDir.exists())
            matchDir.mkdirs();
        return matchDir;
    }

    public ArrayList<MarkMatch> getMatchList() {
        ArrayList<MarkMatch> mList = new ArrayList<>();

        File matchDir = getMatchDir();

        if(!matchDir.isDirectory())
            return mList;

        File[] fileList = matchDir.listFiles();

        for(int i = 0; i < fileList.length; ++i) {
            String json = getFileString(fileList[i]);
            MarkMatch markMatch;
            try {
                markMatch = new MarkMatch(new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
                markMatch = new MarkMatch();
            }
            mList.add(markMatch);
        }

        return mList;
    }

    public void receiveMessage(JSONObject json) {

        String status = json.optString("status");
        float progress;
        if(status.equals("onFinish")) {
            progress = Float.parseFloat(json.optString("progress"));
            Log.d("progress", String.valueOf(100 * progress) + "%");
        } else if(status.equals("finish")) {
            Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
        }
        Log.d("STATUS", status);
        //Log.d("MESSAGE", json.optString("message"));
    }
}

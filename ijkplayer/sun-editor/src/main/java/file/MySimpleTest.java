package file;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sunweijun on 18-3-20.
 */

public class MySimpleTest {
    private Context mContext;
    private final String TAG = "SimpleTest";
    private static MySimpleTest mSimpleTest = null;

    private MySimpleTest(Context _context) {
        mContext = _context;
    }

    public static synchronized MySimpleTest getInstance(Context context) {
        if(mSimpleTest == null)
            mSimpleTest = new MySimpleTest(context);
        return mSimpleTest;
    }

    public void simpleTest() {
        Log.i(TAG, "BEGIN");
        Log.i(TAG, "-------------------------------------------------");

        FileHelper fileHelper = FileHelper.getInstance(mContext);

        Log.i(TAG, "SD = " + fileHelper.getSDCard().getAbsolutePath());
        Log.i(TAG, "FilesDir = " + fileHelper.getFilesDir().getAbsolutePath());
        Log.i(TAG, "DataDir = " + fileHelper.getCacheDir().getAbsolutePath());

        String video = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear4/prog_index.m3u8";
        //fileHelper.getMediaSegment(video, 10.5f, 0.5f);

        Log.i(TAG, "-------------------------------------------------");
        Log.i(TAG, "END");
    }

    public void testJson() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put("username", "ugly");
                    json.put("password", "ugly");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String urlPath = "http://166.111.68.66:28000";
                String loginPath = urlPath + "/login/";
                JSONObject response = HttpRequest.submitPostData(loginPath,
                        json, "utf-8");
                Log.i("ST", response.optString("response"));
            }
        }).start();
    }
}

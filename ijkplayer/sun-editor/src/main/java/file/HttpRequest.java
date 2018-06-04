package file;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sunweijun on 18-5-14.
 */

public class HttpRequest {

    public static JSONObject submitPostData(String strUrlPath, JSONObject json, String encode) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        JSONObject res = new JSONObject();

        try {
            res.put("statusCode", 32767);
            res.put("response", "Http Request Error!");
            URL url = new URL(strUrlPath);
            urlConnection = (HttpURLConnection) url.openConnection();

//            urlConnection.setRequestProperty("Connection", "keep-alive");
//            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) " +
//                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
//                    " Chrome/64.0.3282.186 Safari/537.36");
//            urlConnection.setRequestProperty("Content-Type", "application/json");
//            urlConnection.setRequestProperty("Charset", "utf-8");
//            urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml," +
//                    "application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//            urlConnection.setRequestProperty("Accept-Language", "zh-CN,zh,q=0.9");
//            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
//            urlConnection.setRequestProperty("User-Agent", "curl/7.47.0");
            urlConnection.setRequestProperty("Accept", "*/*");
            urlConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
//                    "application/json; charset=UTF-8");
//                    "text/plain");
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            DataOutputStream wr;
            wr = new DataOutputStream(urlConnection.getOutputStream());

            Iterator<String> ite = json.keys();
            StringBuffer urlCode = new StringBuffer();

            while(ite.hasNext()) {
                String key = ite.next();
                urlCode.append(key + "=" + json.optString(key) + "&");
            }
            if(urlCode.length() > 0)
                urlCode.deleteCharAt(urlCode.length() - 1);

            String jsonData = urlCode.toString();
            wr.write(jsonData.getBytes(encode));
            urlConnection.connect();
            wr.flush();
            wr.close();
            // try to get response
            int statusCode = urlConnection.getResponseCode();

            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            StringBuffer response = new StringBuffer();
            byte[] data = new byte[1024];
            int length;
            while((length = inputStream.read(data, 0, data.length)) != -1)
                response.append(new String(data, 0, length, encode));
            res.put("statusCode", statusCode);
            res.put("response", response.toString());

            return res;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally
        {

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (urlConnection != null) {
                urlConnection.disconnect();
            }

        }
        return json;
    }

}

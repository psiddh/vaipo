package app.com.vaipo.rest;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Created by siddartha on 12/23/15.
 */
public class RestAPI {

    private static String TAG = "RestAPI";
    private final String BASE_URL = "http://vaipo.herokuapp.com";

    public static String REGISTER = "register";
    public static String CALL = "call";
    public static String USERACK = "userack";
    public static String AUTHENTICATE = "authenticate";
    public static String RESEND = "resend";


    private AsyncTask<Void, Void, Integer> postRestCallAsync(final String baseUrl, final String message, final String msg, final onPostCallBackDone callback) {
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            public Integer status;
            @Override
            protected Integer doInBackground(Void... params) {
                Log.i(TAG, "postMessageAsync: " + msg + ")");
                try {
                    status = post(baseUrl + "/" + message, msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return status;
            }

            @Override
            protected void onPostExecute(Integer status) {
                if (callback != null)
                    callback.onResult(status);
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null, null);

        return task;
    }

    private Integer post(String endpoint, String message)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        Log.v(TAG, "Posting '" + message + "' to " + url);
        byte[] bytes = message.getBytes();
        HttpURLConnection conn = null;
        int status;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("connection", "close");

            //conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");

           /* // Create the SSL connection
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLSv1");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            if (null == sc)
                return null;;
            try {
                sc.init(null, null, new java.security.SecureRandom());
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            conn.setSSLSocketFactory(sc.getSocketFactory());*/
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            status = conn.getResponseCode();
            if(conn.getContentLength() > 0) {
                byte[] buffer = new byte[conn.getContentLength()];
                conn.getInputStream().read(buffer);
                return status;
            }

            if (status != 200 && conn.getContentLength() > 0) {
                byte[] buffer = new byte[conn.getContentLength()];
                int is = conn.getInputStream().read(buffer);
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return status;
    }

    public RestAPI() {

    }

    public interface onPostCallBackDone {
        public void onResult(Integer result);
    }

    public AsyncTask<Void, Void, Integer> call(final String message, final String msgParams, final onPostCallBackDone callback) {
        return postRestCallAsync(BASE_URL, message, msgParams, callback);
    }
}

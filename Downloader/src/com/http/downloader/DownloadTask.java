
package com.http.downloader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 下载任务
 * <p>
 * 参数为url及保存路径
 */
public class DownloadTask extends
        AsyncTask<String, Integer, State> {
    public static final int REQUEST_TIMEOUT = 60 * 1000;

    public interface Callback {
        public void onStarteded(long contentLength);
        public void onProgressUpdate(int progress, int count);
        public void onFinished(State state, String filePath, String errorMsg);
    }

    private static final String TAG = "DownloadTask";
    private static final int PARAMS_NUM = 2;
    private Context mContext = null;
    private Callback mCallback = null;
    private boolean debug = false;
    String errorMsg = null;
    String url;
    String filePath;
    private boolean mCanceled = false;

    public DownloadTask(Context context, Callback cb) {
        mContext = context;
        mCallback = cb;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "pre execute");
    }

    private String getDefultFileName() {
        String fileName = "temp.tmp";
        if (url != null && url.length() > 0) {
            fileName = url.substring(url.lastIndexOf(File.separator) + 1);
        }
        return fileName;
    }

    private String getStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }

    public String getDefultFilePath() {
        return getStorageDir() + getDefultFileName();
    }

    @Override
    protected State doInBackground(String... params) {
        if (params == null || PARAMS_NUM != params.length) {
            throw new IllegalArgumentException(TAG + " only accepts " + PARAMS_NUM
                    + " params.");
        }
        url = params[0];
        filePath = params[1];
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url must be not null");
        }
        if (TextUtils.isEmpty(filePath)) {
            filePath = getDefultFilePath();
            //throw new IllegalArgumentException("download path is null.");
        }
        if (mCanceled) {
            return State.CANCELD;
        }
        State state = State.UNKOWN_ERROR;
        HttpClient httpClient = getHttpClient(mContext);
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(request);
            if (response == null
                    || HttpStatus.SC_OK != response.getStatusLine()
                            .getStatusCode()) {
                throw new IOException("url connection error.");
            }
            long fileLength = response.getEntity().getContentLength();
            if (fileLength <= 0) {
                throw new IOException("download file length is negative.");
            }
            if (mCallback != null) {
                mCallback.onStarteded(fileLength);
            }
            Log.i(TAG, "file length:" + fileLength);
            
            InputStream input = null;
            OutputStream output = null;
            try {
                if (mCanceled) {
                    return State.CANCELD;
                }
                filePath = createFilePath(filePath, url);
                if (debug) Log.i(TAG, "url:" + url + ", file path:" + filePath);
                File file = new File(filePath);
                input = response.getEntity().getContent();
                output = new BufferedOutputStream(
                        new FileOutputStream(file));
                byte[] buffer = new byte[4 * 1024];
                int read = -1;
                int count = 0;
                int progress = 0;
                while ((read = input.read(buffer)) != -1) {
                    if (mCanceled) {
                        file.delete();
                        return State.CANCELD;
                    }
                    output.write(buffer, 0, read);
                    count += read;
                    int pro = (int) (count * 100 / fileLength);
                    if(pro != progress) {
                        progress = pro;
                        publishProgress(progress, count);
                    }
                }
                output.flush();
                state = State.SUCCESS;
            } catch (IOException e) {
                e.printStackTrace();
                state = State.FILE_IO_ERROR;
                errorMsg = "File IOException: " + e.getMessage();
            } finally {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            state = State.HTTP_IO_ERROR;
            errorMsg = "Connection IOException: " + e.getMessage();
        }
        return state;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        int count = values[1];
        if(debug ) Log.i(TAG, "progress:" + progress);
        if (mCallback != null) {
            mCallback.onProgressUpdate(progress, count);
        }
    }

    protected void onPostExecute(State state) {
        Log.i(TAG, "post execute state:" + state);
        if (mCallback != null) {
            mCallback.onFinished(state, filePath, errorMsg);
        }
    }

    public HttpClient getHttpClient(Context context) {
        return getHttpClient(context, REQUEST_TIMEOUT, REQUEST_TIMEOUT);
    }

    public HttpClient getHttpClient(Context context, int connectionTimeout, int socketTimeout) {
        BasicHttpParams params = new BasicHttpParams();
        // 设置连接及响应超时时间(s)
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, socketTimeout);
        return new DefaultHttpClient(params);
    }

    public String createFilePath(String filePath, String url) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            filePath = filePath + "/" + getDefultFileName();
        }
        if(file.isFile() && file.exists()) {
            file.delete();
            System.out.println("DELETE file: " + filePath);
        }
        File dir = new File(file.getParent());
        if(!dir.exists()) {
            dir.mkdirs();
        }
        try {
            boolean res = file.createNewFile();
            System.out.println("createNewFile: " + res + ", file: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public void setCanceled(boolean canceled) {
        mCanceled  = canceled;
    }
}

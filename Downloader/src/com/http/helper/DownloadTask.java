package com.http.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Environment;

public class DownloadTask implements Runnable {

    public interface DownloadCallback {
        void onDownloadStart(int taskId, int fileLength);
        void onDownloadProgress(int taskId, int progress, int progressCount);
        void onDownloadFinished(int taskId, String filePath, String errorMsg);
    }

    private int taskId = 0;
    private String url;
    private String fileDir;
    private String fileName;
    
    private int timeoutMillis;
    private DownloadCallback callback;
    private boolean canceled = false;

    public DownloadTask(String url) {
        this.url = url;
        fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        fileName = url.substring(url.lastIndexOf(File.separator) + 1);
    }

    public DownloadTask(String url, String fileDir, String fileName) {
        this.url = url;
        this.fileDir = fileDir;
        this.fileName = fileName;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        File dir = new File(fileDir);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = fileDir + fileName;
        File file = new File(filePath);
        if(file.exists()) {
            deleteFile(file);
            System.out.println("DELETE file: " + filePath);
        }
        try {
            boolean res = file.createNewFile();
            System.out.println("createNewFile: " + res + ", file: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private void deleteFile(File file) {
        if(file != null && file.exists()) {
            file.delete();
        }
    }

    public DownloadCallback getCallback() {
        return callback;
    }

    public void setCallback(DownloadCallback callback) {
        this.callback = callback;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public void run() {
        downloadFile();
    }

    public boolean downloadFile() {
        InputStream input = null;
        FileOutputStream output = null;
        String errorMsg = null;
        String filePath = null;
        try {
            if (canceled) return false;
            URL _url = new URL(url);
            HttpURLConnection urlConn = (HttpURLConnection) _url
                    .openConnection();
            urlConn.setConnectTimeout(timeoutMillis);
            int len = urlConn.getContentLength();
            if(callback != null) {
                callback.onDownloadStart(taskId, len);
            }
            if(len <= 0) {
                errorMsg = "Content Length is 0";
                return false;
            }
            input = urlConn.getInputStream();
            int count = 0;
            if(input != null) {
                filePath = getFilePath();
                File file = new File(filePath);
                output = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer);
                    count += read;
                    if(callback != null) {
                        int progress = (count * 100) / len;
                        callback.onDownloadProgress(taskId, progress, count);
                    }
                    if (canceled) {
                        deleteFile(file);
                        output.close();
                        return false;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(output != null) {
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(callback != null) {
                callback.onDownloadFinished(taskId, filePath, errorMsg);
            }
        }
        return true;
    }
}

package com.http.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Environment;

public class Downloader implements Runnable {

    public interface DownloadCallback {
        void onDownloadStart(int contentLength);
        void onDownloadProgress(int progress, int progressCount);
        void onDownloadFinished(State state, String filePath, String errorMsg);
    }

    private String url;
    private String filePath;
    
    private DownloadCallback callback;
    private boolean canceled = false;

    public Downloader(String url) {
        this.url = url;
        filePath = getDefultFilePath();
    }

    public Downloader(String url, String filePath) {
        this.url = url;
        this.filePath = filePath;
    }

    private String getStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }

    public String getDefultFilePath() {
        return getStorageDir() + getDefultFileName();
    }

    public String getDefultFileName() {
        String fileName = "temp.tmp";
        if (url != null && url.length() > 0) {
            fileName = url.substring(url.lastIndexOf(File.separator) + 1);
        }
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String createFilePath(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            filePath = getDefultFilePath();
        }
        File file = new File(filePath);
        if (file.isDirectory()) {
            filePath = filePath + "/" + getDefultFileName();
        }
        if(file.isFile() && file.exists()) {
            deleteFile(file);
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
        int count = 0;
        int progress = 0;
        State state = State.UNKOWN_ERROR;
        try {
            if (canceled) return false;
            URL _url = new URL(url);
            HttpURLConnection urlConn = (HttpURLConnection) _url
                    .openConnection();
            int len = urlConn.getContentLength();
            if(callback != null) {
                callback.onDownloadStart(len);
            }
            if(len <= 0) {
                errorMsg = "Content Length is 0";
                return false;
            }
            input = urlConn.getInputStream();
            if(input != null) {
                try {
                    filePath = createFilePath(filePath);
                    File file = new File(filePath);
                    output = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer);
                        count += read;
                        int pro = (count * 100) / len;
                        if(progress != pro) {
                            progress = pro;
                            if(callback != null) {
                                callback.onDownloadProgress(progress, count);
                            }
                        }
                        if (canceled) {
                            deleteFile(file);
                            break;
                        }
                    }
                    output.flush();
                    state = State.SUCCESS;
                } catch (IOException e) {
                    e.printStackTrace();
                    errorMsg = e.getMessage();
                    state = State.FILE_IO_ERROR;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            state = State.HTTP_IO_ERROR;
            errorMsg = e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            errorMsg = e.getMessage();
            state = State.HTTP_IO_ERROR;
        } finally {
            if(input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(callback != null) {
                callback.onDownloadFinished(state, filePath, errorMsg);
            }
        }
        return true;
    }
}

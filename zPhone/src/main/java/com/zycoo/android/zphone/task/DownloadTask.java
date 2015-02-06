
package com.zycoo.android.zphone.task;

import android.os.AsyncTask;

import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.ZphoneApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadTask extends AsyncTask<String, Integer, Boolean> {
    public static final int BUF_SIZE = 1024;
    private Logger mLogger = LoggerFactory.getLogger(DownloadTask.class);
    private String mUrl;
    private String mSavePath;

    @Override
    protected void onPreExecute() {
        //download confirm
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result)
        {

        }
        else
        {

        }
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (2 != params.length)
        {
            mLogger.error("DownloadTask argv error");
            return false;
        }
        else
        {
            mUrl = params[0];
            mSavePath = params[1];
            int bytesRead = 0;
            int downloadSize = 0;
            byte[] buffer = new byte[BUF_SIZE];
            try {
                InputStream in = Engine.getInstance().getHttpClientService().getBinary(
                        ZphoneApplication.getHttpSite(mUrl));
                OutputStream os = new FileOutputStream(new File(mSavePath));
                while ((bytesRead = in.read(buffer, 0, BUF_SIZE)) != -1) {
                    publishProgress(downloadSize);
                    downloadSize += bytesRead;
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                mLogger.error("DownloadTask error: " + e.getMessage());
                return false;
            }
        }
        return true;
    }
}

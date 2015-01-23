
package com.zycoo.android.zphone;

import org.doubango.ngn.services.INgnConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public abstract class HttpConnectBase implements Runnable
{
    private static final int BUFFER_SIZE = 1024;
    protected InputStream mInputStream;
    protected BufferedReader mBufferedReader;
    protected URL mUrl;
    protected URLConnection mUrlConnection;
    protected String mUsername, mPassword, mHost, mPort;
    protected INgnConfigurationService mConfigurationService;
    protected byte[] mByteBuffer;
    protected Logger mLogger;

    public HttpConnectBase()
    {
        mLogger = LoggerFactory.getLogger(HttpConnectBase.class);
        mByteBuffer = new byte[BUFFER_SIZE];
        mConfigurationService = Engine.getInstance().getConfigurationService();
        mHost = ZphoneApplication.getHost();
        mUsername = ZphoneApplication.getUserName();
    }

    public abstract void doGet();

    public abstract void doPost();

    public boolean createConnect(String urlStr)
    {
        try
        {
            cleanConnect();
            mLogger.info("http request url " + urlStr);
            mUrl = new URL(urlStr);
            mUrlConnection = (HttpURLConnection) mUrl.openConnection();
            mInputStream = mUrlConnection.getInputStream();
        } catch (IOException e)
        {
            mLogger.debug(e.getMessage());
            return false;
        }
        return true;
    }

    public void cleanConnect()
    {
        try
        {
            if (null != mInputStream)
            {
                mInputStream.close();
            }
            if (null != mBufferedReader)
            {
                mBufferedReader.close();
            }
        } catch (Exception e)
        {

            mLogger.error(e.getMessage());
        }
    }
}

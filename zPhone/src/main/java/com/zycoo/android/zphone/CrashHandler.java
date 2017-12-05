
package com.zycoo.android.zphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class CrashHandler implements UncaughtExceptionHandler
{
    public static final String LOG_TAG = CrashHandler.class.getCanonicalName();
    private static final String PATH_CRASH_LOG = "/sdcard/zycoo/crash/";
    // 默认UncaughtException处理,当CrashHandler处理失败时调用默认处理
    private UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    // 单例模式
    private static class CrashHandlerInstance
    {
        private static final CrashHandler instance = new CrashHandler();
    }

    private Context mContext;
    // 保存存储设备信息和异常信息
    private Map<String, String> mInfos = new HashMap<String, String>();
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",
            Locale.getDefault());

    private CrashHandler()
    {
    }

    public static CrashHandler getInstance()
    {
        return CrashHandlerInstance.instance;
    }

    public void init(Context context)
    {
        mContext = context;
        mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置Crashhandler为程序的默认处理方式
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread arg0, Throwable arg1)
    {

        // Crashhandler 处理失败后调用默认处理方式
        if (null != mDefaultUncaughtExceptionHandler && !handleException(arg1, PATH_CRASH_LOG))
        {
            mDefaultUncaughtExceptionHandler.uncaughtException(arg0, arg1);
        }
        // android.os.Process.killProcess(android.os.Process.myPid());
        mDefaultUncaughtExceptionHandler = null;
        ActivityManager activityMgr = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        activityMgr.killBackgroundProcesses(mContext.getPackageName());
        System.runFinalization();
        //NewIMSDroidApplication.getInstance().onTerminate();
    }

    private boolean handleException(Throwable ex, String path)
    {
        boolean result = true;
        if (null == ex)
        {
            result = false;
        }
        else
        {
            /*
             * // 处理异常 new Thread() {
             * @Override public void run() { Looper.prepare();
             * Toast.makeText(mContext, "程序异常，请联系tqcenglish@gmail.com",
             * Toast.LENGTH_LONG) .show(); Looper.loop(); super.run(); }
             * }.start(); //添加延时， Toast显示 SystemClock.sleep(1000);
             */
            Log.e(LOG_TAG, ex.toString());
            try
            {
                Log.d(LOG_TAG, "saveCrashInfo2File");
                // 收集设备信息
                collectDeviceInfo(mContext);
                // 保存异常日志
                saveCrashInfo2File(ex, path);
            } catch (Exception e)
            {
                Log.e(LOG_TAG, "error");
                if (e instanceof IOException)
                {
                    Log.e(LOG_TAG, "saveCrashInfo2File" + e.getMessage());
                }
                if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException)
                {
                    Log.e(LOG_TAG, "collectDeviceInfo" + e.getMessage());
                }
                result = false;
            }
        }
        return result;
    }

    private void saveCrashInfo2File(Throwable ex, String path) throws IOException
    {
        FileOutputStream fos = null;
        IOException ioException = null;
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : mInfos.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key + " : " + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (null != cause)
        {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.flush();
        printWriter.close();
        String result = writer.toString();
        stringBuffer.append(result);
        long timeStamp = System.currentTimeMillis();
        String time = mSimpleDateFormat.format(new Date());
        String fileName = "crash-" + time + "-" + timeStamp + ".log";
        Log.d(LOG_TAG, "file_name" + fileName);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            File dir = new File(path);
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            try
            {
                fos = new FileOutputStream(path + fileName);
                fos.write(stringBuffer.toString().getBytes());
            } catch (IOException e)
            {
                ioException = e;
            } finally
            {
                if (null != fos)
                {
                    try
                    {
                        fos.close();
                    } catch (IOException e2)
                    {
                        if (null == ioException)
                        {
                            ioException = e2;
                        }
                    }
                }
            }
            if (null != ioException)
            {
                throw ioException;
            }
        }
    }

    private void collectDeviceInfo(Context context) throws IllegalAccessException,
            IllegalArgumentException
    {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(context.getPackageName(),
                PackageManager.GET_ACTIVITIES);
        if (null != pi)
        {
            String versionName = pi.versionName == null ? "null" : pi.versionName;
            String versionCode = pi.versionCode + "";
            mInfos.put("versionName", versionName);
            mInfos.put("versionCode", versionCode);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields)
        {
            field.setAccessible(true);
            mInfos.put(field.getName(), field.get(null).toString());
        }
    }

    /**
     * 网络是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager mgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (info != null)
        {
            for (int i = 0; i < info.length; i++)
            {
                if (info[i].getState() == NetworkInfo.State.CONNECTED)
                {
                    return true;
                }
            }
        }
        return false;
    }
}

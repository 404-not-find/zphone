
package com.zycoo.android.zphone.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * The <tt>AndroidUtils</tt> class provides a set of utility methods allowing an
 * easy way to show an alert dialog on android, show a general notification,
 * etc.
 */
@SuppressLint("NewApi")
public class AndroidUtils
{
    private static final String LOG_TAG = AndroidUtils.class.getSimpleName();
    /**
     * Api level constant. Change it here to simulate lower api on new devices.
     * All API level decisions should be done based on {@link #hasAPI(int)} call
     * result.
     */
    private static final int API_LEVEL = Build.VERSION.SDK_INT;

    /**
     * Returns <tt>true</tt> if this device supports at least given API level.
     * 
     * @param minApiLevel API level value to check
     * @return <tt>true</tt> if this device supports at least given API level.
     */
    public static boolean hasAPI(int minApiLevel)
    {
        return API_LEVEL >= minApiLevel;
    }

    /**
     * Returns <tt>true</tt> if current <tt>Thread</tt> is UI thread.
     * 
     * @return <tt>true</tt> if current <tt>Thread</tt> is UI thread.
     */
    public static boolean isUIThread()
    {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static void getOverflowMenu(Context context)
    {
        try
        {
            ViewConfiguration config = ViewConfiguration.get(context);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null)
            {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void addLegacyOverflowButton(Window window)
    {
        if (window.peekDecorView() == null)
        {
            throw new RuntimeException("Must call addLegacyOverflowButton() after setContentView()");
        }
        try
        {
            window.addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY")
                    .getInt(null));
        } catch (NoSuchFieldException e)
        {
            // Ignore since this field won't exist in most versions of Android
        } catch (IllegalAccessException e)
        {
            Log.w(LOG_TAG, "Could not access FLAG_NEEDS_MENU_KEY in addLegacyOverflowButton()", e);
        }
    }

    public static int getScreenWidth()
    {
        WindowManager wm = (WindowManager) ZphoneApplication.getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.x;
    }

    /**
     * 
     * @author tqcenglish
     * @param time  String or Int
     * @return
     */
    public static String formatIntToTimeStr(Object time)
    {
        long second;
        if (time instanceof String)
        {
            second = Integer.parseInt((String) time);
        }
        else if (time instanceof Integer)
        {
            second = ((Integer) time).longValue();
        }
        else
        {
            second = (long) time;
        }

        StringBuilder stringBuilder = new StringBuilder();
        int hour = 0;
        int minute = 0;
        if (second > 60)
        {
            minute = (int)second / 60;
            second = second % 60;
        }
        if (minute > 60)
        {
            hour = minute / 60;
            minute = minute % 60;
        }
        if (hour > 0)
        {
            stringBuilder.append(hour
                    + ZphoneApplication.getAppResources().getString(R.string.hour));
        }
        if (minute > 0)
        {
            stringBuilder.append(minute
                    + ZphoneApplication.getAppResources().getString(R.string.minute));
        }
        if (second > 0)
        {
            stringBuilder.append(second
                    + ZphoneApplication.getAppResources().getString(R.string.second));
        }
        return stringBuilder.toString();
    }

    public static String utc2Local(String utcTime)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss",
                Locale.getDefault());
        return simpleDateFormat.format(Long.parseLong(utcTime) * 1000);
    }

    // OverflowShow显示图标
    public static void onMenuOpened(int featureId, Menu menu)
    {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try
                {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible",
                            Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e)
                {
                }
            }
        }
    }

    // 统一OverflowShow样式
    public static void setOverflowShowingAlways(Context context)
    {
        try
        {
            ViewConfiguration config = ViewConfiguration.get(context);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

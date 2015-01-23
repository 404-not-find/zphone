/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zycoo.android.zphone.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;
import com.zycoo.android.zphone.ui.LaunchActivity;
import com.zycoo.android.zphone.ui.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains static utility methods.
 */

/**
 * http://developer.android.com/training/contacts-provider/display-contact-badge
 * .html
 *
 * @author tqcenglish Oct 15, 2014 2:07:08 PM
 */
public class Utils {
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Utils.class);

    // Prevents instantiation.
    private Utils() {
    }

    /**
     * Enables strict mode. This should only be called when debugging the
     * application and is useful for finding some potential bugs or best
     * practice violations.
     */
    /**
     * 开启精确模式， 只有在调试模式下可以调用此函数. 本函数对于寻找潜在bug是非常有用的
     *
     * @author tqcenglish
     */
    @TargetApi(11)
    public static void enableStrictMode() {
        // Strict mode is only available on gingerbread or later
        if (Utils.hasGingerbread()) {

            // Enable all thread strict mode policies
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Enable all VM strict mode policies
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Honeycomb introduced some additional strict mode features
            if (Utils.hasHoneycomb()) {
                // Flash screen when thread policy is violated
                threadPolicyBuilder.penaltyFlashScreen();
                // For each activity class, set an instance limit of 1. Any more instances and
                // there could be a memory leak.
                vmPolicyBuilder
                        .setClassInstanceLimit(MainActivity.class, 1)
                        .setClassInstanceLimit(LaunchActivity.class, 2);
            }

            // Use builders to enable strict mode policies
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    /**
     * Uses static final constants to detect if the device's platform version is
     * Gingerbread or later.
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * Uses static final constants to detect if the device's platform version is
     * Honeycomb or later.
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Uses static final constants to detect if the device's platform version is
     * Honeycomb MR1 or later.
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Uses static final constants to detect if the device's platform version is
     * Honeycomb MR2 or later.
     */
    public static boolean hasHoneycombMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    }



    /**
     * Uses static final constants to detect if the device's platform version is
     * ICS or later.
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean18() {
        return Build.VERSION.SDK_INT >= 18;
    }

    public static List<String> splitCallid(String callid) {

        Pattern pattern = Pattern
                .compile(callid.startsWith("\"") ? ZycooConfigurationEntry.VOICE_CALLER_ID1
                        : ZycooConfigurationEntry.VOICE_CALLER_ID2);
        Matcher matcher = pattern.matcher(callid);
        List<String> list = new ArrayList<String>();
        while (matcher.find()) {
            list.add(matcher.group(0));
        }
        return list;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static boolean isPro(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            String flavors = appInfo.metaData.getString("productFlavors");
            if ("pro".equals(flavors)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("" + e.getMessage());
            return false;
        }
    }

    public static String getVersion(Context context)//获取版本号
    {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            logger.error("getVersion errror" + e.getMessage());
            e.printStackTrace();
            return "UnKonw";
        }
    }

    public static int getVersionCode(Context context)//获取版本号(内部识别号)
    {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            logger.error("getVersionCode error " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public static String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public static void CustomSetAlpha(View view) {
        CustomSetAlpha(view, 0);
    }

    public static void CustomSetAlpha(View view, float value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // > 11 version
            view.setAlpha(value);
        } else {
            // Nine Old Androids version
            ViewHelper.setAlpha(view, value);
        }
    }

    public static void CustomSetScaleXY(View view, float x, float y)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // > 11 version
            view.setScaleX(x);
            view.setScaleY(y);
        } else {
            // Nine Old Androids version
            ViewHelper.setScaleX(view, x);
            ViewHelper.setScaleY(view, y);
        }
    }

    public static void CustomSetTranslationX(View view, float x)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // > 11 version
            view.setTranslationX(x);
        } else {
            // Nine Old Androids version
            ViewHelper.setTranslationX(view, x);
        }
    }

}


package com.zycoo.android.zphone;

import android.app.Activity;
import android.content.res.Resources;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import java.util.ArrayList;
import java.util.List;

public class ZphoneApplication extends NgnApplication {
    private static List<Activity> mActivitys = new ArrayList<Activity>();
    protected INgnConfigurationService mConfigurationService;
    private INgnSipService mSipService;
    public static int color_grey_100;
    public static int color_grey_200;
    private static ZphoneApplication sInstance;

    public ZphoneApplication() {
        CrashHandler.getInstance().init(this);
    }

    public static Resources getAppResources() {

        return getContext().getResources();
    }

    @Override
    public void onCreate() {
        mConfigurationService = Engine.getInstance().getConfigurationService();
        mSipService = Engine.getInstance().getSipService();
        sInstance = (ZphoneApplication) getInstance();
        color_grey_100 = getContext().getResources().getColor(R.color.grey_100);
        color_grey_200 = getContext().getResources().getColor(R.color.grey_200);
        super.onCreate();
    }

    public static void addActivity(Activity activity) {
        mActivitys.add(activity);
    }

    public static List<Activity> getActivitys() {
        return mActivitys;
    }

    public static String getHost() {
        return sInstance.mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM,
                com.zycoo.android.zphone.utils.ZycooConfigurationEntry.DEFAULT_NETWORK_HTTP_HOST);
    }

    public static String getSIPPort() {
        return sInstance.mConfigurationService.getString(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
                com.zycoo.android.zphone.utils.ZycooConfigurationEntry.DEFAULT_NETWORK_HTTP_PORT);
    }

    public static String getHttpPort() {
        return "4242";
    }

    public static String getHttpSite(String str) {
        return String.format("http://%s:%s%s", getHost(), getHttpPort(), str);
    }

    public static String getUserName() {
        return sInstance.mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, "");
    }
    public static String getNickName() {
        return sInstance.mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, "Zycoo");
    }

    public static INgnConfigurationService getConfigurationService() {
        return sInstance.mConfigurationService;
    }

    public static INgnSipService getSipService() {
        return sInstance.mSipService;
    }
}

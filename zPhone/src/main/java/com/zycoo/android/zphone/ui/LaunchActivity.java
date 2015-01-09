
package com.zycoo.android.zphone.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.settings.BaseScreen.SCREEN_TYPE;
import com.zycoo.android.zphone.ui.dialpad.ScreenAV;
import com.zycoo.android.zphone.ui.dialpad.ScreenAVQueue;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.utils.ZycooConfigurationEntry;

import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.Util;

public class LaunchActivity extends Activity {
    private static final String LOG_TAG = LaunchActivity.class.getSimpleName();
    private Logger mLogger = LoggerFactory.getLogger(LaunchActivity.class.getCanonicalName());
    private Engine mEngine;
    public static final int ACTION_NONE = 0;
    public static final int ACTION_RESTORE_LAST_STATE = 1;
    public static final int ACTION_SHOW_AVSCREEN = 2;
    public static final int ACTION_SHOW_CONTSHARE_SCREEN = 3;
    public static final int ACTION_SHOW_SMS = 4;
    public static final int ACTION_SHOW_CHAT_SCREEN = 5;

    public LaunchActivity() {
        mLogger.debug("time: " + Utils.getTime());
        mEngine = (Engine) Engine.getInstance();
        mEngine.setMainActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogger.debug("time: " + Utils.getTime());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launch);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        // Engine未启动时，注册广播接受器，跳转到主界面
        if (!Engine.getInstance().isStarted()) {
            mLogger.debug("start splashactivity");
            ZphoneApplication.addActivity(this);
            startActivity(new Intent(this, SplashActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            return;
        }
        // TODO 恢复界面
        // 通过获取的action跳转或进入主界面
        /* Bundle bundle = savedInstanceState;
         if (bundle == null)
         {
             Intent intent = getIntent();
             bundle = intent == null ? null : intent.getExtras();
         }*/

        Bundle bundle;
        Intent intent = getIntent();
        bundle = intent == null ? null : intent.getExtras();

        if (bundle != null
                && bundle.getInt("action", LaunchActivity.ACTION_NONE) != LaunchActivity.ACTION_NONE) {
            handleAction(bundle);
        } else {
            mLogger.debug("start Mainctivity");
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            ZphoneApplication.addActivity(this);
        }
    }

    private void handleAction(Bundle bundle) {
        switch (bundle.getInt("action", LaunchActivity.ACTION_NONE)) {
            // Default or ACTION_RESTORE_LAST_STATE
            default:
            case ACTION_RESTORE_LAST_STATE:
                String id = bundle.getString("screen-id");
                final String screenTypeStr = bundle.getString("screen-type");

                final SCREEN_TYPE screenType = NgnStringUtils.isNullOrEmpty(screenTypeStr) ? SCREEN_TYPE.HOME_T
                        : SCREEN_TYPE.valueOf(screenTypeStr);
                switch (screenType) {
                    case AV_T:
                        Intent intent = new Intent(this, ScreenAV.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(ZycooConfigurationEntry.SESSION_ID, id);
                        this.startActivity(intent);
                        break;
                    default:
                        break;
                }
                Log.d(LOG_TAG, "handleAction ACTION_RESTORE_LAST_STATE go to home");
                Intent it = new Intent(this, MainActivity.class);
                startActivity(it);
                break;
            // Show Audio/Video Calls
            case ACTION_SHOW_AVSCREEN:
                Log.d(LOG_TAG, "Main.ACTION_SHOW_AVSCREEN");
                final int activeSessionsCount = NgnAVSession
                        .getSize(new NgnPredicate<NgnAVSession>() {
                            @Override
                            public boolean apply(NgnAVSession session) {
                                return session != null && session.isActive();
                            }
                        });
                // 当前激活电话大于一时判断
                if (activeSessionsCount > 1) {
                    mLogger.debug("activeSessionsCount more than 1");
                    Intent intent = new Intent(this, ScreenAVQueue.class);
                    startActivity(intent);
                } else {
                    NgnAVSession avSession = NgnAVSession
                            .getSession(new NgnPredicate<NgnAVSession>() {
                                @Override
                                public boolean apply(NgnAVSession session) {
                                    return session != null && session.isActive()
                                            && !session.isLocalHeld() && !session.isRemoteHeld();
                                }
                            });
                    if (avSession == null) {
                        avSession = NgnAVSession.getSession(new NgnPredicate<NgnAVSession>() {
                            @Override
                            public boolean apply(NgnAVSession session) {
                                return session != null && session.isActive();
                            }
                        });
                    }
                    if (avSession != null) {
                        Intent intent = new Intent(this, ScreenAV.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(ZycooConfigurationEntry.SESSION_ID, avSession.getId());
                        mLogger.debug("start goto ScreenAV");
                        this.startActivity(intent);
                    } else {
                        mLogger.error("Failed to find associated audio/video session");
                        startActivity(new Intent(this, MainActivity.class));
                        mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
                    }
                }
                break;
        }
    }
}

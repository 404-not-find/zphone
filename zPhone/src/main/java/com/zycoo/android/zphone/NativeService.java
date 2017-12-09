
package com.zycoo.android.zphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zycoo.android.zphone.task.RegisterTask;
import com.zycoo.android.zphone.ui.LaunchActivity;
import com.zycoo.android.zphone.ui.message.MessageFragment;
import com.zycoo.android.zphone.utils.NetWorkUtils;
import com.zycoo.android.zphone.utils.Utils;

import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMsrpEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.events.NgnSubscriptionEventArgs;
import org.doubango.ngn.events.NgnSubscriptionEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.impl.NgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.SipMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * @author tqcenglish Oct 8, 2014 11:10:26 AM
 */
public class NativeService extends NgnNativeService implements Observer {
    private final static String LOG_TAG = NativeService.class.getCanonicalName();
    private Logger mLogger = LoggerFactory.getLogger(NativeService.class);
    public static final String ACTION_STATE_EVENT = LOG_TAG + ".ACTION_STATE_EVENT";
    private PowerManager.WakeLock mWakeLock;
    private BroadcastReceiver mBroadcastReceiver;
    private Engine mEngine;
    private INgnHistoryService mHistoryService;
    private INgnConfigurationService mConfigurationService;
    private IBinder mBinder = new NativeServiceBinder();
    private Handler mMainActivityHandler;
    private ChangeListener changeListener = new ChangeListener();
    private String clientHandle;
    private JsonParser jsonparer;
    private boolean mConnected = false;

    public NativeService() {
        super();
        mEngine = Engine.getInstance();
        mConfigurationService = mEngine.getConfigurationService();
        mHistoryService = mEngine.getHistoryService();
        mHistoryService.getObservableEvents().addObserver(this);
        jsonparer = new JsonParser();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        mLogger.debug("onCreate()");
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null && mWakeLock == null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE
                            | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    LOG_TAG);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mLogger.debug("NativeService onStartCommand");
        if (!Engine.getInstance().isStarted()) {
            mLogger.debug("NativeService Engine not start");
            if (Engine.getInstance().start()) {
                mLogger.debug("onStartCommand start engine successful");
            } else {
                mLogger.debug("onStartCommand start engine failure");
            }
            flags = START_STICKY;
            return super.onStartCommand(intent, flags, startId);
        }

        // 后台服务，启动广播接受，修改状态栏显示，同时发送广播事件
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ConstantIntent.CONNECTIVITY_CHANGE.equals(action)) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                    NetworkInfo mobNetInfo = connectivityManager
                            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    NetworkInfo wifiInfo = connectivityManager
                            .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (wifiInfo != null && wifiInfo.isConnected()
                            && ZphoneApplication.getConfigurationService().getBoolean(
                            NgnConfigurationEntry.NETWORK_USE_WIFI,
                            NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI)) {
                        new RegisterTask(true).execute();
                    }

                    if (mobNetInfo != null && mobNetInfo.isConnected()
                            && ZphoneApplication.getConfigurationService().getBoolean(
                            NgnConfigurationEntry.NETWORK_USE_3G,
                            NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G)) {
                        new RegisterTask(true).execute();
                    }
                    if (activeNetInfo == null) {
                        new RegisterTask(false).execute();
                    }
                }
                //ACTION_SUBSCRIBTION_EVENT
                if (NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT.equals(action)) {
                    NgnSubscriptionEventArgs args = intent
                            .getParcelableExtra(NgnSubscriptionEventArgs.EXTRA_EMBEDDED);
                    final NgnSubscriptionEventTypes type;
                    if (null == args) {
                        mLogger.debug("Subscription event invalid event args");
                        return;
                    }

                    switch (type = args.getEventType()) {
                        case INCOMING_NOTIFY:
                            byte[] content = args.getContent();

                            if (null != content && content.length > 0) {
                                String str = new String(content);
                                mLogger.debug(str);
                                Map<String, String> properties = Splitter.on("\r\n").omitEmptyStrings().withKeyValueSeparator(": ").split(str);
                                if (properties.get("Messages-Waiting").equals("yes")) {
                                    int newVoiceMail = Integer.parseInt(properties.get("Voice-Message").split(" ")[0].split("/")[0]);
                                    if (newVoiceMail > 0) {
                                        mEngine.showMissCallNotif(R.drawable.ic_voicemail_white, getResources().getString(R.string.voice_mail), String.valueOf(newVoiceMail), new java.util.Date().getTime());
                                    }
                                }
                            }
                            break;
                    }
                }

                // Registration Events
                if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)) {
                    NgnRegistrationEventArgs args = intent
                            .getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
                    final NgnRegistrationEventTypes type;
                    if (args == null) {
                        Log.e(LOG_TAG, "Invalid event args");
                        return;
                    }
                    switch ((type = args.getEventType())) {
                        case REGISTRATION_OK:
                            // mwi
                            //TODO add function to interface
                            // ((NgnSipService) mEngine.getSipService()).doPostRegistrationOp();
                            break;
                        case REGISTRATION_NOK:
                            Toast.makeText(ZphoneApplication.getContext(),
                                    args.getPhrase() + args.getSipCode(), Toast.LENGTH_SHORT)
                                    .show();
                        case REGISTRATION_INPROGRESS:
                        case UNREGISTRATION_INPROGRESS:
                            break;
                        case UNREGISTRATION_OK:
                            // ((NgnSipService) mEngine.getSipService()).doPostRegistrationOp();
                            break;
                        case UNREGISTRATION_NOK:
                        default:
                            break;
                    }
                    mLogger.debug("registration event type " + type);
                    final boolean bTrying = (type == NgnRegistrationEventTypes.REGISTRATION_INPROGRESS || type == NgnRegistrationEventTypes.UNREGISTRATION_INPROGRESS);
                    if (mEngine.getSipService().isRegistered()) {
                        mEngine.showAppNotif(bTrying ? R.drawable.ic_status_dot_yellow
                                : R.drawable.ic_status_dot_green, null);
                        ZphoneApplication.acquirePowerLock();
                    } else {
                        mEngine.showAppNotif(bTrying ? R.drawable.ic_status_dot_yellow
                                : R.drawable.ic_status_dot_red, null);
                        ZphoneApplication.releasePowerLock();
                    }
                }

                // Invite Events
                if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
                    NgnInviteEventArgs args = intent
                            .getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
                    Log.d(LOG_TAG,
                            " receive NgnInviteEventArgs.ACTION_INVITE_EVENT getMediaType().name() "
                                    + args.getMediaType().name());

                    final NgnMediaType mediaType = args.getMediaType();
                    switch (args.getEventType()) {
                        case TERMWAIT:
                        case TERMINATED:
                            if (NgnMediaType.isAudioVideoType(mediaType)) {
                                mEngine.refreshAVCallNotif(R.drawable.ic_call_grey600);
                                mEngine.getSoundService().stopRingBackTone();
                                mEngine.getSoundService().stopRingTone();
                            }
                            break;
                        case INCOMING:
                            if (NgnMediaType.isAudioVideoType(mediaType)) {
                                mConnected = true;
                                final NgnAVSession avSession = NgnAVSession.getSession(args
                                        .getSessionId());
                                if (avSession != null) {
                                    mEngine.showAVCallNotif(R.drawable.ic_call_grey600,
                                            getResources().getString(R.string.string_in_coming));
                                    Intent income = new Intent(ZphoneApplication.getContext()
                                            .getApplicationContext(),
                                            LaunchActivity.class);
                                    income.addCategory(Intent.CATEGORY_HOME);
                                    income.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                            | Intent.FLAG_FROM_BACKGROUND);
                                    income.putExtra("action", LaunchActivity.ACTION_SHOW_AVSCREEN);
                                    if (mWakeLock != null && !mWakeLock.isHeld()) {
                                        mWakeLock.acquire(10);
                                    }
                                    getApplication().startActivity(income);
                                    /*ZphoneApplication.getContext().getApplicationContext()
                                            .startActivity(income);*/
                                    //ScreenAV.receiveCall(avSession);
                                    mEngine.getSoundService().startRingTone();
                                } else {
                                    // TODO java.util.UnknownFormatConversionException
                                    mLogger.debug(String.format(
                                            "Failed to find session with id=%ld",
                                            args.getSessionId()));
                                }
                            }
                            break;
                        case INPROGRESS:
                            if (NgnMediaType.isAudioVideoType(mediaType)) {
                                mEngine.showAVCallNotif(R.drawable.ic_call_grey600, "Call outing");
                            }
                            break;
                        case RINGING:
                            if (NgnMediaType.isAudioVideoType(mediaType)) {
                                mEngine.getSoundService().startRingBackTone();
                            }
                            break;
                        case CONNECTED:
                        case EARLY_MEDIA:
                            if (NgnMediaType.isAudioVideoType(mediaType)) {
                                mEngine.showAVCallNotif(R.drawable.ic_call_grey600, "in call");
                                mEngine.getSoundService().stopRingBackTone();
                                mEngine.getSoundService().stopRingTone();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
        intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
        intentFilter.addAction(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
        intentFilter.addAction(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
        //SUBSCRIBTION
        intentFilter.addAction(NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT);
        //not use
        intentFilter.addAction(ConstantIntent.VOICEMAILCOUNTATION_INTENT);
        // network
        intentFilter.addAction(ConstantIntent.CONNECTIVITY_CHANGE);
        registerReceiver(mBroadcastReceiver, intentFilter);
        final Bundle bundle = intent != null ? intent.getExtras() : null;
        mLogger.debug("new thread to register and mqtt connect");
        new Thread() {
            @Override
            public void run() {

                boolean isFirst = mEngine.getConfigurationService().getBoolean(ZycooConfigurationEntry.FIRST_LOGIN, ZycooConfigurationEntry.DEFAULT_FIRST_LOGIN);
                if (bundle != null && bundle.getBoolean("autostarted") && !isFirst) {
                    if (new NetWorkUtils()
                            .isNetworkConnected(ZphoneApplication.getContext())) {
                        mEngine.getSipService().register(null);
                    }
                }
                if (Utils.isPro(ZphoneApplication.getContext())) {
                    if (new NetWorkUtils().isNetworkConnected(ZphoneApplication.getContext())) {
                    }
                }
                final Intent i = new Intent(ACTION_STATE_EVENT);
                i.putExtra("started", true);
                sendBroadcast(i);
            }

        }.start();
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mLogger.debug("NativieService onDestroy");
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
                mWakeLock = null;
            }
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (mConnected) {
            List<NgnHistoryEvent> events = mHistoryService.getObservableEvents().filter(new NgnHistoryAVCallEvent.HistoryEventAVFilter());
            for (NgnHistoryEvent event : events) {
                mLogger.debug("is connect " + mConnected);
                if (event.getStatus() == NgnHistoryEvent.StatusType.Missed) {
                    if ((new java.util.Date().getTime()) - event.getEndTime() < 60000) {
                        mEngine.showMissCallNotif(R.drawable.ic_phone_missed_grey600, event.getDisplayName(), event.getRemoteParty(), event.getEndTime());
                        break;
                    }
                }
            }
            mConnected = false;
        }
    }

    public boolean isInComingMiss(String phrase) {
        switch (phrase) {
            case "Call Terminated":
            case "Call Cancelled":
                return true;
            default:
                return false;
        }
    }

    public class NativeServiceBinder extends Binder {
        public NativeService getService() {
            return NativeService.this;
        }
    }

    public void setHandler(Handler handler) {
        mLogger.debug("set handler ");
        mMainActivityHandler = handler;
    }


    /**
     * This class ensures that the user interface is updated as the Connection
     * objects change their states 这个类用于用户更新连接的状态改变
     */
    private class ChangeListener implements PropertyChangeListener {

        /**
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent event) {

        }
    }



    public ChangeListener getChangeListener() {
        return changeListener;
    }
}

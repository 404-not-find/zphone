
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zycoo.android.zphone.mqtt.ActionListener;
import com.zycoo.android.zphone.mqtt.ActionListener.Action;
import com.zycoo.android.zphone.mqtt.ActivityConstants;
import com.zycoo.android.zphone.mqtt.Connection;
import com.zycoo.android.zphone.mqtt.Connection.ConnectionStatus;
import com.zycoo.android.zphone.mqtt.Connections;
import com.zycoo.android.zphone.mqtt.Notify;
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
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * @author tqcenglish Oct 8, 2014 11:10:26 AM
 */
public class NativeService extends NgnNativeService implements MqttCallback, Observer {
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
                            break;
                        case REGISTRATION_NOK:
                            Toast.makeText(ZphoneApplication.getContext(),
                                    args.getPhrase() + args.getSipCode(), Toast.LENGTH_SHORT)
                                    .show();
                        case REGISTRATION_INPROGRESS:
                        case UNREGISTRATION_INPROGRESS:
                        case UNREGISTRATION_OK:
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
                                mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
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
                                    mEngine.showAVCallNotif(R.drawable.call_incoming_45,
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
                                mEngine.showAVCallNotif(R.drawable.call_outgoing_45, "Call outing");
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
                                mEngine.showAVCallNotif(R.drawable.phone_call_25, "in call");
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
        intentFilter.addAction(ConstantIntent.VOICEMAILCOUNTATION_INTENT);
        intentFilter.addAction(ConstantIntent.CONNECTIVITY_CHANGE);
        registerReceiver(mBroadcastReceiver, intentFilter);
        final Bundle bundle = intent != null ? intent.getExtras() : null;
        mLogger.debug("new thread to register and mqtt connect");
        new Thread() {
            @Override
            public void run() {
                if (bundle != null && bundle.getBoolean("autostarted")) {
                    if (new NetWorkUtils()
                            .isNetworkConnected(ZphoneApplication.getContext())) {
                        mLogger.debug("autostarted ....");
                        mEngine.getSipService().register(null);
                    }
                }
                if (Utils.isPro(ZphoneApplication.getContext())) {
                    if (new NetWorkUtils().isNetworkConnected(ZphoneApplication.getContext())) {
                        connectAction();
                    }
                }
                mLogger.debug("send ACTION_STATE_EVENT");
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
        Map<String, Connection> connections = Connections.getInstance(
                this)
                .getConnections();
        for (Connection connection : connections.values()) {
            connection
                    .registerChangeListener((this)
                            .getChangeListener());
            connection.getClient().unregisterResources();
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
                        mEngine.showMissCallNotif(R.drawable.call_missed_45, event.getDisplayName(), event.getRemoteParty(), event.getEndTime());
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
     * mqtt connect
     *
     * @author tqcenglish
     */
    public void connectAction() {
        MqttConnectOptions conOpt = new MqttConnectOptions();
        /*
         * Mutal Auth connections could do something like this
         * 
         * 
         * SSLContext context = SSLContext.getDefault();
         * context.init({new CustomX509KeyManager()},null,null); //where CustomX509KeyManager proxies calls to keychain api
         * SSLSocketFactory factory = context.getSSLSocketFactory();
         * 
         * MqttConnectOptions options = new MqttConnectOptions();
         * options.setSocketFactory(factory);
         * 
         * client.connect(options);
         * 
         */

        // The basic client information
        //host
        String server = mConfigurationService
                .getString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, "");
        //account
        String clientId = mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, "");
        /* int port = Integer.parseInt(configurationService.getString(
                 NgnConfigurationEntry.NETWORK_PCSCF_PORT, ""));*/
        int port = 1883;
        boolean cleanSession = true;

        boolean ssl = false;
        String uri = null;
        if (ssl) {
            Log.e("SSLConnection", "Doing an SSL Connect");
            uri = "ssl://";

        } else {
            uri = "tcp://";
        }

        uri = uri + server + ":" + port;

        MqttAndroidClient client;
        client = Connections.getInstance(this).createClient(this, uri, clientId);
        // create a client handle
        clientHandle = uri + clientId;

        // last will message
        /*String message = (String) data.get(ActivityConstants.message);
        String topic = (String) data.get(ActivityConstants.topic);
        Integer qos = (Integer) data.get(ActivityConstants.qos);
        Boolean retained = (Boolean) data.get(ActivityConstants.retained);*/

        // connection options

        /* String username = (String) data.get(ActivityConstants.username);
         String password = (String) data.get(ActivityConstants.password);
         int timeout = (Integer) data.get(ActivityConstants.timeout);
         int keepalive = (Integer) data.get(ActivityConstants.keepalive);*/

        Connection connection = new Connection(clientHandle, clientId, server, port,
                this, client, ssl);

        connection.registerChangeListener(changeListener);
        // connect client

        String[] actionArgs = new String[1];
        actionArgs[0] = clientId;
        connection.changeConnectionStatus(ConnectionStatus.CONNECTING);
        conOpt.setCleanSession(cleanSession);
        conOpt.setConnectionTimeout(60);
        conOpt.setKeepAliveInterval(120);
        //password username
        /* if (!username.equals(ActivityConstants.empty)) {
             conOpt.setUserName(username);
         }
         if (!password.equals(ActivityConstants.empty)) {
             conOpt.setPassword(password.toCharArray());
         }*/

        final ActionListener callback = new ActionListener(this,
                ActionListener.Action.CONNECT, clientHandle, actionArgs);

        boolean doConnect = true;

        //last will message
        /* if ((!message.equals(ActivityConstants.empty))
                 || (!topic.equals(ActivityConstants.empty))) {
             // need to make a message since last will is set
             try {
                 conOpt.setWill(topic, message.getBytes(), qos.intValue(),
                         retained.booleanValue());
             } catch (Exception e) {
                 Log.e(this.getClass().getCanonicalName(), "Exception Occured", e);
                 doConnect = false;
                 callback.onFailure(null, e);
             }
         }*/
        //MqttCallbackHandler mqttCallbackHandler = new MqttCallbackHandler(getApplicationContext(), clientHandle);
        //client.setCallback(mqttCallbackHandler);
        client.setCallback(this);
        connection.addConnectionOptions(conOpt);
        Connections.getInstance(this).addConnection(connection);
        if (doConnect) {
            try {
                client.connect(conOpt, null, callback);
            } catch (MqttException e) {
                Log.e(this.getClass().getCanonicalName(),
                        "MqttException Occured", e);
            }
        }

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

            if (!event.getPropertyName().equals(ActivityConstants.ConnectionStatusProperty)) {
                return;
            }
        }
    }

    /**
     * Subscribe to a topic that the user has specified
     */
    public void subscribe() {
        if (null == Connections
                .getInstance(this)
                .getConnection(clientHandle)
                .getClient()) {
            mLogger.error("getClient is null");
            return;
        }
        /*
        RadioGroup radio = (RadioGroup) connectionDetails.findViewById(R.id.qosSubRadio);
         int checked = radio.getCheckedRadioButtonId();
         int qos = ActivityConstants.defaultQos;

         switch (checked) {
           case R.id.qos0 :
             qos = 0;
             break;
           case R.id.qos1 :
             qos = 1;
             break;
           case R.id.qos2 :
             qos = 2;
             break;
         }
        */
        String[] topics = new String[]{
                mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, "")
        };
        try {
            Connections
                    .getInstance(this)
                    .getConnection(clientHandle)
                    .getClient()
                    .subscribe(
                            topics[0],
                            1,
                            null,
                            new ActionListener(this, Action.SUBSCRIBE, clientHandle,
                                    topics));

        } catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topics[0]
                    + " the client with the handle " + clientHandle, e);
        } catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topics[0]
                    + " the client with the handle " + clientHandle, e);
        }
    }

    /**
     * Disconnect the client
     */
    public void disconnect() {

        Connection c = Connections.getInstance(this).getConnection(clientHandle);

        //if the client is not connected, process the disconnect
        if (!c.isConnected()) {
            return;
        }

        try {
            c.getClient().disconnect(null,
                    new ActionListener(this, Action.DISCONNECT, clientHandle, null));
            c.changeConnectionStatus(ConnectionStatus.DISCONNECTING);
        } catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "Failed to disconnect the client with the handle " + clientHandle, e);
            c.addAction("Client failed to disconnect");
        }
    }

    public ChangeListener getChangeListener() {
        return changeListener;
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
        //    cause.printStackTrace();
        if (cause != null) {
            Connection c = Connections.getInstance(this).getConnection(clientHandle);
            c.addAction("Connection Lost");
            c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

            //format string to use a notification text
            Object[] args = new Object[2];
            args[0] = c.getId();
            args[1] = c.getHostName();

            String message = this.getString(R.string.connection_lost, args[0], args[1]);

            //build intent
            Intent intent = new Intent();
            intent.setClassName(this,
                    "org.eclipse.paho.android.service.sample.ConnectionDetails");
            intent.putExtra("handle", clientHandle);

            //notify the user
            //Notify.notifcation(context, message, intent, R.string.notifyTitle_connectionLost);

            //reconnect
            connectAction();
        }
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String,
     * org.eclipse.paho.client.mqttv3.MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        //Get connection object associated with this object
        Connection c = Connections.getInstance(this).getConnection(clientHandle);

        //create arguments to format message arrived notifcation string
        String[] args = new String[2];
        args[0] = new String(message.getPayload());
        args[1] = topic;

        //get the string from strings.xml and format
        String messageString = getString(R.string.messageRecieved, (Object[]) args);

        //create intent to start activity
        Intent intent = new Intent();
        intent.setClassName(this, "org.eclipse.paho.android.service.sample.ConnectionDetails");
        intent.putExtra("handle", clientHandle);

        //format string args
        Object[] notifyArgs = new String[3];
        notifyArgs[0] = c.getId();
        notifyArgs[1] = new String(message.getPayload());
        notifyArgs[2] = topic;

        JsonObject root = jsonparer.parse(new String(message.getPayload())).getAsJsonObject();
        //insert DataBase
        if (root.get("action").getAsString().equals("sql")
                && root.get("type").getAsString().equals("voicemail")) {

            SQLiteDatabase wSQLiteDatabase = new DatabaseHelper(this.getApplicationContext(),
                    "SoftPhone.db", null, 1)
                    .getWritableDatabase();
            String sql = root.get("message").getAsString();
            mLogger.debug(sql);
            wSQLiteDatabase.execSQL(sql);
            wSQLiteDatabase.close();
            if (sql.contains("insert") && sql.contains("INBOX")) {
                Notify.notifcation(this, "New Voice Mail", intent, R.string.notifyTitle);
            }
            mMainActivityHandler.sendEmptyMessage(MessageFragment.HANDLE_WHAT);
            //intent.setAction(MessageFragment.UPDATE_MESSAGE_FRAGMENT);  

        }
        //notify the user 
        //Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);

        //update client history
        c.addAction(messageString);

    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Do nothing
    }

    public boolean isConnected() {
        try {
            if (null == Connections
                    .getInstance(this)
                    .getConnection(clientHandle)
                    .getClient()) {
                mLogger.error("getClient is null");
                return false;
            }
            return Connections
                    .getInstance(this)
                    .getConnection(
                            clientHandle).isConnected();
        } catch (Exception e) {
            mLogger.error("getClient is null" + e.getMessage());
            return false;
        }

    }
}

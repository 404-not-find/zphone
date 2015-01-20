/*
 * Copyright (C) 2010-2011, Mamadou Diop. Copyright (C) 2011, Doubango Telecom.
 * 
 * Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
 * 
 * This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
 * 
 * imsdroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * @contributors: See $(DOUBANGO_HOME)\contributors.txt
 */

package com.zycoo.android.zphone.ui.dialpad;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.ui.LaunchActivity;
import com.zycoo.android.zphone.ui.MainActivity;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.dialpad.DtmfDialogFragment.OnDtmfListener;
import com.zycoo.android.zphone.utils.AndroidUtils;
import com.zycoo.android.zphone.utils.ZycooConfigurationEntry;

import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnGraphicsUtils;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimerTask;

public class ScreenAV extends SherlockFragmentActivity implements OnClickListener, OnDtmfListener {
    private static Logger mLogger = LoggerFactory.getLogger(ScreenAV.class.getCanonicalName());
    private static final String TAG = ScreenAV.class.getCanonicalName();
    private static final SimpleDateFormat sDurationTimerFormat = new SimpleDateFormat("mm:ss",
            Locale.getDefault());
    private Ringtone mRingtone;
    // 统计空包
    private static int mCountBlankPacket;
    // 旋转度数
    private static int mLastRotation; // values: degrees
    // 是否设备信息
    private boolean mSendDeviceInfo;
    // 最后角度
    private int mLastOrientation; // values: portrait, landscape...
    // 远端显示名
    private String mRemotePartyDisplayName;
    // 远端照片
    private Bitmap mRemotePartyPhoto;
    // 当前界面类型
    private ViewType mCurrentView;
    private LayoutInflater mInflater;
    private RelativeLayout mMainLayout;
    private BroadcastReceiver mBroadCastRecv;
    // 五种界面
    private View mViewTrying;
    private View mViewInAudioCall;
    private View mViewInCallVideo;
    private View mViewTermwait;
    private View mViewProxSensor;
    // 本地和远端视频预览
    private FrameLayout mViewLocalVideoPreview;
    private FrameLayout mViewRemoteVideoPreview;
    // 三个定时器
    private final NgnTimer mTimerInCall;
    private final NgnTimer mTimerSuicide;
    private final NgnTimer mTimerBlankPacket;
    private NgnAVSession mAVSession;
    // 判断是否视频通话
    private boolean mIsVideoCall;
    private TextView mTvInfo;
    private TextView mTvDuration;
    private AlertDialog mTransferDialog;
    private NgnAVSession mAVTransfSession;
    private MyProxSensor mProxSensor;
    private KeyguardLock mKeyguardLock;
    // 方向事件监听器
    private OrientationEventListener mListener;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager.WakeLock mProSensorWakeLock;
    private static final int SELECT_CONTENT = 1;
    private final static int MENU_PICKUP = 0;
    private final static int MENU_HANGUP = 1;
    private final static int MENU_HOLD_RESUME = 2;
    private final static int MENU_SEND_STOP_VIDEO = 3;
    private final static int MENU_SHARE_CONTENT = 4;
    private final static int MENU_SPEAKER = 5;
    private static boolean SHOW_SIP_PHRASE = true;

    private static enum ViewType {
        ViewNone, ViewTrying, ViewInCall, ViewProxSensor, ViewTermwait
    }

    public ScreenAV() {
        mCurrentView = ViewType.ViewNone;
        mTimerInCall = new NgnTimer();
        mTimerSuicide = new NgnTimer();
        mTimerBlankPacket = new NgnTimer();
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);// 系统自带提示音
        mRingtone = RingtoneManager.getRingtone(ZphoneApplication.getContext(), uri);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_av);
        // 添加设置按钮
        AndroidUtils.addLegacyOverflowButton(getWindow());
        long id = getIntent().getLongExtra(ZycooConfigurationEntry.SESSION_ID, -1);
        // TODO
        /*
         * if (NgnStringUtils.isNullOrEmpty(id)) { Log.e(TAG,
         * "Invalid audio/video session"); finish(); startActivity(new
         * Intent(this, ScreenHome.class)); return; }
         */
        mAVSession = NgnAVSession.getSession(id);
        if (mAVSession == null) {
            Log.e(TAG, String.format("Cannot find audio/video session with id=%s", id));
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }
        // 添加计数引用
        mAVSession.incRef();
        mAVSession.setContext(this);
        // 查询电话本，获取拨打者的号码和显示名
        final NgnContact remoteParty = Engine.getInstance().getContactService()
                .getContactByUri(mAVSession.getRemotePartyUri());
        if (remoteParty != null) {
            mRemotePartyDisplayName = remoteParty.getDisplayName();
            if ((mRemotePartyPhoto = remoteParty.getPhoto()) != null) {
                mRemotePartyPhoto = NgnGraphicsUtils.getResizedBitmap(mRemotePartyPhoto,
                        NgnGraphicsUtils.getSizeInPixel(128), NgnGraphicsUtils.getSizeInPixel(128));
            }
        } else {
            mRemotePartyDisplayName = NgnUriUtils.getDisplayName(mAVSession.getRemotePartyUri());
        }
        if (NgnStringUtils.isNullOrEmpty(mRemotePartyDisplayName)) {
            mRemotePartyDisplayName = "Unknown";
        }
        // 视频类型有两种
        mIsVideoCall = mAVSession.getMediaType() == NgnMediaType.AudioVideo
                || mAVSession.getMediaType() == NgnMediaType.Video;
        mSendDeviceInfo = Engine
                .getInstance()
                .getConfigurationService()
                .getBoolean(NgnConfigurationEntry.GENERAL_SEND_DEVICE_INFO,
                        NgnConfigurationEntry.DEFAULT_GENERAL_SEND_DEVICE_INFO);
        mCountBlankPacket = 0;
        mLastRotation = -1;
        mLastOrientation = -1;
        mInflater = LayoutInflater.from(this);
        // 处理两种action，
        mBroadCastRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(intent.getAction())) {
                    handleSipEvent(intent);
                } else if (NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(intent
                        .getAction())) {
                    handleMediaEvent(intent);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
        intentFilter.addAction(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT);
        registerReceiver(mBroadCastRecv, intentFilter);
        // 视频通话对屏幕旋转的处理
        if (mIsVideoCall) {
            mListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orient) {
                    try {
                        if ((orient > 345 || orient < 15) || (orient > 75 && orient < 105)
                                || (orient > 165 && orient < 195) || (orient > 255 && orient < 285)) {
                            int rotation = mAVSession.compensCamRotation(true);
                            if (rotation != mLastRotation) {
                                Log.d(ScreenAV.TAG,
                                        "Received Screen Orientation Change setRotation["
                                                + String.valueOf(rotation) + "]");
                                applyCamRotation(rotation);
                                if (mSendDeviceInfo && mAVSession != null) {
                                    final android.content.res.Configuration conf = getResources()
                                            .getConfiguration();
                                    if (conf.orientation != mLastOrientation) {
                                        mLastOrientation = conf.orientation;
                                        switch (mLastOrientation) {
                                            case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                                                mAVSession.sendInfo(
                                                        "orientation:landscape\r\nlang:fr-FR\r\n",
                                                        NgnContentType.DOUBANGO_DEVICE_INFO);
                                                break;
                                            case android.content.res.Configuration.ORIENTATION_PORTRAIT:
                                                mAVSession.sendInfo(
                                                        "orientation:portrait\r\nlang:fr-FR\r\n",
                                                        NgnContentType.DOUBANGO_DEVICE_INFO);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            if (!mListener.canDetectOrientation()) {
                Log.w(TAG, "canDetectOrientation() is equal to false");
            }
        }
        mMainLayout = (RelativeLayout) findViewById(R.id.screen_av_relativeLayout);
        loadView();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        final KeyguardManager keyguardManager = ZphoneApplication.getKeyguardManager();
        if (keyguardManager != null) {
            if (mKeyguardLock == null) {
                mKeyguardLock = keyguardManager.newKeyguardLock(ScreenAV.TAG);
            }
            if (keyguardManager.inKeyguardRestrictedInputMode()) {
                mKeyguardLock.disableKeyguard();
            }
        }
        final PowerManager powerManager = ZphoneApplication.getPowerManager();
        if (powerManager != null && mWakeLock == null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE
                            | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    TAG);

            if (mWakeLock != null) {
                mWakeLock.acquire();
            }
        }
        if(powerManager != null && mProSensorWakeLock == null)
        {
            mProSensorWakeLock = powerManager.newWakeLock(32, TAG);
        }
        if (mProxSensor == null && !ZphoneApplication.isBuggyProximitySensor()) {
            mProxSensor = new MyProxSensor(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        /*if (mProxSensor != null) {
            mProxSensor.stop();
        }
        */
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (mListener != null && mListener.canDetectOrientation()) {
            mListener.disable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (mProxSensor != null) {
            mProxSensor.start();
        }
        if (mAVSession != null) {
            if (mAVSession.getState() == InviteState.INCALL) {
                mTimerInCall.schedule(mTimerTaskInCall, 0, 1000);
            }
        }
        if (mListener != null && mListener.canDetectOrientation()) {
            mListener.enable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        if (mKeyguardLock != null) {
            mKeyguardLock.reenableKeyguard();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (mBroadCastRecv != null) {
            unregisterReceiver(mBroadCastRecv);
            mBroadCastRecv = null;
        }
        mTimerInCall.cancel();
        mTimerSuicide.cancel();
        cancelBlankPacket();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mWakeLock = null;
        if (mAVSession != null) {
            mAVSession.setContext(null);
            mAVSession.decRef();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_CONTENT:
                    if (mAVSession != null) {
                        Uri selectedContentUri = data.getData();
                        // String selectedContentPath =
                        // super.getPath(selectedContentUri);
                        /*
                         * ScreenFileTransferView.sendFile(
                         * mAVSession.getRemotePartyUri(), selectedContentPath);
                         */
                    }
                    break;
            }
        }
    }

    public boolean onVolumeChanged(boolean bDown) {
        if (mAVSession != null) {
            return mAVSession.onVolumeChanged(bDown);
        }
        return false;
    }

    public static boolean receiveCall(NgnAVSession avSession) {
        Intent intent = new Intent(ZphoneApplication.getContext(), LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("action", LaunchActivity.ACTION_SHOW_AVSCREEN);
        ZphoneApplication.getContext().startActivity(intent);
        return true;
    }

    public static boolean makeCall(String remoteUri, NgnMediaType mediaType, Context context) {
        final Engine engine = (Engine) Engine.getInstance();
        final INgnSipService sipService = engine.getSipService();
        final INgnConfigurationService configurationService = engine.getConfigurationService();
        final String validUri = NgnUriUtils.makeValidSipUri(remoteUri);
        if (validUri == null) {
            Log.e(TAG, "failed to normalize sip uri '" + remoteUri + "'");
            return false;
        } else {
            remoteUri = validUri;
            if (remoteUri.startsWith("tel:")) {
                // E.164 number => use ENUM protocol
                final NgnSipStack sipStack = sipService.getSipStack();
                if (sipStack != null) {
                    String phoneNumber = NgnUriUtils.getValidPhoneNumber(remoteUri);
                    if (phoneNumber != null) {
                        String enumDomain = configurationService.getString(
                                NgnConfigurationEntry.GENERAL_ENUM_DOMAIN,
                                NgnConfigurationEntry.DEFAULT_GENERAL_ENUM_DOMAIN);
                        String sipUri = sipStack.dnsENUM("E2U+SIP", phoneNumber, enumDomain);
                        if (sipUri != null) {
                            remoteUri = sipUri;
                        }
                    }
                }
            }
        }
        final NgnAVSession avSession = NgnAVSession.createOutgoingSession(sipService.getSipStack(),
                mediaType);
        avSession.setRemotePartyUri(remoteUri); // HACK
        Intent intent = new Intent(context, ScreenAV.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Bundle bundle = new Bundle();
        intent.putExtra(ZycooConfigurationEntry.SESSION_ID, avSession.getId());
        context.startActivity(intent);
        // screenService.show(ScreenAV.class, Long.toString(avSession.getId()));
        // Hold the active call
        final NgnAVSession activeCall = NgnAVSession.getFirstActiveCallAndNot(avSession.getId());
        if (activeCall != null) {
            activeCall.holdCall();
        }
        return avSession.makeCall(remoteUri);
    }

    private void applyCamRotation(int rotation) {
        if (mAVSession != null) {
            mLastRotation = rotation;
            // libYUV
            mAVSession.setRotation(rotation);
            // FFmpeg
            /*
             * switch (rotation) { case 0: case 90:
             * mAVSession.setRotation(rotation);
             * mAVSession.setProducerFlipped(false); break; case 180:
             * mAVSession.setRotation(0); mAVSession.setProducerFlipped(true);
             * break; case 270: mAVSession.setRotation(90);
             * mAVSession.setProducerFlipped(true); break; }
             */
        }
    }

    private boolean hangUpCall() {
        if (mAVSession != null) {
            return mAVSession.hangUpCall();
        }
        return false;
    }

    private boolean acceptCall() {
        if (mAVSession != null) {
            return mAVSession.acceptCall();
        }
        return false;
    }

    private void handleMediaEvent(Intent intent) {
        final String action = intent.getAction();
        if (NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(action)) {
            NgnMediaPluginEventArgs args = intent
                    .getParcelableExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED);
            if (args == null) {
                Log.e(TAG, "Invalid event args");
                return;
            }
            switch (args.getEventType()) {
                case STARTED_OK: // started or restarted (e.g. reINVITE)
                {
                    mIsVideoCall = (mAVSession.getMediaType() == NgnMediaType.AudioVideo || mAVSession
                            .getMediaType() == NgnMediaType.Video);
                    loadView();
                    break;
                }
                case PREPARED_OK:
                case PREPARED_NOK:
                case STARTED_NOK:
                case STOPPED_OK:
                case STOPPED_NOK:
                case PAUSED_OK:
                case PAUSED_NOK: {
                    break;
                }
            }
        }
    }

    private void handleSipEvent(Intent intent) {
        @SuppressWarnings("unused")
        InviteState state;
        if (mAVSession == null) {
            Log.e(TAG, "Invalid session object");
            return;
        }
        final String action = intent.getAction();
        if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
            NgnInviteEventArgs args = intent.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
            if (args == null) {
                Log.e(TAG, "Invalid event args");
                return;
            }
            if (args.getSessionId() != mAVSession.getId()) {
                if (args.getEventType() == NgnInviteEventTypes.REMOTE_TRANSFER_INPROGESS) {
                    // Native code created new session handle to be used to
                    // replace the current one (event = "tsip_i_ect_newcall").
                    mAVTransfSession = NgnAVSession.getSession(args.getSessionId());
                }
                return;
            }
            switch ((state = mAVSession.getState())) {
                case NONE:
                default:
                    break;
                case INCOMING:
                case INPROGRESS:
                case REMOTE_RINGING:
                    loadTryingView();
                    break;
                case EARLY_MEDIA:
                case INCALL:
                    if (state == InviteState.INCALL) {
                        // stop using the speaker (also done in
                        // ServiceManager())
                        Engine.getInstance().getSoundService().stopRingTone();
                        mAVSession.setSpeakerphoneOn(false);
                    }
                    if (state == InviteState.INCALL) {
                        loadInCallView();
                    }
                    // Send blank packets to open NAT pinhole
                    if (mAVSession != null) {
                        applyCamRotation(mAVSession.compensCamRotation(true));
                        mTimerBlankPacket.schedule(mTimerTaskBlankPacket, 0, 250);
                        if (!mIsVideoCall) {
                            mTimerInCall.schedule(mTimerTaskInCall, 0, 1000);
                        }
                    }
                    // release power lock if not video call
                    if (!mIsVideoCall && mWakeLock != null && mWakeLock.isHeld()) {
                        mWakeLock.release();
                    }
                    switch (args.getEventType()) {
                        case REMOTE_DEVICE_INFO_CHANGED: {
                            Log.d(TAG, String.format("Remote device info changed: orientation: %s",
                                    mAVSession.getRemoteDeviceInfo().getOrientation()));
                            break;
                        }
                        case MEDIA_UPDATED: {
                            if ((mIsVideoCall = (mAVSession.getMediaType() == NgnMediaType.AudioVideo || mAVSession
                                    .getMediaType() == NgnMediaType.Video))) {
                                loadInCallVideoView();
                            } else {
                                loadInCallAudioView();
                            }
                            break;
                        }
                        case LOCAL_TRANSFER_TRYING: {
                            if (mTvInfo != null) {
                                mTvInfo.setText("Call Transfer: Initiated");
                            }
                            break;
                        }
                        case LOCAL_TRANSFER_FAILED: {
                            if (mTvInfo != null) {
                                mTvInfo.setText("Call Transfer: Failed");
                            }
                            break;
                        }
                        case LOCAL_TRANSFER_ACCEPTED: {
                            if (mTvInfo != null) {
                                mTvInfo.setText("Call Transfer: Accepted");
                            }
                            break;
                        }
                        case LOCAL_TRANSFER_COMPLETED: {
                            if (mTvInfo != null) {
                                mTvInfo.setText("Call Transfer: Completed");
                            }
                            break;
                        }
                        case LOCAL_TRANSFER_NOTIFY:
                        case REMOTE_TRANSFER_NOTIFY: {
                            if (mTvInfo != null && mAVSession != null) {
                                short sipCode = intent.getShortExtra(
                                        NgnInviteEventArgs.EXTRA_SIPCODE, (short) 0);
                                mTvInfo.setText("Call Transfer: " + sipCode + " "
                                        + args.getPhrase());
                                if (sipCode >= 300 && mAVSession.isLocalHeld()) {
                                    mAVSession.resumeCall();
                                }
                            }
                            break;
                        }
                        case REMOTE_TRANSFER_REQUESTED: {
                            String referToUri = intent
                                    .getStringExtra(NgnInviteEventArgs.EXTRA_REFERTO_URI);
                            if (!NgnStringUtils.isNullOrEmpty(referToUri)) {
                                String referToName = NgnUriUtils.getDisplayName(referToUri);
                                if (!NgnStringUtils.isNullOrEmpty(referToName)) {
                                    mTransferDialog = CustomDialog.create(ScreenAV.this,
                                            R.drawable.ic_exit_to_app_grey600, null,
                                            "Call Transfer to "
                                                    + referToName + " requested. Do you accept?",
                                            "Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.cancel();
                                                    mTransferDialog = null;
                                                    if (mAVSession != null) {
                                                        mAVSession.acceptCallTransfer();
                                                    }
                                                }
                                            }, "No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.cancel();
                                                    mTransferDialog = null;
                                                    if (mAVSession != null) {
                                                        mAVSession.rejectCallTransfer();
                                                    }
                                                }
                                            });
                                    mTransferDialog.show();
                                }
                            }
                            break;
                        }
                        case REMOTE_TRANSFER_FAILED: {
                            if (mTransferDialog != null) {
                                mTransferDialog.cancel();
                                mTransferDialog = null;
                            }
                            mAVTransfSession = null;
                            break;
                        }
                        case REMOTE_TRANSFER_COMPLETED: {
                            if (mTransferDialog != null) {
                                mTransferDialog.cancel();
                                mTransferDialog = null;
                            }
                            if (mAVTransfSession != null) {
                                mAVTransfSession.setContext(mAVSession.getContext());
                                mAVSession = mAVTransfSession;
                                mAVTransfSession = null;
                                loadInCallView(true);
                            }
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    break;
                case TERMINATING:
                case TERMINATED:
                    if (mTransferDialog != null) {
                        mTransferDialog.cancel();
                        mTransferDialog = null;
                    }
                    mTimerSuicide
                            .schedule(mTimerTaskSuicide, new Date(new Date().getTime() + 1500));
                    mTimerTaskInCall.cancel();
                    mTimerBlankPacket.cancel();
                    loadTermView(SHOW_SIP_PHRASE ? args.getPhrase() : null);
                    // release power lock
                    if (mWakeLock != null && mWakeLock.isHeld()) {
                        mWakeLock.release();
                    }
                    break;
            }
        }
    }

    /*
     * 来电，远端响铃, 正在处理 对应tryingView 终止和正在终止对应TermView 拨入和Early_media对应InCallView
     */
    private void loadView() {
        if (null != mRingtone && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
        switch (mAVSession.getState()) {
            case INCOMING:
            case INPROGRESS:
            case REMOTE_RINGING:
                loadTryingView();
                break;
            case INCALL:
            case EARLY_MEDIA:
                loadInCallView();
                break;
            case NONE:
            case TERMINATING:
            case TERMINATED:
            default:
                loadTermView();
                break;
        }
    }

    private void loadTryingView() {
        if (mCurrentView == ViewType.ViewTrying) {
            return;
        }
        if (mViewTrying == null) {
            mViewTrying = mInflater.inflate(R.layout.view_call_trying, null);
        }
        mTvInfo = (TextView) mViewTrying.findViewById(R.id.view_call_trying_textView_info);
        final TextView tvRemote = (TextView) mViewTrying
                .findViewById(R.id.view_call_trying_textView_remote);
        tvRemote.setOnClickListener(this);
        final Button btPick = (Button) mViewTrying
                .findViewById(R.id.view_call_trying_imageButton_pick);
        btPick.setOnClickListener(this);
        final Button btTransfer = (Button) mViewTrying
                .findViewById(R.id.view_call_trying_imageButton_transfer);
        btTransfer.setOnClickListener(this);
        final Button btHang = (Button) mViewTrying
                .findViewById(R.id.view_call_trying_imageButton_hang);
        btHang.setOnClickListener(this);
        final ImageView ivAvatar = (ImageView) mViewTrying
                .findViewById(R.id.view_call_trying_imageView_avatar);
        switch (mAVSession.getState()) {
            case INCOMING:
                mTvInfo.setText(getResources().getString(R.string.string_in_coming));
                mRingtone.play();
                break;
            case INPROGRESS:
            case REMOTE_RINGING:
            case EARLY_MEDIA:
            default:
                mTvInfo.setText(getResources().getString(R.string.string_outgoing));
                btHang.setText(getResources().getString(R.string.string_end_call));
                btPick.setVisibility(View.GONE);
                btTransfer.setVisibility(View.GONE);
                break;
        }
        tvRemote.setText(mRemotePartyDisplayName.replace("%23", "#"));
        if (mRemotePartyPhoto != null) {
            ivAvatar.setImageBitmap(mRemotePartyPhoto);
        }
        mMainLayout.removeAllViews();
        mMainLayout.addView(mViewTrying);
        mCurrentView = ViewType.ViewTrying;
    }

    private void loadInCallAudioView() {
        if (mViewInAudioCall == null) {
            mViewInAudioCall = mInflater.inflate(R.layout.view_call_incall_audio, null);
        }
        // 通话过程中4个功能键
        final LinearLayout speakerImageView = (LinearLayout) mViewInAudioCall
                .findViewById(R.id.speakerphoneButton);
        speakerImageView.setOnClickListener(this);
        if (null != mAVSession && mAVSession.isSpeakerOn()) {
            setColorFilter(mViewInAudioCall, R.id.speaker_iv, Color.BLUE);
        } else {
            setColorFilter(mViewInAudioCall, R.id.speaker_iv, Color.WHITE);
        }


        final LinearLayout holdResumImageView = (LinearLayout) mViewInAudioCall
                .findViewById(R.id.callHoldButton);
        holdResumImageView.setOnClickListener(this);
        if (null != mAVSession && mAVSession.isLocalHeld()) {
            setColorFilter(mViewInAudioCall, R.id.hold_iv, Color.BLUE);
        } else {
            setColorFilter(mViewInAudioCall, R.id.hold_iv, Color.WHITE);
        }
        final LinearLayout keyBoardImageView = (LinearLayout) mViewInAudioCall
                .findViewById(R.id.KeyboardButton);
        keyBoardImageView.setOnClickListener(this);

        final LinearLayout microphoneButton = (LinearLayout) mViewInAudioCall
                .findViewById(R.id.callMicrophoneButton);

        microphoneButton.setOnClickListener(this);
        if (null != mAVSession && mAVSession.isMicrophoneMute()) {
            setColorFilter(mViewInAudioCall, R.id.mic_iv, Color.BLUE);
        } else {
            setColorFilter(mViewInAudioCall, R.id.mic_iv, Color.WHITE);
        }
        mTvInfo = (TextView) mViewInAudioCall
                .findViewById(R.id.view_call_incall_audio_textView_info);
        final TextView tvRemote = (TextView) mViewInAudioCall
                .findViewById(R.id.view_call_incall_audio_textView_remote);
        final Button btHang = (Button) mViewInAudioCall
                .findViewById(R.id.view_call_incall_audio_imageButton_hang);
        final ImageView ivAvatar = (ImageView) mViewInAudioCall
                .findViewById(R.id.view_call_incall_audio_imageView_avatar);
        mTvDuration = (TextView) mViewInAudioCall
                .findViewById(R.id.view_call_incall_audio_textView_duration);
        btHang.setOnClickListener(this);
        tvRemote.setText(mRemotePartyDisplayName.replace("%23", "#"));
        if (mRemotePartyPhoto != null) {
            ivAvatar.setImageBitmap(mRemotePartyPhoto);
        }
        mTvInfo.setText(getResources().getString(R.string.string_incall));
        mViewInAudioCall.findViewById(R.id.view_call_incall_audio_imageView_secure).setVisibility(
                mAVSession.isSecure() ? View.VISIBLE : View.INVISIBLE);
        // 呼叫保持,扬声器，发送文件, 静音 功能开启
        holdResumImageView.setEnabled(true);
        speakerImageView.setEnabled(true);
        mMainLayout.removeAllViews();
        mMainLayout.addView(mViewInAudioCall);
        mCurrentView = ViewType.ViewInCall;
    }

    private void loadInCallVideoView() {
        Log.d(TAG, "loadInCallVideoView()");
        if (mViewInCallVideo == null) {
            mViewInCallVideo = mInflater.inflate(R.layout.view_call_incall_video, null);
            mViewLocalVideoPreview = (FrameLayout) mViewInCallVideo
                    .findViewById(R.id.view_call_incall_video_FrameLayout_local_video);
            mViewRemoteVideoPreview = (FrameLayout) mViewInCallVideo
                    .findViewById(R.id.view_call_incall_video_FrameLayout_remote_video);
        }
        if (mTvDuration != null) {
            synchronized (mTvDuration) {
                mTvDuration = null;
            }
        }
        mTvInfo = null;
        mMainLayout.removeAllViews();
        mMainLayout.addView(mViewInCallVideo);
        final View viewSecure = mViewInCallVideo
                .findViewById(R.id.view_call_incall_video_imageView_secure);
        if (viewSecure != null) {
            viewSecure.setVisibility(mAVSession.isSecure() ? View.VISIBLE : View.INVISIBLE);
        }
        // Video Consumer
        loadVideoPreview();
        // Video Producer
        startStopVideo(mAVSession.isSendingVideo());
        mCurrentView = ViewType.ViewInCall;
    }

    private void loadInCallView(boolean force) {
        // 当前已经是通话界面且不强制刷新
        if (mCurrentView == ViewType.ViewInCall && !force) {
            return;
        }
        Log.d(TAG, "loadInCallView()");
        if (mIsVideoCall) {
            loadInCallVideoView();
        } else {
            loadInCallAudioView();
        }
    }

    private void loadInCallView() {
        loadInCallView(false);
    }

    private void loadProxSensorView() {
        if (mCurrentView == ViewType.ViewProxSensor) {
            return;
        }
        Log.d(TAG, "loadProxSensorView()");
        if (mViewProxSensor == null) {
            mViewProxSensor = mInflater.inflate(R.layout.view_call_proxsensor, null);

        }
        mMainLayout.removeAllViews();
        mMainLayout.addView(mViewProxSensor);
        mCurrentView = ViewType.ViewProxSensor;
    }

    private void loadTermView(String phrase) {
        Log.d(TAG, "loadTermView()");
        if (mViewTermwait == null) {
            mViewTermwait = mInflater.inflate(R.layout.view_call_trying, null);
        }
        mTvInfo = (TextView) mViewTermwait.findViewById(R.id.view_call_trying_textView_info);
        if (NgnStringUtils.isNullOrEmpty(phrase)) {
            mTvInfo.setText(getResources().getString(
                    R.string.string_call_terminated));
        } else {
            if (phrase.equals("Call Terminated")) {
                phrase = getResources().getString(R.string.string_call_terminated);
            } else if (phrase.equals("Request cancelled")) {
                phrase = getResources().getString(R.string.string_request_canceled);
            } else if (phrase.equals("Terminating dialog")) {
                phrase = getResources().getString(R.string.string_terminating_dialog);
            } else if (phrase.equals("Not Found")) {
                phrase = getResources().getString(R.string.string_not_found);
            }
            mTvInfo.setText(phrase);
        }


        // loadTermView() could be called twice (onTermwait() and OnTerminated)
        // and this is why we need to
        // update the info text for both
        if (mCurrentView == ViewType.ViewTermwait) {
            return;
        }
        final TextView tvRemote = (TextView) mViewTermwait
                .findViewById(R.id.view_call_trying_textView_remote);
        final ImageView ivAvatar = (ImageView) mViewTermwait
                .findViewById(R.id.view_call_trying_imageView_avatar);
        mViewTermwait.findViewById(R.id.view_call_trying_imageButton_pick).setVisibility(View.GONE);
        mViewTermwait.findViewById(R.id.view_call_trying_imageButton_hang).setVisibility(View.GONE);
        mViewTermwait.findViewById(R.id.view_call_trying_imageButton_transfer).setVisibility(
                View.GONE);
        mViewTermwait.setBackgroundResource(R.drawable.grad_bkg_termwait);
        tvRemote.setText(mRemotePartyDisplayName.replace("%23", "#"));
        if (mRemotePartyPhoto != null) {
            ivAvatar.setImageBitmap(mRemotePartyPhoto);
        }
        mMainLayout.removeAllViews();
        mMainLayout.addView(mViewTermwait);
        mCurrentView = ViewType.ViewTermwait;
    }

    private void loadTermView() {
        loadTermView(null);
    }

    private void loadVideoPreview() {
        mViewRemoteVideoPreview.removeAllViews();
        final View remotePreview = mAVSession.startVideoConsumerPreview();
        if (remotePreview != null) {
            final ViewParent viewParent = remotePreview.getParent();
            if (viewParent != null && viewParent instanceof ViewGroup) {
                ((ViewGroup) (viewParent)).removeView(remotePreview);
            }
            mViewRemoteVideoPreview.addView(remotePreview);
        }
    }

    private final TimerTask mTimerTaskInCall = new TimerTask() {
        @Override
        public void run() {
            if (mAVSession != null && mTvDuration != null) {
                synchronized (mTvDuration) {
                    final Date date = new Date(new Date().getTime() - mAVSession.getStartTime());
                    ScreenAV.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                mTvDuration.setText(sDurationTimerFormat.format(date));
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }
        }
    };
    private final TimerTask mTimerTaskBlankPacket = new TimerTask() {
        @Override
        public void run() {
            Log.d(TAG, "Resending Blank Packet " + String.valueOf(mCountBlankPacket));
            if (mCountBlankPacket < 3) {
                if (mAVSession != null) {
                    mAVSession.pushBlankPacket();
                }
                mCountBlankPacket++;
            } else {
                cancel();
                mCountBlankPacket = 0;
            }
        }
    };

    private void cancelBlankPacket() {
        if (mTimerBlankPacket != null) {
            mTimerBlankPacket.cancel();
            mCountBlankPacket = 0;
        }
    }

    private final TimerTask mTimerTaskSuicide = new TimerTask() {
        @Override
        public void run() {
            ScreenAV.this.runOnUiThread(new Runnable() {
                public void run() {
                    startActivity(new Intent(ScreenAV.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                }
            });
        }
    };

    private void startStopVideo(boolean bStart) {
        Log.d(TAG, "startStopVideo(" + bStart + ")");
        if (!mIsVideoCall) {
            return;
        }
        mAVSession.setSendingVideo(bStart);
        if (mViewLocalVideoPreview != null) {
            mViewLocalVideoPreview.removeAllViews();
            if (bStart) {
                cancelBlankPacket();
                final View localPreview = mAVSession.startVideoProducerPreview();
                if (localPreview != null) {
                    final ViewParent viewParent = localPreview.getParent();
                    if (viewParent != null && viewParent instanceof ViewGroup) {
                        ((ViewGroup) (viewParent)).removeView(localPreview);
                    }
                    if (localPreview instanceof SurfaceView) {
                        ((SurfaceView) localPreview).setZOrderOnTop(true);
                    }
                    mViewLocalVideoPreview.addView(localPreview);
                    mViewLocalVideoPreview.bringChildToFront(localPreview);
                }
            }
            mViewLocalVideoPreview.setVisibility(bStart ? View.VISIBLE : View.GONE);
            mViewLocalVideoPreview.bringToFront();
        }
    }

    private void loadKeyboard() {
        DtmfDialogFragment newFragment = DtmfDialogFragment.newInstance(1);
        newFragment.show(getSupportFragmentManager(), "dialog");

    }

    /**
     * MyProxSensor 监视原始的传感器数据
     */
    class MyProxSensor implements SensorEventListener {

        private final INgnConfigurationService mConfigurationService;
        private final SensorManager mSensorManager;
        private Sensor mSensor;
        private final ScreenAV mAVScreen;
        private float mMaxRange;

        MyProxSensor(ScreenAV avScreen) {
            mAVScreen = avScreen;
            mSensorManager = ZphoneApplication.getSensorManager();
            mConfigurationService = Engine.getInstance().getConfigurationService();
        }

        void start() {
            if (mSensorManager != null && mSensor == null) {
                if ((mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)) != null) {
                    mMaxRange = mSensor.getMaximumRange();
                    mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
                }
            }
        }

        void stop() {
            if (mSensorManager != null && mSensor != null) {
                mSensorManager.unregisterListener(this);
                mSensor = null;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] its = event.values;

            try { // Keep it until we get a phone supporting this feature
                if (mAVScreen == null) {
                    Log.e(ScreenAV.TAG, "invalid state");
                    return;
                }
                if (event.values != null && event.values.length > 0) {
                    if (event.values[0] < this.mMaxRange) {
                        Log.d(TAG, "reenableKeyguard()");
                        mAVScreen.loadProxSensorView();
                    } else {

                        Log.d(TAG, "disableKeyguard()");
                        mAVScreen.loadView();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean enable_pro_sensor = mConfigurationService.getBoolean(ZycooConfigurationEntry.GENERAL_PROXIMITY_SENSOR, ZycooConfigurationEntry.DEFAULT_GENERAL_PROXIMITY_SENSOR);
            if (enable_pro_sensor && null != mProSensorWakeLock && its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (its[0] != mMaxRange) {// 贴近手机
                    if (mProSensorWakeLock.isHeld()) {
                        return;
                    } else {
                        mProSensorWakeLock.acquire();// 申请设备电源锁
                    }
                } else {// 远离手机
                    if (mProSensorWakeLock.isHeld()) {
                        return;
                    } else {
                        mProSensorWakeLock.setReferenceCounted(false);
                        mProSensorWakeLock.release(); // 释放设备电源锁
                    }
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_call_trying_imageButton_transfer:
                transferCall();
                break;
            case R.id.view_call_trying_imageButton_pick:
                acceptCall();
                break;
            case R.id.view_call_trying_imageButton_hang:
            case R.id.view_call_incall_audio_imageButton_hang:
                hangUpCall();
                break;
            case R.id.speakerphoneButton:
                mAVSession.toggleSpeakerphone();
                if (mAVSession.isSpeakerOn()) {
                    setColorFilter(v, R.id.speaker_iv, Color.BLUE);
                } else {
                    setColorFilter(v, R.id.speaker_iv, Color.WHITE);
                }
                break;
            /*case R.id.callVideoButton:
                mLogger.debug( "startStopVideo");
                startStopVideo(mAVSession.isSendingVideo());
                if (mAVSession.isSendingVideo())
                {
                    v.setBackgroundColor(resources.getColor(R.color.light_blue));
                }
                else
                {
                    v.setBackgroundColor(resources.getColor(R.color.gray));
                }
                break;*/
            case R.id.callHoldButton:
                if (mAVSession.isLocalHeld()) {
                    setColorFilter(v, R.id.hold_iv, Color.WHITE);
                    mAVSession.resumeCall();
                } else {
                    mAVSession.holdCall();
                    setColorFilter(v, R.id.hold_iv, Color.BLUE);
                }
                break;

            case R.id.callMicrophoneButton:
                if (mAVSession.isMicrophoneMute()) {
                    mAVSession.setMicrophoneMute(false);
                    setColorFilter(v, R.id.mic_iv, Color.WHITE);
                } else {
                    mAVSession.setMicrophoneMute(true);
                    setColorFilter(v, R.id.mic_iv, Color.BLUE);
                }
                break;
            case R.id.KeyboardButton:
                loadKeyboard();
                break;
            default:
                break;
        }
    }

    private void setColorFilter(View lv, int id, int color) {
        ImageView iv = (ImageView) lv.findViewById(id);
        iv.setColorFilter(color);
    }

    private void transferCall() {
        acceptCall();
        mAVSession.sendDTMF(10);
        mAVSession.sendDTMF(2);
        loadKeyboard();
    }

    @Override
    public void OnDtmf(int callId, int keyCode, int dialTone) {
        if (mAVSession != null) {
            mAVSession.sendDTMF(keyCode - 7);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}

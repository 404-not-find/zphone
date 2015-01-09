package com.zycoo.android.zphone.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.ui.LaunchActivity;

import org.doubango.ngn.sip.NgnSubscriptionSession;


/**
 * Created by tqcenglish on 14-12-29.
 */
public class ActivityTest extends ActivityInstrumentationTestCase2<LaunchActivity> {
    private static final int TONE_RELATIVE_VOLUME = 80;

    public ActivityTest() {
        super(LaunchActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Context context = getActivity();
        //context.startActivity(new Intent(getActivity(), SplashActivity.class));
        SystemClock.sleep(3000);
        //MainActivity
    }

    public void testMwi() throws Exception {
        NgnSubscriptionSession subSession;
        if (Engine.getInstance().getSipService().isRegistered()
                ) {
            subSession = NgnSubscriptionSession.createOutgoingSession(Engine.getInstance().getSipService().getSipStack(), "sip:804@192.168.1.49", NgnSubscriptionSession.EventPackageType.Presence);
            boolean result = subSession.subscribe();
            Log.d("TQC", "subscribe" + result);
            SystemClock.sleep(30000);

            result =  subSession.isConnected();

            Log.d("TQC", "subscribe" + result);
        }
    }

    public void testPlayTone() throws Exception {
        // DialerFragment df = (DialerFragment) getActivity().getCurrentFragment();
       /* df.onTrigger(1, 2);
        df.onTrigger(1, 3);
        df.onTrigger(1, 4);
        df.onTrigger(1, 5);
        df.onTrigger(1, 6);
        df.onTrigger(1, 7);
        df.onTrigger(1, 8);*/
        //df.onTrigger(1, 9);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}

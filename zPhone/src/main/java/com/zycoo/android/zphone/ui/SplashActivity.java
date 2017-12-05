
package com.zycoo.android.zphone.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.NativeService;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.ZycooConfigurationEntry;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SplashActivity extends Activity {
    private BroadcastReceiver mBroadCastRecv;
    private Logger mLogger = LoggerFactory.getLogger(SplashActivity.class.getCanonicalName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.id_launch_pgb);
        progressBar.setVisibility(View.VISIBLE);

        mBroadCastRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (NativeService.ACTION_STATE_EVENT.equals(intent.getAction())) {
                    if (intent.getBooleanExtra("started", false)) {
                        //TODO CheckNewWork
                        //check weather first Login ?
                        INgnConfigurationService configurationService = Engine.getInstance()
                                .getConfigurationService();
                        boolean isFirst = configurationService.getBoolean(ZycooConfigurationEntry.FIRST_LOGIN, ZycooConfigurationEntry.DEFAULT_FIRST_LOGIN);
                        if (isFirst) {
                            configurationService.putBoolean(ZycooConfigurationEntry.FIRST_LOGIN, false);
                            startActivity(new Intent(SplashActivity.this,
                                    IdentitySettingsActivity.class));
                        } else {
                            mLogger.debug("startActivity " + Utils.getTime());
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            configurationService.putBoolean(
                                    NgnConfigurationEntry.GENERAL_AUTOSTART, true);
                        }
                        configurationService.commit();
                        finish();
                    }
                }
            }
        };
        // NativeService.ACTION_STATE_EVENT
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NativeService.ACTION_STATE_EVENT);
        registerReceiver(mBroadCastRecv, intentFilter);
    }

    @Override
    protected void onDestroy() {
        if (mBroadCastRecv != null) {
            unregisterReceiver(mBroadCastRecv);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Engine.getInstance().isStarted()) {
                    Engine.getInstance().start();
                }
            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }
}

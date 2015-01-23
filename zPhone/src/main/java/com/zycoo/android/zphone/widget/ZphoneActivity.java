
package com.zycoo.android.zphone.widget;

import android.app.Activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZphoneActivity extends Activity {
    protected Logger mLogger;

    public ZphoneActivity(String name)
    {
        mLogger = LoggerFactory.getLogger(name);
    }

}

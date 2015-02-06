
package com.zycoo.android.zphone.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;

import org.doubango.ngn.services.INgnSipService;

import android.os.Handler;

public class RegisterTask extends AsyncTask<Void, Void, Boolean> {
    private SwitchButton switchButton;
    private Context mContext;
    private boolean mRegister;
    private INgnSipService mSipService;


    public RegisterTask(boolean register) {
        this(null, register);
    }

    public RegisterTask(boolean register, SwitchButton switchButton) {
        this(null, register);
        this.switchButton = switchButton;
    }

    public RegisterTask(Context context, boolean register) {
        if (null == context) {
            mContext = ZphoneApplication.getContext();
        } else {
            mContext = context;
        }
        mSipService = Engine.getInstance().getSipService();
        mRegister = register;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (!result) {
            Toast.makeText(mContext, ZphoneApplication.
                            getAppResources().getString(R.string.connect_server_failure),
                    Toast.LENGTH_SHORT).show();
            if (null != switchButton) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switchButton.setChecked(false);
                    }
                }, 1500);
            }
        }

        super.onPostExecute(result);
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        boolean result = false;
        if (mRegister) {
            if (mSipService.isRegistered()) {
                mSipService.unRegister();
            }
            result = mSipService.register(ZphoneApplication.getContext());
        } else {
            result = mSipService.unRegister();
        }
        return result;
    }

}

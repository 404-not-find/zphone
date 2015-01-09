
package com.zycoo.android.zphone.ui.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.kyleduo.switchbutton.SwitchButton;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.dialpad.CustomDialog;

import org.doubango.ngn.utils.NgnStringUtils;

/**
 * 设置界面继承本类，自动载入和保存配置值
 *
 * @author root
 */
public class BaseScreen
        extends SherlockFragmentActivity {

    public static enum SCREEN_TYPE {
        // Well-Known
        ABOUT_T,
        AV_QUEUE_T,
        CHAT_T,
        CHAT_QUEUE_T,
        CODECS_T,
        CONTACTS_T,
        DIALER_T,
        FILETRANSFER_QUEUE_T,
        FILETRANSFER_VIEW_T,
        HOME_T,
        IDENTITY_T,
        INTERCEPT_CALL_T,
        GENERAL_T,
        MESSAGING_T,
        NATT_T,
        NETWORK_T,
        PRESENCE_T,
        QOS_T,
        SETTINGS_T,
        SECURITY_T,
        SPLASH_T,

        TAB_CONTACTS,
        TAB_HISTORY_T,
        TAB_INFO_T,
        TAB_ONLINE,
        TAB_MESSAGES_T,

        // All others
        AV_T
    }

    protected boolean mComputeConfiguration;

    protected Handler mHandler;

    protected ProgressDialog mProgressDialog;

    public Engine getEngine() {
        return (Engine) Engine.getInstance();
    }

    @Override
    protected void onCreate(Bundle arg0) {
        mHandler = new Handler();
        super.onCreate(arg0);
    }

    protected int getSpinnerIndex(String value, String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (NgnStringUtils.equals(value, values[i], true)) {
                return i;
            }
        }
        return 0;
    }


    protected void addConfigurationListener(SwitchButton switchButton) {
        switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mComputeConfiguration = true;
            }
        });
    }

    protected void addConfigurationListener(RadioButton radioButton) {
        radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                mComputeConfiguration = true;
            }
        });
    }

    protected void addConfigurationListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                mComputeConfiguration = true;
            }
        });
    }

    protected void addConfigurationListener(CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                mComputeConfiguration = true;
            }
        });
    }

    protected void addConfigurationListener(Spinner spinner) {
        // setOnItemClickListener not supported by Spinners
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                mComputeConfiguration = true;
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    protected int getSpinnerIndex(int value, int[] values) {
        for (int i = 0; i < values.length; i++) {
            if (value == values[i]) {
                return i;
            }
        }
        return 0;
    }

    protected void showInProgress(String text, boolean bIndeterminate,
                                  boolean bCancelable) {
        synchronized (this) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mProgressDialog = null;
                    }
                });
                mProgressDialog.setMessage(text);
                mProgressDialog.setIndeterminate(bIndeterminate);
                mProgressDialog.setCancelable(bCancelable);
                mProgressDialog.show();
            }
        }
    }

    protected void cancelInProgress() {
        synchronized (this) {
            if (mProgressDialog != null) {
                mProgressDialog.cancel();
                mProgressDialog = null;
            }
        }
    }

    protected void cancelInProgressOnUiThread() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelInProgress();
            }
        });
    }

    protected void showInProgressOnUiThread(final String text,
                                            final boolean bIndeterminate, final boolean bCancelable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showInProgress(text, bIndeterminate, bCancelable);
            }
        });
    }

    protected void showMsgBox(String title, String message) {
        final AlertDialog dialog =
                CustomDialog.create(this, R.drawable.icon, title, message, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }, null, null);
        dialog.show();
    }

    protected void showMsgBoxOnUiThread(final String title, final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showMsgBox(title, message);
            }
        });
    }

    protected String getPath(Uri uri) {
        try {
            String[] projection =
                    {
                            MediaStore.Images.Media.DATA
                    };
            Cursor cursor = managedQuery(uri, projection, null, null, null);
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            final String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            int SUCCESS_RESULT = 1;
            setResult(SUCCESS_RESULT, new Intent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setComputeConfiguration()
    {
        mComputeConfiguration =  true;
    }
}

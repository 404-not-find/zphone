
package com.zycoo.android.zphone.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;

import org.doubango.ngn.services.INgnSipService;

import java.io.File;
import java.util.List;

public class DeleteFileTask extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    private String mPath;
    public DeleteFileTask(Context context, String path, List<?> list) {
        mContext = context;
        mPath = path;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return  new File(mPath).delete();
    }

}

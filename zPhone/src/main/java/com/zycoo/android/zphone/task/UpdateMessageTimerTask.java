
package com.zycoo.android.zphone.task;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zycoo.android.zphone.DatabaseHelper;
import com.zycoo.android.zphone.HttpConnectBase;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ZycooConfigurationEntry;
import com.zycoo.android.zphone.ui.message.MessageFragment;
import com.zycoo.android.zphone.ui.message.MonitorBean;
import com.zycoo.android.zphone.ui.message.VoiceMailBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class UpdateMessageTimerTask extends HttpConnectBase
{
    private DatabaseHelper mDatabaseHelper;
    private Handler mHandler;
    private Logger mLogger = LoggerFactory.getLogger(UpdateMessageTimerTask.class);

    public UpdateMessageTimerTask(Handler handler)
    {
        mHandler = handler;
        mDatabaseHelper = new DatabaseHelper(ZphoneApplication.getContext(),
                ZycooConfigurationEntry.DATA_BASE_NAME, null, 1);

    }

    public String get_all_voicemails()
    {
        return String
                .format("http://%s:4242/get_all_voicemails?origmailbox=%s",
                        mHost, mUsername);
    }

    public String get_all_records()
    {
        return String
                .format("http://%s:4242/get_all_records?extension=%s",
                        mHost, mUsername);
    }

    @Override
    public void doGet()
    {
        createConnect(get_all_voicemails());
        decodingJson();
        cleanConnect();
        createConnect(get_all_records());
        decodingJson();
        cleanConnect();
    }

    public String convertStreamToString(InputStream is) {
        if (null != is)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }

    public void decodingJson()
    {
        SQLiteDatabase wSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        mLogger.debug("json befor time" + dateFormat.format(cal.getTime()));
        String outJson = convertStreamToString(mInputStream);
        mLogger.debug("json after time" + dateFormat.format(cal.getTime()));
        if (!outJson.isEmpty())
        {
            JsonParser jsonParser = new JsonParser();
            JsonObject root = jsonParser.parse(outJson).getAsJsonObject();
            Gson gson = new Gson();
            switch (root.get("type").getAsString()) {
                case "voicemail":
                    List<VoiceMailBean> voiceMails = gson.fromJson(root.get("voicemails")
                            .getAsJsonArray(),
                            new TypeToken<List<VoiceMailBean>>() {
                            }.getType());
                    //remove_from_call_log old
                    wSqLiteDatabase.execSQL("remove_from_call_log from INFOS");
                    //insert init
                    for (com.zycoo.android.zphone.ui.message.VoiceMailBean voiceMailBean : voiceMails)
                    {
                        String sql = voiceMailBean.insertSql();
                        wSqLiteDatabase.execSQL(sql);
                        mLogger.debug(sql);
                    }
                    break;
                case "monitor":
                    List<MonitorBean> monitorBeans = gson.fromJson(root.get("monitors")
                            .getAsJsonArray(),
                            new TypeToken<List<MonitorBean>>() {
                            }.getType());
                    //remove_from_call_log old
                    wSqLiteDatabase.execSQL("remove_from_call_log from MONITORS");
                    //insert init
                    for (MonitorBean monitorBean : monitorBeans)
                    {
                        String sql = monitorBean.insertSql();
                        wSqLiteDatabase.execSQL(sql);
                        mLogger.debug(sql);
                    }
                    break;

                default:
                    break;
            }

            wSqLiteDatabase.close();
            mHandler.sendEmptyMessage(MessageFragment.HANDLE_WHAT);
        }
    }

    @Override
    public void doPost() {
    }

    @Override
    public void run() {
        doGet();
    }
}

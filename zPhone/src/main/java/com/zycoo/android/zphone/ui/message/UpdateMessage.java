
package com.zycoo.android.zphone.ui.message;

import android.database.sqlite.SQLiteDatabase;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zycoo.android.zphone.DatabaseHelper;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.utils.ZycooConfigurationEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class UpdateMessage {
    private DatabaseHelper mDatabaseHelper;
    private Logger mLogger = LoggerFactory.getLogger(UpdateMessage.class);

    public UpdateMessage() {
        mDatabaseHelper = new DatabaseHelper(ZphoneApplication.getContext(),
                ZycooConfigurationEntry.DATA_BASE_NAME, null, 1);

    }

    public void Get() {
        String rquestVoicemail = String
                .format("http://%s:4242/get_all_voicemails?origmailbox=%s",
                        ZphoneApplication.getHost(), ZphoneApplication.getUserName());
        HttpRequest request = HttpRequest.get(rquestVoicemail);
        if (request.ok()) {
            decodingJson(request.body());
            request.disconnect();
        }

        String rquestRecord = String
                .format("http://%s:4242/get_all_records?extension=%s",
                        ZphoneApplication.getHost(), ZphoneApplication.getUserName());
        request = HttpRequest.get(rquestRecord);
        if (request.ok()) {
            decodingJson(request.body());
            request.disconnect();
        }
    }

    public String convertStreamToString(InputStream is) {
        if (null != is) {
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
        } else {
            return "";
        }
    }

    public void decodingJson(String outJson) {
        SQLiteDatabase wSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
        if (!"".equals(outJson)) {
            JsonParser jsonParser = new JsonParser();
            JsonObject root = jsonParser.parse(outJson).getAsJsonObject();
            Gson gson = new Gson();
            switch (root.get("type").getAsString()) {
                case "voicemail":
                    List<VoiceMailBean> voiceMails = gson.fromJson(root.get("voicemails")
                                    .getAsJsonArray(),
                            new TypeToken<List<VoiceMailBean>>() {
                            }.getType());
                    //delete old
                    wSqLiteDatabase.execSQL("delete from INFOS");
                    //insert init
                    for (VoiceMailBean voiceMailBean : voiceMails) {
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
                    //delete old
                    wSqLiteDatabase.execSQL("delete from MONITORS");
                    //insert init
                    for (MonitorBean monitorBean : monitorBeans) {
                        String sql = monitorBean.insertSql();
                        wSqLiteDatabase.execSQL(sql);
                        mLogger.debug(sql);
                    }
                    break;

                default:
                    break;
            }
            wSqLiteDatabase.close();
        }
    }
}

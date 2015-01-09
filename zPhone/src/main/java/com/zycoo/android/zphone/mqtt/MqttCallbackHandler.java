/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */

package com.zycoo.android.zphone.mqtt;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zycoo.android.zphone.DatabaseHelper;
import com.zycoo.android.zphone.NativeService;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.mqtt.Connection.ConnectionStatus;
import com.zycoo.android.zphone.ui.message.MessageFragment;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles call backs from the MQTT Client
 */
public class MqttCallbackHandler implements MqttCallback {

    /**
     * {@link Context} for the application used to format and import external
     * strings
     **/
    private Context context;
    /**
     * Client handle to reference the connection that this handler is attached
     * to
     **/
    private String clientHandle;

    /**
     * 解析mqtt传送的消息
     */
    private JsonParser jsonparer;
    /**
     * 更新messagefragment
     */
    private Handler handler;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * log
     */
    private Logger logger = LoggerFactory.getLogger(MqttCallbackHandler.class);

    /**
     * Creates an <code>MqttCallbackHandler</code> object
     * 
     * @param context The application's context
     * @param clientHandle The handle to a {@link Connection} object
     */
    public MqttCallbackHandler(Context context, String clientHandle)
    {
        this.context = context;
        this.clientHandle = clientHandle;
        this.jsonparer = new JsonParser();
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
        //	  cause.printStackTrace();
        if (cause != null) {
            Connection c = Connections.getInstance(context).getConnection(clientHandle);
            c.addAction("Connection Lost");
            c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

            //format string to use a notification text
            Object[] args = new Object[2];
            args[0] = c.getId();
            args[1] = c.getHostName();

            String message = context.getString(R.string.connection_lost, args);

            //build intent
            Intent intent = new Intent();
            intent.setClassName(context,
                    "org.eclipse.paho.android.service.sample.ConnectionDetails");
            intent.putExtra("handle", clientHandle);

            //notify the user
            //Notify.notifcation(context, message, intent, R.string.notifyTitle_connectionLost);

            //reconnect
           ((NativeService)context).connectAction();
        }
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String,
     *      org.eclipse.paho.client.mqttv3.MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        //Get connection object associated with this object
        Connection c = Connections.getInstance(context).getConnection(clientHandle);

        //create arguments to format message arrived notifcation string
        String[] args = new String[2];
        args[0] = new String(message.getPayload());
        args[1] = topic;

        //get the string from strings.xml and format
        String messageString = context.getString(R.string.messageRecieved, (Object[]) args);

        //create intent to start activity
        Intent intent = new Intent();
        intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
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

            SQLiteDatabase wSQLiteDatabase = new DatabaseHelper(context.getApplicationContext(),
                    "SoftPhone.db", null, 1)
                    .getWritableDatabase();
            String sql = root.get("message").getAsString();
            logger.debug(sql);
            wSQLiteDatabase.execSQL(sql);
            wSQLiteDatabase.close();
            if (sql.contains("insert") && sql.contains("INBOX"))
            {
                Notify.notifcation(context, "New Voice Mail", intent, R.string.notifyTitle);
            }
            //null
            handler.sendEmptyMessage(MessageFragment.HANDLE_WHAT);
            //intent.setAction(MessageFragment.UPDATE_MESSAGE_FRAGMENT);  
            context.sendBroadcast(intent);//发送广播 
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
    
    

}

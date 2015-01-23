
package com.zycoo.android.zphone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String LOG_TAG = DatabaseHelper.class.getCanonicalName();
    private Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

    public DatabaseHelper(Context context, String name, CursorFactory factory, int version)
    {
        super(context, name, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createVoiceMailTB = "CREATE TABLE INFOS( ID INTEGER PRIMARY KEY AUTOINCREMENT, ORIGMAILBOX TEXT NOT NULL,  CONTEXT TEXT NOT NULL, MACROCONTEXT TEXT, EXTEN TEXT, RDNIS TEXT, PRIORITY TEXT,  CALLERCHAN TEXT, CALLERID TEXT, ORIGDATE TEXT, ORIGTIME TEXT, CATEGORY TEXT, MSG_ID TEXT, FLAG TEXT, DURATION TEXT, TYPE TEXT NOT NULL, WD INTEGER NOT NULL, FILE_NAME TEXT NOT NULL);";
        String createMonitorTB = "CREATE TABLE MONITORS(ID INTEGER PRIMARY KEY AUTOINCREMENT, DURATION INTEGER, TIME TEXT, FILE_FORMATE TEXT, FROM_EXTENSION TEXT, TO_EXTENSION TEXT, TYPE TEXT, FILE_NAME TEXT);";
        logger.debug(LOG_TAG + "voicemails table: " + createVoiceMailTB);
        logger.debug(LOG_TAG + "monitory table: " + createMonitorTB);
        db.execSQL(createVoiceMailTB);
        db.execSQL(createMonitorTB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}

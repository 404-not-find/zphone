package com.zycoo.android.zphone.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.snowdream.android.app.AbstractUpdateListener;
import com.github.snowdream.android.app.DownloadTask;
import com.github.snowdream.android.app.UpdateFormat;
import com.github.snowdream.android.app.UpdateInfo;
import com.github.snowdream.android.app.UpdateManager;
import com.github.snowdream.android.app.UpdateOptions;
import com.github.snowdream.android.app.UpdatePeriod;
import com.kyleduo.switchbutton.BuildConfig;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.me.ListViewItem;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemTextOnly;
import com.zycoo.android.zphone.ui.me.ListViewItemWhite;
import com.zycoo.android.zphone.utils.Utils;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.Util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AboutActivity extends BaseScreen implements AdapterView.OnItemClickListener {

    private ListViewItem[] items;
    private ListView mListView;
    private TextView mVersionTv;
    private BaseAdapter adapter;
    private Logger mLogger = LoggerFactory.getLogger(AboutActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(android.R.id.list);
        mVersionTv = (TextView) findViewById(R.id.zphone_version);
        initData();
        isFreeVersion();
        adapter = new ScreenSettingsAdapter(this, items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
        mVersionTv.setText("ZPhone " + Utils.getVersion(this));
    }

    private void initData() {
        items = new ListViewItem[3];
        items[0] = new ListViewItemWhite(20);
        items[1] = new ListViewItemTextOnly(R.string.feedback, true);
        items[2] = new ListViewItemTextOnly(R.string.check_for_updates, false);

    }

    private void isFreeVersion() {

    }

    public void onCheckUpdateClick() {

        UpdateManager manager = new UpdateManager(this);
        UpdateOptions options = new UpdateOptions.Builder(this)
                .checkUrl("https://raw.githubusercontent.com/tqcenglish/ZPhone/master/updateinfo.xml")
                .updateFormat(UpdateFormat.XML)
                .updatePeriod(new UpdatePeriod(UpdatePeriod.EACH_TIME))
                .checkPackageName(false)
                .build();
        manager.check(this, options);
//
//        UpdateOptions options = new UpdateOptions.Builder(this)
//                .checkUrl("https://raw.github.com/snowdream/android-autoupdate/master/docs/test/updateinfo.json")
//                .updateFormat(UpdateFormat.JSON)
//                .updatePeriod(new UpdatePeriod(UpdatePeriod.EACH_TIME))
//                .checkPackageName(true)
//                .build();


        /*manager.check(this, options, new AbstractUpdateListener() {
            *//**
         * Exit the app here
         *//*
            @Override
            public void ExitApp() {

            }

            *//**
         * show the update dialog
         *
         * @param info the info for the new app
         *//*
            @Override
            public void onShowUpdateUI(UpdateInfo info) {

            }

            *//**
         * It's the latest app,or there is no need to update.
         *//*
            @Override
            public void onShowNoUpdateUI() {

            }

            *//**
         * show the progress when downloading the new app
         *
         * @param info
         * @param task
         * @param progress
         *//*
            @Override
            public void onShowUpdateProgressUI(UpdateInfo info, DownloadTask task, int progress) {

            }

            *//**
         * show the checking dialog
         *//*
            @Override
            public void onStart() {
                super.onStart();
            }

            *//**
         * hide the checking dialog
         *//*
            @Override
            public void onFinish() {
                super.onFinish();
                mVersionTv.setText("ZPhone " + org.doubango.ngn.BuildConfig.VERSION_CODE);
            }
        });*/
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 1:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "tqcenglishc@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ZPhone " + getResources().getString(R.string.feedback));
                zipFileAtPath(Environment.getExternalStorageDirectory() + "/zycoo/crash", Environment.getExternalStorageDirectory() + "/zycoo/crash.zip");
                //send email
                Uri crashUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "/zycoo/crash.zip"));
                emailIntent.putExtra(Intent.EXTRA_STREAM, crashUri);
                //emailIntent.setType("text/plain");
                //emailIntent.setType("*/*");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
            case 2:
                onCheckUpdateClick();
                break;
            default:
                break;
        }
    }

 /*
 *
 * Zips a file at a location and places the resulting zip file at the toLocation
 * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
 */

    public boolean zipFileAtPath(String sourcePath, String toLocation) {
        // ArrayList<String> contentList = new ArrayList<String>();
        final int BUFFER = 2048;


        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


   /*
    Zips a subfolder
    */

    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                mLogger.debug("ZIP SUBFOLDER", "Relative Path : " + relativePath);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }
}

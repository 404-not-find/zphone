package com.zycoo.android.zphone.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.zycoo.android.zphone.R;

import java.util.Locale;

public class HelpActivity extends BaseScreen
{
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mWebView = (WebView) findViewById(R.id.id_about_htlp_wv);
		mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        if("zh".equals(Locale.getDefault().getLanguage()))
        {
            mWebView.loadUrl("file:///android_asset/www/help_zh.html");
        }
        else
        {
            mWebView.loadUrl("file:///android_asset/www/help.html");
        }
		//mWebView.loadDataWithBaseURL("file:///android_asset/www/help.html", "text/html", "UTF-8") ;
	}


}

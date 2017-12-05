package com.zycoo.android.zphone.ui.settings;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.zycoo.android.zphone.R;

import java.util.Locale;

public class HelpActivity extends BaseScreen
{
	private WebView mWebView;
    private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
        getActionBar().setDisplayHomeAsUpEnabled(true);
		mProgressBar = (ProgressBar) findViewById(R.id.web_view_pb);
        mWebView = (WebView) findViewById(R.id.id_about_htlp_wv);

		mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebViewClient());
        mWebView.setWebViewClient(new android.webkit.WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }


        });
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

    @Override
    public boolean onKeyDown(int keyCoder,KeyEvent event){
        if(mWebView.canGoBack() && keyCoder == KeyEvent.KEYCODE_BACK){
            mWebView.goBack();   //goBack()表示返回webView的上一页面
            return true;
        }
        return super.onKeyDown(keyCoder, event);
    }

    private class WebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);
            if(newProgress==100){
                mProgressBar.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }



    }
}

package com.zycoo.android.zphone.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.sql.Connection;

/**
 * Created by tqcenglish on 14-9-12.
 */
public class NetWorkUtils
{
	public boolean isNetworkConnected(Context context)
	{
		if (null != context)
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (null != networkInfo)
			{
				return networkInfo.isAvailable();
			}
		}
		return false;
	}

	public boolean isWifiConnected(Context context)
	{
		if (null != context)
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (null != networkInfo)
			{
				return networkInfo.isAvailable();
			}
		}
		return false;
	}

	public boolean isMobileConnected(Context context)
	{
		if (null != context)
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (null != networkInfo)
			{
				return networkInfo.isAvailable();
			}
		}
		return false;
	}

	public int getConnectedType(Context context)
	{
		if (null != context)
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (null != networkInfo && networkInfo.isAvailable())
			{
				return networkInfo.getType();
			}
		}
		return 0;
	}
}

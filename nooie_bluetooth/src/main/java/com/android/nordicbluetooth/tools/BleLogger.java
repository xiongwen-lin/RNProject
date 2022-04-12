package com.android.nordicbluetooth.tools;

import android.util.Log;

import com.android.nordicbluetooth.BuildConfig;

public class BleLogger {
	public static void v(final String tag, final String text) {
		if (BuildConfig.DEBUG)
			Log.v(tag, text);
	}

	public static void d(final String tag, final String text) {
		if (BuildConfig.DEBUG) {
			Log.d(tag, text);
		}
	}

	public static void i(final String tag, final String text) {
		if (BuildConfig.DEBUG)
			Log.i(tag, text);
	}

	public static void w(final String tag, final String text) {
		if (BuildConfig.DEBUG) {
			Log.w(tag, text);
		}
	}

	public static void e(final String tag, final String text) {
		if (BuildConfig.DEBUG)
			Log.e(tag, text);
	}

	public static void e(final String tag, final String text, final Throwable e) {
		if (BuildConfig.DEBUG)
			Log.e(tag, text, e);
	}

	public static void wtf(final String tag, final String text) {
		if (BuildConfig.DEBUG) {
			Log.wtf(tag, text);
		}
	}

	public static void wtf(final String tag, final String text, final Throwable e) {
		if (BuildConfig.DEBUG) {
			Log.wtf(tag, text, e);
		}
	}
}

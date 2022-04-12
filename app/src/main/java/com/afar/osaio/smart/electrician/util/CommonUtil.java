package com.afar.osaio.smart.electrician.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.log.NooieLog;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * CommonUtil
 *
 * @author Administrator
 * @date 2019/2/19
 */
public class CommonUtil {

    public interface OnDelayTimeFinishListener {
        void onFinish();
    }

    public static void delayAction(int millisecond, @NonNull final OnDelayTimeFinishListener listener) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onFinish();
            }
        }, millisecond);
    }

    /**
     * get version code
     *
     * @param ctx
     * @return
     */
    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * get version name
     *
     * @param ctx
     * @return
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * format error message
     *
     * @param throwable
     * @return
     */
    public static String errorMsg(Throwable throwable) {
        return "";
    }

    /**
     * 验证邮箱
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        final String expression = "[A-Z0-9a-z\\._\\%\\+\\-]+@[A-Za-z0-9\\._\\%\\+\\-]+\\.[A-Za-z0-9_\\%\\+\\-]{2,64}";
        return Pattern.matches(expression, email);
    }


    /**
     * Get the Mime Type from a File
     *
     * @param fileName 文件名
     * @return 返回MIME类型
     * thx https://www.oschina.net/question/571282_223549
     * add by fengwenhua 2017年5月3日09:55:01
     */
    public static String getMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(fileName);
        return type;
    }

    /**
     * 获取app版本
     *
     * @return
     */
    public static String getApVersion() {
        PackageManager manager = NooieApplication.get().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(NooieApplication.get().getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取app版本号
     *
     * @return
     */
    public static int getApVersionCode() {
        PackageManager manager = NooieApplication.get().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(NooieApplication.get().getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }


    /**
     * 获取UUID
     *
     * @param context
     * @return UUID
     */
    public static String getUUID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, tmPhone, androidId;
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return new UUID(androidId.hashCode(), ((long) androidId.hashCode() << 32)).toString();
        }
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }

    /**
     * MD5加密
     *
     * @return 加密后的字符串
     */
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static final int MIN_DELAY_TIME = 1000;  // 两次点击间隔不能少于1000ms
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }


    public static void gotoBrower(Context context, String url) {
        if (context == null || TextUtils.isEmpty(url)) {
            return;
        }

        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);         //要跳转的网页
            intent.setData(content_url);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                NooieLog.e("-->> SystemUtil gotoBrower has browser");
                context.startActivity(intent);
            }
        } catch (Exception e) {
            NooieLog.e("-->> SystemUtil gotoBrower fail e=" + (e != null ? e.getMessage() : ""));
            e.printStackTrace();
        }
    }

}

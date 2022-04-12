package com.afar.osaio.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.protocol.bean.Week;
import com.afar.osaio.util.preference.GlobalPrefs;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.afar.osaio.util.preference.GlobalPrefs.KEY_START_FORMAT_TIME;
import static com.afar.osaio.util.preference.GlobalPrefs.KEY_START_UPGRADE_TIME;

public class Util {
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
     * 验证邮箱
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            //use do
            //String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            //String check = "^\\\\w+((-\\\\w+)|(\\\\.\\\\w+))*@\\\\w+(\\\\.\\\\w{2,3}){1,3}$";
//            final String check = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
            //final String check ="^\\w+((-\\w+)|(\\.\\w+))*@\\w+(\\.\\w{2,32}){1,32}$";
            final String check = "[A-Z0-9a-z\\._\\%\\+\\-]+@[A-Za-z0-9\\._\\%\\+\\-]+\\.[A-Za-z0-9_\\%\\+\\-]{2,64}";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 查询是否正在升级
     *
     * @param deviceId
     * @return
     */
    public static boolean isUpdating(String deviceId) {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        long last = prefs.getLong(String.format("%s-%s", deviceId, KEY_START_UPGRADE_TIME));
        return (System.currentTimeMillis() - last < 1000 * 60 * 4);
    }

    /**
     * 查询是否正在格式化SDCard
     *
     * @param deviceId
     * @return
     */
    public static boolean isFormattingCard(String deviceId) {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        long last = prefs.getLong(String.format("%s-%s", deviceId, KEY_START_FORMAT_TIME));
        return (System.currentTimeMillis() - last < 1000 * 60 * 1);
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
     * 根据文件后缀名判断 文件是否是视频文件
     *
     * @param fileName 文件名
     * @return 是否是视频文件
     */
    public static boolean isVideoFile(String fileName) {
        String mimeType = getMimeType(fileName);
        if (!TextUtils.isEmpty(fileName) && mimeType.contains("video/")) {
            return true;
        }
        return false;
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
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    public static int convertDeviceVersion(String verStr) {
        int verNum = 0;
        if (!TextUtils.isEmpty(verStr)) {
            try {
                String[] verArr = verStr.split("\\.");
                int len = verArr.length;
                if (len < 3) {
                    return 0;
                }
                verNum = Integer.parseInt(verArr[0]) * 1000 + Integer.parseInt(verArr[1]) * 100 + Integer.parseInt(verArr[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return verNum;
    }

    public interface OnDelayTaskFinishListener {
        void onFinish();
    }

    public static void delayTask(int millisecond, @NonNull final OnDelayTaskFinishListener listener) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onFinish();
            }
        }, millisecond);
    }

    public static String formatWeek(Week week) {
        switch (week) {
            case Mon:
                return NooieApplication.get().getString(R.string.w_monday);
            case Tues:
                return NooieApplication.get().getString(R.string.w_tuesday);
            case Wed:
                return NooieApplication.get().getString(R.string.w_wednesday);
            case Thur:
                return NooieApplication.get().getString(R.string.w_thursday);
            case Fri:
                return NooieApplication.get().getString(R.string.w_friday);
            case Sat:
                return NooieApplication.get().getString(R.string.w_saturday);
            case Sun:
                return NooieApplication.get().getString(R.string.w_sunday);
            default:
                return NooieApplication.get().getString(R.string.unknown);
        }
    }

}

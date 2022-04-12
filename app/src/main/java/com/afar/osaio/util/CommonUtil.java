package com.afar.osaio.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.text.TextUtils;

import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    public static List<String> usCountryCodeList = new ArrayList<>();

    public static String getPrivacyPolicy(Context context) {
        return String.format(ConstantValue.URL_PRIVACY_POLICY, LanguageUtil.getLanguageKeyByLocale(context));
    }

    public static String getTerms(Context context) {
        return String.format(ConstantValue.URL_TERMS, LanguageUtil.getLanguageKeyByLocale(context));
    }

    public static String getPrivacyPolicyByRegion(Context context, String region) {
        return String.format(ConstantValue.URL_PRIVACY_POLICY_TEMPLATE, region, LanguageUtil.getLanguageKeyByLocale(context));
    }

    public static String getPrivacyPolicyByCountry(Context context, String countryCode) {
        return getPrivacyPolicyByRegion(context, getPrivacyPolicyRegionByCountry(countryCode));
    }

    public static String[] getStoragePermGroup() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            return ConstantValue.PERM_GROUP_STORAGE_API_30;
        } else {
            return ConstantValue.PERM_GROUP_STORAGE;
        }
    }

    /**
     * 检查密码非法字符，除键盘上支持的所有字符外的都是非法
     * @param password
     * @return
     */
    public static boolean checkPasswordIllegalChar(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        int len = password.length();
        StringBuilder illegalStr = new StringBuilder();
        for(int i = 0; i < len; i++) {
            if (password.charAt(i) >= 0x20 && password.charAt(i) <= 0x7E){
            } else {
                illegalStr.append(password.charAt(i));
            }
        }
        return illegalStr.length() > 0;
    }

    public static String getPrivacyPolicyRegionByCountry(String countryCode) {
        if (checkIsAmericaRegion(countryCode)) {
            return "us";
        } else {
            return "de";
        }
    }

    public static String getCurrentNetworkInfo(Context context) {
        if (context == null){
            return "";
        }
        StringBuilder networkInfoSb = new StringBuilder();
        try {
            WifiInfo wifiInfo = NetworkUtil.getWifiInfo(context);
            if (wifiInfo != null) {
                networkInfoSb.append("wifi info:\n")
                        .append(wifiInfo.toString());
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return networkInfoSb.toString();
    }

    public static boolean isNetworkValid(Context context) {
        NooieLog.d("-->>> debug isNetworkValid 1001");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            boolean isNetworkValid = NetworkUtil.isNetworkAvailable(context);
            String ssid = NetworkUtil.getSSIDAuto(context);
            NooieLog.d("-->>> debug isNetworkValid 1001 isNetworkValid=" + isNetworkValid + " ssid=" + ssid);
            return isNetworkValid;
        }
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities networkCapabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            boolean isNetUsable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            boolean isNetworkValid = NetworkUtil.isNetworkAvailable(context);
            NooieLog.d("-->>> debug isNetworkValid 1001 isNetUsable=" + isNetUsable + " isNetworkValid=" + isNetworkValid);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return NetworkUtil.isNetworkAvailable(context);
    }

    private static void initPrivacyPolicyCountryCodeList() {
        if (usCountryCodeList == null) {
            usCountryCodeList = new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(usCountryCodeList)) {
            usCountryCodeList.add("44");
            usCountryCodeList.add("501");
            usCountryCodeList.add("1");
            usCountryCodeList.add("1");
            usCountryCodeList.add("506");
            usCountryCodeList.add("192");
            usCountryCodeList.add("1876");
            usCountryCodeList.add("52");
            usCountryCodeList.add("51");
            usCountryCodeList.add("56");
            usCountryCodeList.add("54");
            usCountryCodeList.add("598");
            usCountryCodeList.add("595");
            usCountryCodeList.add("55");
        }
    }

    private static boolean checkIsAmericaRegion(String countryCode) {
        initPrivacyPolicyCountryCodeList();
        return !TextUtils.isEmpty(countryCode) && usCountryCodeList != null && usCountryCodeList.contains(countryCode);
    }

}

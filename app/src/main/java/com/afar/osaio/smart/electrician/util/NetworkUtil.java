package com.afar.osaio.smart.electrician.util;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 * NetworkUtil
 *
 * @author Administrator
 * @date 2019/3/5
 */
public class NetworkUtil {

    /**
     * get wifi is connected
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NooieLog.e("----------- += 》》》 isWifiConnected() return wifiInfo.isConnected() "+wifiInfo.isConnected());
            return wifiInfo.isConnected();
        } else {
            NooieLog.e("----------- += 》》》 isWifiConnected() return false ");
            return false;
        }
    }

    /**
     * get wifi's ssid
     * 若部分机型（如Oppo）无法使用WifiInfo获取ssid，则使用NetworkInfo通过getExtraInfo（）来获取
     *
     * @param context
     * @return
     */
    public static String getSSID(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo wifiInfo = wm.getConnectionInfo();
            if (wifiInfo != null) {
                String s = wifiInfo.getSSID();
                NooieLog.e("----------- += 》》》 getSSID() s ==  "+s+"  wifiInfo.getSSID() "+wifiInfo.getSSID());
                if (TextUtils.isEmpty(s) || s.equals("<unknown ssid>")) {
                    ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    s = networkInfo.getExtraInfo();
                    NooieLog.e("----------- += 》》》 getSSID()  networkInfo.getExtraInfo() s == "+s);
                }else {
                    NooieLog.e("----------- += 》》》 getSSID()TextUtils.isEmpty(s) "+TextUtils.isEmpty(s));
                }
                if (!TextUtils.isEmpty(s) && s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    NooieLog.e("----------- += 》》》 getSSID() return s.substring(1, s.length() - 1) s "+s.substring(1, s.length() - 1));
                    return s.substring(1, s.length() - 1);
                }
            }else {
                NooieLog.e("----------- += 》》》 getSSID() wifiInfo == null ");
            }
        }else {
            NooieLog.e("----------- += 》》》 getSSID() wm == null ");
        }
        return "";
    }

    public static List<WifiConfiguration> getWifiList(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        List<WifiConfiguration> wifiConfigurations = new ArrayList<>();
        if (wifiManager != null) {
            if (CollectionUtil.isNotEmpty(wifiManager.getConfiguredNetworks())) {
                NooieLog.e("----------- += 》》》 getWifiList（） wifiManager.getConfiguredNetworks() size "+wifiManager.getConfiguredNetworks().size());
                NooieLog.e("----------- += 》》》 getWifiList（） wifiManager.getConfiguredNetworks() info "+ new Gson().toJson(wifiManager.getConfiguredNetworks()) );
                wifiConfigurations.addAll(wifiManager.getConfiguredNetworks());
            }else {
                NooieLog.e("----------- += 》》》 getWifiList() wifiManager.getConfiguredNetworks() isEmpty  size ");
            }
        }else {
            NooieLog.e("----------- += 》》》 getWifiList() wifiManager == null ");
        }
        return wifiConfigurations;
    }

    /**
     * 获取wifi的ssid，如果获取ssid为空，需要请求ACCESS_FINE_LOCATION，ACCESS_COARSE_LOCATION后重新获取
     * @param context
     * @return
     */
    public static String getSSIDAuto(Context context) {
        String ssid = "";
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !TextUtils.isEmpty(getSSIDFromWifiList(context))) {
            ssid = getSSIDFromWifiList(context);
        } else {
            ssid = getSSID(context);
        }
        return ssid;
    }


    public static String getSSIDFromWifiList(Context context) {
        String result = "";
        List<WifiConfiguration> wifiConfigurations = getWifiList(context);
        NooieLog.e("----------- += 》》》 getSSIDFromWifiList() wifiConfigurations size "+wifiConfigurations.size());
        WifiManager wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        int networkId = wifiManager != null && wifiManager.getConnectionInfo() != null ? wifiManager.getConnectionInfo().getNetworkId() : -1;

        /**
         * rihzi
         */

        if (wifiManager == null){
            NooieLog.e("----------- += 》》》 getSSIDFromWifiList() wifiManager null");
        }

        if ( wifiManager != null && wifiManager.getConnectionInfo() == null){
            NooieLog.e("----------- += 》》》 getSSIDFromWifiList() getConnectionInfo null");
        }

        if ( wifiManager != null  &&  wifiManager.getConnectionInfo() != null){
            NooieLog.e("----------- += 》》》 getSSIDFromWifiList() wifiManager.getConnectionInfo().getNetworkId() "+wifiManager.getConnectionInfo().getNetworkId());
        }

        NooieLog.e("----------- += 》》》 getSSIDFromWifiList() networkId "+networkId);

        //LogUtil.d("-->> NetworkUtil getSSIDFromWifiList networkId=" + networkId);
        for (WifiConfiguration wifiConfiguration : CollectionUtil.safeFor(wifiConfigurations)) {
            //LogUtil.d("-->> NetworkUtil getSSIDFromWifiList ssid" + wifiConfiguration.SSID + " networkId=" + wifiConfiguration.networkId);
            NooieLog.e("----------- += 》》》getSSIDFromWifiList() wifiConfiguration.SSID "+wifiConfiguration.SSID+" wifiConfiguration.networkId "+wifiConfiguration.networkId);
            if (networkId == wifiConfiguration.networkId && !TextUtils.isEmpty(wifiConfiguration.SSID)) {
                String ssid = wifiConfiguration.SSID;
                NooieLog.e("----------- += 》》》getSSIDFromWifiList() ssid "+ssid);
                if (!TextUtils.isEmpty(ssid) && ssid.length() > 2 && ssid.charAt(0) == '"' && ssid.charAt(ssid.length() - 1) == '"') {
                    result = ssid.substring(1, ssid.length() - 1);
                    NooieLog.e("----------- += 》》》getSSIDFromWifiList() result "+result);
                    break;
                }
            }
        }
        NooieLog.e("----------- += 》》》getSSIDFromWifiList() return result "+result);
        return result;
    }

    public static String getWifiSSID(Context context){
        WifiManager wifimanager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiinfo = wifimanager.getConnectionInfo();
        NooieLog.i(wifiinfo.toString());
        int netWordID = wifiinfo.getNetworkId();
        String ssid = wifiinfo.getSSID();
        if (!TextUtils.isEmpty(ssid) && !ssid.contains("<unknown ssid>")){
            ssid = ssid.replace("\"", "");
            return ssid;
        }
        List<WifiConfiguration> list = ((WifiManager) context.getSystemService(WIFI_SERVICE)). getConfiguredNetworks();
        if (list ==null || list.size()==0){
            return "";
        }

        String foundMatchSSIDname="";
        for (WifiConfiguration wifiConfiguration : list){
            int wcf_id = wifiConfiguration.networkId;
            if (wcf_id == netWordID){
                foundMatchSSIDname = wifiConfiguration.SSID;
                break;
            }
        }

        if (TextUtils.isEmpty(foundMatchSSIDname)){
            return "";
        }
        return foundMatchSSIDname;
    }

    /**
     * is open GPS
     *
     * @param context
     * @return
     */
    public static final boolean isGPSOpen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;

    }

    /**
     * open locatio service settings page
     *
     * @param context
     */
    public static void openLocationSettingPage(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

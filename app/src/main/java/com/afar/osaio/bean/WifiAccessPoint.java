package com.afar.osaio.bean;


import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiAccessPoint implements Comparable {
    private static final String TAG = "WifiAccessPoint";
    private static final int WIFI_LEVEL_NUM = 4;

    /**
     * These values are matched in string arrays -- changes must be kept in sync
     */
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;
    public String ssid;
    public String bssid;
    public int security;
    public int networkId;
    public boolean wpsAvailable = false;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    public PskType pskType = PskType.UNKNOWN;

    /* package */
    public ScanResult mScanResult;
    private WifiConfiguration mConfig;
    private int mRssi;
    private WifiInfo mInfo;
    private NetworkInfo.DetailedState mState;

    private boolean mNewConfig = false;

    public WifiAccessPoint() {
        initFilter();
    }

    public WifiAccessPoint(WifiConfiguration config) {
        initFilter();
        loadConfig(config);
        mNewConfig = false;
        /* refresh(); */
    }

    public WifiAccessPoint(ScanResult result) {
        initFilter();
        loadResult(result);
        mNewConfig = true;
        /* refresh(); */
    }

    private void initFilter() {
    }

    private void loadConfig(WifiConfiguration config) {
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        bssid = config.BSSID;
        security = getSecurity(config);
        networkId = config.networkId;
        mRssi = Integer.MAX_VALUE;
        mConfig = config;
    }

    private void loadResult(ScanResult result) {
        ssid = result.SSID;
        bssid = result.BSSID;
        security = getSecurity(result);
        wpsAvailable = security != SECURITY_EAP && result.capabilities.contains("WPS");
        if (security == SECURITY_PSK)
            pskType = getPskType(result);
        networkId = -1;
        mRssi = result.level;
        mScanResult = result;

        /* create mConfig */
        generateNewNetworkConfig();
    }

    public static String removeDoubleQuotes(String string) {
        if (string == null) {
            return null;
        }
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Log.w(TAG, "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

    public int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, WIFI_LEVEL_NUM);
    }

    public WifiConfiguration getConfig() {
        return mConfig;
    }

    public WifiInfo getInfo() {
        return mInfo;
    }

    public NetworkInfo.DetailedState getState() {
        return mState;
    }

    public void update(WifiInfo info, NetworkInfo.DetailedState state) {
        boolean reorder = false;
        if (info == null) {
            return;
        }

        if (networkId == info.getNetworkId()) {
            reorder = (mInfo == null);
            mRssi = info.getRssi();
            mInfo = info;
            mState = state;
        } else {
            reorder = true;
            mInfo = null;
            mState = null;
        }

        /*
         * if (reorder) { notifyHierarchyChanged(); }
         */
    }

    public boolean update(ScanResult result) {
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                int oldLevel = getLevel();
                mRssi = result.level;
            }
            // This flag only comes from scans, is not easily saved in config
            if (security == SECURITY_PSK) {
                pskType = getPskType(result);
            }
            return true;
        }
        return false;
    }

    public boolean checkSameConfigure(WifiAccessPoint ap) {
        WifiConfiguration apConf = ap.getConfig();
        return ssid.equals(apConf.SSID) && security == getSecurity(apConf);
    }

    protected void generateNewNetworkConfig() {
        if (mConfig != null)
            return;
        if (security == SECURITY_NONE) {
            generateOpenNetworkConfig();
        } else if (security == SECURITY_PSK) {
            generatePskNetworkConfig();
        } else if (security == SECURITY_WEP) {
            generateWepNetworkConfig();
        } else {
            //WeyeFeye Product should never reach here !
            Log.w(TAG, "should never reach here generateSafeNetworkConfig()");
            generateSafeNetworkConfig();
        }
    }

    public void updatePassword(String pass) {
        if (mConfig == null)
            return;
        if (security == SECURITY_NONE) {
            return;
        } else if (security == SECURITY_PSK) {
            mConfig.preSharedKey = convertToQuotedString(pass);
        } else if (security == SECURITY_WEP) {
            mConfig.wepKeys[0] = convertToQuotedString(pass);
        }
    }

    protected void generateSafeNetworkConfig() {
        if (mConfig != null)
            return;
        mConfig = new WifiConfiguration();
        mConfig.SSID = WifiAccessPoint.convertToQuotedString(ssid);

        mConfig.allowedAuthAlgorithms.clear();
        mConfig.allowedGroupCiphers.clear();
        mConfig.allowedKeyManagement.clear();
        mConfig.allowedPairwiseCiphers.clear();
        mConfig.allowedProtocols.clear();
        mConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        mConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        mConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        mConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

        mConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); /* no key */

        mConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        mConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        mConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        mConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        updateWeyefeyePass();
    }

    private void updateWeyefeyePass() {
        if (security == SECURITY_PSK) {
            mConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            mConfig.preSharedKey = getDefaultPasswork(ssid);
        } else {
            mConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            /*damn , No change !!*/
            /*mConfig.preSharedKey = "";
            mConfig.wepKeys[0] = "";
            mConfig.wepTxKeyIndex = 0;*/
        }
    }

    protected void generateWepNetworkConfig() {
        if (mConfig != null)
            return;
        mConfig = new WifiConfiguration();
        mConfig.SSID = WifiAccessPoint.convertToQuotedString(ssid);

        mConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        mConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        mConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        mConfig.wepKeys[0] = getDefaultPasswork(ssid);
    }

    protected void generatePskNetworkConfig() {
        if (mConfig != null)
            return;
        mConfig = new WifiConfiguration();
        mConfig.SSID = WifiAccessPoint.convertToQuotedString(ssid);

        mConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        mConfig.preSharedKey = getDefaultPasswork(ssid);
    }

    private String getDefaultPasswork(String ssid) {
        return convertToQuotedString(ssid);
    }

    protected void generateOpenNetworkConfig() {
        if (mConfig != null) {
            return;
        }
        mConfig = new WifiConfiguration();
        mConfig.SSID = WifiAccessPoint.convertToQuotedString(ssid);
        mConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); /* no key */
    }


    @Override
    public int compareTo(Object o) {
        if (o instanceof WifiAccessPoint) {
            WifiAccessPoint wifiAP = (WifiAccessPoint) o;

            if (mState == NetworkInfo.DetailedState.CONNECTED && wifiAP.mState != NetworkInfo.DetailedState.CONNECTED) {
                return -1;
            } else if (mState != NetworkInfo.DetailedState.CONNECTED && wifiAP.mState == NetworkInfo.DetailedState.CONNECTED) {
                return 1;
            } else {
                return compareByDetail(wifiAP);
            }
        } else {
            return 1;
        }

    }

    private int compareByDetail(WifiAccessPoint wifiAP) {
        // Active one goes first.
        if (mInfo != null && wifiAP.mInfo == null)
            return -1;
        if (mInfo == null && wifiAP.mInfo != null)
            return 1;

        // Reachable one goes before unreachable one.
        if (mRssi != Integer.MAX_VALUE && wifiAP.mRssi == Integer.MAX_VALUE)
            return -1;
        if (mRssi == Integer.MAX_VALUE && wifiAP.mRssi != Integer.MAX_VALUE)
            return 1;


        // // Configured one goes before unconfigured one.
        // if (networkId != WifiConfiguration.INVALID_NETWORK_ID
        // && other.networkId == WifiConfiguration.INVALID_NETWORK_ID) return
        // -1;
        // if (networkId == WifiConfiguration.INVALID_NETWORK_ID
        // && other.networkId != WifiConfiguration.INVALID_NETWORK_ID) return 1;

        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(wifiAP.mRssi, mRssi);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return ssid.compareToIgnoreCase(wifiAP.ssid);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WifiAccessPoint))
            return false;
        return (this.compareTo(other) == 0);
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (mInfo != null)
            result += 13 * mInfo.hashCode();
        result += 19 * mRssi;
        result += 23 * networkId;
        result += 29 * ssid.hashCode();
        return result;
    }
}

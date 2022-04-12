package com.afar.osaio.util.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.nooie.common.base.BasisData;
import com.nooie.common.base.SDKGlobalData;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.db.dao.ServerNodeService;
import com.nooie.sdk.db.dao.UserInfoService;
import com.nooie.sdk.db.entity.ServerNodeEntity;
import com.nooie.sdk.db.entity.UserInfoEntity;
import com.afar.osaio.util.ConstantValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by victor on 2018/6/27
 * Email is victor.qiao.0604@gmail.com
 */
public class GlobalPrefs {
    public static final int MOTION_DETECT_STATE_IGNORE = 0x10;
    public static final int MOTION_DETECT_STATE_CLOSE = 0x20;
    public static final int MOTION_DETECT_STATE_HIGH = 0x30;
    public static final int MOTION_DETECT_STATE_MEDIUM = 0x40;
    public static final int MOTION_DETECT_STATE_LOW = 0x50;

    public static final String GLOBAL_PREFS_FILE_NAME = "global.prefs.name";
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_UID = "uid";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_WEB_URL = "web_url";
    public static final String KEY_TUYA_PHOTO = "tuya_photo";
    public static final String KEY_P2P_URL = "p2p_url";
    public static final String KEY_S3_URL = "s3_url";
    public static final String KEY_REGION = "region";
    public static final String KEY_GAP_TIME = "gap_time";
    public static final String KEY_PHONE_ID = "phone_id";
    public static final String KEY_MOTION_DETECT_STATE = "motion_detect_state";
    public static final String KEY_SOUND_DETECT_STATE = "sound_detect_state";
    public static final String KEY_NOOIE_MOTION_DETECT_STATE = "nooie_motion_detect_state";
    public static final String KEY_NOOIE_SOUND_DETECT_STATE = "nooie_sound_detect_state";

    public static final String KEY_CLOSE_ALARM_DEVICE_ID = "close_alarm_device_id";
    public static final String KEY_WIFI_INFO = "wifi_info";
    public static final String KEY_ACCOUNT_HISTORY = "account_history";

    public static final String KEY_UPGRADE_TIP_VERSION = "upgrade_tip_version";
    public static final String KEY_UPGRADE_TIP_OFF = "upgrade_tip_off";

    public static final String KEY_START_UPGRADE_TIME = "start_upgrade_time";
    public static final String KEY_START_FORMAT_TIME = "start_format_time";

    public static final String KEY_DEVICE_PREVIEW = "device_preview";

    public static final String KEY_SYSTEM_MESSAGE_LAST_READ_TIME = "system_message_last_read_time";
    public static final String KEY_DEVICE_MESSAGE_LAST_READ_TIME = "device_message_last_read_time";

    public static final String KEY_DEVICE_GUIDE = "device_guide";
    public static final String KEY_DEVICE_AUDIO_STATE = "device_audio_state";
    public static final String KEY_DEVICE_PLAYBACK_AUDIO_STATE = "device_play_back_audio_state";

    public static final String KEY_APP_NOTIFICATION_REQUEST = "app_notification_request";
    public static final String KEY_APP_IS_STARTED = "app_is_start";
    public static final String KEY_IS_IGNORE_PRIVACY = "is_ignore_privacy";

    public static final String SPLITTER = "_Splitter_";

    public static final String ROUTER_BACK_UP = "ROUTER_BACK_UP";
    public static final String ROUTER_NAME = "ROUTER_NAME";
    public static final String ROUTER_ADMIN_PASSWORD = "ROUTER_ADMIN_PASSWORD";
    public static final String ROUTER_ADMIN_PASSWORD_OPEN = "ROUTER_ADMIN_PASSWORD_OPEN";
    public static final String ROUTER_MAC = "ROUTER_MAC";

    public static final String ROUTER_NET_MODE = "ROUTER_NET_MODE";
    public static final String ROUTER_STATIC_IP = "ROUTER_STATIC_IP";
    public static final String ROUTER_STATIC_MASK = "ROUTER_STATIC_MASK";
    public static final String ROUTER_STATIC_GW = "ROUTER_STATIC_GW";
    public static final String ROUTER_STATIC_PRIDNS = "ROUTER_STATIC_PRIDNS";
    public static final String ROUTER_STATIC_SECDNS = "ROUTER_STATIC_SECDNS";
    public static final String ROUTER_PPPOE_USER = "ROUTER_PPPOE_USER";
    public static final String ROUTER_PPPOE_PASS = "ROUTER_PPPOE_PASS";

    public static final String IS_DENY_PERMISSION_LOCATION = "IS_DENY_PERMISSION_LOCATION";


    private static GlobalPrefs mInstance;
    protected Context mContext;

    private String mAccount;
    private String mPassword;
    private String mUid;
    private String mToken;
    private String mRefreshToken;
    private long mExpireTime;
    private String mPushToken;
    private String mTuyaPhoto;

    private String mWebUrl;
    private String mP2pUrl;
    private String mS3Url;
    private String mRegion;
    private String mSsUrl;
    private int mGapTime;

    protected final ConcurrentHashMap<String, String> WIFI_INFO = new ConcurrentHashMap<String, String>();
    protected final ConcurrentHashMap<String, String> STRING_PREFS = new ConcurrentHashMap<String, String>();
    protected final ConcurrentHashMap<String, Integer> INTEGER_PREFS = new ConcurrentHashMap<String, Integer>();
    private final ConcurrentHashMap<String, Integer> AUDIO_INFO = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> PLAYBACK_AUDIO_INFO = new ConcurrentHashMap<>();

    public static boolean isStartFromRegister = false;

    public static GlobalPrefs getPreferences(Context context) {
        if (mInstance == null) {
            mInstance = new GlobalPrefs(context);
        }
        return mInstance;
    }

    private GlobalPrefs(Context context) {
        mContext = context;
        //initData();
    }

    private void initData() {
        mAccount = getString(GLOBAL_PREFS_FILE_NAME, KEY_ACCOUNT, new String());
        mPassword = getString(GLOBAL_PREFS_FILE_NAME, KEY_PASSWORD, new String());
        mUid = getString(GLOBAL_PREFS_FILE_NAME, KEY_UID, new String());
        mToken = getString(GLOBAL_PREFS_FILE_NAME, KEY_TOKEN, new String());
        loadBrain();
    }

    public void login(String account, String psd) {
        mAccount = account;
        mPassword = psd;
        mTuyaPhoto = getString(GLOBAL_PREFS_FILE_NAME, KEY_TUYA_PHOTO, new String());
        UserInfoCache.getInstance().init(account);
    }

    public void logout() {
        mAccount = new String();
        mPassword = new String();
        mTuyaPhoto = new String();
        cleanUidAndToken();
        clearBrain();
        setPushToken("");
    }

    private void initGlobalShareData() {
        mGapTime = getInt(NooieApplication.mCtx, GLOBAL_PREFS_FILE_NAME, KEY_GAP_TIME, 0);
    }

    public int getGapTime() {
        return mGapTime;
    }

    public void setGapTime(int gapTime) {
        mGapTime = gapTime;
        putInt(NooieApplication.mCtx, GLOBAL_PREFS_FILE_NAME, KEY_GAP_TIME, 0);
    }

    public String getPhoneId() {
        return getString(NooieApplication.mCtx, GLOBAL_PREFS_FILE_NAME, KEY_PHONE_ID, "");
    }

    public void setPhoneId(String phoneId) {
        putString(NooieApplication.mCtx, GLOBAL_PREFS_FILE_NAME, KEY_PHONE_ID, phoneId);
    }

    public String getUid() {
        return mUid;
    }

    public String getToken() {
        return mToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public long getExpireTime() {
        return mExpireTime;
    }

    public void saveUidAndToken(String uid, String token, String refreshToken, long expireTime) {
        mUid = uid;
        mToken = token;
        mRefreshToken = refreshToken;
        mExpireTime = expireTime;
    }

    public void saveToken(String token, String refreshToken, long expireTime) {
        mToken = token;
        mRefreshToken = refreshToken;
        mExpireTime = expireTime;
        UserInfoService.getInstance().updateLoginUserInfo(mAccount, "", "", token, refreshToken, expireTime);
    }

    public void cleanUidAndToken() {
        mUid = new String();
        mToken = new String();
        mRefreshToken = new String();
        mExpireTime = 0;
    }

    public void setPushToken(String pushToken) {
        this.mPushToken = pushToken;
    }

    public String getPushToken() {
        return this.mPushToken;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public String getP2pUrl() {
        return mP2pUrl;
    }

    public String getS3Url() {
        return mS3Url;
    }

    public String getSsUrl() {
        return mSsUrl;
    }

    public String getRegion() {
        return mRegion;
    }

    private String phoneUuid = new String();

    public void setPhoneUuid(String uuid) {
        phoneUuid = uuid;
    }

    public String getPhoneUuid() {
        return phoneUuid;
    }

    public Observable<Boolean> initDataObservable() {
        return Observable.just(mAccount)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String account) {
                        initGlobalShareData();
                        boolean result = false;
                        UserInfoEntity userInfoEntity = UserInfoService.getInstance().getLoginUserInfo();
                        if (UserInfoService.getInstance().checkUserInfoAvailable(userInfoEntity)) {
                            login(userInfoEntity.getAccount(), userInfoEntity.getPsd());
                            saveUidAndToken(userInfoEntity.getUid(), userInfoEntity.getToken(), userInfoEntity.getRefreshToken(), userInfoEntity.getExpireTime());
                            ServerNodeEntity serverNodeEntity = ServerNodeService.getInstance().getServerNodeByAccount(userInfoEntity.getAccount());
                            if (ServerNodeService.getInstance().checkServerNodeAvailable(serverNodeEntity)) {
                                saveBrain(serverNodeEntity.getWeb(), serverNodeEntity.getP2p(), serverNodeEntity.getS3(), serverNodeEntity.getRegion(), serverNodeEntity.getSs());
                                result = true;
                            }
                        }
                        return Observable.just(result);
                    }
                });
    }

    public Observable<Boolean> releaseDataObservable() {
        return Observable.just(mAccount)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String account) {
                        UserInfoService.getInstance().restoreAllUser();
                        ServerNodeService.getInstance().deleteServerNodeByAccount(account);
                        return Observable.just(true);
                    }
                });
    }

    public void loadBrain() {
        mWebUrl = getString(GLOBAL_PREFS_FILE_NAME, KEY_WEB_URL, new String());
        mP2pUrl = getString(GLOBAL_PREFS_FILE_NAME, KEY_P2P_URL, new String());
        mS3Url = getString(GLOBAL_PREFS_FILE_NAME, KEY_S3_URL, new String());
        mRegion = getString(GLOBAL_PREFS_FILE_NAME, KEY_REGION, new String());
    }

    public void saveBrain(String webUrl, String p2pUrl, String s3Url, String region, String ssUrl) {
        mWebUrl = webUrl;
        mP2pUrl = p2pUrl;
        mS3Url = s3Url;
        mRegion = region;
        mSsUrl = ssUrl;
    }

    public void clearBrain() {
        mWebUrl = new String();
        mP2pUrl = new String();
        mS3Url = new String();
        mRegion = new String();
        mSsUrl = new String();
    }

    public void saveAccountHistory(String account) {
        if (TextUtils.isEmpty(account)) return;

        List<String> list = getAccountHistory();
        if (list != null && !list.contains(account)) {
            list.add(account);
            saveAccountHistory(list);
        }
    }

    public void saveAccountHistory(List<String> accounts) {
        if (accounts == null) return;

        StringBuilder builder = new StringBuilder();
        for (String acc : accounts) {
            builder.append(acc).append(SPLITTER);
        }
        putString(KEY_ACCOUNT_HISTORY, new String(builder));
    }

    public List<String> getAccountHistory() {
        String[] info = getString(KEY_ACCOUNT_HISTORY, new String()).split(SPLITTER);
        List<String> list = new ArrayList<>(Arrays.asList(info));

        while (list.contains(new String())) {
            list.remove(new String());
        }

        return list;
    }

    public void cleanAllAccountHistory() {
        saveAccountHistory(new ArrayList<String>());
    }

    public void cleanAccountHistory(String account) {
        if (TextUtils.isEmpty(account)) return;

        String info = getString(KEY_ACCOUNT_HISTORY, new String());
        if (info.contains(account + SPLITTER)) {
            info.replace(account + SPLITTER, "");
            putString(KEY_ACCOUNT_HISTORY, info);
        } else if (info.contains(SPLITTER + account)) {
            info.replace(SPLITTER + account, "");
            putString(KEY_ACCOUNT_HISTORY, info);
        }

        /*
        List<String> list = getAccountHistory();
        for (String tmp : list) {
            if (!TextUtils.isEmpty(tmp) && tmp.equals(account)) {
                list.remove(tmp);

                //remove one account
                saveAccountHistory(list);
                break;
            }
        }
        */
    }

    public String getTuyaPhoto() {
        return mTuyaPhoto;
    }

    public void setTuyaPhoto(String tuyaPhoto) {
        mTuyaPhoto = tuyaPhoto;
        putString(GLOBAL_PREFS_FILE_NAME, KEY_TUYA_PHOTO, tuyaPhoto);
    }

    public void saveWifiInfo(String ssid, String psd) {
        loadWifiInfo();

        WIFI_INFO.put(ssid, psd);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : WIFI_INFO.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey())) continue;
            builder.append(entry.getKey() + "_Equal_" + entry.getValue()).append("_Splitter_");
        }
        putString(mAccount, KEY_WIFI_INFO, new String(builder));
    }

    public String getWifiPsd(String ssid) {
        if (WIFI_INFO.containsKey(ssid)) {
            return WIFI_INFO.get(ssid);
        }
        return new String();
    }

    public void loadWifiInfo() {
        WIFI_INFO.clear();
        String wifiInfo = getString(KEY_WIFI_INFO, new String()).trim();
        if (!TextUtils.isEmpty(wifiInfo)) {
            String infos[] = wifiInfo.split("_Splitter_");
            for (int i = 0; i < infos.length; i++) {
                if (TextUtils.isEmpty(infos[i]) || !infos[i].contains("_Equal_") || infos[i].split("_Equal_").length < 2)
                    continue;

                String key = infos[i].split("_Equal_")[0];
                String value = infos[i].split("_Equal_")[1];
                WIFI_INFO.put(key, value);
            }
        }
    }

    public void clearWifiInfo() {
        WIFI_INFO.clear();
        putString(mAccount, KEY_WIFI_INFO, new String());
    }

    public void loadAudioInfo() {
        AUDIO_INFO.clear();
        String audioInfo = getString(KEY_DEVICE_AUDIO_STATE, new String()).trim();
        if (!TextUtils.isEmpty(audioInfo)) {
            String infos[] = audioInfo.split("_Splitter_");
            for (int i = 0; i < infos.length; i++) {
                if (TextUtils.isEmpty(infos[i]) || !infos[i].contains("_Equal_") || infos[i].split("_Equal_").length < 2) {
                    continue;
                }

                String key = infos[i].split("_Equal_")[0];
                String value = infos[i].split("_Equal_")[1];
                AUDIO_INFO.put(key, Integer.parseInt(value));
            }
        }
    }

    public void saveAudioInfo(String deviceId, int state) {
        loadAudioInfo();

        AUDIO_INFO.put(deviceId, state);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : AUDIO_INFO.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey())) {
                continue;
            }
            builder.append(entry.getKey() + "_Equal_" + entry.getValue()).append("_Splitter_");
        }
        putString(mAccount, KEY_DEVICE_AUDIO_STATE, new String(builder));
    }

    public int getAudioInfo(String deviceId) {
        if (AUDIO_INFO.containsKey(deviceId)) {
            return AUDIO_INFO.get(deviceId);
        }
        return ConstantValue.AUDIO_STATE_OFF;
    }

    public void loadPlaybackAudioInfo() {
        PLAYBACK_AUDIO_INFO.clear();
        String audioInfo = getString(KEY_DEVICE_PLAYBACK_AUDIO_STATE, new String()).trim();
        if (!TextUtils.isEmpty(audioInfo)) {
            String infos[] = audioInfo.split("_Splitter_");
            for (int i = 0; i < infos.length; i++) {
                if (TextUtils.isEmpty(infos[i]) || !infos[i].contains("_Equal_") || infos[i].split("_Equal_").length < 2) {
                    continue;
                }

                String key = infos[i].split("_Equal_")[0];
                String value = infos[i].split("_Equal_")[1];
                PLAYBACK_AUDIO_INFO.put(key, Integer.parseInt(value));
            }
        }
    }

    public void savePlaybackAudioInfo(String deviceId, int state) {
        loadAudioInfo();

        PLAYBACK_AUDIO_INFO.put(deviceId, state);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : PLAYBACK_AUDIO_INFO.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey())) {
                continue;
            }
            builder.append(entry.getKey() + "_Equal_" + entry.getValue()).append("_Splitter_");
        }
        putString(mAccount, KEY_DEVICE_PLAYBACK_AUDIO_STATE, new String(builder));
    }

    public int getPlaybackAudioInfo(String deviceId) {
        if (PLAYBACK_AUDIO_INFO.containsKey(deviceId)) {
            return PLAYBACK_AUDIO_INFO.get(deviceId);
        }
        return ConstantValue.AUDIO_STATE_OFF;
    }

    public void setAppNotificationRequest(int status) {
        putInt(mContext, mAccount, KEY_APP_NOTIFICATION_REQUEST, status);
    }

    public int getAppNotificationRequest() {
        return getInt(mContext, mAccount, KEY_APP_NOTIFICATION_REQUEST, ConstantValue.APP_NOTIFICATION_REQUEST_NO);
    }

    public String account() {
        return mAccount;
    }

    public String password() {
        return mPassword;
    }

    public void setPassword(String psd) {
        mPassword = psd;
    }

    public void setRouterBackup(String backup, String routerName) {
        putString(ROUTER_BACK_UP, backup);
        putString(ROUTER_NAME, routerName);
    }

    public String getRouterIsBackup() {
        String isBackup = getString(ROUTER_BACK_UP, "");
        return isBackup;
    }

    public String getRouterNameBackup() {
        String routerName = getString(ROUTER_NAME, "");
        if ("".equals(routerName)) {
            routerName = "Device Name";
        }
        return routerName;
    }

    /**
     * 保存设备 管理内容
     *
     * @param routerName
     * @param adminPassword
     */
    public void setRouterAdminInfo(String routerName, String adminPassword) {
        putString(routerName, adminPassword);
    }


    /**
     * 获取
     *
     * @param routerName
     * @return
     */
    public String getRouterAdminInfo(String routerName) {
        String routerPassword = getString(routerName, "admin");
        return routerPassword;
    }

    public void setRouterInternetMode(String mode, String staticIp, String staticMask, String staticGw,
                                      String priDns, String secDns, String pppoeUser, String pppoePass) {
        putString(ROUTER_NET_MODE, mode);
        if ("0".equals(mode)) {
            putString(ROUTER_STATIC_IP, staticIp);
            putString(ROUTER_STATIC_MASK, staticMask);
            putString(ROUTER_STATIC_GW, staticGw);
            putString(ROUTER_STATIC_PRIDNS, priDns);
            putString(ROUTER_STATIC_SECDNS, secDns);
        } else if ("3".equals(mode)) {
            putString(ROUTER_PPPOE_USER, pppoeUser);
            putString(ROUTER_PPPOE_PASS, pppoePass);
        }
    }

    public List<String> getRouterInternetMode() {
        List<String> routerInternetInfos = new ArrayList<>();
        String mode = getString(ROUTER_NET_MODE, "");
        routerInternetInfos.add(mode);
        if ("0".equals(mode)) {
            routerInternetInfos.add(getString(ROUTER_STATIC_IP, ""));
            routerInternetInfos.add(getString(ROUTER_STATIC_MASK, ""));
            routerInternetInfos.add(getString(ROUTER_STATIC_GW, ""));
            routerInternetInfos.add(getString(ROUTER_STATIC_PRIDNS, ""));
            routerInternetInfos.add(getString(ROUTER_STATIC_SECDNS, ""));
        } else if ("3".equals(mode)) {
            routerInternetInfos.add(getString(ROUTER_PPPOE_USER, ""));
            routerInternetInfos.add(getString(ROUTER_PPPOE_PASS, ""));
        }
        return routerInternetInfos;
    }

    public void setRouterMac(String mac) {
        putString(ROUTER_MAC, mac);
    }

    public String getRouterMac() {
        return getString(ROUTER_MAC, "");
    }

    public void setRouterAdminPassword(String password, boolean isOpen) {
        putString(ROUTER_ADMIN_PASSWORD, password);
        putBoolean(mContext, mAccount, ROUTER_ADMIN_PASSWORD_OPEN, isOpen);
    }

    public String getRouterAdminPassword() {
        String password = getString(ROUTER_ADMIN_PASSWORD, "admin");
        if ("".equals(password)) {
            password = "admin";
        }
        return password;
    }

    public boolean getIsOpenRouterAdminPasswordSwitch() {
        return getBoolean(mContext, mAccount, ROUTER_ADMIN_PASSWORD_OPEN, false);
    }

    public Integer getInt(String key, int defVal) {
        if (INTEGER_PREFS.get(key) == null) {
            INTEGER_PREFS.put(key, getInt(mContext, mAccount, key, defVal));
        }
        return INTEGER_PREFS.get(key);
    }

    public void putInt(String key, int value) {
        INTEGER_PREFS.put(key, value);
        putInt(mContext, mAccount, key, value);
    }


    public String getString(String key, String defVal) {
        if (STRING_PREFS.get(key) == null) {
            STRING_PREFS.put(key, getString(mContext, mAccount, key, defVal));
        }
        return STRING_PREFS.get(key);
    }

    public void putString(String key, String value) {
        STRING_PREFS.put(key, value);
        putString(mContext, mAccount, key, value);
    }


    public static String getString(Context context, String prefsFileName, String key, String defVal) {
        return getSharedPreference(context, prefsFileName).getString(key, defVal);
    }

    public static SharedPreferences getSharedPreference(Context context, String prefFileName) {
        return context.getSharedPreferences(prefFileName, Context.MODE_MULTI_PROCESS);
    }

    public static void putString(Context context, String prefsFileName, String key, String value) {
        getEditor(context, prefsFileName).putString(key, value).commit();
    }

    public static SharedPreferences.Editor getEditor(Context context, String prefsFileName) {
        return getSharedPreference(context, prefsFileName).edit();
    }

    public static int getInt(Context context, String prefsFileName, String key, int defVal) {
        return getSharedPreference(context, prefsFileName).getInt(key, defVal);
    }

    public static void putInt(Context context, String prefsFileName, String key, int value) {
        getEditor(context, prefsFileName).putInt(key, value).commit();
    }

    public static long getLong(Context context, String prefsFileName, String key, long defVal) {
        return getSharedPreference(context, prefsFileName).getLong(key, defVal);
    }

    public static void putLong(Context context, String prefsFileName, String key, long value) {
        getEditor(context, prefsFileName).putLong(key, value).commit();
    }

    public static boolean getBoolean(Context context, String prefsFileName, String key, boolean defVal) {
        return getSharedPreference(context, prefsFileName).getBoolean(key, defVal);
    }

    public static void putBoolean(Context context, String prefsFileName, String key, boolean value) {
        getEditor(context, prefsFileName).putBoolean(key, value).commit();
    }

    // base api for getLong()
    private Long getLong(String prefsFileName, String key, long defVal, boolean isGlobal) {
        return getSharedPreference(mContext, prefsFileName).getLong(key, defVal);
    }

    public Long getLong(String key, long defVal) {
        return getLong(mAccount, key, defVal, false);
    }

    public Long getLong(String key) {
        return getLong(mAccount, key, 0l, false);
    }

    // base api for putLong()
    private void putLong(String prefsFileName, String key, long value, boolean isGloabl) {
        getEditor(mContext, prefsFileName).putLong(key, value).commit();
    }

    public void putLong(String key, long value) {
        putLong(mAccount, key, value, false);
    }

    public String getCloseAlarmDeviceId() {
        return getString(mContext.getPackageName(), KEY_CLOSE_ALARM_DEVICE_ID, "");
    }

    public void putCloseAlarmDeviceId(String deviceId) {
        putGlobalString(KEY_CLOSE_ALARM_DEVICE_ID, deviceId);
    }

    private String getString(String prefsFileName, String key, String defVal) {
        if (TextUtils.isEmpty(STRING_PREFS.get(key))) {
            STRING_PREFS.put(key, getString(mContext, prefsFileName, key, defVal));
        }
        String strVal = STRING_PREFS.get(key);
        return strVal;
    }

    public void putGlobalString(String key, String value) {
        putString(mContext.getPackageName(), key, value);
    }

    private void putString(String prefsFileName, String key, String value) {
        STRING_PREFS.put(key, value);
        putString(mContext, prefsFileName, key, value);
    }

    public static void setAppIsStarted(boolean isStart) {
        try {
            BasisData.getInstance().putBool(KEY_APP_IS_STARTED, isStart);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public static boolean getAppIsStarted() {
        try {
            return BasisData.getInstance().getBool(KEY_APP_IS_STARTED, false);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return true;
    }

    public static void setIsDenyLocationPermission(boolean isDeny) {
        try {
            BasisData.getInstance().putBool(IS_DENY_PERMISSION_LOCATION, isDeny);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public static boolean getIsDenyLocationPermission() {
        try {
            return BasisData.getInstance().getBool(IS_DENY_PERMISSION_LOCATION, false);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return true;
    }

    public static void setIsIgnorePrivacy(boolean isIgnorePrivacy) {
        try {
            BasisData.getInstance().putBool(KEY_IS_IGNORE_PRIVACY, isIgnorePrivacy);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public static boolean getIgnorePrivacy() {
        try {
            return BasisData.getInstance().getBool(KEY_IS_IGNORE_PRIVACY, false);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return false;
    }

    public static void setPrivacyIsReadByAccount(String account, boolean isRead) {
        if (TextUtils.isEmpty(account)) {
            return;
        }
        try {
            BasisData.getInstance().putBool(account, isRead);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public static boolean getPrivacyIsReadByAccount(String account) {
        if (TextUtils.isEmpty(account)) {
            return false;
        }
        try {
            return BasisData.getInstance().getBool(account, false);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return false;
    }

    public void setmAccount(String mAccount) {
        this.mAccount = mAccount;
    }

    public void setmPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public void setmUid(String mUid) {
        this.mUid = mUid;
    }

    public void setmToken(String mToken) {
        this.mToken = mToken;
    }

    public void setmRefreshToken(String mRefreshToken) {
        this.mRefreshToken = mRefreshToken;
    }

    public void setmExpireTime(long mExpireTime) {
        this.mExpireTime = mExpireTime;
    }

    public void setmPushToken(String mPushToken) {
        this.mPushToken = mPushToken;
    }

    public void setmTuyaPhoto(String mTuyaPhoto) {
        this.mTuyaPhoto = mTuyaPhoto;
    }

    public void setmWebUrl(String mWebUrl) {
        this.mWebUrl = mWebUrl;
    }

    public void setmP2pUrl(String mP2pUrl) {
        this.mP2pUrl = mP2pUrl;
    }

    public void setmS3Url(String mS3Url) {
        this.mS3Url = mS3Url;
    }

    public void setmRegion(String mRegion) {
        this.mRegion = mRegion;
    }

    public void setmSsUrl(String mSsUrl) {
        this.mSsUrl = mSsUrl;
    }

    public void setmGapTime(int mGapTime) {
        this.mGapTime = mGapTime;
    }
}


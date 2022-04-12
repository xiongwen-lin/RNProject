package com.afar.osaio.base;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.afar.osaio.smart.bridge.BridgeManager;
import com.android.nordicbluetooth.SmartBleManager;
import com.apemans.datastore.db.base.SmartDatabase;
import com.apemans.platformbridge.helper.PlatformInitHelper;
import com.bumptech.glide.Glide;
import com.cantalou.dexoptfix.DexOptFix;
import com.afar.osaio.BuildConfig;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseAnalyticsManager;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.base.BasisData;
import com.nooie.common.base.GlobalData;
import com.nooie.common.base.listener.GlobalDataListener;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.eventtracking.EventTrackingOptions;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.LogState;
import com.nooie.sdk.receiver.NetworkWatcher;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.push.umeng.UmengPushManager;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.afar.osaio.util.Util;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.base.SDKConfigOptions;
import com.nooie.sdk.base.SDKDataAPI;
import com.nooie.sdk.device.DeviceCmdService;
import com.umeng.message.IUmengRegisterCallback;

import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.provider.ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;

public class NooieApplication extends BaseApplication {
    public static final String PUSH_CHANNEL_ID = "PUSH_NOTIFY_ID";
    public static final String PUSH_CHANNEL_NAME = "PUSH_NOTIFY_NAME";
    public static boolean DEBUG_MODE = false;
    public static boolean LOG_MODE = true/*false*/;
    public static boolean TEST_MODE = false;
    private static final String NEW_LINE = "\n";
    private static final String DB_NAME = "osaio";
    private static final String APP_MAIN_DIR = "osaio";
    private static final String HTTP_HEADER_USER_AGENT_PREFIX = "OSAIO_ANDROID_";
    private static final String EVENT_TRACKING_PLATFORM = "200";
    public static int mFlag = -1;
    private static NooieApplication INSTANCE;

    private String username;
    private boolean mIsUseJPush = false;
    private boolean mJPushInited = false;

    @Override
    public void onCreate() {
        Log.d("debug", "-->> App Start tracking 1001");
        super.onCreate();
        //BlockDetectByPrinter.start();
        INSTANCE = this;
        DEBUG_MODE = BuildConfig.LOG_DEBUG;
        UmengPushManager.getInstance().initUpush(mCtx, DEBUG_MODE, null);
        Log.d("debug", "-->> App Start tracking 1002");
        String currentProcessName = getCurrentProcessName();
        Log.v("Application", "NooieApplication processName：" + currentProcessName + " APPLICATION_ID=" + BuildConfig.APPLICATION_ID);
        if (currentProcessName != null && !currentProcessName.equalsIgnoreCase(BuildConfig.APPLICATION_ID)) {
            return;
        }
        Log.d("debug", "-->> App Start tracking 1003");
        initNooieSDK();
        Log.d("debug", "-->> App Start tracking 1004");
        initBluetooth();
        Log.d("debug", "-->> App Start tracking 1005");
        NooieLog.d("-->> debug NooieApplication onCreate: flag=" + mFlag);
        mFlag = 0;
        registerGlobalDataListener();
        Log.d("debug", "-->> App Start tracking 1006");
        initPushChannel();
        Log.d("debug", "-->> App Start tracking 1007");
        DisplayUtil.initDisplayOpinion(NooieApplication.mCtx);
        Log.d("debug", "-->> App Start tracking 1008");
        initNetworkWatcher();
        Log.d("debug", "-->> App Start tracking 1009");

        // delay 1000ms to log device info
        Util.delayTask(1000, new Util.OnDelayTaskFinishListener() {
            @Override
            public void onFinish() {
                Log.d("debug", "-->> App Start tracking 3001");
                logDeviceInfo();
                Log.d("debug", "-->> App Start tracking 3002");
                logNetworkInfo();
                Log.d("debug", "-->> App Start tracking 3003");
            }
        });
        Log.d("debug", "-->> App Start tracking 1010");
        initYRPlatform();
        SmartDatabase.Companion.init(this, "osaio");
        BridgeManager.getInstance().userInit();
    }

    @Override
    protected void attachBaseContext(Context base) {
        Log.d("debug", "-->> App Start tracking 2001");
        super.attachBaseContext(base);
        Log.d("debug", "-->> App Start tracking 2002");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                DexOptFix.fix(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("debug", "-->> App Start tracking 2003");
        MultiDex.install(this);
        Log.d("debug", "-->> App Start tracking 2004");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        NooieLog.d("-->> debug NooieApplication onTerminate: ");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        NooieLog.d("-->> debug NooieApplication onLowMemory: ");
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        NooieLog.d("-->> NooieApplication onTrimMemory level=" + level);
        if (level == TRIM_MEMORY_COMPLETE) {
            NooieLog.d("-->> debug NooieApplication onTrimMemory: TRIM_MEMORY_COMPLETE");
            /*
            NooieNativeSDK.getInstance().uninit();
            if (DEBUG_MODE && LOG_MODE) {
                WatchDog.stopWatchDog(getApplicationContext());
            }
            SDKDataAPI.sharedInstance().unInit(this);
             */
        }
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            NooieLog.d("-->> debug NooieApplication onTrimMemory: TRIM_MEMORY_UI_HIDDEN");
            Glide.get(this).clearMemory();
            return;
        }
        Glide.get(this).trimMemory(level);
    }

    /**
     * 禁止app字体大小跟随系统字体大小调节
     * @return
     */
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            if (configuration != null) {
                configuration.fontScale = 1.0f;
                resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            }
        }
        return resources;
    }

    public static NooieApplication get() {
        return NooieApplication.INSTANCE;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setIsUseJPush(boolean isUseJPush) {
        mIsUseJPush = isUseJPush;
    }

    public boolean getIsUseJPush() {
        return mIsUseJPush;
        //返回false表示默认使用FCM推送
        //return false;
    }

    private void initNooieSDK() {
        LogState logState = DEBUG_MODE ?  LogState.LOG_CONSOLE : LogState.LOG_FILE;
        //若使用FileUtil的日志目录生成接口，需要先配置app根目，且和下面配置的根目录必须一致
        FileUtil.setMainDir(APP_MAIN_DIR);
        int time = (int)(DateTimeUtil.getTodayStartTimeStamp() / 1000L);
        EventTrackingOptions eventTrackingOptions = new EventTrackingOptions()
                .setUploadDataTime(time)
                .setUploadDataLimitCount(50)//设置每次上传埋点数据条数
                .setNetworkTypePolicy(NetworkUtil.NetworkType.TYPE_ALL)//设置上传网络策略
                .setAutoTrack(true)//设置是否开启自动埋点
                .setPlatform("300")//设置app对应的平台类型
                .enableHttpLog(false)//设置是否输出Http请求日志
                .setEventTrackingDisable(true)//设置是否屏蔽埋点和上传
                .setUserAgent(getUserAgent());//设置接口请求header的use-agent
        SDKConfigOptions sdkConfigOptions = new SDKConfigOptions();
        sdkConfigOptions
                .setNetConfigurePlatform(NetConfigure.PLATFORM_OSAIO)
                .setIsEncryptPrivateData(false)//设置是否加密隐私数据，默认不加密
                .enableHttpLog(DEBUG_MODE && LOG_MODE)//设置是否输出Http请求日志
                .enableLog(DEBUG_MODE)
                .setLogState(logState)//设置日志输出类型
                .setLogPath(FileUtil.createLogFilePath(this))//设置日志文件输出路径
                .setDbName(DB_NAME)//设置数据库名，空使用默认
                .setMainDir(APP_MAIN_DIR)//设置存储根目录名，空使用默认
                .setUserAgent(getUserAgent())//设置接口请求header的use-agent
                .setEventTrackingOptions(eventTrackingOptions);//设置埋点配置
        SDKDataAPI.sharedInstance().startWithConfigOptions(mCtx, sdkConfigOptions);

        FirebaseAnalyticsManager.getInstance().init(mCtx);
    }

    /**
     * set Notification channel. otherwise, unable to show notification
     */
    private void initPushChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PUSH_CHANNEL_ID, PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void initNetworkWatcher() {
        NetworkWatcher.registerNetworkWatcher(new NetworkWatcher.OnNetworkChangedListener() {
            @Override
            public void onChanged() {
                NooieLog.e("---===>>>>network changed");
                DeviceCmdService.getInstance(NooieApplication.mCtx).switchNetwork();
            }

            @Override
            public  void onConnectivityChanged() {
                NooieDeviceHelper.sendBroadcast(mCtx, SDKConstant.ACTION_NETWORK_MANAGER_ON_CHANGED, null);
            }
        });
        DeviceConnectionHelper.getInstance().init(this);
    }

    private void unInitNetworkWatcher() {
    }

    public boolean initJPush(boolean debug, IUmengRegisterCallback callback) {
        if (!mIsUseJPush || mJPushInited) {
            return false;
        }
        // 初始化 JPush
        // 设置开启日志,发布时请关闭日志
        UmengPushManager.getInstance().initUpush(mCtx, debug, callback);
        mJPushInited = true;
        return true;
    }

    public static String getUserAgent() {
        return createUserAgent(HTTP_HEADER_USER_AGENT_PREFIX);
    }

    private void initBluetooth() {
        SmartBleManager.INSTANCE.initCore(this)
                .configServiceUUID("0000181c-0000-1000-8000-00805f9b34fb")
                .configWriteUUID("0000181d-0000-1000-8000-00805f9b34fb")
                //.configReadUUID("00002222-0000-1000-8000-00805f9b34fb")
                .configNotifyUUID("0000181e-0000-1000-8000-00805f9b34fb")
                .configReceiveMessageCharacteristicUUID("0000181e-0000-1000-8000-00805f9b34fb")
                .configAutoSplitLongData(true);
    }

    private void logDeviceInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("App Version: " + Util.getApVersion());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("App Version Code: " + Util.getApVersionCode());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Phone Model: " + Util.getDeviceModel());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Phone Brand: " + Util.getDeviceBrand());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Phone Version: " + Util.getSystemVersion());
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("Phone Screen: " + "[" + DisplayUtil.SCREEN_WIDTH_PX + " , " + DisplayUtil.SCREEN_HIGHT_PX + "]");
        NooieLog.e(stringBuilder.toString());
    }

    private void logNetworkInfo(){
        ConnectivityManager connectionManager = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            switch (networkInfo.getType()) {
                case TYPE_MOBILE:
                    NooieLog.e("use data network");
                    break;
                case TYPE_WIFI:
                    NooieLog.e("use wifi network");
                    break;
                default:
                    NooieLog.e("use unknown network " + networkInfo.getType());
                    break;
            }
        }
    }

    private void registerGlobalDataListener() {
        GlobalData.getInstance().addGlobalDataListener(new GlobalDataListener() {

            @Override
            public void onDataChange(int code, Bundle data) {
                NooieLog.d("-->> NooieApplication onDataChange code=" + code);
                switch (code) {
                    case GlobalDataListener.CODE_UPDATE_GLOBAL_DATA : {
                        if (data == null) {
                            break;
                        }
                        if (data.containsKey(BasisData.KEY_PASSWORD)) {
                            String password = data.getString(BasisData.KEY_PASSWORD);
                        }
                        break;
                    }
                    case GlobalDataListener.CODE_UPDATE_ACCOUNT : {
                        if (data == null) {
                            break;
                        }
                        String account = data.getString(BasisData.KEY_ACCOUNT);
                        String password = data.getString(BasisData.KEY_PASSWORD);
                        GlobalPrefs.getPreferences(NooieApplication.mCtx).login(account, password);
                        if (!TextUtils.isEmpty(account)) {
                            EventTrackingApi.getInstance().setAccount(account);
                        }
//                        PlatformInitHelper.INSTANCE.updateYRCXSDKData(account, account, password, CountryUtil.getCurrentCountry(NooieApplication.mCtx));
                        break;
                    }
                    case GlobalDataListener.CODE_UPDATE_BRAIN_URL : {
                        if (data == null) {
                            break;
                        }
                        String webUrl = data.getString(BasisData.KEY_WEB_URL);
                        String p2pUrl = data.getString(BasisData.KEY_P2P_URL);
                        String s3Url = data.getString(BasisData.KEY_S3_URL);
                        String ssUrl = data.getString(BasisData.KEY_SS_URL);
                        String region = data.getString(BasisData.KEY_REGION);
                        GlobalPrefs.getPreferences(NooieApplication.mCtx).saveBrain(webUrl, p2pUrl, s3Url, region, ssUrl);
                        EventTrackingApi.getInstance().setRegion(region);
//                        PlatformInitHelper.INSTANCE.updateApiSdkParam(null, null, null, null, null, webUrl + "/", s3Url + "/", region);
                        break;
                    }
                    case GlobalDataListener.CODE_UPDATE_TOKEN : {
                        if (data == null) {
                            break;
                        }
                        String uid = data.getString(BasisData.KEY_UID);
                        String token = data.getString(BasisData.KEY_TOKEN);
                        String refreshToken = data.getString(BasisData.KEY_REFRESH_TOKEN);
                        long expireTime = data.getLong(BasisData.KEY_EXPIRE_TIME);
                        GlobalPrefs.getPreferences(NooieApplication.mCtx).saveUidAndToken(uid, token, refreshToken, expireTime);
                        EventTrackingApi.getInstance().setUid(uid);
//                        PlatformInitHelper.INSTANCE.updateApiSdkParam(null, null, null, uid, token, null, null, null);
                        break;
                    }
                    case GlobalDataListener.CODE_UPDATE_REFRESH_TOKEN : {
                        if (data == null) {
                            break;
                        }
                        String token = data.getString(BasisData.KEY_TOKEN);
                        String refreshToken = data.getString(BasisData.KEY_REFRESH_TOKEN);
                        long expireTime = data.getLong(BasisData.KEY_EXPIRE_TIME);
                        GlobalPrefs.getPreferences(NooieApplication.mCtx).saveToken(token, refreshToken, expireTime);
                        break;
                    }
                    case GlobalDataListener.CODE_UPDATE_GAP_TIME : {
                        if (data == null) {
                            return;
                        }
                        int gapTime = data.getInt(BasisData.KEY_GAP_TIME, 0);
                        GlobalPrefs.getPreferences(NooieApplication.mCtx).setGapTime(gapTime);
                        EventTrackingApi.getInstance().setGapTime(gapTime);
//                        PlatformInitHelper.INSTANCE.updateApiSdkParam((System.currentTimeMillis() / 1000L) + gapTime);
                    }
                    case GlobalDataListener.CODE_UPDATE_PHONE_ID : {
                        if (data == null) {
                            return;
                        }
                        String phoneId = data.getString(BasisData.KEY_PHONE_ID);
                        GlobalPrefs.getPreferences(NooieApplication.mCtx).setPhoneUuid(phoneId);
                        break;
                    }
                    case GlobalDataListener.CODE_UPDATE_PUSH_TOKEN : {
                        if (data == null) {
                            return;
                        }
                        String pushToken = data.getString(BasisData.KEY_PUSH_TOKEN);
                        GlobalPrefs.getPreferences(NooieApplication.mCtx).setPushToken(pushToken);
                        break;
                    }
                }
            }
        });
    }

    private static String createUserAgent(String userAgentPrefix) {
        if (TextUtils.isEmpty(userAgentPrefix)) {
            return "";
        }
        return userAgentPrefix + BuildConfig.VERSION_NAME + "_" + BuildConfig.VERSION_CODE;
    }

    private void initYRPlatform() {
        PlatformInitHelper.INSTANCE.startPlatformInit(this);
        PlatformInitHelper.INSTANCE.startPlatformInitApiSdk(this, NetConfigure.getInstance().getBaseUrl()+"v2/");
        PlatformInitHelper.INSTANCE.startPlatformInitMiddleService(this);
    }
}

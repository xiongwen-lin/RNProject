package com.afar.osaio.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.account.presenter.ISplashPresenter;
import com.afar.osaio.account.presenter.SplashPresenterImpl;
import com.afar.osaio.account.view.ISplashView;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieCrashHandler;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.alibaba.android.arouter.launcher.ARouter;
import com.apemans.base.middleservice.YRMiddleServiceManager;
import com.apemans.business.apisdk.client.define.HttpCode;
import com.apemans.platformbridge.helper.YRUserPlatformHelper;
import com.apemans.yrcxsdk.data.YRCXSDKDataManager;
import com.nooie.common.base.SDKGlobalData;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.nooie.common.base.GlobalData;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;

public class SplashActivity extends BaseActivity implements ISplashView {

    private ISplashPresenter splashPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("debug", "-->> App Start tracking 10001");
        //NooieLog.d("-->> SplashActivity onCreate liveFlag=" + NooieApplication.liveFlag);
        //NooieApplication.liveFlag = 1;
        NooieLog.d("-->> SplashActivity onCreate GlobalData liveFlag=" + GlobalData.liveFlag + " context null " + (NooieApplication.mCtx == null));
        GlobalData.liveFlag = 1;
        super.onCreate(savedInstanceState);
        Log.d("debug", "-->> App Start tracking 10002");
        if (getIntent() != null && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            //首次安装直接打开app异常处理
            NooieLog.d("-->> SplashActivity onCreate open app from apk installer");
            finish();
            return;
        }
        Log.d("debug", "-->> App Start tracking 10003");
        setSlideable(false);
        //hideStatusBar();

        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        splashPresenter = new SplashPresenterImpl(this);

        // not check force logout
        //setCheckForceLogout(false);
        cancelBleApDeviceConnect();
        Log.d("debug", "-->> App Start tracking 10004");
        if (splashPresenter != null) {
            splashPresenter.initGlobalData();
        }
        // 注册服务
//        YRMiddleServiceManager.registerAllService(getApplication(), getExcludeRegisterPackageList());
//        YRCXSDKDataManager.INSTANCE.initDataListener();
//        initYRApiSdk();
        // 注册异地登陆等消息码广播
        YRUserPlatformHelper.INSTANCE.registerResponseCodeReceiver();
    }

    private List<String> getExcludeRegisterPackageList() {
        List list = new ArrayList<String>();
        list.add("anet.channel");
        list.add("com.bumptech");
        list.add("com.facebook");
        list.add("com.swmansion");
        list.add("com.tuya");
        list.add("org.apache");
        list.add("com.taobao");
        list.add("demo.");
        list.add("org.repackage");
        list.add("okhttp3.internal");
        return list;
    }

    /**
     * 初始化YRApiSdk
     * 1、调用YRApiManager.init()接口，初始化appId、appSecret、baseurl（获取全球服务器地址的baseUrl，如示例teckin的https://global.teckinhome.com/v2/）
     * 2、调用YRApiManager.registerGlobalObserver()接口，注册全局监听，用于观察接口请求异常通知，并针对不同的情况进行处理。
     * 3、确保数据共享库的uid、token、全球地址有效，接口请求和接口的签名需要用到
     * 4、对应请求接口请调用YRApi中开发的接口，如YRApi.getGlobalApi()代表和全球服务地址相关的接口
     * 5、分别获取全球时间、获取全球服务器地址、登陆成功后，同步相关信息到NetConfigure网络配置；具体示例如下面的updateNetConfigure方法
     */
    private void initYRApiSdk() {

        /*
        中间件初始化暂时停用
        var initParam = mutableMapOf<String, Any>()
        initParam[YR_MIDDLE_SERVICE_PARAM_BASE_URL] = "https://global.osaio.net/v2/"
        YRMiddleServiceManager.requestAsync("yrcx://yrbusiness/init", initParam) {
            YRLog.d { "-->> debug MainActivity testApi requestAsync init code ${it?.code} " }
        }

         */
        HashMap envParam = new HashMap<String, Object>();
        envParam.put("appid","3dab98eee85b7ae8");
        envParam.put("secret","7569f96ceac7c333e981a9604865e413");
        envParam.put("baseurl","https://global.osaio.net/v2");
        envParam.put("service_time",System.currentTimeMillis() / 1000L);
        envParam.put("web_baseurl","https://app.osaio.nooie.cn/v2");
        envParam.put("s3_baseurl","https://app.osaio.nooie.cn/v3/cloud");
        envParam.put("region","cn");
        HashMap tokenUidParams = new HashMap<String, Object>();
        tokenUidParams.put("uid","b02343fa4bfe870e");
        tokenUidParams.put("api-token","eyJhbGciOiJoczI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NDYzNzgxNDcsInVpZCI6ImIwMjM0M2ZhNGJmZTg3MGUiLCJyZWciOiJjbiIsInNjaCI6IjNkYWI5OGVlZTg1YjdhZTgiLCJ0X2lkIjoib21nZndhZGx2dXdzbmd4bmNteHJlcWRpYmpjZWxweGwiLCJyZWZfdG9rIjoiYzE1ZmY5ODMtOTYwYS0xMWVjLThhMjctYTI3MzYwNjg1OTdiIiwicmVmX2V4cCI6MTY0Njk4Mjk0N30=.MzYwNjgxMzY0M2MzNDI5MGFhYmE2YjE2MTE5YWMyMmQzMzIwMGI4NjBjYTY3NTU0MDIwODk1NzkwNTZiNzM5MQ==");

        envParam.put("token_uid_paramters", tokenUidParams);
        HashMap extensionHeaders = new HashMap<String, Object>();
        tokenUidParams.put("User-Agent","OSAIO_ANDROID_1.0.0");
        envParam.put("extension_headers", extensionHeaders);

        List listGlobal = new ArrayList<String>();
        listGlobal.add("global/time");
        listGlobal.add("user/tuya");
        listGlobal.add("account/country");
        listGlobal.add("account/baseurl");
        listGlobal.add("user/account");
        envParam.put("global_url", listGlobal);
        List listBeforeLogin = new ArrayList<String>();
        listBeforeLogin.add("global/time");
        listBeforeLogin.add("user/tuya");
        listBeforeLogin.add("account/country");
        listBeforeLogin.add("account/baseurl");
        listBeforeLogin.add("user/account");
        listBeforeLogin.add("login/login");
        listBeforeLogin.add("login/send");
        listBeforeLogin.add("register/send");
        listBeforeLogin.add("register/verify");
        listBeforeLogin.add("login/verify");
        listBeforeLogin.add("tuya/reset");
        listBeforeLogin.add("register/register");
        envParam.put("before_login_url", listBeforeLogin);

        List listS3 = new ArrayList<String>();
        listS3.add("feedbackput_presignurl");
        listS3.add("photoget_presignurl");
        listS3.add("app/fetch/file_event");
        envParam.put("s3_url", listS3);

        HashMap responseCodeMap = new HashMap<String, List<String>>();
        responseCodeMap.put("update_server_time", new ArrayList<String>().add(""+HttpCode.CODE_1004));
        List listCode = new ArrayList<String>();
        listCode.add(""+HttpCode.CODE_1006);
        listCode.add(""+HttpCode.CODE_1059);
        responseCodeMap.put("token_invalid", listCode);
        responseCodeMap.put("login_elsewhere", new ArrayList<String>().add(""+HttpCode.CODE_1056));
        responseCodeMap.put("data_migrated", new ArrayList<String>().add(""+HttpCode.CODE_1909));
        envParam.put("action_responsed_error_code", responseCodeMap);
        YRMiddleServiceManager.request("yrcx://yrbusiness/setparamters", envParam);
//        YRMiddleServiceManager.request("yrcx://yrplatformbridge/setparamters",envParam);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("debug", "-->> App Start tracking 10005");
    }

    @Override
    public void protectApp() {
    }

    @Override
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_DARK_MODE;
    }

    @Override
    protected void onReturnFromAppSettingActivity() {
        super.onReturnFromAppSettingActivity();
    }

    @Override
    public void onInitGlobalDataResult(String result, boolean initResult) {
        Log.d("debug", "-->> App Start tracking 10006");
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (initResult) {
                initGlobalData();
                NooieDeviceHelper.initNativeConnect();
                DeviceConnectionCache.getInstance().clearCache();
                NooieCrashHandler.getINSTANCE().setUidAndAccount(mUid, mUserAccount);
                NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGIN, null);
                DeviceConnectionHelper.getInstance().startCheckDeviceConnection(GlobalData.getInstance().getUid());
            } else {
                MyAccountHelper.getInstance().logout();
            }
            Log.d("debug", "-->> App Start tracking 10007");
            checkAutoLogin();
        } else {
            Log.d("debug", "-->> App Start tracking 10008");
            checkAutoLogin();
        }
    }

    @Override
    public void notifySignInResult(String msg) {
        Log.d("debug", "-->> App Start tracking 100015");
        if (isDestroyed()) {
            Log.d("debug", "-->> App Start tracking 100016");
            return;
        }
        updataUserInfo();
        NooieLog.d("-->> SplashActivity notifySignInResult sign msg=" + msg);
        gotoHomePage(ConstantValue.SUCCESS.equalsIgnoreCase(msg));
        Log.d("debug", "-->> App Start tracking 100017");
    }

    private void updataUserInfo() {
        HashMap<String, Object> hashMap = new HashMap<>();
        HashMap tokenUidParams = new HashMap<String, Object>();
        tokenUidParams.put("uid",SDKGlobalData.getInstance().getUid());
        tokenUidParams.put("api-token",SDKGlobalData.getInstance().getToken());
        hashMap.put("token_uid_paramters", tokenUidParams);
        hashMap.put("refresh_token", SDKGlobalData.getInstance().getRefreshToken());
        hashMap.put("expire_time", SDKGlobalData.getInstance().getExpireTime());
        hashMap.put("push_token", SDKGlobalData.getInstance().getPushToken());
        hashMap.put("web_baseurl", SDKGlobalData.getInstance().getWebUrl());
        hashMap.put("p2p", SDKGlobalData.getInstance().getP2pUrl());
        hashMap.put("s3_baseurl", SDKGlobalData.getInstance().getS3Url());
        hashMap.put("region", SDKGlobalData.getInstance().getRegion());
        hashMap.put("ssUrl", SDKGlobalData.getInstance().getSsUrl());

        hashMap.put("appid", NetConfigure.getInstance().getAppId());
        hashMap.put("secret", NetConfigure.getInstance().getAppSecret());
        hashMap.put("baseurl", NetConfigure.getInstance().getBaseUrl());

        hashMap.put("userAccount", SDKGlobalData.getInstance().getAccount());
        hashMap.put("userHeadPic", GlobalPrefs.getPreferences(NooieApplication.mCtx).getTuyaPhoto());
        hashMap.put("userCountryCode", UserInfoCache.getInstance().getUserInfo().getCountry());
        hashMap.put("userNickname", UserInfoCache.getInstance().getUserInfo().getNickname());
        hashMap.put("firstLogin", false);
        YRUserPlatformHelper.INSTANCE.updataUserInfo(hashMap);
    }

    @Override
    public void onCheckAppIsStarted(int state, boolean isStarted) {
        if (isDestroyed()) {
            return;
        }
        if (isStarted) {
//            SignInActivity.toSignInActivity(this, "", "", true);
            // 发送跳转到登录页命令
            ARouter.getInstance().build("/user/login")
//                    .withString("userAccount", "")
//                    .withString("password", "")
//                    .withBoolean("isClearTask", true)
                    .navigation();
            finish();
        } else {
            gotoFirstStartInstructionPage();
        }
    }

    private void checkAutoLogin() {
        Log.d("debug", "-->> App Start tracking 10009");
        if (isDestroyed() || checkNull(splashPresenter)) {
            return;
        }
        // Auto login
        Log.d("debug", "-->> App Start tracking 100010");
        if (MyAccountHelper.getInstance().isLoginWithTuya()) {
            Log.d("debug", "-->> App Start tracking 100011");
            NooieLog.d("-->> SplashActivity checkAutoLogin sign start");
            splashPresenter.autoLogin(false);
            NooieLog.d("-->> SplashActivity checkAutoLogin sign end");
            Log.d("debug", "-->> App Start tracking 100012");
        } else {
            Log.d("debug", "-->> App Start tracking 100013");
            MyAccountHelper.getInstance().logout();
            splashPresenter.checkAppIsStarted();
            Log.d("debug", "-->> App Start tracking 100014");
        }
    }

    /**
     * 统一跳转主页
     * @param isSuccess
     */
    private void gotoHomePage(boolean isSuccess) {
        Log.d("debug", "-->> App Start tracking 100018");
        NooieApplication.get().setIsUseJPush(NooieDeviceHelper.isUseJPush(GlobalData.getInstance().getRegion()));
        if (getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, 0) == ConstantValue.NOOIE_MSG_TYPE_DEVICE) {
            HomeActivity.toHomeActivity(this);
            Log.d("debug", "-->> App Start tracking 100019");
        } else if (getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, 0) == ConstantValue.NOOIE_MSG_TYPE_SYS) {
            HomeActivity.toHomeActivity(this);
            Log.d("debug", "-->> App Start tracking 100020");
        } else {
            HomeActivity.toHomeActivity(this);
            Log.d("debug", "-->> App Start tracking 100021");
        }
        overridePendingTransition(R.anim.activity_enter_alpha_anim, R.anim.activity_exit_alpha_anim);
        finish();
        Log.d("debug", "-->> App Start tracking 100022");
    }

    private void cancelBleApDeviceConnect() {
        try {
            ApHelper.getInstance().tryResetApConnectMode("", new ApHelper.APDirectListener() {
                @Override
                public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                }
            });
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private void gotoFirstStartInstructionPage() {
        FirstStartInstructionActivity.toFirstStartInstructionActivity(this);
        finish();
    }
}

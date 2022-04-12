package com.afar.osaio.account.helper;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.afar.osaio.BuildConfig;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieCrashHandler;
import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.electrician.manager.DeviceManager;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.base.BasisData;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.configure.PhoneUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.tool.SystemUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.request.ReportUserRequest;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.processor.cloud.CloudManager;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.helper.AccountHelper;
import com.nooie.sdk.processor.user.UserApi;
import com.tuya.smart.android.user.api.ILogoutCallback;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.clearcache.api.ClearCacheService;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.wrapper.api.TuyaWrapper;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * AccountHelper
 *
 * @author Administrator
 * @date 2019/7/4
 */
public class MyAccountHelper {

    private Map<String, String> mAppCountBrands = new HashMap<>();

    private MyAccountHelper() {
    }

    private static class AccountHelperInstance {
        private static final MyAccountHelper INSTANCE = new MyAccountHelper();
    }

    public static MyAccountHelper getInstance() {
        return AccountHelperInstance.INSTANCE;
    }

    public boolean isLogin() {
        return AccountHelper.getInstance().isLogin();
    }

    public boolean isLoginWithTuya() {
        return AccountHelper.getInstance().isLogin() && TuyaHomeSdk.getUserInstance().isLogin();
    }

    public void logout() {
        DeviceConnectionHelper.getInstance().stopCheckDeviceConnection();
        //EventTrackingHelper.getInstance().appKill();
        UserApi.getInstance().releaseDataObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        //clearCache();
                    }

                    @Override
                    public void onNext(Boolean result) {
                        //clearCache();
                    }
                });
        clearCache();
    }

    private void tuyaLogout() {
        if (TuyaHomeSdk.getUserInstance().isLogin()) {
            TuyaHomeSdk.getUserInstance().logout(new ILogoutCallback() {
                @Override
                public void onSuccess() {
                    NooieLog.e("-------->>> tuyaLogout onSuccess");
                    TuyaHomeSdk.onDestroy();
                }

                @Override
                public void onError(String s, String s1) {
                    NooieLog.e("-------->>> onError s " + s + " s1 " + s1);
                    TuyaHomeSdk.onDestroy();
                }
            });
        }
    }

    public void clearCache() {
        ClearCacheService service = MicroContext.getServiceManager().findServiceByInterface(ClearCacheService.class.getName());
        if (service != null) {
            service.clearCache(NooieApplication.mCtx);
        }
        unBindUmengPush();
        tuyaLogout();
        TuyaWrapper.onLogout(NooieApplication.mCtx);
        DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
        UserInfoCache.getInstance().clear();
        DeviceInfoCache.getInstance().clearCache();
        GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        globalPrefs.logout();
        DeviceManager.getInstance().clearTyDeviceList();
        NooieCrashHandler.getINSTANCE().clearUidAndAccount();
        NooieApplication.get().setIsUseJPush(false);
        NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGOUT, null);
        CloudManager.getInstance().release();
    }

    private void unBindUmengPush() {
        PushAgent.getInstance(NooieApplication.mCtx).deleteAlias(PushAgent.getInstance(NooieApplication.mCtx).getRegistrationId(), "TUYA_SMART", new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean isSuccess, String message) {
            }
        });
    }

    public boolean isAppBeKilled(Context context) {
        boolean isAppAlive = SystemUtil.isAppAlive(context, context.getPackageName()) && isLogin();
        return !isAppAlive;
    }

    public ReportUserRequest createReportUserRequest() {
        NooieLog.d("-->> debug MyAccountHelper createReportUserRequest: phoneCode create=" + GlobalData.getInstance().getPhoneId());
        PhoneUtil.logPhoneId(NooieApplication.mCtx);
        int pushType = NooiePushMsgHelper.getPushType();
        int deviceType = ApiConstant.DEVICE_TYPE;
        String pushToken = NooiePushMsgHelper.getPushToken();
        String phoneCode = GlobalData.getInstance().getPhoneId();
        String country = CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        float zone = CountryUtil.getCurrentTimeZone();
        String nickname = new String();
        String photo = new String();
        String appVersion = BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
        String appVersionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String phoneModel = Build.MODEL;
        String phoneBrand = Build.BRAND;
        String phoneVersion = String.valueOf(Build.VERSION.SDK_INT);
        String phoneScreen = DisplayUtil.SCREEN_WIDTH_PX + "*" + DisplayUtil.SCREEN_HIGHT_PX;
        String language = LanguageUtil.getLocal(NooieApplication.mCtx).getLanguage();
        String packageName = BuildConfig.APPLICATION_ID;
        return new ReportUserRequest(pushType, deviceType, pushToken, phoneCode, country, zone, nickname, photo, appVersion, appVersionCode, phoneModel, phoneBrand, phoneVersion, phoneScreen, language, packageName, PhoneUtil.getPhoneName(NooieApplication.mCtx));
    }

    public void log(String tag) {
        StringBuilder logSb = new StringBuilder();
        logSb.append(tag);
        logSb.append("\nApplication context exist=" + (NooieApplication.mCtx != null));
        logSb.append("\nApplicationContext exist=" + (NooieApplication.get() != null));
        logSb.append("\nliveFlage:");
        logSb.append(GlobalData.liveFlag);
        logSb.append("\nisLogin:");
        logSb.append(isLogin());
        logSb.append("\nphoneCode:");
        logSb.append(GlobalData.getInstance().getPhoneId());
        logSb.append("\nkvPhoneCode:");
        logSb.append(BasisData.getInstance().getPhoneId());
        logSb.append("\naccount:");
        logSb.append(GlobalData.getInstance().getAccount());
        NooieLog.d("-->> debug MyAccountHelper log: " + logSb.toString());
    }

    public boolean checkAppCountSelf(List<String> brandList) {
        return CollectionUtil.isEmpty(brandList) || brandList.contains(ConstantValue.APP_ACCOUNT_BRAND_CURRENT);
    }

    public String convertFromBrandList(List<String> brandList) {
        if (CollectionUtil.isEmpty(brandList)) {
            return "";
        }
        try {
            List<String> brandNameList = new ArrayList<>();
            Iterator<String> iterator = brandList.iterator();
            while (iterator.hasNext()) {
                String brand = iterator.next();
                String brandName = convertAppBrandName(brand);
                if (TextUtils.isEmpty(brand) || ConstantValue.APP_ACCOUNT_BRAND_CURRENT.equalsIgnoreCase(brand)) {
                    iterator.remove();
                } else if (!TextUtils.isEmpty(brandName)) {
                    brandNameList.add(brandName);
                }
            }
            if (CollectionUtil.isEmpty(brandNameList)) {
                return "";
            }
            if (CollectionUtil.size(brandNameList) == 1) {
                return brandNameList.get(0);
            }
            StringBuilder brandSb = new StringBuilder();
            for (int i = 0; i < CollectionUtil.size(brandNameList); i++) {
                if (i == i - 1) {
                    brandSb.append(brandNameList.get(i));
                } else {
                    brandSb.append(brandNameList.get(i)).append("/");
                }
            }
            return brandSb.toString();
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return "";
    }

    public String convertAppBrandName(String brand) {
        if (TextUtils.isEmpty(brand)) {
            return "";
        }
        initAppCountBrand();
        String brandName = mAppCountBrands.containsKey(brand) ? mAppCountBrands.get(brand) : brand;
        return brandName;
    }

    private void initAppCountBrand() {
        if (mAppCountBrands == null) {
            mAppCountBrands = new HashMap<>();
        }
        if (!mAppCountBrands.isEmpty()) {
            return;
        }
        mAppCountBrands.clear();
        mAppCountBrands.put(ConstantValue.APP_ACCOUNT_BRAND_VICTURE, ConstantValue.APP_NAME_OF_BRAND_VICTURE);
        mAppCountBrands.put(ConstantValue.APP_ACCOUNT_BRAND_TECKIN, ConstantValue.APP_NAME_OF_BRAND_TECKIN);
    }
}

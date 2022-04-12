package com.afar.osaio.base;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.afar.osaio.BuildConfig;
import com.afar.osaio.smart.electrician.activity.LampSettingActivity;
import com.afar.osaio.smart.electrician.widget.BizBundleFamilyServiceImpl;
import com.apemans.yruibusiness.base.BaseComponentApplication;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.nooie.common.utils.configure.AppInfoUtil;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.log.ILogInterception;
import com.tuya.smart.api.router.UrlBuilder;
import com.tuya.smart.api.service.RouteEventListener;
import com.tuya.smart.api.service.ServiceEventListener;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.optimus.sdk.TuyaOptimusSdk;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.wrapper.api.TuyaWrapper;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import java.lang.reflect.Field;

public class BaseApplication extends BaseComponentApplication {
    public static Context mCtx;

    @Override
    public void onCreate() {
        super.onCreate();
        mCtx = this;
        initTuya();
        String currentProcessName = getCurrentProcessName();
        Log.v("Application", "BaseApplication processName：" + currentProcessName + " APPLICATION_ID=" + BuildConfig.APPLICATION_ID);
        if (currentProcessName != null && !currentProcessName.equalsIgnoreCase(BuildConfig.APPLICATION_ID)) {
            return;
        }
        // api level > 7.0,FileUriExposedException
        // https://blog.csdn.net/yyh352091626/article/details/54908624
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        ZXingLibrary.initDisplayOpinion(this);
        initTypeface();
        //NooieCrashHandler.getINSTANCE().init(this);
    }

    private void initTuya() {
        // 请不要修改初始化顺序
        Fresco.initialize(this);

        // SDK 初始化
        TuyaHomeSdk.setDebugMode(true);
        TuyaHomeSdk.init(this);

        // 业务包初始化
        TuyaWrapper.init(this, new RouteEventListener() {
            @Override
            public void onFaild(int errorCode, UrlBuilder urlBuilder) {
                // 路由未实现回调
                // 点击无反应表示路由未现实，需要在此实现， urlBuilder.target 目标路由， urlBuilder.params 路由参数
                if (urlBuilder.params != null && urlBuilder.params.containsKey("extra_panel_dev_id")) {
                    NooieLog.e("right event devId " + urlBuilder.params.getString("extra_panel_dev_id"));
                    String devId = urlBuilder.params.getString("extra_panel_dev_id");
                    if (!TextUtils.isEmpty(devId)) {
                        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
                        if (deviceBean != null) {
                            LampSettingActivity.toLampSettingActivity(urlBuilder.context, devId, deviceBean.getIsShare());
                        }
                    }
                }
                Log.e("router not implement", urlBuilder.target + urlBuilder.params.toString());
            }
        }, new ServiceEventListener() {
            @Override
            public void onFaild(String serviceName) {
                // 服务未实现回调
                Log.e("service not implement", serviceName);
            }
        });
        TuyaOptimusSdk.init(this);
        TuyaWrapper.registerService(AbsBizBundleFamilyService.class, new BizBundleFamilyServiceImpl());
        L.setLogSwitcher(false);
        TuyaHomeSdk.setLogInterception(Log.VERBOSE, new ILogInterception() {
            @Override
            public void log(int i, String s, String s1) {
          //      NooieLog.e("Tuya i " + i + " s " + s + " s1 " + s1);
            }
        });
    }

    public String getCurrentProcessName() {
        String currentProcessName = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            currentProcessName = getProcessName();
        }
        if (TextUtils.isEmpty(currentProcessName) || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            currentProcessName = AppInfoUtil.getProcessName(this, android.os.Process.myPid());
        }
        return currentProcessName;
    }

    private void initTypeface() {
        //Typeface typeface = FontUtil.loadTypeface(getApplicationContext(), "fonts/Avenir.ttc");
        Typeface typeface = FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope.otf");
        try {
            Field field = Typeface.class.getDeclaredField("MONOSPACE");
            field.setAccessible(true);
            field.set(null, typeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

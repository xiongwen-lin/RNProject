package com.afar.osaio.smart.bridge.helper;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.alibaba.fastjson.JSONObject;
import com.apemans.platformbridge.bridge.contract.YRIUserInfoHelper;
import com.apemans.yrcxsdk.data.YRCXSDKDataManager;
import com.dylanc.longan.ActivityKt;
import com.nooie.common.base.SDKGlobalData;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.db.dao.ServerNodeService;
import com.nooie.sdk.db.dao.UserInfoService;
import com.nooie.sdk.db.dao.UserRegionService;

import java.util.HashMap;

public class YRUserInfoPresenter implements YRIUserInfoHelper {

    HashMap<String, Object> hashMaps = new HashMap<String, Object>();
    @Override
    public void updataUserInfo(@NonNull HashMap<String, Object> hashMap) {
        hashMaps = hashMap;
        upData(hashMaps);
    }

    private void upData(HashMap<String, Object> hashMap) {
        if (hashMap.containsKey("userHeadPic")) {
            UserInfoCache.getInstance().getUserInfo().setPhoto(null != YRCXSDKDataManager.INSTANCE.getUserHeadPic() ? YRCXSDKDataManager.INSTANCE.getUserHeadPic() : "");
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setTuyaPhoto(null != YRCXSDKDataManager.INSTANCE.getUserHeadPic() ? YRCXSDKDataManager.INSTANCE.getUserHeadPic() : "");
            NooieLog.d("************************* UserInfoCache userHeadPic: " + UserInfoCache.getInstance().getUserInfo().getPhoto()
                    + " =================TuyaPhoto: " + GlobalPrefs.getPreferences(NooieApplication.mCtx).getTuyaPhoto());
//            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmTuyaPhoto((String)hashMap.get("userHeadPic"));
        }
        if (hashMap.containsKey("logout")) {
            // 用户退出登入，清除相关信息
            releaseCacheData();
            return;
        }
        Log.d("YRUserInfoPresenter", "*********************: " + hashMap.toString());
        if (hashMap.containsKey("login")) {
            saveUserLoginDbData();
            HomeActivity.toHomeActivity(ActivityKt.getTopActivity());
        }
        if (hashMap.containsKey("logup")) {
            saveUserLogupDbData();
            HomeActivity.toHomeActivity(ActivityKt.getTopActivity());
        }
        if (hashMap.containsKey("refresh_token")) {
            NooieLog.d("************************* refresh_token: " + (String)hashMap.get("refresh_token"));
            Bundle bundle = new Bundle();
            bundle.putString("refresh_token", (String)hashMap.get("refresh_token"));
            SDKGlobalData.getInstance().updateRefreshToken(bundle);
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmRefreshToken((String)hashMap.get("refresh_token"));
        }
        if (hashMap.containsKey("expire_time")) {
            NooieLog.d("************************* expire_time: " + (Long)hashMap.get("expire_time"));
            Bundle bundle = new Bundle();
            bundle.putLong("expire_time", (Long)hashMap.get("expire_time"));
            SDKGlobalData.getInstance().updateRefreshToken(bundle);
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmExpireTime((Long)hashMap.get("expire_time"));
        }
//        if (hashMap.containsKey("register_country")) {
//            register_country = (String)hashMap.get("register_country");
//        }
        if (hashMap.containsKey("token_uid_paramters")) {
            JSONObject jsonObject = JSONObject.parseObject((hashMap.get("token_uid_paramters").toString()));
            NooieLog.d("************************* token: " + (String)jsonObject.get("api-token") + " uid: " + (String)jsonObject.get("uid"));
            Bundle bundle = new Bundle();
            bundle.putString("token", (String)jsonObject.get("api-token"));
            bundle.putString("uid", (String)jsonObject.get("uid"));
            SDKGlobalData.getInstance().updateToken(bundle);
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmToken((String)jsonObject.get("api-token"));
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmUid((String)jsonObject.get("uid"));
        }
        if (hashMap.containsKey("p2p")) {
            Bundle bundle = new Bundle();
            bundle.putString("p2p_url", (String)hashMap.get("p2p"));
            SDKGlobalData.getInstance().updateBrainUrl(bundle);
            NooieLog.d("************************* p2p: " + (String)hashMap.get("p2p"));
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmP2pUrl((String)hashMap.get("p2p"));
        }
        if (hashMap.containsKey("region")) {
            Bundle bundle = new Bundle();
            bundle.putString("region", (String)hashMap.get("region"));
            SDKGlobalData.getInstance().updateBrainUrl(bundle);
            NooieLog.d("************************* region: " + (String)hashMap.get("region"));
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmRegion((String)hashMap.get("region"));
        }
        if (hashMap.containsKey("web_baseurl")) {
            Bundle bundle = new Bundle();
            bundle.putString("web_url", (String)hashMap.get("web_baseurl"));
            SDKGlobalData.getInstance().updateBrainUrl(bundle);
            NooieLog.d("************************* web_baseurl: " + (String)hashMap.get("web_baseurl"));
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmWebUrl((String)hashMap.get("web_baseurl"));
        }
        if (hashMap.containsKey("s3_baseurl")) {
            Bundle bundle = new Bundle();
            bundle.putString("s3_url", (String)hashMap.get("s3_baseurl"));
            SDKGlobalData.getInstance().updateBrainUrl(bundle);
            NooieLog.d("************************* s3_baseurl: " + (String)hashMap.get("s3_baseurl"));
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmS3Url((String)hashMap.get("s3_baseurl"));
        }
        if (hashMap.containsKey("ssUrl")) {
            Bundle bundle = new Bundle();
            bundle.putString("ss_url", (String)hashMap.get("ssUrl"));
            SDKGlobalData.getInstance().updateBrainUrl(bundle);
            NooieLog.d("************************* ssUrl: " + (String)hashMap.get("ssUrl"));
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmSsUrl((String)hashMap.get("ssUrl"));
        }
        if (hashMap.containsKey("baseurl")) {
            // 处理不一样，Osaio1.0采用在本地写死了baseurl，通过传入不同key值获取不同平台的baseUrl（查看代码，发现Osaio的key值为5）
            NetConfigure.getInstance().setPlatform(5);
//            baseUrl = (String)hashMap.get("baseurl");
        }
        if (hashMap.containsKey("appid")) {
            NooieLog.d("************************* appid: " + (String)hashMap.get("appid"));
            NetConfigure.getInstance().setAppId((String)hashMap.get("appid"));
        }
        if (hashMap.containsKey("secret")) {
            NooieLog.d("************************* secret: " + (String)hashMap.get("secret"));
            NetConfigure.getInstance().setAppSecret((String)hashMap.get("secret"));
        }
        if (hashMap.containsKey("userPassword")) {
            NooieLog.d("************************* userPassword: " + (String)hashMap.get("userPassword"));
            GlobalPrefs.getPreferences(NooieApplication.mCtx).login(YRCXSDKDataManager.INSTANCE.getUserAccount(), YRCXSDKDataManager.INSTANCE.getUserPassword());
            SDKGlobalData.getInstance().updatePassword((String)hashMap.get("userPassword"));
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmPassword((String)hashMap.get("userPassword"));
        }
        if (hashMap.containsKey("userAccount")) {
            NooieLog.d("************************* userAccount: " + (String)hashMap.get("userAccount"));
            UserInfoCache.getInstance().getUserInfo().setAccount((String)hashMap.get("userAccount"));
            UserInfoCache.getInstance().updateNickName((String)hashMap.get("userAccount"));
            Bundle bundle = new Bundle();
            bundle.putString("account", (String)hashMap.get("userAccount"));
            SDKGlobalData.getInstance().updateAccount(bundle);
            GlobalPrefs.getPreferences(NooieApplication.mCtx).setmAccount((String)hashMap.get("userAccount"));
        }
        if (hashMap.containsKey("userNickname")) {
            NooieLog.d("************************* userNickname: " + (String)hashMap.get("userNickname"));
            UserInfoCache.getInstance().getUserInfo().setNickname((String)hashMap.get("userNickname"));
        }
        if (hashMap.containsKey("userCountryCode")) {
            NooieLog.d("************************* userCountryCode: " + (String)hashMap.get("userCountryCode"));
            UserInfoCache.getInstance().getUserInfo().setCountry((String)hashMap.get("userCountryCode"));
        }
        hashMaps.clear();
    }

    private void addServerNode() {
        ServerNodeService.getInstance().addServerNode(SDKGlobalData.getInstance().getAccount(), SDKGlobalData.getInstance().getWebUrl(),
                SDKGlobalData.getInstance().getP2pUrl(), SDKGlobalData.getInstance().getS3Url(),
                SDKGlobalData.getInstance().getRegion(), SDKGlobalData.getInstance().getSsUrl());
    }

    private void updateBrainUrl() {
        Bundle brainUrlData = new Bundle();
        brainUrlData.putString("web_url", SDKGlobalData.getInstance().getWebUrl());
        brainUrlData.putString("p2p_url", SDKGlobalData.getInstance().getP2pUrl());
        brainUrlData.putString("s3_url", SDKGlobalData.getInstance().getS3Url());
        brainUrlData.putString("ss_url", SDKGlobalData.getInstance().getSsUrl());
        brainUrlData.putString("region", SDKGlobalData.getInstance().getRegion());
        SDKGlobalData.getInstance().updateBrainUrl(brainUrlData);
    }

    private void addUserRegion() {
        UserRegionService.getInstance().addUserRegion(SDKGlobalData.getInstance().getAccount(), SDKGlobalData.getInstance().getPassword());
    }

    private void addUserInfo() {
        UserInfoService.getInstance().addUserInfo(SDKGlobalData.getInstance().getAccount(), SDKGlobalData.getInstance().getPassword(),
                SDKGlobalData.getInstance().getUid(), SDKGlobalData.getInstance().getToken(), 1,
                SDKGlobalData.getInstance().getRefreshToken(), SDKGlobalData.getInstance().getExpireTime(), 1, new String(), 0);
    }

    private void updateAccount() {
        Bundle accountData = new Bundle();
        accountData.putString("account", SDKGlobalData.getInstance().getAccount());
        accountData.putString("password", SDKGlobalData.getInstance().getPassword());
        SDKGlobalData.getInstance().updateAccount(accountData);
    }

    private void updateLoginInfo() {
        Bundle loginInfo = new Bundle();
        loginInfo.putInt("login_type", 1);
        loginInfo.putString("third_party_open_id", new String());
        loginInfo.putInt("third_party_user_type", 0);
        SDKGlobalData.getInstance().updateLoginInfo(loginInfo);
    }

    private void updateToken() {
        Bundle tokenData = new Bundle();
        tokenData.putString("uid", SDKGlobalData.getInstance().getUid());
        tokenData.putString("token", SDKGlobalData.getInstance().getToken());
        tokenData.putString("refresh_token", SDKGlobalData.getInstance().getRefreshToken());
        tokenData.putLong("expire_time", SDKGlobalData.getInstance().getExpireTime());
        SDKGlobalData.getInstance().updateToken(tokenData);
    }

    private void saveUserLogupDbData() {
        addServerNode();
        updateBrainUrl();
        addUserRegion();
        addUserInfo();
        updateAccount();
        updateLoginInfo();
        updateToken();
//        updatePushToken();
        GlobalPrefs.getPreferences(NooieApplication.mCtx).login(SDKGlobalData.getInstance().getAccount(), SDKGlobalData.getInstance().getPassword());
        GlobalPrefs.getPreferences(NooieApplication.mCtx).saveToken(SDKGlobalData.getInstance().getToken(),
                SDKGlobalData.getInstance().getRefreshToken(),
                SDKGlobalData.getInstance().getExpireTime());
    }

    private void saveUserLoginDbData() {
        addServerNode();
        updateBrainUrl();
        addUserInfo();
        updateAccount();
        updateLoginInfo();
        updateToken();
//        updatePushToken();
        GlobalPrefs.getPreferences(NooieApplication.mCtx).login(SDKGlobalData.getInstance().getAccount(), SDKGlobalData.getInstance().getPassword());
        GlobalPrefs.getPreferences(NooieApplication.mCtx).saveToken(SDKGlobalData.getInstance().getToken(),
                SDKGlobalData.getInstance().getRefreshToken(),
                SDKGlobalData.getInstance().getExpireTime());
    }

    private void releaseCacheData() {
        GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        globalPrefs.logout();
    }
}

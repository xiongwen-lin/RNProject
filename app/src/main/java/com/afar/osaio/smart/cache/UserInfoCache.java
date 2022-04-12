package com.afar.osaio.smart.cache;

import android.text.TextUtils;

import com.nooie.common.utils.data.PrefsUtil;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.UserInfo;

public class UserInfoCache {

    public static final String PREFS_NAME_PREFIX = "UIC_";
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_IS_DEBUG = "isdebug";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_TWO_AUTH = "two_auth";

    private PrefsUtil mPrefsUtils;
    private String mAccount;
    private UserInfo mUserInfo;

    private UserInfoCache() {
        mPrefsUtils = PrefsUtil.getPreferences(NooieApplication.mCtx);
    }

    private static class UserInfoCacheHolder {
        private static final UserInfoCache INSTANCE = new UserInfoCache();
    }

    public static UserInfoCache getInstance() {
        return UserInfoCacheHolder.INSTANCE;
    }

    public void init(String account) {
        if (!TextUtils.isEmpty(account)) {
            mAccount = account;
            mUserInfo = new UserInfo();
            mUserInfo.setAccount(PrefsUtil.getString(NooieApplication.mCtx, getPrefsName(), KEY_ACCOUNT, ""));
            mUserInfo.setNickname(PrefsUtil.getString(NooieApplication.mCtx, getPrefsName(), KEY_NICKNAME, ""));
            mUserInfo.setPhoto(PrefsUtil.getString(NooieApplication.mCtx, getPrefsName(), KEY_PHOTO, ""));
            mUserInfo.setCountry(PrefsUtil.getString(NooieApplication.mCtx, getPrefsName(), KEY_COUNTRY, ""));
            mUserInfo.setLevel(PrefsUtil.getString(NooieApplication.mCtx, getPrefsName(), KEY_LEVEL, ""));
            mUserInfo.setIsdebug(PrefsUtil.getInt(NooieApplication.mCtx, getPrefsName(), KEY_IS_DEBUG, 0));
            mUserInfo.setTwoAuth(PrefsUtil.getInt(NooieApplication.mCtx, getPrefsName(), KEY_TWO_AUTH, 0));
        }
    }

    public void clear() {
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_ACCOUNT, "");
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_NICKNAME, "");
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_PHOTO, "");
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_COUNTRY, "");
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_LEVEL, "");
        PrefsUtil.putInt(NooieApplication.mCtx, getPrefsName(), KEY_IS_DEBUG, 0);
        PrefsUtil.putInt(NooieApplication.mCtx, getPrefsName(), KEY_TWO_AUTH, 0);
        mAccount = null;
        mUserInfo = null;
    }

    public String getPrefsName() {
        StringBuilder prefsNameSb = new StringBuilder();
        prefsNameSb.append(PREFS_NAME_PREFIX);
        prefsNameSb.append(mAccount);
        return prefsNameSb.toString();
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public void setUserInfo(UserInfoResult result) {
        if (result == null || !isCacheAvailable()) {
            return;
        }

        if (mUserInfo == null) {
            mUserInfo = new UserInfo();
        }

        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_ACCOUNT, result.getAccount());
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_NICKNAME, result.getNickname());
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_PHOTO, result.getPhoto());
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_COUNTRY, result.getCountry());
        PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_LEVEL, result.getLevel());
        PrefsUtil.putInt(NooieApplication.mCtx, getPrefsName(), KEY_IS_DEBUG, result.getIsdebug());
        PrefsUtil.putInt(NooieApplication.mCtx, getPrefsName(), KEY_TWO_AUTH, result.getTwo_auth());

        mUserInfo.setAccount(result.getAccount());
        mUserInfo.setNickname(result.getNickname());
        mUserInfo.setPhoto(result.getPhoto());
        mUserInfo.setCountry(result.getCountry());
        mUserInfo.setLevel(result.getLevel());
        mUserInfo.setIsdebug(result.getIsdebug());
        mUserInfo.setTwoAuth(result.getTwo_auth());
    }

    public void updateNickName(String name) {
        if (isCacheAvailable() && !TextUtils.isEmpty(name)) {
            PrefsUtil.putString(NooieApplication.mCtx, getPrefsName(), KEY_NICKNAME, name);
            if (mUserInfo != null) {
                mUserInfo.setNickname(name);
            }
        }
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public boolean isCacheAvailable() {
        return !TextUtils.isEmpty(mAccount);
    }

}

package com.afar.osaio.smart.electrician.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;

/**
 * FamilySpHelper
 *
 * @author Administrator
 * @date 2019/3/6
 */
public class FamilySpHelper {

    private SharedPreferences mPreferences;

    private static final String PREFERENCE_NAME = "Nooie_Home";

    private static final String CURRENT_FAMILY_SUFFIX = "currentHome_";


    public static final String TAG = FamilySpHelper.class.getSimpleName();

    public FamilySpHelper() {
        try {
            mPreferences = NooieApplication.mCtx.getSharedPreferences(
                    PREFERENCE_NAME, Context.MODE_PRIVATE);
        } catch (Exception error) {
            NooieLog.e(error.getMessage());
        }
    }

    public void putCurrentHome(HomeBean homeBean) {
        if (null == homeBean) {
            return;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        String userId = null;
        User user = TuyaHomeSdk.getUserInstance().getUser();
        if (null != user) {
            userId = user.getUid();
        }
        try {
            editor.putString(CURRENT_FAMILY_SUFFIX + userId, JSON.toJSONString(homeBean, SerializerFeature.DisableCircularReferenceDetect));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public HomeBean getCurrentHome() {
        String userId = null;
        User user = TuyaHomeSdk.getUserInstance().getUser();
        if (null != user) {
            userId = user.getUid();
        }

        String currentFamilyStr = mPreferences.getString(CURRENT_FAMILY_SUFFIX + userId, "");
        if (TextUtils.isEmpty(currentFamilyStr)) {
            return null;
        }
        NooieLog.d("--->>> getCurrentHome currentFamilyStr " + currentFamilyStr);
        HomeBean homeBean = null;
        try {
            //homeBean = JSON.parseObject(currentFamilyStr, HomeBean.class);
            homeBean = JSONObject.parseObject(currentFamilyStr, HomeBean.class);
        } catch (Exception e) {
            e.printStackTrace();
            NooieLog.e("--->>> getCurrentHome exception " + e.getMessage());
        }
        return homeBean;

    }


}

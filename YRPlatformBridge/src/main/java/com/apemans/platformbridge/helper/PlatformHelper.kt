package com.apemans.platformbridge.helper

import com.apemans.base.middleservice.YRMiddleServiceManager
import com.apemans.logger.YRLog
import com.apemans.platformbridge.middleservice.YR_MIDDLE_SERVICE_PARAM_ACCOUNT
import com.apemans.platformbridge.middleservice.YR_MIDDLE_SERVICE_PARAM_EXTRA
import com.apemans.platformbridge.middleservice.YR_MIDDLE_SERVICE_PARAM_UID

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 11:04 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
object PlatformHelper {

    fun test(account: String?, uid: String?, extra: String?) {
        val param = mutableMapOf<String, Any?>()
        param[YR_MIDDLE_SERVICE_PARAM_ACCOUNT] = account.orEmpty()
        param[YR_MIDDLE_SERVICE_PARAM_UID] = uid.orEmpty()
        param[YR_MIDDLE_SERVICE_PARAM_EXTRA] = extra.orEmpty()
        YRMiddleServiceManager.requestAsync("yrcx://yripccomponentdevice/transformipcdevice", param) {
            YRLog.d { "-->> debug PlatformHelper test" }
        }
    }

    fun testTuya() {
        //tuya uid 加密
        val parameters1 = mutableMapOf<String, Any>()
        parameters1["uid"] = "dd"
        val encrypetUid = YRMiddleServiceManager.request("yrcx://yrtuya/encryptuid", parameters1)?.data as? String
        // 涂鸦登录接口调用
        val parameters = mutableMapOf<String, Any>()
        parameters["countryCode"] = "86"
        parameters["uid"] = encrypetUid.orEmpty()
        parameters["password"] = "123456"
        YRMiddleServiceManager.requestAsync("yrcx://yrtuya/loginwithuid", parameters) {
            YRLog.d { "-->> debug PlatformHelper test" }
        }
    }
}
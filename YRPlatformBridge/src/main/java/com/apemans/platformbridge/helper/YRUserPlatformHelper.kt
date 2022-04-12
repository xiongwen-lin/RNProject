package com.apemans.platformbridge.helper

import com.apemans.base.middleservice.YRMiddleServiceManager

object YRUserPlatformHelper {

    fun updataUserInfo(hashMap: HashMap<String, Any?>) {
        YRMiddleServiceManager.request("yrcx://yrbusiness/setparamters",hashMap)
    }

    fun registerResponseCodeReceiver() {
        YRMiddleServiceManager.request("yrcx://yrxmine/registerresponsecodereceiver", emptyMap())
    }

    fun unregisterResponseCodeReceiver() {
        YRMiddleServiceManager.request("yrcx://yrxmine/unregisterresponsecodereceiver", emptyMap())
    }
}
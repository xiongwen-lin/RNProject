package com.apemans.platformbridge

import android.app.Application
import android.util.Log
import com.apemans.base.middleservice.*
import com.apemans.base.utils.JsonConvertUtil
import com.apemans.platformbridge.bridge.YROriginBridgeManager
import com.apemans.platformbridge.middleservice.YR_MIDDLE_SERVICE_MODULE_PLATFORM_BRIDGE
import org.json.JSONObject

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/12/9 10:05 上午
 * 说明:
 *
 * 备注:
 *
 */
class YRPlatformBridge : YRMiddleService() {

    override fun registerSelf(application: Application) {
        YRMiddleServiceManager.registerServiceClass(YR_MIDDLE_SERVICE_MODULE_PLATFORM_BRIDGE, YRPlatformBridge::class.java.name, "桥接服务组件");
    }

    override fun requestAsync(protocol: MutableMap<String, String>?, parameters: MutableMap<String, Any>?, listener: YRMiddleServiceListener?) {
        listener?.onCall(errorParametersResponse(""))
    }

    override fun request(protocol: MutableMap<String, String>?, parameters: MutableMap<String, Any>?): YRMiddleServiceResponse<*>? {
        if ("setparamters" == protocol?.get(YRMiddleServiceFunctionName)) {
            Log.d("YRPlatformBridge", "*********************: ${parameters.toString()}")
            YROriginBridgeManager.yrUserBridgeManager?.updataUserInfo(dealMiddleData(parameters))
            return okResponse("")
        } else {
            return errorNoFunctionResponse()
        }
    }

    override fun listening(protocol: MutableMap<String, String>?, lifeCycle: Any?, parameters: MutableMap<String, Any>?, listener: YRMiddleServiceListener?) {
        listener?.onCall(errorNoFunctionResponse())
    }

    private fun dealMiddleData(parameters: MutableMap<String, Any>?) : HashMap<String, Any?> {
        var hashMap = HashMap<String, Any?>()
        val jsonObject = JSONObject(JsonConvertUtil.convertToJson(parameters))
        val iterator = jsonObject.keys()
        var infoString = ""
        while (iterator.hasNext()) {
            infoString = iterator.next()
            hashMap[infoString] = jsonObject[infoString]
        }
        return hashMap
    }


}
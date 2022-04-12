package com.apemans.platformbridge.middleservice.ipc

import com.apemans.platformbridge.middleservice.*
import com.apemans.platformbridge.utils.DataFormatUtil

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/12/9 11:41 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
object YRXIpcMiddleServiceUtil {

    fun parseParamByKey(params: Map<String, Any>?, key: String?) : String? {
        if (params == null || key.isNullOrEmpty()) {
            return ""
        }
        return if (params[key] is String) params[key] as? String else ""
    }

    fun parseParamForExtra(params: Map<String, Any>?) : String? {
        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_EXTRA)
    }

    fun parseParamForUuid(params: Map<String, Any>?) : String? {
        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_UUID)
    }

    fun parseParamForParentDeviceId(params: Map<String, Any>?) : String? {
        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_PARENT_DEVICE_ID)
    }

    fun parseParamForDeviceModel(params: Map<String, Any>?) : String? {
        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_DEVICE_MODEL)
    }

    fun parseParamForDeviceSsid(params: Map<String, Any>?) : String? {
        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_DEVICE_SSID)
    }

    fun parseParamForBleDeviceId(params: Map<String, Any>?) : String? {
        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_BLE_DEVICE_ID)
    }

    fun parseParamForUid(params: Map<String, Any>?) : String? {
        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_UID)
    }

    fun parseParamForAccount(params: Map<String, Any>?) : String? {
        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_ACCOUNT)
    }

    fun parseParamForSeekTime(params: Map<String, Any>?) : Long {
        return DataFormatUtil.parseParamAsLong(params, YR_MIDDLE_SERVICE_PARAM_SEEK_TIME)
    }

    fun parseParamForIsCloud(params: Map<String, Any>?) : Boolean {
        return DataFormatUtil.parseParamAsBoolean(params, YR_MIDDLE_SERVICE_PARAM_IS_CLOUD)
    }

    fun parseParamForLinkType(params: Map<String, Any>?) : String {
        return DataFormatUtil.parseParamAsString(params, YR_MIDDLE_SERVICE_PARAM_LINK_TYPE)
    }

    fun parseParamForState(params: Map<String, Any>?) : Boolean {
        return DataFormatUtil.parseParamAsBoolean(params, YR_MIDDLE_SERVICE_PARAM_STATE)
    }

    fun parseParamForCmd(params: Map<String, Any>?) : String {
        return DataFormatUtil.parseParamAsString(params, YR_MIDDLE_SERVICE_PARAM_CMD)
    }

//    fun parseParamFor(params: Map<String, Any>?) : String? {
//        return parseParamByKey(params, YR_MIDDLE_SERVICE_PARAM_)
//    }
}
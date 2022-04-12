package com.apemans.platformbridge.middleservice.ipc

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import com.apemans.base.middleservice.*
import com.apemans.base.utils.JsonConvertUtil
import com.apemans.platformbridge.bean.YRBindDeviceResult
import com.apemans.platformbridge.bean.YRPlatformDevice
import com.apemans.platformbridge.bridge.YROriginBridgeManager
import com.apemans.platformbridge.listener.IBridgeResultListener
import com.apemans.platformbridge.utils.YRPlatformBridgeUtil
import com.dylanc.longan.topActivity

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/12/9 10:05 上午
 * 说明:
 *
 * 备注:
 *
 */
class YRXIpc : YRMiddleService() {

    override fun registerSelf(application: Application) {
        YRMiddleServiceManager.registerServiceClass(YR_IPC_COMPONENT_MODULE_DEVICE, YRXIpc::class.java.name, "Ipc服务组件");
    }

    override fun requestAsync(protocol: MutableMap<String, String>?, parameters: MutableMap<String, Any>?, listener: YRMiddleServiceListener?) {
        if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_TRANSFORM_IPC_DEVICE, protocol)) {
            var uid = YRXIpcMiddleServiceUtil.parseParamForUid(parameters)
            var account = YRXIpcMiddleServiceUtil.parseParamForAccount(parameters)
            var extra = YRXIpcMiddleServiceUtil.parseParamForExtra(parameters)
            val result = JsonConvertUtil.convertData(extra, YRBindDeviceResult::class.java)
            if (YROriginBridgeManager.bridgeDeviceManager == null) {
                listener?.onCall(errorParametersResponse(""))
                return
            }
            YROriginBridgeManager.bridgeDeviceManager?.queryDeviceList(uid, account, result, object : IBridgeResultListener<List<YRPlatformDevice>> {
                override fun onResult(code: String?, data: List<YRPlatformDevice>?) {
                    if (YRPlatformBridgeUtil.checkResultIsSuccess(code)) {
                        listener?.onCall(okResponse(JsonConvertUtil.convertToJson(data)))
                    } else {
                        listener?.onCall(errorParametersResponse(""))
                    }
                }
            })
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_STOP_NET_SPOT_CONNECTION, protocol)) {
            var deviceModel = YRXIpcMiddleServiceUtil.parseParamForDeviceModel(parameters)
            if (YROriginBridgeManager.bridgeDeviceManager == null) {
                listener?.onCall(errorParametersResponse(""))
                return
            }
            YROriginBridgeManager.bridgeDeviceManager?.stopAPDirectConnection(deviceModel, object : IBridgeResultListener<Any?> {
                override fun onResult(code: String?, data: Any?) {
                    if (YRPlatformBridgeUtil.checkResultIsSuccess(code)) {
                        listener?.onCall(okResponse(true))
                    } else {
                        listener?.onCall(okResponse(false))
                    }
                }
            })
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_ITEM_SWITCH, protocol)) {
            var uuid = YRXIpcMiddleServiceUtil.parseParamForUuid(parameters)
            if (YROriginBridgeManager.bridgeDeviceManager == null || uuid.isNullOrEmpty()) {
                listener?.onCall(errorParametersResponse(""))
            }
            var state = YRXIpcMiddleServiceUtil.parseParamForState(parameters)
            var linkType = YRXIpcMiddleServiceUtil.parseParamForLinkType(parameters)
            var ssid = YRXIpcMiddleServiceUtil.parseParamForDeviceSsid(parameters)
            YROriginBridgeManager.bridgeDeviceManager?.deviceItemSwitch(uuid, linkType, ssid, state, object : IBridgeResultListener<Boolean> {
                override fun onResult(code: String?, data: Boolean?) {
                    listener?.onCall(okResponse(data))
                }
            })
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_ITEM_CLICK, protocol)) {
            var uuid = YRXIpcMiddleServiceUtil.parseParamForUuid(parameters)
            if (YROriginBridgeManager.bridgeDeviceManager == null || uuid.isNullOrEmpty()) {
                listener?.onCall(errorParametersResponse(""))
                return
            }
            var linkType = YRXIpcMiddleServiceUtil.parseParamForLinkType(parameters)
            var model = YRXIpcMiddleServiceUtil.parseParamForDeviceModel(parameters)
            var ssid = YRXIpcMiddleServiceUtil.parseParamForDeviceSsid(parameters)
            var bleDeviceId = YRXIpcMiddleServiceUtil.parseParamForBleDeviceId(parameters)
            YROriginBridgeManager.bridgeDeviceManager?.deviceItemClick(uuid, linkType, model, ssid, bleDeviceId, topActivity, object : IBridgeResultListener<String> {
                override fun onResult(code: String?, data: String?) {
                    listener?.onCall(okResponse(data))
                }
            })
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_SEND_CMD, protocol)) {
            var cmd = YRXIpcMiddleServiceUtil.parseParamForCmd(parameters)
            if (YROriginBridgeManager.bridgeDeviceManager == null || cmd.isNullOrEmpty()) {
                listener?.onCall(errorParametersResponse(""))
                return
            }
            YROriginBridgeManager.bridgeDeviceManager?.sendCmd(cmd, object : IBridgeResultListener<String> {
                override fun onResult(code: String?, data: String?) {
                    listener?.onCall(okResponse(data))
                }
            })
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_QUERY_ALL_DEVICE_INFO, protocol)) {
            var cmd = YRXIpcMiddleServiceUtil.parseParamForCmd(parameters)
            if (YROriginBridgeManager.bridgeDeviceManager == null || cmd.isNullOrEmpty()) {
                listener?.onCall(errorParametersResponse(""))
                return
            }
            YROriginBridgeManager.bridgeDeviceManager?.queryAllIpcDevice(object : IBridgeResultListener<String> {
                override fun onResult(code: String?, data: String?) {
                    listener?.onCall(okResponse(data))
                }
            })
        } else {
            listener?.onCall(errorParametersResponse(""))
        }
    }

    override fun request(protocol: MutableMap<String, String>?, parameters: MutableMap<String, Any>?): YRMiddleServiceResponse<*>? {
        if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_QUERY_NET_SPOT_INFO, protocol)) {
            if (YROriginBridgeManager.bridgeDeviceManager == null) {
                return errorParametersResponse("")
            }
            var result = YROriginBridgeManager.bridgeDeviceManager?.queryNetSpotDevice()
            return okResponse(JsonConvertUtil.convertToJson(result))
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_REFRESH_NET_SPOT_CONNECTION, protocol)) {
            if (YROriginBridgeManager.bridgeDeviceManager == null) {
                return errorParametersResponse("")
            }
            YROriginBridgeManager.bridgeDeviceManager?.refreshNetSpotConnection()
            return okResponse(true)
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_CHECK_IS_NET_SPOT, protocol)) {
            if (YROriginBridgeManager.bridgeDeviceManager == null) {
                return errorParametersResponse("")
            }
            YROriginBridgeManager.bridgeDeviceManager?.checkIsNetSpot()
            return okResponse(true)
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_OPEN_ADD_DEVICE, protocol)) {
            if (YROriginBridgeManager.bridgeDeviceManager == null) {
                return errorParametersResponse("")
            }
            YROriginBridgeManager.bridgeDeviceManager?.openAddDevicePage()
            return okResponse(true)
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_OPEN_LIVE, protocol)) {
            var uuid = YRXIpcMiddleServiceUtil.parseParamForUuid(parameters)
            if (YROriginBridgeManager.bridgeDeviceManager == null || uuid.isNullOrEmpty()) {
                return errorParametersResponse("")
            }
            YROriginBridgeManager.bridgeDeviceManager?.openLiveAsSingle(uuid)
            return okResponse(true)
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_OPEN_PLAYBACK, protocol)) {
            var uuid = YRXIpcMiddleServiceUtil.parseParamForUuid(parameters)
            if (YROriginBridgeManager.bridgeDeviceManager == null || uuid.isNullOrEmpty()) {
                return errorParametersResponse("")
            }
            var seekTime = YRXIpcMiddleServiceUtil.parseParamForSeekTime(parameters)
            var isCloud = YRXIpcMiddleServiceUtil.parseParamForIsCloud(parameters)
            YROriginBridgeManager.bridgeDeviceManager?.openPlaybackAsSingle(uuid, seekTime, isCloud)
            return okResponse(true)
        } else if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_OPEN_SENSITIVITY, protocol)) {
            var uuid = YRXIpcMiddleServiceUtil.parseParamForUuid(parameters)
            if (YROriginBridgeManager.bridgeDeviceManager == null || uuid.isNullOrEmpty()) {
                return errorParametersResponse("")
            }
            YROriginBridgeManager.bridgeDeviceManager?.openSensitivityPage(uuid)
            return okResponse(true)
        } else {
            return errorNoFunctionResponse()
        }
    }

    override fun listening(protocol: MutableMap<String, String>?, lifeCycle: Any?, parameters: MutableMap<String, Any>?, listener: YRMiddleServiceListener?) {
        if (YRMiddleServiceUtil.checkServiceFunctionNameAssignable(YR_IPC_COMPONENT_FUNCTION_OBSERVE_NET_SPOT_CONNECTION_STATE, protocol)) {
            val lifecycleOwner = lifeCycle as? LifecycleOwner
            if (lifecycleOwner == null || YROriginBridgeManager.bridgeDeviceManager == null) {
                listener?.onCall(errorParametersResponse(""))
                return
            }
            YROriginBridgeManager.bridgeDeviceManager?.netSpotConnectionState?.observe(lifecycleOwner) { state ->
                listener?.onCall(okResponse(state))
            }
        } else {
            val lifecycleOwner = lifeCycle as? LifecycleOwner
            var lfc = lifecycleOwner?.lifecycle
            if (lfc == null) {
                listener?.onCall(errorNoFunctionResponse())
                return
            }
        }
        topActivity
    }

}
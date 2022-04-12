package com.apemans.platformbridge.constant

import com.apemans.platformbridge.middleservice.ipc.YR_IPC_COMPONENT_FUNCTION_ITEM_CLICK
import com.apemans.platformbridge.middleservice.ipc.YR_IPC_COMPONENT_FUNCTION_ITEM_SWITCH
import com.apemans.platformbridge.middleservice.ipc.YR_IPC_COMPONENT_MODULE_DEVICE

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 11:53 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
object BridgeConstant {

    const val RESULT_SUCCESS = "SUCCESS"
    const val RESULT_ERROR = "ERROR"

    const val YR_PLATFORM_KEY_EVENT_ID = "id"
    const val YR_PLATFORM_KEY_EVENT_URL = "url"
    const val YR_PLATFORM_KEY_EVENT_PARAM = "param"
    const val YR_PLATFORM_KEY_EVENT_VALUE = "value"

    const val YR_PLATFORM_KEY_UUID = "uuid"
    const val YR_PLATFORM_KEY_STATE = "state"
    const val YR_PLATFORM_KEY_MODEL = "model"
    const val YR_PLATFORM_KEY_SSID = "ssid"
    const val YR_PLATFORM_KEY_BLE_DEVICE_ID = "bledeviceid"
    const val YR_PLATFORM_KEY_DEVICE_LINK_TYPE = "linkType"
    const val YR_PLATFORM_KEY_CLOUD_STATE = "cloud_state"

    const val YR_PLATFORM_OF_YRCX = "yrcx"
    const val YR_PLATFORM_CATEGORY_OF_IPC = "ipc"

    const val YR_PLATFORM_EVENT_ID_SWITCH = "switch"
    const val YR_PLATFORM_EVENT_ID_CLICK = "click"

    const val YR_PLATFORM_EVENT_CLICK_FOR_IPC = "yrcx://".plus(YR_IPC_COMPONENT_MODULE_DEVICE).plus("/").plus(YR_IPC_COMPONENT_FUNCTION_ITEM_CLICK)
    const val YR_PLATFORM_EVENT_SWITCH_FOR_IPC = "yrcx://".plus(YR_IPC_COMPONENT_MODULE_DEVICE).plus("/").plus(YR_IPC_COMPONENT_FUNCTION_ITEM_SWITCH)

    const val DEVICE_BIND_TYPE_OWNER = "OWNER"
    const val DEVICE_BIND_TYPE_SHARER = "SHARER"

    /**
     * Ipc设备命令参数字段
     * 请求命令格式
     * {"uuid": "xxx", "cmd_action": "", ...}
     * 响应命令格式
     * {"uuid": "xxx", "cmd_action": "", "cmd_code": "", ...}
     */
    const val YR_IPC_CMD_KEY_UUID = "uuid"
    const val YR_IPC_CMD_KEY_PROTOCOL = "protocol"
    const val YR_IPC_CMD_KEY_CMD_ACTION = "cmd_action"
    const val YR_IPC_CMD_KEY_CMD_ID = "cmd_id"

    const val YR_IPC_CMD_KEY_CMD_CODE = "cmd_code"
    const val YR_IPC_CMD_KEY_TOTAL = "total"
    const val YR_IPC_CMD_KEY_FREE = "free"
    const val YR_IPC_CMD_KEY_PROCESS = "process"
    const val YR_IPC_CMD_KEY_STATUS = "status"

    /**
     * YR_IPC_CMD_ACTION_STORAGE_INFO 获取卡存储信息
     */
    const val YR_IPC_CMD_ACTION_STORAGE_INFO = "storage_info"

    /**
     * 命令响应状态
     * success 命令成功
     * cache 命令缓存
     * error 命令失败
     */
    const val YR_IPC_CMD_KEY_CMD_CODE_SUCCESS = "success"
    const val YR_IPC_CMD_KEY_CMD_CODE_CACHE = "cache"
    const val YR_IPC_CMD_KEY_CMD_CODE_ERROR = "error"

}
package com.apemans.platformbridge.middleservice.ipc

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/12/9 10:22 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/

/** Ipc模块的模块名称 */
const val YR_IPC_COMPONENT_MODULE_DEVICE = "yrxipc"

/** Ipc模块方法名称-转换Ipc设备数据 */
const val YR_IPC_COMPONENT_FUNCTION_TRANSFORM_IPC_DEVICE = "transformipcdevice"

/** Ipc模块方法名称-获取当前直连是否存在 */
const val YR_IPC_COMPONENT_FUNCTION_CHECK_IS_NET_SPOT = "checkisnetspot"
/** Ipc模块方法名称-获取当前直连信息 */
const val YR_IPC_COMPONENT_FUNCTION_QUERY_NET_SPOT_INFO = "querynetspotinfo"
/** Ipc模块方法名称-自检当前直连状态 */
const val YR_IPC_COMPONENT_FUNCTION_REFRESH_NET_SPOT_CONNECTION = "refreshnetspotconnection"
/** Ipc模块方法名称-断开当前直连 */
const val YR_IPC_COMPONENT_FUNCTION_STOP_NET_SPOT_CONNECTION = "stopnetspotconnection"
/** Ipc模块方法名称-监听直连状态 */
const val YR_IPC_COMPONENT_FUNCTION_OBSERVE_NET_SPOT_CONNECTION_STATE = "observenetspotconnectionstate"

/** Ipc模块方法名称-依次当前全部Ipc设备信息，该接口快速动态获取设备信息 */
const val YR_IPC_COMPONENT_FUNCTION_QUERY_ALL_DEVICE_INFO = "queryalldeviceinfo"

/**
 * Ipc模块方法名称-发送Ipc设备命令
 * 参数param
 * {"cmd": ""}
 */
const val YR_IPC_COMPONENT_FUNCTION_SEND_CMD = "sendcmd"

/** Ipc模块方法名称-打开设备配网入口 */
const val YR_IPC_COMPONENT_FUNCTION_OPEN_ADD_DEVICE = "openadddevice"
/** Ipc模块方法名称-打开设备直播入口 */
const val YR_IPC_COMPONENT_FUNCTION_OPEN_LIVE = "openlive"
/** Ipc模块方法名称-打开设备回放入口 */
const val YR_IPC_COMPONENT_FUNCTION_OPEN_PLAYBACK = "openplayback"
/** Ipc模块方法名称-打开设备侦测设置入口 */
const val YR_IPC_COMPONENT_FUNCTION_OPEN_SENSITIVITY = "opensensitiviy"
/** Ipc模块方法名称-首页设备点击事件 */
const val YR_IPC_COMPONENT_FUNCTION_ITEM_CLICK = "click"
/** Ipc模块方法名称-首页设备开关事件 */
const val YR_IPC_COMPONENT_FUNCTION_ITEM_SWITCH = "switch"
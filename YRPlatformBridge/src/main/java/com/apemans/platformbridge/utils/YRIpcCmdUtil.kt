package com.apemans.platformbridge.utils

import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_CMD_ACTION
import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_CMD_CODE
import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_CMD_ID
import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_FREE
import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_PROCESS
import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_PROTOCOL
import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_STATUS
import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_TOTAL
import com.apemans.platformbridge.constant.BridgeConstant.YR_IPC_CMD_KEY_UUID

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/12/9 11:41 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
object YRIpcCmdUtil {

    fun parseParamForUuid(params: Map<String, Any>?) : String {
        return DataFormatUtil.parseParamAsString(params, YR_IPC_CMD_KEY_UUID)
    }

    fun parseParamForProtocol(params: Map<String, Any>?) : String {
        return DataFormatUtil.parseParamAsString(params, YR_IPC_CMD_KEY_PROTOCOL)
    }

    fun parseParamForCmdAction(params: Map<String, Any>?) : String {
        return DataFormatUtil.parseParamAsString(params, YR_IPC_CMD_KEY_CMD_ACTION)
    }

    fun parseParamForCmdId(params: Map<String, Any>?) : String {
        return DataFormatUtil.parseParamAsString(params, YR_IPC_CMD_KEY_CMD_ID)
    }

    fun parseParamForCmdCode(params: Map<String, Any>?) : String {
        return DataFormatUtil.parseParamAsString(params, YR_IPC_CMD_KEY_CMD_CODE)
    }

    fun parseParamForTotal(params: Map<String, Any>?) : Int {
        return DataFormatUtil.parseParamAsInt(params, YR_IPC_CMD_KEY_TOTAL)
    }

    fun parseParamForFree(params: Map<String, Any>?) : Int {
        return DataFormatUtil.parseParamAsInt(params, YR_IPC_CMD_KEY_FREE)
    }

    fun parseParamForProcess(params: Map<String, Any>?) : Int {
        return DataFormatUtil.parseParamAsInt(params, YR_IPC_CMD_KEY_PROCESS)
    }

    fun parseParamForStatus(params: Map<String, Any>?) : Int {
        return DataFormatUtil.parseParamAsInt(params, YR_IPC_CMD_KEY_STATUS)
    }

    fun checkCmdActionValid(cmdAction: String?, targetCmdAction: String?) : Boolean {
        return !cmdAction.isNullOrEmpty() && !targetCmdAction.isNullOrEmpty() && cmdAction.equals(targetCmdAction, true)
    }

}
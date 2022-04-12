package com.apemans.platformbridge.utils

import com.apemans.platformbridge.constant.BridgeConstant

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 4:21 下午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
object YRPlatformBridgeUtil {

    fun checkResultIsSuccess(result: String?) : Boolean {
        return result == BridgeConstant.RESULT_SUCCESS
    }
}
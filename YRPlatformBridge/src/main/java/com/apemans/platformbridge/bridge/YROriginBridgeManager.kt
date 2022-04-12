package com.apemans.platformbridge.bridge

import com.apemans.platformbridge.bridge.contract.IBridgeDeviceHelper
import com.apemans.platformbridge.bridge.contract.YRIUserInfoHelper

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 10:37 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
object YROriginBridgeManager {

    var bridgeDeviceManager: IBridgeDeviceHelper? = null

    var yrUserBridgeManager : YRIUserInfoHelper? = null

}
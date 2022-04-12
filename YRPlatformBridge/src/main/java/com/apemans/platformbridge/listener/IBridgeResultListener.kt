package com.apemans.platformbridge.listener

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 10:11 上午
 * 说明:
 *
 * 备注:
 *
 */
interface IBridgeResultListener<T> {

    fun onResult(code: String?, data: T?)

}
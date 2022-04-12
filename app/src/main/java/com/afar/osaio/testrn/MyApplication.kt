package com.afar.osaio.testrn

import android.app.Application
import com.apemans.base.middleservice.YRMiddleServiceManager
import com.apemans.logger.YRLogManager
import java.io.File

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initYRLoggerSDK()
        YRMiddleServiceManager.registerAllService(this, getExcludeRegisterPackageList())
    }

    private fun getExcludeRegisterPackageList() : List<String> {
        val list = mutableListOf<String>()
        list.add("anet.channel")
        list.add("com.bumptech")
        list.add("com.facebook")
        list.add("com.swmansion")
        list.add("com.tuya")
        list.add("org.apache")
        list.add("com.taobao")
        list.add("com.ut")
        list.add("demo.")
        list.add("org.conscrypt")
        list.add("org.repackage")
        list.add("okhttp3.internal")
        list.add("com.apemans.yrrnsafeareacontext")
        list.add("com.apemans.yrrnscreens")
        list.add("net.time4j")
        list.add("com.apemans.rn.utils")
        list.add("com.apemans.business.apisdk.client.configure")
        list.add("com.apemans.yrcxsdk.data")
        list.add("com.apemans.custom.webapi.CustomApiHelper")
        list.add("org.greenrobot.greendao")
        list.add("com.apemans.usercomponent.userapi.UserApiHelper")
        list.add("com.apemans.xmessage.webapi.MessageApiHelper")

        list.add("com.apemans.rnscreens")
        return list
    }

    /**
     * 初始化Logger
     * 1、调用YRApiManager.init()接口，初始化配置，具体配置参数如下。
     */
    private fun initYRLoggerSDK() {
        /**
         * 初始化日志设置
         * LoganConfig 配置参数
         * cachePath mmap缓存路径
         * logFilePath file文件路径
         * maxFileSize 删除文件最大值
         * day 删除天数
         * encryptKey 128位ase加密Key
         * encryptIv 128位aes加密IV
         * debug 是否为debug模式, true 表示使用调试模式，日志输出到控制台。false表示关闭调试模式，日志输出到文件中
         */
        var cachePath = getFilesDir().getAbsolutePath()
        var logFilePath =
            getExternalFilesDir(null)!!.getAbsolutePath() + File.separator + "yrlog_v1";
        var maxFileSize = 10 * 1024 * 1024L
        YRLogManager.init(true) {
            it.setCachePath(cachePath)
                .setPath(logFilePath)
                .setMaxFile(maxFileSize)
                .setDay(10)
                .setEncryptKey16("0123456789012345".encodeToByteArray())
                .setEncryptIV16("0123456789012345".encodeToByteArray())
        }
    }
}
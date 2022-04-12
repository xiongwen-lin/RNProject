package com.apemans.platformbridge.helper

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.apemans.base.middleservice.YRMiddleServiceManager
import com.apemans.business.apisdk.ApiManager
import com.apemans.business.apisdk.client.define.*
import com.apemans.logger.YRLog
import com.apemans.logger.YRLogManager
import com.apemans.messagepush.push.MessagePushManager
import com.apemans.messagepush.push.base.MessagePushConstant
import com.apemans.platformbridge.R
import com.apemans.quickui.label.GlobalLabelTextConfigure
import com.apemans.yrcxsdk.data.YRCXSDKDataManager
import java.io.File
import java.lang.Exception

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/8 10:32 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
object PlatformInitHelper {

    fun startPlatformInit(context: Application) {
        initYRLogger(context)
    }

    fun startPlatformInitApiSdk(context: Application, baseUrl: String) {
        initApiSdk(baseUrl)
        startPlatformInitMiddleService(context)
        updateApiSdkParamForInit()
        var appId: String = ""
        var appSecret: String = ""
        try {
            val appInfo: ApplicationInfo = context.getApplicationContext().getPackageManager()
                .getApplicationInfo(context.getApplicationContext().packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.let {
                appId = it.getString("com.nooie.sdk.appid").orEmpty()
                appSecret = it.getString("com.nooie.sdk.secret").orEmpty()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateApiSdkParam(appId = appId, appSecret = appSecret, baseUrl = "https://global.osaio.net/v2", uid = "b02343fa4bfe870e", token = "eyJhbGciOiJoczI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NDYzNzgxNDcsInVpZCI6ImIwMjM0M2ZhNGJmZTg3MGUiLCJyZWciOiJjbiIsInNjaCI6IjNkYWI5OGVlZTg1YjdhZTgiLCJ0X2lkIjoib21nZndhZGx2dXdzbmd4bmNteHJlcWRpYmpjZWxweGwiLCJyZWZfdG9rIjoiYzE1ZmY5ODMtOTYwYS0xMWVjLThhMjctYTI3MzYwNjg1OTdiIiwicmVmX2V4cCI6MTY0Njk4Mjk0N30=.MzYwNjgxMzY0M2MzNDI5MGFhYmE2YjE2MTE5YWMyMmQzMzIwMGI4NjBjYTY3NTU0MDIwODk1NzkwNTZiNzM5MQ==",
            webUrl = "https://app.osaio.nooie.cn/v2", s3Url = "https://app.osaio.nooie.cn/v3/cloud", region = "cn")
        initLabelTextThemeConfigure()
    }

    fun updateApiSdkParamForInit() {
        YRCXSDKDataManager.initDataListener()

        var envParam = mutableMapOf<String, Any>()

        envParam[KEY_ENV_SERVER_TIME] = System.currentTimeMillis() / 1000L

        val extensionHeaders = mutableMapOf<String, String>()
        extensionHeaders["User-Agent"] = "OSAIO_ANDROID_1.0.0"
        envParam[KEY_ENV_EXTENSION_HEADERS] = extensionHeaders

        envParam[KEY_ENV_GLOBAL_SHORT_URL] = listOf("global/time", "user/tuya", "account/country", "account/baseurl", "user/account")
        envParam[KEY_ENV_BEFORE_LOGIN_SHORT_URL] = listOf("global/time", "user/tuya", "account/country", "account/baseurl", "user/account", "login/login", "login/send", "register/send", "register/verify", "login/verify", "tuya/reset", "register/register")
        envParam[KEY_ENV_S3_SHORT_URL] = listOf("feedbackput_presignurl", "photoget_presignurl", "app/fetch/file_event")

        val responseCodeMap = mutableMapOf<String, List<String>>()
        responseCodeMap[RESPONSE_CODE_UPDATE_SERVER_TIME] = listOf(HttpCode.CODE_1004.toString())
        responseCodeMap[RESPONSE_CODE_LOGIN_EXPIRE] = listOf(HttpCode.CODE_1006.toString(), HttpCode.CODE_1059.toString())
        responseCodeMap[RESPONSE_CODE_LOGIN_BY_OTHER] = listOf(HttpCode.CODE_1056.toString())
        responseCodeMap[RESPONSE_CODE_ACCOUNT_MOVED] = listOf(HttpCode.CODE_1909.toString())

        envParam[KEY_ENV_RESPONSE_ERROR_CODE] = responseCodeMap

        YRMiddleServiceManager.request("yrcx://yrbusiness/setparamters", envParam)
    }

    fun updateApiSdkParam(appId: String? = "", appSecret: String? = "", baseUrl: String? = "", uid: String? = "", token: String? = "", webUrl: String? = "", s3Url: String? = "", region: String? = "") {
        var envParam = mutableMapOf<String, Any>()

        if (!appId.isNullOrEmpty() && !appSecret.isNullOrEmpty() && !baseUrl.isNullOrEmpty()) {
            envParam[KEY_ENV_APP_ID] = appId
            envParam[KEY_ENV_APP_SECRET] = appSecret
            envParam[KEY_ENV_BASE_URL] = baseUrl
        }

        if (!uid.isNullOrEmpty() && !token.isNullOrEmpty()) {
            val tokenUidParams = mutableMapOf<String, Any>()
            tokenUidParams[KEY_ENV_UID] = uid
            tokenUidParams[KEY_ENV_API_TOKEN] = token
            envParam[KEY_ENV_TOKEN_UID_PARAMS] = tokenUidParams
        }

        if (!webUrl.isNullOrEmpty() && !s3Url.isNullOrEmpty()) {
            envParam[KEY_ENV_WEB_URL] = webUrl
            envParam[KEY_ENV_S3_URL] = s3Url
            envParam[KEY_ENV_REGION] = if (region.isNullOrEmpty()) "us" else region
        }

        YRMiddleServiceManager.request("yrcx://yrbusiness/setparamters", envParam)
    }

    fun updateApiSdkParam(serviceTime: Long) {
        var envParam = mutableMapOf<String, Any>()
        envParam[KEY_ENV_SERVER_TIME] = serviceTime
        YRMiddleServiceManager.request("yrcx://yrbusiness/setparamters", envParam)
    }

    fun updateYRCXSDKData(userAccount: String? = "", userNickname: String? = "", userPassword: String? = "", userCountryCode: String? = "") {
        YRLog.d { "-->> debug PlatformInitHelper updateYRCXSDKData userAccount $userAccount userNickname $userNickname userPassword $userPassword userCountryCode $userCountryCode" }
        if (!userAccount.isNullOrEmpty()) {
            YRCXSDKDataManager.userAccount = userAccount
        }
        if (!userNickname.isNullOrEmpty()) {
            YRCXSDKDataManager.userNickname = userNickname
        }
        if (!userPassword.isNullOrEmpty()) {
            YRCXSDKDataManager.userPassword = userPassword
        }
        if (!userCountryCode.isNullOrEmpty()) {
            YRCXSDKDataManager.userCountryCode = userCountryCode
        }
    }

    fun startPlatformInitMiddleService(application: Application) {
        YRMiddleServiceManager.registerAllService(application, getExcludeRegisterPackageList())
    }

    /**
     * 初始化Logger
     * 1、调用YRApiManager.init()接口，初始化配置，具体配置参数如下。
     */
    private fun initYRLogger(context: Context) {
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
        var cachePath = context.getFilesDir().getAbsolutePath()
        var logFilePath =
            context.getExternalFilesDir(null)!!.getAbsolutePath() + File.separator + "osaio";
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

    /**
     * 初始化ApiSdk
     * 1、ApiManager.init()接口，初始化appId、appSecret、baseurl（获取全球服务器地址的baseUrl，如示例teckin的https://global.teckinhome.com/v2/）
     * 2、ApiManager.registerGlobalObserver()接口，注册全局监听，用于观察接口请求异常通知，并针对不同的情况进行处理。
     * 3、确保数据共享库的uid、token、全球地址有效，接口请求和接口的签名需要用到
     * 4、对应请求接口请调用YRApi中开发的接口，如YRApi.getGlobalApi()代表和全球服务地址相关的接口
     * 5、分别获取全球时间、获取全球服务器地址、登陆成功后，同步相关信息到NetConfigure网络配置；具体示例如下面的updateNetConfigure方法
     */
    private fun initApiSdk(baseUrl: String) {
        ApiManager.init(baseUrl)
    }

    /**
     * 初始化messagePushComponent模块
     * 1、如果支持友盟推送，设置友盟推送相关配置
     */
    private fun initMessagePush(context: Application) {
        /**
         * context
         * param 配置参数，如下
         * MessagePushConstant.PARAM_KEY_UM_APP_KEY - 友盟app key
         * MessagePushConstant.PARAM_KEY_UM_MESSAGE_SECRET - 友盟message secret
         * MessagePushConstant.PARAM_KEY_UM_LOG_ENABLE - 友盟日志调试打印，可选且默认关闭
         */
        /*
        val param = mutableMapOf<String, Any>();
        param[MessagePushConstant.PARAM_KEY_UM_APP_KEY] = "61237f7fe0080c5063767777";
        param[MessagePushConstant.PARAM_KEY_UM_MESSAGE_SECRET] = "2a39e8b12b59ef13e5920a91a51e44ba";
        param[MessagePushConstant.PARAM_KEY_UM_LOG_ENABLE] = true;
        MessagePushManager.init(context, param)

         */
    }

    private fun initThemeConfigure() {
        /*
        GlobalLabelTextConfigure.apply {
            textColor = R.color.theme_text_color
            selectionOnColor = R.color.theme_color
            selectionOffColor = R.color.theme_sub_text_color
        }

         */
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
        list.add("demo.")
        list.add("org.repackage")
        list.add("okhttp3.internal")
        list.add("org.greenrobot.greendao")
        return list
    }

    private fun initLabelTextThemeConfigure() {
        GlobalLabelTextConfigure.apply {
            textColor = R.color.theme_text_color
            subTextColor = R.color.theme_sub_text_color
            detailTextColor =  R.color.theme_sub_text_color
            selectionOnColor = R.color.theme_color
            selectionOffColor = R.color.theme_sub_text_color
        }
    }
}
package com.apemans.platformbridge.bean

class YRPlatformDevice {

    var uuid: String? = ""
    var name: String? = ""
    var online: Boolean = false
    var version: String? = ""
    var iconUrl: String? = ""
    var productId: String? = ""
    var platform: String? = ""
    var category: String? = ""
    var extra: Map<String, Any?>? = null
    var eventSchema: Map<String, Map<String, Any?>>? = null
}
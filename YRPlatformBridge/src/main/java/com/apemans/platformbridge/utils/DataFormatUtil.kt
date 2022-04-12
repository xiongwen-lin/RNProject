package com.apemans.platformbridge.utils

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/12/9 11:41 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
object DataFormatUtil {

    fun parseInt(value: String?) : Int {
        return value?.toIntOrNull() ?: 0
    }

    fun parseLong(value: String?) : Long {
        return value?.toLongOrNull() ?: 0L
    }

    fun parseFloat(value: String?) : Float {
        return value?.toFloatOrNull() ?: 0f
    }

    fun parseDouble(value: String?) : Double {
        return value?.toDouble() ?: 0.0
    }

    fun parseBoolean(value: String?) : Boolean {
        return value.toBoolean()
    }

    fun parseParamAsInt(params: Map<String, Any>?, key: String?) : Int {
        return parseParamByKey<Int>(params, key) ?: 0
    }

    fun parseParamAsLong(params: Map<String, Any>?, key: String?) : Long {
        return parseParamByKey<Long>(params, key) ?: 0L
    }

    fun parseParamAsFloat(params: Map<String, Any>?, key: String?) : Float {
        return parseParamByKey<Float>(params, key) ?: 0f
    }

    fun parseParamAsDouble(params: Map<String, Any>?, key: String?) : Double {
        return parseParamByKey<Double>(params, key) ?: 0.0
    }

    fun parseParamAsBoolean(params: Map<String, Any>?, key: String?) : Boolean {
        return parseParamByKey<Boolean>(params, key) ?: false
    }

    fun parseParamAsString(params: Map<String, Any>? = null, key: String? = null) : String {
        return parseParamByKey<String>(params, key) ?: ""
    }

    fun <T> parseParamAsMap(params: Map<String, Any>?, key: String?) : Map<String, T> {
        return parseParamByKey<Map<String, T>>(params, key) ?: emptyMap<String, T>()
    }

    fun <T> parseParamAsList(params: Map<String, Any>?, key: String?) : List<T> {
        return parseParamByKey<List<T>>(params, key) ?: emptyList<T>()
    }

    inline fun <reified T> parseParamByKey(params: Map<String, Any>?, key: String?) : T? {
        if (params == null || key.isNullOrEmpty()) {
            return null
        }
        try {
            return if (params[key] is T) params[key] as? T else null
        } catch (e: Exception) {
        }
        return null
    }

}
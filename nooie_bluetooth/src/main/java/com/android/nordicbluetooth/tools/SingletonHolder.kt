package com.android.nordicbluetooth.tools

/***********************************************************
 * @Author : caro
 * @Date   : 2/20/21
 * @Func:
 *
 *
 * @Description:
 *
 *
 ***********************************************************/
/**
 *创建时间：2019/11/29
 *编写人：kanghb
 *功能描述：Kotlin中的带参单例模式
 *参考：https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e
 */
open class SingletonHolder<out T : Any, in A>(private val creator: (A) -> T) {

    private var instance: T? = null

    fun getInstance(arg: A): T =
        instance ?: synchronized(this) {
            instance ?: creator(arg).apply {
                instance = this
            }
        }
}
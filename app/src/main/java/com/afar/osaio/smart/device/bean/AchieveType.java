package com.afar.osaio.smart.device.bean;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public enum AchieveType {
    /**
     * 只从缓存获取
     */
    CACHE_ONLY,

    /**
     * 只从服务器获取
     */
    SERVER_ONLY,

    /**
     * 优先缓存获取
     */
    CACHE_FIRST,

    /**
     * 优先服务器获取
     */
    SERVER_FIRST,

    /**
     * 先缓存后服务器
     */
    DEFAULT
}

package com.afar.osaio.message.model;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public enum RefreshType {
    SET_DATA, // 给Adapter设置数据源
    PULL_TO_REFRESH, // 下拉刷新，从头部插入数据
    LOAD_MORE // 上拉加载更多，从尾部插入数据
}

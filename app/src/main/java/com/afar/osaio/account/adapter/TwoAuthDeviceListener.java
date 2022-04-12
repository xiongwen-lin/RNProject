package com.afar.osaio.account.adapter;

import com.nooie.sdk.api.network.base.bean.entity.TwoAuthDevice;

/**
 * TwoAuthDeviceListener
 *
 * @author Administrator
 * @date 2020/10/12
 */
public interface TwoAuthDeviceListener {

    void onItemClick(TwoAuthDevice device);
}
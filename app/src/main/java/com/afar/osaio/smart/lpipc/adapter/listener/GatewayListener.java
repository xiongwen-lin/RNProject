package com.afar.osaio.smart.lpipc.adapter.listener;

import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;

public interface GatewayListener {

    void onGatewayItemClick(GatewayDevice deviceResult);

    void onGatewaySubDeviceClick(BindDevice device);
}

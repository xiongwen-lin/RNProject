package com.afar.osaio.smart.home.adapter.listener;

import com.afar.osaio.bean.ProductType;

public interface DeviceListListener {

    void onItemMoreClick(ProductType productType);

    void onAddDevice();
}

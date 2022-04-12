package com.afar.osaio.smart.scan.adapter.listener;

import com.afar.osaio.adapter.listener.BaseListener;
import com.afar.osaio.bean.SelectDeviceBean;
import com.afar.osaio.bean.SelectProduct;

public interface ProductSelectListListener extends BaseListener<SelectProduct> {

    void onModelItemClick(SelectDeviceBean selectDevice);
}

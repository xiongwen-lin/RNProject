package com.afar.osaio.bean;

import java.util.List;

public class DeviceItem<T> {

    private ProductType mProductType;
    private List<T> mDatas;

    public void setProductType(ProductType productType) {
        mProductType = productType;
    }

    public ProductType getProductType() {
        return mProductType;
    }

    public void setDatas(List<T> datas) {
        mDatas = datas;
    }

    public List<T> getDatas() {
        return mDatas;
    }
}

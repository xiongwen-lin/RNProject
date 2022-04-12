package com.afar.osaio.smart.media.bean;

import com.nooie.sdk.device.bean.ImgItem;

import java.util.List;

public class CameraPhotoResult {

    private List<ImgItem> imgItemList;
    private List<CameraPhotoItemBean> photoItemList;

    public List<ImgItem> getImgItemList() {
        return imgItemList;
    }

    public void setImgItemList(List<ImgItem> imgItemList) {
        this.imgItemList = imgItemList;
    }

    public List<CameraPhotoItemBean> getPhotoItemList() {
        return photoItemList;
    }

    public void setPhotoItemList(List<CameraPhotoItemBean> photoItemList) {
        this.photoItemList = photoItemList;
    }
}

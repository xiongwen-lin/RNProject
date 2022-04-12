package com.afar.osaio.smart.media.listener;

import com.afar.osaio.smart.media.bean.BaseCameraMediaBean;

public interface CameraPhotoListener {
    void onItemClick(BaseCameraMediaBean mediaBean);

    void onRetryClick();
}

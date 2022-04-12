package com.afar.osaio.smart.media.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.media.bean.CameraPhotoItemBean;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.ImgItem;

import java.util.List;

public interface PhotoMediaContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onGetFormatInfo(int state, FormatInfo formatInfo, boolean isCache, int dateIndex);

        void onGetSDCardRecDay(int state, int[] recDayList);

        void onLoadStorageImageList(int state, List<ImgItem> imageList);

        void onGetCameraPhotoList(int state, List<CameraPhotoItemBean> photoItemBeanList, boolean isRefresh);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void getFormatInfo(String deviceId, boolean isInitDate);

        void getSDCardRecDay(String deviceId);

        void loadStorageImageList(String deviceId, boolean isRefresh, int start);

        void getCameraPhotoList(boolean isRefresh);
    }
}

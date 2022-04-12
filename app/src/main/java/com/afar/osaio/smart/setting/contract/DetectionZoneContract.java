package com.afar.osaio.smart.setting.contract;

import android.graphics.RectF;
import androidx.annotation.NonNull;

import com.nooie.sdk.device.bean.MTAreaInfo;

public interface DetectionZoneContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onUpdateMtAreaInfo(String result, int type, MTAreaInfo info);

        void onSetMtAreaInfo(String result);

        void onGetMtAreaInfo(String result, MTAreaInfo info);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void updateMtAreaInfo(String deviceId, int type, RectF selectZoneRectF);

        void updateMtAreaInfo(String deviceId, int type, boolean state);

        void setMtAreaInfo(String deviceId, MTAreaInfo mtAreaInfo);

        void getMtAreaInfo(String deviceId);
    }
}

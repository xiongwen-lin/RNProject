package com.afar.osaio.smart.media.contract;

import androidx.annotation.NonNull;

public interface CameraPhotoDetailContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onSaveCameraPhoto(int state, String path);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void updateFileToMediaStore(String account, String deviceId, String path, String mediaType);
    }
}

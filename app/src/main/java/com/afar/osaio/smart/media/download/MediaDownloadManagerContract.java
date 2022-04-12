package com.afar.osaio.smart.media.download;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.media.bean.MediaDownloadBean;

import java.util.List;

public interface MediaDownloadManagerContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onMediaDownloadResult(int state, MediaDownloadBean downloadBean);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void startDownloadMediaFileTask(List<MediaDownloadBean> mediaDownloadBeanList);
    }

    interface MediaDownloadManagerListener {

        void onMediaDownloadResult(int state, MediaDownloadBean downloadBean);
    }
}

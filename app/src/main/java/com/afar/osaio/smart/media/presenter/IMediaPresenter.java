package com.afar.osaio.smart.media.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.afar.osaio.smart.media.bean.BaseMediaBean;

/**
 * IMediaPresenter
 *
 * @author Administrator
 * @date 2019/10/5
 */
public interface IMediaPresenter extends IBasePresenter {

    void detachView();

    void loadAlbum(String account, String deviceId, BaseMediaBean.TYPE type);

    void removeMedia(String account, String deviceId, BaseMediaBean.TYPE type);

    void removeMediaByPath(String account, String deviceId, BaseMediaBean.TYPE type, String path);

    void resetMedia(String account, String deviceId, BaseMediaBean.TYPE type, boolean isSelected);

    void getSelectedMedia(BaseMediaBean.TYPE type);
}

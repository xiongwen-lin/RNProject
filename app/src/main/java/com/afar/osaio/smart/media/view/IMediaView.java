package com.afar.osaio.smart.media.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.MediaItemBean;

import java.util.List;

/**
 * IMediaView
 *
 * @author Administrator
 * @date 2019/10/5
 */
public interface IMediaView extends IBaseView {

    void onLoadAlbumResult(String msg, BaseMediaBean.TYPE type, List<MediaItemBean> result);

    void onGetAlbumSelectedResult(int count);

    void onRemoveAlbumResult(String result);
}

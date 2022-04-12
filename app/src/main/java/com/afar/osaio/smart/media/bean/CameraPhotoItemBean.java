package com.afar.osaio.smart.media.bean;

import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public class CameraPhotoItemBean {

    public static final int MAX_MEDIA_NUM = 6;

    private long date;
    private List<CameraPhotoMediaBean> medias = new ArrayList<>();

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public List<CameraPhotoMediaBean> getMedias() {
        return medias;
    }

    public void setMedias(List<CameraPhotoMediaBean> medias) {
        if (this.medias == null) {
            this.medias = new ArrayList<>();
        }

        if (CollectionUtil.isNotEmpty(medias)) {
            this.date = medias.get(0).getTime();
            /*
            if (medias.size() > MAX_MEDIA_NUM) {
                medias = medias.subList(0, MAX_MEDIA_NUM - 1);
            }
            */
        }
        this.medias.clear();
        this.medias.addAll(CollectionUtil.safeFor(medias));
    }
}

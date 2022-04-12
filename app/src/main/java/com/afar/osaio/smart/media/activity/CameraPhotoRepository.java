package com.afar.osaio.smart.media.activity;

import android.text.TextUtils;

import com.afar.osaio.smart.media.bean.CameraPhotoMediaBean;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraPhotoRepository {

    private Map<String, CameraPhotoMediaBean> mPhotoMediaBeanMap = new HashMap<>();

    private static final class CameraPhotoRepositoryHolder {
        public static final CameraPhotoRepository INSTANCE = new CameraPhotoRepository();
    }

    public static CameraPhotoRepository getInstance() {
        return CameraPhotoRepositoryHolder.INSTANCE;
    }

    private CameraPhotoRepository() {
    }

    public List<CameraPhotoMediaBean> getPhotoMediaBeanList() {
        return CollectionUtil.safeFor(convertToCameraPhotoMediaList(mPhotoMediaBeanMap));
    }

    public void updatePhotoMediaBeanList(List<CameraPhotoMediaBean> photoMediaBeanList) {
        if (CollectionUtil.isEmpty(photoMediaBeanList)) {
            return;
        }
        for (CameraPhotoMediaBean photoMediaBean : photoMediaBeanList) {
            updatePhotoMediaBean(photoMediaBean);
        }
    }

    public void updatePhotoMediaBean(CameraPhotoMediaBean photoMediaBean) {
        if (!checkPhotoMediaBeanValid(photoMediaBean)) {
            return;
        }
        initPhotoMediaBeanMap();
        mPhotoMediaBeanMap.put(photoMediaBean.getPath(), photoMediaBean);
    }

    public boolean checkPhotoMediaBeanValid(CameraPhotoMediaBean photoMediaBean) {
        return photoMediaBean != null && !TextUtils.isEmpty(photoMediaBean.getPath());
    }

    private void initPhotoMediaBeanMap() {
        if (mPhotoMediaBeanMap == null) {
            mPhotoMediaBeanMap = new HashMap<>();
        }
    }

    private List<CameraPhotoMediaBean> convertToCameraPhotoMediaList(Map<String, CameraPhotoMediaBean> photoMediaBeanMap) {
        if (photoMediaBeanMap == null || mPhotoMediaBeanMap.entrySet() == null || mPhotoMediaBeanMap.entrySet().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList(mPhotoMediaBeanMap.entrySet());
    }
}

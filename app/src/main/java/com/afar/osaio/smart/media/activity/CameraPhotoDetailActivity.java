package com.afar.osaio.smart.media.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnPageChangeListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.adapter.PreviewCameraPhotoAdapter;
import com.afar.osaio.smart.media.bean.CameraPhotoMediaBean;
import com.afar.osaio.smart.media.contract.CameraPhotoDetailContract;
import com.afar.osaio.smart.media.presenter.CameraPhotoDetailPresenter;
import com.afar.osaio.smart.media.repository.PhotoMediaRepository;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.github.chrisbanes.photoview.PhotoView;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.file.MediaStoreUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.bean.SDKConstant;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class CameraPhotoDetailActivity extends BaseActivity implements CameraPhotoDetailContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.pvCameraPhotoPreviewImage)
    PhotoView pvCameraPhotoPreviewImage;

    @BindView(R.id.cbCameraPhotoPreview)
    ConvenientBanner cbCameraPhotoPreview;

    private CameraPhotoDetailContract.Presenter mPresenter;
    private CameraPhotoMediaBean mPhotoMediaBean;

    public static void toCameraPhotoDetailActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, CameraPhotoDetailActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_photo_detail);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new CameraPhotoDetailPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.saving_black_icon);
        ivRight.setVisibility(View.VISIBLE);
        setupPreviewView();
        //setupPreviewImageView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        pvCameraPhotoPreviewImage = null;
        if (cbCameraPhotoPreview != null) {
            cbCameraPhotoPreview.setOnPageChangeListener(null);
            cbCameraPhotoPreview = null;
        }
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                saveCameraPhoto(mPhotoMediaBean);
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull CameraPhotoDetailContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onSaveCameraPhoto(int state, String path) {
        if (isDestroyed()) {
            return;
        }
        String msg = state == SDKConstant.SUCCESS ? getString(R.string.photo_media_save_photo_success) : getString(R.string.get_fail);
        ToastUtil.showToast(this, msg);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !TextUtils.isEmpty(path)) {
            sendRefreshPicture(path);
        }
    }

    private void setupPreviewView() {
        mPhotoMediaBean = new CameraPhotoMediaBean();
        mPhotoMediaBean.setPath(getPhotoPath());
        mPhotoMediaBean.setTime(getPhotoTime());
        if (tvTitle != null) {
            tvTitle.setText(DateTimeUtil.formatDate(NooieApplication.mCtx, getPhotoTime(), DateTimeUtil.PATTERN_YMD_HM));
        }
        refreshCameraPhotoPreview(getPhotoPath());
    }

    private void refreshCameraPhotoPreview(String path) {
        if (TextUtils.isEmpty(path) || !(new File(path).exists())) {
            return;
        }
        Glide.with(NooieApplication.mCtx)
                .load(getPhotoPath())
                .apply(new RequestOptions()
                        //.dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 5))))
                        .format(DecodeFormat.PREFER_RGB_565)
                        .error(R.drawable.default_preview_thumbnail)
                )
                .transition(withCrossFade())
                .into(pvCameraPhotoPreviewImage);
    }

    private void setupPreviewImageView() {
        List<CameraPhotoMediaBean> photoMediaBeans = PhotoMediaRepository.getInstance().getAllPhoto();
        if (CollectionUtil.isEmpty(photoMediaBeans)) {
            return;
        }
        mPhotoMediaBean = getPhotoMediaBeanByPath(getPhotoPath(), photoMediaBeans);
        if (mPhotoMediaBean == null) {
            mPhotoMediaBean = photoMediaBeans.get(0);
        }
        showOnPageSelected(mPhotoMediaBean);
        cbCameraPhotoPreview.setPages(new PreviewCameraPhotoAdapter(), photoMediaBeans)
                .setOnPageChangeListener(new OnPageChangeListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    }

                    @Override
                    public void onPageSelected(int index) {
                        if (CollectionUtil.isIndexSafe(index, CollectionUtil.size(photoMediaBeans))) {
                            if (photoMediaBeans != null) {
                                showOnPageSelected(photoMediaBeans.get(index));
                            }
                        }
                    }
                });
        cbCameraPhotoPreview.setCanLoop(CollectionUtil.size(photoMediaBeans) > 1).stopTurning();
        int position = getPhotoMediaBeanIndex(mPhotoMediaBean, photoMediaBeans);
        if (position != -1) {
            cbCameraPhotoPreview.setFirstItemPos(position);
        }
    }

    private void showOnPageSelected(CameraPhotoMediaBean photoMediaBean) {
        if (isDestroyed() || checkNull(photoMediaBean)) {
            return;
        }
        mPhotoMediaBean = photoMediaBean;
        tvTitle.setText(DateTimeUtil.formatDate(NooieApplication.mCtx, photoMediaBean.getTime(), DateTimeUtil.PATTERN_YMD_HM));
    }

    private int getPhotoMediaBeanIndex(CameraPhotoMediaBean photoMediaBean, List<CameraPhotoMediaBean> photoMediaBeanList) {
        int index = -1;
        if (photoMediaBean == null || CollectionUtil.isEmpty(photoMediaBeanList)) {
            return index;
        }
        index = 0;
        int size = CollectionUtil.size(photoMediaBeanList);
        for (int i = 0; i < size; i++) {
            if (photoMediaBeanList.get(i) != null && !TextUtils.isEmpty(photoMediaBeanList.get(i).getPath()) && photoMediaBeanList.get(i).getPath().equalsIgnoreCase(photoMediaBean.getPath())) {
                index = i;
                break;
            }
        }
        return index;
    }

    private CameraPhotoMediaBean getPhotoMediaBeanByPath(String photoMediaPath, List<CameraPhotoMediaBean> photoMediaBeanList) {
        CameraPhotoMediaBean result = null;
        if (TextUtils.isEmpty(photoMediaPath) || CollectionUtil.isEmpty(photoMediaBeanList)) {
            return null;
        }
        int size = CollectionUtil.size(photoMediaBeanList);
        for (int i = 0; i < size; i++) {
            if (photoMediaBeanList.get(i) != null && !TextUtils.isEmpty(photoMediaBeanList.get(i).getPath()) && photoMediaBeanList.get(i).getPath().equalsIgnoreCase(photoMediaPath)) {
                result = photoMediaBeanList.get(i);
                break;
            }
        }
        return result;
    }

    private void saveCameraPhoto(CameraPhotoMediaBean photoMediaBean) {
        if (photoMediaBean == null || TextUtils.isEmpty(getDeviceId())) {
            return;
        }
        if (TextUtils.isEmpty(photoMediaBean.getPath()) || !new File(photoMediaBean.getPath()).exists()) {
            ToastUtil.showToast(this, getString(R.string.get_fail));
            return;
        }
        if (mPresenter != null) {
            mPresenter.updateFileToMediaStore(mUserAccount, getDeviceId(), photoMediaBean.getPath(), MediaStoreUtil.MEDIA_TYPE_IMAGE_JPEG);
        }
    }

    private String getDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private String getPhotoPath() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DATA_PARAM_1);
    }

    private long getPhotoTime() {
        if (getStartParam() == null) {
            return 0;
        }
        return getStartParam().getLong(ConstantValue.INTENT_KEY_TIME_STAMP, 0);
    }
}

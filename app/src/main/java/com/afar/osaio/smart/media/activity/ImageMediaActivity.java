package com.afar.osaio.smart.media.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnPageChangeListener;
import com.afar.osaio.smart.media.adapter.PreviewImageAdapter;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.MediaItemBean;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.bean.ImageMediaBean;
import com.afar.osaio.util.ConstantValue;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageMediaActivity extends BaseActivity {

    public static void toImageMediaActivity(Activity from, ImageMediaBean mediaBean, int requestCode) {
        Intent intent = new Intent(from, ImageMediaActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, mediaBean);
        from.startActivityForResult(intent, requestCode);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.cbPreviewImage)
    ConvenientBanner cbPreviewImage;

    ImageMediaBean mImageMediaBean;
    private List<MediaItemBean> mImageMediaBeans;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_media);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        }

        mImageMediaBean = getCurrentIntent().getParcelableExtra(ConstantValue.INTENT_KEY_DATA_PARAM);
        mImageMediaBeans = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(MediaActivity.getImageMedias())) {
            mImageMediaBeans.addAll(MediaActivity.getImageMedias());
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.delete_icon_state_list);
        setupPreviewImageView();
    }

    private void setupPreviewImageView() {
        List<ImageMediaBean> imageMediaBeans = getAllImageMediaBeans();
        if (CollectionUtil.isEmpty(imageMediaBeans)) {
            return;
        }
        if (mImageMediaBean == null) {
            mImageMediaBean = imageMediaBeans.get(0);
        }
        showOnPageSelected(mImageMediaBean);
        cbPreviewImage.setPages(new PreviewImageAdapter(), imageMediaBeans)
                .setOnPageChangeListener(new OnPageChangeListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    }

                    @Override
                    public void onPageSelected(int index) {
                        if (CollectionUtil.isIndexSafe(index, CollectionUtil.size(imageMediaBeans))) {
                            showOnPageSelected(imageMediaBeans.get(index));
                        }
                    }
                });
        cbPreviewImage.setCanLoop(CollectionUtil.size(imageMediaBeans) > 1).stopTurning();
        int position = getImageMediaBeanIndex(mImageMediaBean, imageMediaBeans);
        if (position != -1) {
            cbPreviewImage.setFirstItemPos(position);
        }
    }

    private void showOnPageSelected(ImageMediaBean imageMediaBean) {
        if (isDestroyed() || checkNull(tvTitle, imageMediaBean)) {
            return;
        }
        mImageMediaBean = imageMediaBean;
        StringBuilder titleSb = new StringBuilder();
        titleSb.append(DateTimeUtil.getUtcMonthDisplayName(NooieApplication.mCtx, imageMediaBean.getTime()));
        titleSb.append(" ");
        titleSb.append(DateTimeUtil.getUtcTimeString(imageMediaBean.getTime(), DateTimeUtil.PATTERN_D));
        tvTitle.setText(titleSb.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideDeleteMediaDialog();
        releaseRes();
        if (mImageMediaBeans != null) {
            mImageMediaBeans.clear();
        }
    }

    @Override
    public void releaseRes() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        if (cbPreviewImage != null) {
            cbPreviewImage = null;
        }
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                showDeleteMediaDialog();
                break;
        }
    }

    private void deleteMedia() {
        Bundle bundle = new Bundle();
        if (mImageMediaBean != null) {
            bundle.putString(ConstantValue.INTENT_KEY_DATA_ID, mImageMediaBean.getPath());
        }
        finishForResult(bundle);
    }

    private List<ImageMediaBean> getAllImageMediaBeans() {
        List<ImageMediaBean> imageMediaBeans = new ArrayList<>();
        if (CollectionUtil.isEmpty(mImageMediaBeans)) {
            return imageMediaBeans;
        }
        for (MediaItemBean mediaItemBean : CollectionUtil.safeFor(mImageMediaBeans)) {
            if (mediaItemBean != null && CollectionUtil.isNotEmpty(mediaItemBean.getMedias())) {
                for (BaseMediaBean mediaBean : CollectionUtil.safeFor(mediaItemBean.getMedias())) {
                    if (mediaBean != null) {
                        imageMediaBeans.add((ImageMediaBean)mediaBean);
                    }
                }
            }
        }
        return imageMediaBeans;
    }

    private int getImageMediaBeanIndex(ImageMediaBean imageMediaBean, List<ImageMediaBean> imageMediaBeans) {
        int index = -1;
        if (imageMediaBean == null || CollectionUtil.isEmpty(imageMediaBeans)) {
            return index;
        }
        index = 0;
        int size = CollectionUtil.size(imageMediaBeans);
        for (int i = 0; i < size; i++) {
            if (imageMediaBeans.get(i) != null && !TextUtils.isEmpty(imageMediaBeans.get(i).getPath()) && imageMediaBeans.get(i).getPath().equalsIgnoreCase(imageMediaBean.getPath())) {
                index = i;
                break;
            }
        }
        return index;
    }

    private Dialog mDeleteMediaDialog;

    private void showDeleteMediaDialog() {
        hideDeleteMediaDialog();
        mDeleteMediaDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.media_delete_photo_title, R.string.media_delete_photo_content, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                deleteMedia();
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideDeleteMediaDialog() {
        if (mDeleteMediaDialog != null) {
            mDeleteMediaDialog.dismiss();
            mDeleteMediaDialog = null;
        }
    }
}

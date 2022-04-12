package com.afar.osaio.smart.media.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.media.adapter.MediaAdapter;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.ImageMediaBean;
import com.afar.osaio.smart.media.bean.MediaItemBean;
import com.afar.osaio.smart.media.bean.VideoMediaBean;
import com.afar.osaio.smart.media.presenter.IMediaPresenter;
import com.afar.osaio.smart.media.presenter.MediaPresenter;
import com.afar.osaio.smart.media.view.IMediaView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.TagLabelView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * MediaActivity
 *
 * @author Administrator
 * @date 2019/10/5
 */
public class MediaActivity extends BaseActivity implements IMediaView {

    private static final int MEDIA_OPERATION_NORMAL = 1;
    private static final int MEDIA_OPERATION_SELECTED = 2;
    private static final int MEDIA_OPERATION_SELECT_ALL = 3;

    public static void toMediaActivity(Context from, String deviceId) {
        Intent intent = new Intent(from, MediaActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        from.startActivity(intent);
    }

    public static void toMediaActivity(Context from) {
        Intent intent = new Intent(from, MediaActivity.class);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tlvMediaVideo)
    TagLabelView tlvMediaVideo;
    @BindView(R.id.tlvMediaImage)
    TagLabelView tlvMediaImage;
    @BindView(R.id.btnMediaCancel)
    TextView btnMediaCancel;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.btnMediaSelect)
    TextView btnMediaSelect;
    @BindView(R.id.btnMediaDelete)
    FButton btnMediaDelete;
    @BindView(R.id.rcvMedias)
    RecyclerView rcvMedias;

    private IMediaPresenter mMediaPresenter;
    private MediaAdapter mMediaAdapter;
    private String mDeviceId;
    private static List<MediaItemBean> mImageMedias = new ArrayList<>();
    private static List<MediaItemBean> mVideoMedias = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        }
        mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        mMediaPresenter = new MediaPresenter(this);
    }

    private void initView() {
        setupMenuBar();
        setupMediasView();
        mMediaPresenter.loadAlbum(mUserAccount, mDeviceId, BaseMediaBean.TYPE.VIDEO);
    }

    public void setupMenuBar() {
        showSelectView(false);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tlvMediaVideo.setTagSelected(true)
                .setText(getString(R.string.media_tag_label_video));
        tlvMediaImage.setTagSelected(false)
                .setText(getString(R.string.media_tag_label_image));
        btnMediaSelect.setText(getString(R.string.media_select));
        btnMediaSelect.setTag(MEDIA_OPERATION_NORMAL);
    }

    public void setupMediasView() {
        mMediaAdapter = new MediaAdapter();
        mMediaAdapter.setListener(new MediaAdapter.MediaListener() {
            @Override
            public void onDataChange() {
                updateSelectNum();
            }

            @Override
            public void onItemClick(BaseMediaBean mediaBean) {
                if (mediaBean != null && mediaBean.getType() == BaseMediaBean.TYPE.IMAGE) {
                    ImageMediaActivity.toImageMediaActivity(MediaActivity.this, (ImageMediaBean)mediaBean, ConstantValue.REQUEST_CODE_DELETE_FILE);
                } else if (mediaBean != null && mediaBean.getType() == BaseMediaBean.TYPE.VIDEO) {
                    VideoMediaActivity.toVideoMediaActivity(MediaActivity.this, (VideoMediaBean)mediaBean, ConstantValue.REQUEST_CODE_DELETE_FILE);
                }
            }
        });
        rcvMedias.setLayoutManager(new LinearLayoutManager(this));
        rcvMedias.setAdapter(mMediaAdapter);
        btnMediaDelete.setVisibility(View.GONE);
    }

    public void showSelectView(boolean show) {
        ivLeft.setVisibility(show ? View.GONE : View.VISIBLE);
        tlvMediaVideo.setVisibility(show ? View.GONE : View.VISIBLE);
        tlvMediaImage.setVisibility(show ? View.GONE : View.VISIBLE);
        btnMediaCancel.setVisibility(show ? View.VISIBLE : View.GONE);
        tvTitle.setVisibility(show ? View.VISIBLE : View.GONE);
        btnMediaDelete.setVisibility(View.GONE);
    }

    private void updateSelectNum() {
        if (btnMediaSelect.getTag() != null && (Integer)btnMediaSelect.getTag() == MEDIA_OPERATION_SELECTED && mMediaPresenter != null) {
            mMediaPresenter.getSelectedMedia(tlvMediaVideo.isTagSelected() ? BaseMediaBean.TYPE.VIDEO : BaseMediaBean.TYPE.IMAGE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMedias();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_DELETE_FILE:
                    if (data != null && data.getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM) != null) {
                        /*
                        Observable.just(data.getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM).getString(ConstantValue.INTENT_KEY_DATA_ID))
                                .flatMap(new Func1<String, Observable<Boolean>>() {
                                    @Override
                                    public Observable<Boolean> call(String path) {
                                        FileUtils.deleteFile(path);
                                        return Observable.just(true);
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Boolean>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onNext(Boolean aBoolean) {
                                        if (mMediaPresenter != null) {
                                            mMediaPresenter.loadAlbum(mUserAccount, mDeviceId, tlvMediaVideo.isTagSelected() ? BaseMediaBean.TYPE.VIDEO : BaseMediaBean.TYPE.IMAGE);
                                        }
                                    }
                                });
                                */
                        mMediaPresenter.removeMediaByPath(mUserAccount, mDeviceId, tlvMediaVideo.isTagSelected() ? BaseMediaBean.TYPE.VIDEO : BaseMediaBean.TYPE.IMAGE, data.getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM).getString(ConstantValue.INTENT_KEY_DATA_ID));
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick({R.id.ivLeft, R.id.tlvMediaVideo, R.id.tlvMediaImage, R.id.btnMediaCancel, R.id.btnMediaSelect, R.id.btnMediaDelete})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tlvMediaVideo:
                if (tlvMediaVideo.isTagSelected()) {
                    break;
                }
                tlvMediaImage.setTagSelected(false);
                tlvMediaVideo.setTagSelected(true);
                mMediaPresenter.loadAlbum(mUserAccount, mDeviceId, BaseMediaBean.TYPE.VIDEO);
                break;
            case R.id.tlvMediaImage:
                if (tlvMediaImage.isTagSelected()) {
                    break;
                }
                tlvMediaVideo.setTagSelected(false);
                tlvMediaImage.setTagSelected(true);
                mMediaPresenter.loadAlbum(mUserAccount, mDeviceId, BaseMediaBean.TYPE.IMAGE);
                break;
            case R.id.btnMediaCancel:
                cancelSelectedView();
                break;
            case R.id.btnMediaSelect:
                if (btnMediaSelect.getTag() != null && (Integer)btnMediaSelect.getTag() == MEDIA_OPERATION_NORMAL) {
                    showSelectView(true);
                    btnMediaSelect.setTag(MEDIA_OPERATION_SELECTED);
                    btnMediaSelect.setText(R.string.media_select_all);
                    tvTitle.setText(R.string.media_select_title);
                    if (mMediaAdapter != null) {
                        mMediaAdapter.setIsEdited(true);
                    }
                } else if (btnMediaSelect.getTag() != null && (Integer)btnMediaSelect.getTag() == MEDIA_OPERATION_SELECTED) {
                    if (mMediaPresenter != null) {
                        mMediaPresenter.resetMedia(mUserAccount, mDeviceId, tlvMediaVideo.isTagSelected() ? BaseMediaBean.TYPE.VIDEO : BaseMediaBean.TYPE.IMAGE, true);
                        if (mMediaAdapter != null) {
                            mMediaAdapter.notifyDataSetChanged();
                        }
                        updateSelectNum();
                    }
                }
                break;
            case R.id.btnMediaDelete:
                showDeleteMediaDialog();
                break;
        }
    }

    private void cancelSelectedView() {
        if (btnMediaSelect.getTag() != null && (Integer)btnMediaSelect.getTag() != MEDIA_OPERATION_NORMAL) {
            if (mMediaPresenter != null) {
                mMediaPresenter.resetMedia(mUserAccount, mDeviceId, tlvMediaVideo.isTagSelected() ? BaseMediaBean.TYPE.VIDEO : BaseMediaBean.TYPE.IMAGE, false);
            }
            showSelectView(false);
            btnMediaSelect.setTag(MEDIA_OPERATION_NORMAL);
            btnMediaSelect.setText(R.string.media_select);
            if (mMediaAdapter != null) {
                mMediaAdapter.setIsEdited(false);
            }
        }
    }

    private AlertDialog mDeleteMediaDialog = null;

    private void showDeleteMediaDialog() {
        mDeleteMediaDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.media_delete_title, R.string.media_delete_content, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mMediaPresenter != null) {
                    mMediaPresenter.removeMedia(mUserAccount, mDeviceId, tlvMediaVideo.isTagSelected() ? BaseMediaBean.TYPE.VIDEO : BaseMediaBean.TYPE.IMAGE);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    @Override
    public void onLoadAlbumResult(String msg, BaseMediaBean.TYPE type, List<MediaItemBean> result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg) && mMediaAdapter != null) {
            mMediaAdapter.setDataList(result);
            if (type == BaseMediaBean.TYPE.IMAGE) {
                setImageMedias(result);
            } else if (type == BaseMediaBean.TYPE.VIDEO) {
                setVideoMedias(result);
            }
        }
    }

    @Override
    public void onGetAlbumSelectedResult(int count) {
        if (checkNull(tvTitle, btnMediaDelete)) {
            return;
        }

        if (count > 0) {
            tvTitle.setText(String.format(getString(R.string.media_select_title_num), count));
            btnMediaDelete.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText(R.string.media_select_title);
            btnMediaDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRemoveAlbumResult(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            cancelSelectedView();
            if (mMediaPresenter != null) {
                mMediaPresenter.loadAlbum(mUserAccount, mDeviceId, tlvMediaVideo.isTagSelected() ? BaseMediaBean.TYPE.VIDEO : BaseMediaBean.TYPE.IMAGE);
            }
        }
    }

    public static List<MediaItemBean> getImageMedias() {
        return mImageMedias;
    }

    private void setImageMedias(List<MediaItemBean> mediaItemBeans) {
        if (mImageMedias == null) {
            mediaItemBeans = new ArrayList<>();
        }
        mImageMedias.clear();
        mImageMedias.addAll(mediaItemBeans);
    }

    public static List<MediaItemBean> getVideoMedias() {
        return mVideoMedias;
    }

    private void setVideoMedias(List<MediaItemBean> mediaItemBeans) {
        if (mVideoMedias == null) {
            mediaItemBeans = new ArrayList<>();
        }
        mVideoMedias.clear();
        mVideoMedias.addAll(mediaItemBeans);
    }

    private void releaseMedias() {
        if (mImageMedias != null) {
            mImageMedias.clear();
        }

        if (mVideoMedias != null) {
            mVideoMedias.clear();
        }
    }
}

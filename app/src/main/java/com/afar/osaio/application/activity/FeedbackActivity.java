package com.afar.osaio.application.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.application.adapter.FeedbackAdapter;
import com.afar.osaio.application.presenter.FeedbackPresenterImpl;
import com.afar.osaio.application.presenter.IFeedbackPresenter;
import com.afar.osaio.application.view.IFeedbackView;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.NooieFeedbackOption;
import com.afar.osaio.smart.setting.activity.CustomNameActivity;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.tool.PatternMatchUtil;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.Util;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.adapter.MediaAddAdapter;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.api.network.base.bean.entity.FeedbackProduct;
import com.nooie.sdk.api.network.base.bean.entity.FeedbackType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public class FeedbackActivity extends BaseActivity implements IFeedbackView {

    private static int FEEDBACK_TYPE_CLOSE = 0;
    private static int FEEDBACK_TYPE_OPEN = 1;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.etContent)
    AutoCompleteTextView etContent;
    @BindView(R.id.tvNum)
    TextView tvNum;
    @BindView(R.id.ivClearContent)
    ImageView ivClearContent;
    @BindView(R.id.tvIssueTitle)
    TextView tvIssueTitle;
    @BindView(R.id.ivIssueSwitch)
    ImageView ivIssueSwitch;
    @BindView(R.id.rvIssue)
    RecyclerView rvIssue;
    @BindView(R.id.tvModelTitle)
    TextView tvModelTitle;
    @BindView(R.id.ivModelSwitch)
    ImageView ivModelSwitch;
    @BindView(R.id.rvModel)
    RecyclerView rvModel;
    @BindView(R.id.rvPhotoAndView)
    RecyclerView rvPhotoAndView;
    @BindView(R.id.ivPhotoAndVideoTopAdd)
    ImageView ivPhotoAndVideoTopAdd;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.btnSend)
    FButton btnSend;

    private static final int REQ_CODE_CAMERA = 1;
    private static final int REQ_CODE_GALLERY = 2;
    private static final int REQ_CODE_GALLERY_AFTER_KITKAT = 3;

    private String mCameraPath;
    private List<String> mPictures = new ArrayList<>();
    private String mIssue;
    private String mModel;
    private int mFeedbackTypeId = -1;
    private int mFeedbackProductId = -1;
    private IFeedbackPresenter mFeedbackPresenter;
    private FeedbackAdapter mFeedBackIssueAdapter;
    private FeedbackAdapter mFeedBackModelAdapter;
    private MediaAddAdapter mMediaAddAdapter;
    private Map<String, String> mUploadPictureMap;
    private Dialog mFeedbackDialog;
    private Dialog mSelectPhotoDialog;

    public static void toFeedbackActivity(Context from) {
        Intent intent = new Intent(from, FeedbackActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        initData();
        initView();
        refreshUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerInputListener();
        checkIsNeedToRequestLayout();
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterInputListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideFeedbackDialog();
        hideSelectPhotoDialog();
        hideLoading();
    }

    private void initData() {
        mUploadPictureMap = new HashMap<>(16);
        mUploadPictureMap.clear();
        mFeedbackPresenter = new FeedbackPresenterImpl(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setVisibility(View.GONE);
        tvTitle.setText(R.string.feedback_page_title);

        if (!TextUtils.isEmpty(mUserAccount)) {
            tvEmail.setText(mUserAccount);
        }

        setupPhotoAndVideo();
        setupNooieIssueSpinner(null);
        setupModelSpinner(null);
        setupInputContent();
        mFeedbackPresenter.loadFeedbackInfo();
    }

    private void setupNooieIssueSpinner(final List<FeedbackType> feedbackTypes) {
        tvIssueTitle.setText(getString(R.string.issue_title));
        ivIssueSwitch.setTag(FEEDBACK_TYPE_CLOSE);
        if (feedbackTypes == null) {
            return;
        }

        List<String> issues = new ArrayList<>();
        for (FeedbackType feedbackType : CollectionUtil.safeFor(feedbackTypes)) {
            if (!TextUtils.isEmpty(feedbackType.getName())) {
                issues.add(feedbackType.getName());
            }
        }
        mIssue = CollectionUtil.isNotEmpty(issues) ? issues.get(0) : "";
        //设置列表高度
        /*
        ViewGroup.LayoutParams layoutParams = rvIssue.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (issues.size() > 3) {
            layoutParams.height = (int)getResources().getDimension(R.dimen.dp_100);
            rvIssue.setLayoutParams(layoutParams);
        }
        */
        mFeedBackIssueAdapter = new FeedbackAdapter(getApplicationContext());
        mFeedBackIssueAdapter.setListener(new FeedbackAdapter.OnFeedbackItemClickListener() {
            @Override
            public void onItemClick(int position, String data) {
                for (FeedbackType feedbackType: CollectionUtil.safeFor(feedbackTypes)) {
                    if (feedbackType != null && data.equalsIgnoreCase(feedbackType.getName())) {
                        mFeedbackTypeId = feedbackType.getId();
                        break;
                    }
                }
                mIssue = data;
                tvIssueTitle.setText(mIssue);
                toggleIssueContainer();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvIssue.setVisibility(View.GONE);
        rvIssue.setLayoutManager(layoutManager);
        rvIssue.setAdapter(mFeedBackIssueAdapter);

        mFeedBackIssueAdapter.setData(issues);
    }

    private void setupModelSpinner(final List<FeedbackProduct> feedbackProducts) {
        tvModelTitle.setText(getString(R.string.model_title));
        ivModelSwitch.setTag(FEEDBACK_TYPE_CLOSE);

        if (feedbackProducts == null) {
            return;
        }

        List<String> models = new ArrayList<>();
        for (FeedbackProduct feedbackProduct : CollectionUtil.safeFor(feedbackProducts)) {
            if (!TextUtils.isEmpty(feedbackProduct.getModel())) {
                models.add(feedbackProduct.getModel());
            }
        }
        mModel = CollectionUtil.isNotEmpty(models) ? models.get(0) : "";

        mFeedBackModelAdapter = new FeedbackAdapter(getApplicationContext());
        mFeedBackModelAdapter.setListener(new FeedbackAdapter.OnFeedbackItemClickListener() {
            @Override
            public void onItemClick(int position, String data) {
                mModel = data;
                tvModelTitle.setText(mModel);
                for (FeedbackProduct feedbackProduct : CollectionUtil.safeFor(feedbackProducts)) {
                    if (data.equalsIgnoreCase(feedbackProduct.getModel())) {
                        mFeedbackProductId = feedbackProduct.getId();
                        break;
                    }
                }
                toggleModelContainer();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvModel.setVisibility(View.GONE);
        rvModel.setLayoutManager(layoutManager);
        rvModel.setAdapter(mFeedBackModelAdapter);
        mFeedBackModelAdapter.setData(models);
    }

    private void setupPhotoAndVideo() {
        mMediaAddAdapter = new MediaAddAdapter(getApplicationContext());
        GridLayoutManager gridLayoutManager = MediaAddAdapter.createGridLayoutManager(getApplicationContext(), 4);
        mMediaAddAdapter.setListener(new MediaAddAdapter.MediaAddListener() {
            @Override
            public void onMediaAdd() {
                clickToAddPhoto();
            }

            @Override
            public void onMediaDelete(String path) {
                deletePhotoAndVideo(path);
            }
        });
        rvPhotoAndView.setLayoutManager(gridLayoutManager);
        rvPhotoAndView.setAdapter(mMediaAddAdapter);
    }

    private boolean isPictureFile(String path) {
        String[] pictureTypes = {"jpg", "jpeg", "png", };
        for (int i = 0; i < pictureTypes.length; i++) {
            if (!TextUtils.isEmpty(path) && (path.contains(pictureTypes[i]) || path.contains(pictureTypes[i].toUpperCase()))) {
                return true;
            }
        }
        return false;
    }
    private void addPhotoAndVideo(String path) {
        NooieLog.d("-->> FeedbackActivity addPhotoAndVideo isPicture=" + isPictureFile(path) + " pic size=" + CollectionUtil.size(mPictures));
        if (!isPictureFile(path)) {
            ToastUtil.showToast(FeedbackActivity.this, R.string.feedback_picture_type_error);
            return;
        }
        if (!TextUtils.isEmpty(path) && !mPictures.contains(path)) {
            MediaAddAdapter.MediaInfo mediaInfo = MediaAddAdapter.buildMediaInfo();
            mediaInfo.setPath(path);
            mMediaAddAdapter.addData(mediaInfo);
            mPictures.add(path);
        }

        if (mPictures.size() > 0 && ivPhotoAndVideoTopAdd.getVisibility() == View.VISIBLE) {
            ivPhotoAndVideoTopAdd.setVisibility(View.GONE);
        }
        refreshUI();
    }

    private void deletePhotoAndVideo(String path) {
        if (!TextUtils.isEmpty(path) && mPictures.contains(path)) {
            mPictures.remove(path);
        }

        if (!TextUtils.isEmpty(path) && mUploadPictureMap.containsKey(path)) {
            mUploadPictureMap.remove(path);
        }

        if (mPictures.size() == 0 && ivPhotoAndVideoTopAdd.getVisibility() == View.GONE) {
            ivPhotoAndVideoTopAdd.setVisibility(View.VISIBLE);
        }
        refreshUI();
    }

    private void refreshUI() {
        etContent.clearFocus();
        //tvNum.setText(String.format(getString(R.string.feedback_add_photo), mPictures.size()));
    }

    private void setupInputContent() {
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkBtnEnable();
            }
        });

        checkBtnEnable();
    }

    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(etContent.getText().toString().trim())) {
            btnSend.setEnabled(true);
            btnSend.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnSend.setEnabled(false);
            btnSend.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            Uri uri;
            String path;
            switch (requestCode) {
                case REQ_CODE_GALLERY:
                case REQ_CODE_GALLERY_AFTER_KITKAT:
                    if (intent != null && mFeedbackPresenter != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            File targetFile = new File(FileUtil.getCacheDir(NooieApplication.mCtx, mUserAccount), System.currentTimeMillis() + ".JPG");
                            if (targetFile != null) {
                                mFeedbackPresenter.copyFileToPrivateStorage(intent.getData(), targetFile.getAbsolutePath());
                            }
                        } else {
                            upLoadPicture(FileUtil.getFilePathByUri(this, intent.getData()));
                        }
                    }
                    break;
                case REQ_CODE_CAMERA:
                    upLoadPicture(mCameraPath);
                    break;
                case ConstantValue.REQUEST_CODE_CUSTOM_NAME:
                    String name = intent != null ? intent.getStringExtra(ConstantValue.INTENT_KEY_NICK_NAME) : "";
                    if (!TextUtils.isEmpty(name)) {
                        tvEmail.setText(name);
                    }
                    break;
            }
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnSend, R.id.ivClearContent, R.id.emailContainer, R.id.issueContainer, R.id.modelContainer, R.id.ivPhotoAndVideoTopAdd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnSend:
                String email = tvEmail.getText().toString().trim();
                String content = etContent.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    ToastUtil.showToast(this, R.string.feedback_input_contact_info);
                    return;
                }

                if (!PatternMatchUtil.isEmailAddress(email)) {
                    ToastUtil.showToast(this, R.string.camera_share_account_invalid);
                    return;
                }

                //DialogUtils.showConfirmDialog(this, R.string.feedback_confirm_feedback, mFeedbackListener);
                showFeedbackDialog();
                break;
            case R.id.ivClearContent:
                etContent.setText(new String());
                break;
            case R.id.emailContainer:
                CustomNameActivity.toCustomNameActivity(FeedbackActivity.this, ConstantValue.REQUEST_CODE_CUSTOM_NAME, ConstantValue.NOOIE_CUSTOM_NAME_TYPE_USER, getString(R.string.feedback_contact_info), getString(R.string.feedback_email), "", EventDictionary.EVENT_ID_ACCESS_CHANGE_CONTACT_ACCOUNT);
                break;
            case R.id.issueContainer:
                toggleIssueContainer();
                break;
            case R.id.modelContainer:
                toggleModelContainer();
                break;
            case R.id.ivPhotoAndVideoTopAdd:
                clickToAddPhoto();
                break;
        }
    }

    public void rotateFeedbackArrow(View view, float start, float end) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", start, end);
        rotateAnimator.setDuration(300);
        rotateAnimator.start();
    }

    public void toggleIssueContainer() {
        if (((int)ivIssueSwitch.getTag()) == FEEDBACK_TYPE_CLOSE) {
            ivIssueSwitch.setTag(FEEDBACK_TYPE_OPEN);
            rotateFeedbackArrow(ivIssueSwitch, 0, 90);
            rvIssue.setVisibility(View.VISIBLE);
        } else {
            ivIssueSwitch.setTag(FEEDBACK_TYPE_CLOSE);
            rotateFeedbackArrow(ivIssueSwitch, 90, 0);
            rvIssue.setVisibility(View.GONE);
        }
    }

    public void toggleModelContainer() {
        if (((int)ivModelSwitch.getTag()) == FEEDBACK_TYPE_CLOSE) {
            ivModelSwitch.setTag(FEEDBACK_TYPE_OPEN);
            rotateFeedbackArrow(ivModelSwitch, 0, 90);
            rvModel.setVisibility(View.VISIBLE);
        } else {
            ivModelSwitch.setTag(FEEDBACK_TYPE_CLOSE);
            rotateFeedbackArrow(ivModelSwitch, 90, 0);
            rvModel.setVisibility(View.GONE);
        }
    }

    private void clickToAddPhoto() {
        if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, CommonUtil.getStoragePermGroup())) {
            requestPermission(CommonUtil.getStoragePermGroup());
            return;
        }
        //DialogUtils.showConfirmWithSubMsgDialog(this, R.string.feedback_add_photo_title, R.string.feedback_add_photo_content, R.string.take_photo, R.string.album, mSelectPhotoListener);
        showSelectPhotoDialog();
    }

    @Override
    protected void permissionsGranted() {
        super.permissionsGranted();
        //DialogUtils.showConfirmWithSubMsgDialog(this, R.string.feedback_add_photo_title, R.string.feedback_add_photo_content, R.string.album, R.string.camera, mSelectPhotoListener);
    }

    private void showFeedbackDialog() {
        hideFeedbackDialog();
        mFeedbackDialog = DialogUtils.showConfirmDialog(this, R.string.feedback_confirm_feedback, mFeedbackListener);
    }

    private void hideFeedbackDialog() {
        if (mFeedbackDialog != null) {
            mFeedbackDialog.dismiss();
            mFeedbackDialog = null;
        }
    }

    private DialogUtils.OnClickConfirmButtonListener mFeedbackListener = new DialogUtils.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            submitAction();
        }

        @Override
        public void onClickLeft() {
        }
    };

    private void showSelectPhotoDialog() {
        hideSelectPhotoDialog();
        mSelectPhotoDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.feedback_add_photo_title, R.string.feedback_add_photo_content, R.string.take_photo, R.string.album, mSelectPhotoListener);
    }

    private void hideSelectPhotoDialog() {
        if (mSelectPhotoDialog != null) {
            mSelectPhotoDialog.dismiss();
            mSelectPhotoDialog = null;
        }
    }

    private DialogUtils.OnClickConfirmButtonListener mSelectPhotoListener = new DialogUtils.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //intent.setType("*/*");
            //ArrayList<String> mimes = new ArrayList<>();
            //mimes.add("image/*");
            //mimes.add("video/*");
            //intent.putExtra(Intent.EXTRA_MIME_TYPES, mimes);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(intent, REQ_CODE_GALLERY_AFTER_KITKAT);
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQ_CODE_GALLERY);
            }
        }

        @Override
        public void onClickLeft() {
            if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_CAMERA)) {
                requestPermission(ConstantValue.PERM_GROUP_CAMERA);
                return;
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(FileUtil.getCacheDir(NooieApplication.mCtx, mUserAccount), DateTimeUtil.getTodayStartTimeStamp() + ".JPG");
            mCameraPath = file.getAbsolutePath();
            //Uri uri = Uri.fromFile(file);
            Uri uri =  convertUriByPath(mCameraPath);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, REQ_CODE_CAMERA);
        }
    };

    private Uri convertUriByPath(String path) {
        Uri uri = null;
        try {
            NooieLog.d("-->> FeedbackActivity test change portrait 6 path=" + path);
            File tmpFile = new File(path);
            if (tmpFile == null || !tmpFile.exists()) {
                NooieLog.d("-->> FeedbackActivity test change portrait 7 tmpfile not exist");
                //return null;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(this, "com.afar.osaio.provider", new File(path));
                NooieLog.d("-->> FeedbackActivity test change portrait 8 uri=" + (uri != null ? uri.getPath() : ""));
            } else {
                uri = Uri.fromFile(new File(path));
                NooieLog.d("-->> FeedbackActivity test change portrait 9 uri=" + (uri != null ? uri.getPath() : ""));
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return uri;
    }

    private void upLoadPicture(final String picture) {
        NooieLog.d("-->> FeedbackActivity upLoadPicture isPicture=" + isPictureFile(picture));
        if (!isPictureFile(picture)) {
            ToastUtil.showToast(FeedbackActivity.this, R.string.feedback_picture_type_error);
            return;
        }
        if (!TextUtils.isEmpty(picture) && mFeedbackPresenter != null) {
            Util.delayTask(500, new Util.OnDelayTaskFinishListener() {
                @Override
                public void onFinish() {
                    showLoading();
                    mFeedbackPresenter.upLoadPicture(mUid, mUserAccount, picture);
                }
            });
        }
    }

    private void submitAction() {
        String email = tvEmail.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            ToastUtil.showToast(this, R.string.feedback_input_contact_info);
            return;
        }

        /* forbbiden for ignore typeid and productid and use default value
        if (mFeedbackTypeId == -1 || mFeedbackProductId == -1) {
            ToastUtil.showToast(FeedbackActivity.this, R.string.feedback_type_and_product_error);
            return;
        }
        */
        mFeedbackTypeId = 1;
        mFeedbackProductId = 1;

        /*
        if (content.length() < 10) {
            ToastUtil.showToast(this, R.string.feedback_description_min_number);
            return;
        }
        */

        showLoading();
        mFeedbackPresenter.postFeedback(mFeedbackTypeId, mFeedbackProductId, email, content, getUploadPictureParam(mUploadPictureMap));
    }

    @Override
    public void notifyFeedbackResult(String result) {
        if (isPause()) {
            return;
        }

        hideLoading();
        if (result.equals(ConstantValue.SUCCESS)) {
            ToastUtil.showToast(this, R.string.feedback_send_success);
            Util.delayTask(1000, new Util.OnDelayTaskFinishListener() {
                @Override
                public void onFinish() {
                    finish();
                }
            });
        } else {
            ToastUtil.showLongToast(this, result);
        }
    }

    @Override
    public void onLoadFeedbackInfoSuccess(NooieFeedbackOption option) {
        if (option != null) {
            setupNooieIssueSpinner(option.getFeedbackTypes());
            setupModelSpinner(option.getFeedbackProducts());
        }
    }

    @Override
    public void onLoadFeedbackInfoFailed(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            ToastUtil.showToast(this, msg);
        }
    }

    @Override
    public void notifyUploadPictureResult(String result, String localPath, String uploadPath) {
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            addPhotoAndVideo(localPath);
            mUploadPictureMap.put(localPath, uploadPath);
        } else {
            ToastUtil.showToast(this, R.string.feedback_upload_image_fail);
        }
    }

    @Override
    public void onCopyFileToPrivateStorage(String result, String path) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            upLoadPicture(path);
        }
    }

    private String getUploadPictureParam(Map<String, String> uploadPictureMap) {
        StringBuilder uploadPictureParamSb = new StringBuilder();
        try {
            Iterator<Map.Entry<String, String>> uploadPictureParamIterator = uploadPictureMap.entrySet().iterator();
            while (uploadPictureParamIterator.hasNext()) {
                Map.Entry<String, String> uploadPictureParamEntry = uploadPictureParamIterator.next();
                if (uploadPictureParamEntry != null && !TextUtils.isEmpty(uploadPictureParamEntry.getValue())) {
                    uploadPictureParamSb.append(uploadPictureParamEntry.getValue());
                    if (uploadPictureParamIterator.hasNext()) {
                        uploadPictureParamSb.append(",");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NooieLog.d("-->> FeedbackActivity getUploadPictureParam uploadPictureParamSb=" + uploadPictureParamSb.toString());
        return uploadPictureParamSb.toString();
    }

    @Override
    public void showLoadingDialog() {
        showLoading(false);
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }
}

package com.afar.osaio.smart.electrician.activity;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.afar.osaio.BuildConfig;
import com.afar.osaio.R;
import com.afar.osaio.account.activity.ChangePasswordActivity;
import com.afar.osaio.account.activity.SignInActivity;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.base.ActivityStack;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.UserInfo;
import com.afar.osaio.notification.NotificationManager;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.electrician.eventbus.UpdateProfileEvent;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.IMyProfilePresenter;
import com.afar.osaio.smart.electrician.presenter.MyProfilePresenter;
import com.afar.osaio.smart.electrician.util.CommonUtil;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IMyProfileView;
import com.afar.osaio.smart.event.SelectPortraitEvent;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.PhotoPopupWindows;
import com.alibaba.android.arouter.launcher.ARouter;
import com.apemans.platformbridge.helper.YRUserPlatformHelper;
import com.apemans.yrcxsdk.data.YRCXSDKDataManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.graphics.BitmapUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;
import com.nooie.sdk.cache.DetectionThumbnailCache;
import com.nooie.sdk.processor.cloud.listener.DetectionThumbnailCacheListener;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * MyProfileActivity
 *
 * @author Administrator
 * @date 2019/2/27
 */
public class MyProfileActivity extends BaseActivity implements IMyProfileView {

    @BindView(R.id.ivPortrait)
    ImageView ivPortrait;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvAccount)
    TextView tvAccount;
    @BindView(R.id.containerAccount)
    LinearLayout containerAccount;
    @BindView(R.id.tvNickName)
    TextView tvNickName;
    @BindView(R.id.containerNickname)
    LinearLayout containerNickname;

    public static final int RESULT_CODE_KITKAT_PHOTO = 321;
    protected static final int RESULT_CODE_PHOTO = 322;
    protected static final int RESULT_CODE_CAMERA = 323;
    protected static final int RESULT_CODE_CLIP = 324;
    protected static final int RESULT_CODE_NAME = 325;

    private static final String[] PERMS_CAMERA = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String[] PERMS_READ_WRITE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Bitmap mBitmap;
    private String mAccount;
    private String mNickName;
    private IMyProfilePresenter myProfilePresenter;
    private boolean mIsUploadingPortrait = false;
    private DetectionThumbnailCacheListener mDownloadFileListener = null;

    private PhotoPopupWindows popMenus;

    private boolean haveChoosePhoto;
    private long memberId;
    private String memberNickName;

    public static void toMyProfileActivity(Context from, long memberId, String memberNickName) {
        Intent intent = new Intent(from, MyProfileActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_ID, memberId);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, memberNickName);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        //myProfilePresenter.loadUserInfo();
        if (!mIsUploadingPortrait && myProfilePresenter != null) {
            String portraitPath = FileUtil.getPersonPortraitInPrivate(NooieApplication.mCtx, mUserAccount).getPath();
            myProfilePresenter.getUserInfo(mUid, mUserAccount, portraitPath);
        }
    }

    private void registerDownloadFileListener() {
        if (mDownloadFileListener == null) {
            mDownloadFileListener = new DetectionThumbnailCacheListener() {
                @Override
                public void onUpdatePortrait(String portraitPath) {
                    NooieLog.d("-->> PersonFragment registerDownloadFileListener onUpdatePortrait portraitPath=" + portraitPath);
                    if (myProfilePresenter != null) {
                        myProfilePresenter.setDownloadPortraitState(false);
                    }
                    refreshPortrait(FileUtil.getPortraitPhotoPathInPrivate(NooieApplication.mCtx, mUserAccount, mUid));
                }
            };
            DetectionThumbnailCache.getInstance().addListener(mDownloadFileListener);
        }
    }

    private void unRegisterDownloadFileListener() {
        if (mDownloadFileListener != null) {
            DetectionThumbnailCache.getInstance().removeListener(mDownloadFileListener);
            mDownloadFileListener = null;
        }
    }

    private void initData() {
        myProfilePresenter = new MyProfilePresenter(this);
        memberId = getIntent().getLongExtra(ConstantValue.INTENT_KEY_MEMBER_ID, 0l);
        memberNickName = getIntent().getStringExtra(ConstantValue.INTENT_KEY_NICK_NAME);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.account_setting);
        registerDownloadFileListener();
    }

    private void updateInfoView() {
        UserInfo userInfo = UserInfoCache.getInstance().getUserInfo();
        if (userInfo != null) {
            mAccount = userInfo.getAccount();
            //updatePortrait(false);
            if (userInfo.getPhoto() != null && userInfo.getPhoto().contains("https")) {
                refreshPortrait(userInfo.getPhoto());
            } else {
                String photoUrl = FileUtil.getPortraitPhotoPathInPrivate(NooieApplication.mCtx, mUserAccount, mUid);
                File portraitFile = new File(photoUrl);
                if (portraitFile != null && portraitFile.exists()) {
                    refreshPortrait(photoUrl);
                } else {
                    refreshPortrait("");
                    updatePortrait();
                }
            }
            if (memberId > 0 && memberNickName != null) {
                mNickName = memberNickName;
            } else {
                mNickName = userInfo.getNickname();
            }
            tvNickName.setText(mNickName);
            tvAccount.setText(mAccount);
        }
    }

    /*private void updatePortrait(boolean fromLocal) {
        File file = new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
        if (fromLocal && file.exists()) {
            Glide.with(getApplicationContext())
                    .load(file)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .apply(new RequestOptions().circleCrop().placeholder(R.drawable.user).error(R.drawable.user))
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(ivPortrait);
        } else {
            Glide.with(getApplicationContext())
                    .load(GlobalPrefs.getPreferences(getApplicationContext()).getmTuyaPhoto())
                    .apply(new RequestOptions().circleCrop().placeholder(R.drawable.user).error(R.drawable.user))
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(ivPortrait);
        }
    }*/

    private void updatePortrait() {
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
        Glide.with(getApplicationContext())
                .load(GlobalPrefs.getPreferences(getApplicationContext()).getTuyaPhoto())
                .apply(new RequestOptions().circleCrop().placeholder(R.drawable.user).error(R.drawable.user))
                .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                .into(ivPortrait);

    }

    private void refreshPortrait(String portraitPath) {
        if (TextUtils.isEmpty(portraitPath)) {
            ivPortrait.setImageResource(R.drawable.user);
            return;
        }
        // 更新YRCXSDK 和 YRBusiness 模块中的缓存信息
        YRCXSDKDataManager.INSTANCE.setUserHeadPic(portraitPath);
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
        Glide.with(getApplicationContext())
                .load(portraitPath)
                .skipMemoryCache(true) // 不使用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                .apply(new RequestOptions().circleCrop().placeholder(R.drawable.user).error(R.drawable.user))
                .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                .into(ivPortrait);

      /*  if (TextUtils.isEmpty(portraitPath)) {
            ivPortrait.setImageResource(R.drawable.user);
            return;
        }
        Drawable placeHolderDrawable = null;
        try {
            int reqWidth = DisplayUtil.dpToPx(NooieApplication.mCtx, 160);
            int reqHeight = DisplayUtil.dpToPx(NooieApplication.mCtx, 160);
            placeHolderDrawable = BitmapUtil.getBitmapWithOption(portraitPath, reqWidth, reqHeight);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        RequestOptions requestOptions = new RequestOptions();
        if (placeHolderDrawable != null) {
            requestOptions.placeholder(placeHolderDrawable);
        } else {
            requestOptions.placeholder(R.drawable.user);
        }
        requestOptions.dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 15))))
                .format(DecodeFormat.PREFER_RGB_565)
                //.placeholder(R.drawable.user)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .signature(new ObjectKey(System.currentTimeMillis()));
        Glide.with(NooieApplication.mCtx)
                .load(portraitPath)
                //.apply(requestOptions)
                .apply(new RequestOptions().circleCrop().placeholder(R.drawable.user).error(R.drawable.user))
                .transition(withCrossFade())
                .into(ivPortrait);*/
    }

    @OnClick({R.id.ivLeft, R.id.containerNickname, R.id.containerChangePsd, R.id.btnLogout, R.id.containerMediaLogin, R.id.containerChangeProfile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnLogout:
                DialogUtil.showConfirmWithSubMsgDialog(this, R.string.logout, R.string.account_logout_confirm, R.string.cancel, R.string.confirm_upper, mLogoutListener);
                break;
            case R.id.containerChangePsd:
                ChangePasswordActivity.toChangePasswordActivity(this);
                break;
            case R.id.containerNickname:
                SetNameActivity.toSetNameActivity(this, RESULT_CODE_NAME, tvNickName.getText().toString(), memberId);
                break;
            case R.id.containerMediaLogin:
                //todo 第三方绑定
//                ThirdBindTypeActivity.toThirdBindTypeActivity(this, tvAccount.getText().toString());
                break;
            case R.id.containerChangeProfile:
                showPopMenu();
                break;
        }
    }

    private void showPopMenu() {
        if (popMenus != null) {
            popMenus.dismiss();
        }

        popMenus = new PhotoPopupWindows(this, new PhotoPopupWindows.OnClickSelectPhotoListener() {
            @Override
            public void onClick(boolean takePhoto) {
                if (takePhoto) {
                    haveChoosePhoto = false;
                    requestPermissions(PERMS_CAMERA);
                } else {
                    haveChoosePhoto = true;
                    if (EasyPermissions.hasPermissions(MyProfileActivity.this, PERMS_READ_WRITE)) {//已有权限
                        gotoPhoto();
                    } else {
                        requestPermissions(PERMS_READ_WRITE);
                    }
                }
            }
        });
        popMenus.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popMenus = null;
            }
        });

        popMenus.showAtLocation(this.findViewById(R.id.managerAccountRoot),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void gotoPhoto() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 4.4版本
            startActivityForResult(intent, RESULT_CODE_KITKAT_PHOTO);
        } else {
            startActivityForResult(intent, RESULT_CODE_PHOTO);
        }
    }

    private DialogUtil.OnClickConfirmButtonListener mLogoutListener = new DialogUtil.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            showLoadingDialog();
            myProfilePresenter.logout();
        }

        @Override
        public void onClickLeft() {
        }
    };

    private String getLocalHeaderTmpPath() {
        return FileUtil.getTmpAccountNamePortrait(NooieApplication.mCtx, mUserAccount);
    }

    private void startTakePhotoIntent() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getLocalHeaderTmpPath())));
        Uri cropCameraPicUri = convertUriByPath(getLocalHeaderTmpPath());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropCameraPicUri);
        startActivityForResult(intent, RESULT_CODE_CAMERA);
    }

    private boolean clipPicture(Uri inData, Uri outData) {
        try {
            Intent intent = new Intent();
            intent.setAction("com.android.camera.action.CROP");
            intent.setDataAndType(inData, "image/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 加入访问权限
                grantUriPermission(this, intent, inData);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.putExtra("crop", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outData);
            intent.putExtra("dragAndScale", true);
            intent.putExtra("scaleUpIfNeeded", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 加入访问权限
                grantUriPermission(this, intent, outData);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            NooieLog.d("-->> MyProfileActivity test change portrait 11 crop is supported=" + (intent.resolveActivity(getPackageManager()) != null));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, RESULT_CODE_CLIP);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void grantUriPermission(Context context, Intent intent, Uri uri) {
        if (context == null || intent == null || uri == null) {
            return;
        }
        try {
            List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resolveInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Uri saveUri;
        saveUri = Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount)));
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_CODE_CAMERA:
                    //clipPicture(cropUri, saveUri);
                    Uri cropCameraPicUri = convertUriByPath(getLocalHeaderTmpPath());
                    Uri saveCameraPicUri = convertUriByPath(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
                    if (!clipPicture(cropCameraPicUri, saveCameraPicUri)) {
                        clipWithUcrop(Uri.fromFile(new File(getLocalHeaderTmpPath())), Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount))));
                    }
                    break;
                case RESULT_CODE_PHOTO:
                    if (data == null || data.getData() == null) {
                        break;
                    }
                    if (!clipPicture(data.getData(), saveUri)) {
                        clipWithUcrop(Uri.fromFile(new File(getLocalHeaderTmpPath())), Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount))));
                    }
                    break;
                case RESULT_CODE_KITKAT_PHOTO:
                    if (data == null || data.getData() == null) {
                        break;
                    }
                    String imagePath = BitmapUtil.getPath(this, data.getData());
                    NooieLog.d("-->> MyProfileActivity test change portrait 5 image path=" + imagePath + " uri=" + data.getData().getPath());
                    Uri cropPicUri = data.getData();
                    Uri savePicUri = convertUriByPath(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
                    if (!clipPicture(cropPicUri, savePicUri)) {
                        clipWithUcrop(cropPicUri, Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount))));
                    }
                    break;
                case UCrop.REQUEST_CROP:
                case RESULT_CODE_CLIP:
                    try {
                        NooieLog.d("-->> MyProfileActivity test change portrait 10 savePicPath=" + FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
                        if (requestCode == UCrop.REQUEST_CROP) {
                            Uri resultUri = data != null ? UCrop.getOutput(data) : null;
                            NooieLog.d("-->> MyProfileActivity test change portrait 12 ucrop result uri=" + (resultUri != null ? resultUri.getPath() : ""));
                        }
                        //EventBus.getDefault().post(new SelectPortraitEvent(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount)));
                        /*mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), saveUri);
                        if (mBitmap != null) {
                            String compressPath = BitmapUtil.compressImage(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mAccount), FileUtil.getPresetPointThumbnailFolderPath(NooieApplication.mCtx, mAccount), "", 480, 800);
                            NooieLog.d("-->> MyProfileActivity test change portrait 13 compressPath=" + compressPath);
                            showLoadingDialog();
                            myProfilePresenter.setPortrait(new File(compressPath));
                        } else {
                            ToastUtil.showToast(this, R.string.account_select_photo);
                        }*/
                        mIsUploadingPortrait = true;
                        myProfilePresenter.uploadPictures(mUid, mUserAccount, FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case RESULT_CODE_NAME:
                    if (memberId > 0) {
                        memberNickName = data.getStringExtra(ConstantValue.INTENT_KEY_NICK_NAME);
                    }
                    updateInfoView();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void permissionsGranted() {
        super.permissionsGranted();
        if (haveChoosePhoto) {
            gotoPhoto();
        } else {
            startTakePhotoIntent();
        }
    }

    @Override
    public void notifySetPortraitState(String result) {
        hideLoadingDialog();
        if (isPause()) {
            return;
        }

        if (result.equals(ConstantValue.SUCCESS)) {
            //updatePortrait(true);
            EventBus.getDefault().post(new UpdateProfileEvent(true));
            CommonUtil.delayAction(1000, new CommonUtil.OnDelayTimeFinishListener() {
                @Override
                public void onFinish() {
                    GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(MyProfileActivity.this);
                    globalPrefs.setTuyaPhoto(TextUtils.isEmpty(TuyaHomeSdk.getUserInstance().getUser().getHeadPic()) ? "" : TuyaHomeSdk.getUserInstance().getUser().getHeadPic());
                }
            });
        } else {
            ErrorHandleUtil.toastTuyaError(this, result);
        }
    }

    @Override
    public void notifyLoadUserInfoState(String result) {
        if (isPause()) {
            return;
        }

        if (ConstantValue.SUCCESS.equals(result)) {
            updateInfoView();
        } else {
            ErrorHandleUtil.toastTuyaError(this, result);
        }
    }

    @Override
    public void notifyLoadUserInfoSucess(UserInfoResult userInfo) {
        updateInfoView();
    }

    @Override
    public void notifyLogoutState(String state) {
        hideLoadingDialog();
        if (state.equals(ConstantValue.SUCCESS)) {
            //           refreshPortrait("");
            MyAccountHelper.getInstance().logout();
            FamilyManager.getInstance().resetCurrentHome();
            NotificationManager.getInstance().cancelAllNotifications();
            ActivityStack.instance().clearAll(this);
            // 发送中间件信息（清空YRCXSDK 和 YRBusiness 模块中的缓存信息）
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("logout", "");
            YRUserPlatformHelper.INSTANCE.updataUserInfo(hashMap);
            // 发送跳转到登录页命令
            ARouter.getInstance().build("/user/login")
                    .withString("userAccount", mUserAccount)
                    .withString("password", "")
                    .withBoolean("isClearTask", true)
                    .navigation();
//            SignInActivity.toSignInActivity(this, mUserAccount, "", true);
            finish();
        } else {
        }
    }

    @Override
    public void notifyGetUserInfoResult(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            updateInfoView();
        }
    }

    @Override
    public void notifyRefreshUserPortrait(String result, boolean isUploadPortrait) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            refreshPortrait(FileUtil.getPortraitPhotoPathInPrivate(NooieApplication.mCtx, mUserAccount, mUid));
        }
        if (isUploadPortrait) {
            ToastUtil.showToast(this, ConstantValue.SUCCESS.equals(result) ? R.string.account_set_portrait : R.string.get_fail);
        }
        EventBus.getDefault().post(new SelectPortraitEvent(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount)));
        mIsUploadingPortrait = false;
    }

    private Uri convertUriByPath(String path) {
        NooieLog.d("-->> MyProfileActivity test change portrait 6 path=" + path);
        File tmpFile = new File(path);
        if (tmpFile == null || !tmpFile.exists()) {
            NooieLog.d("-->> MyProfileActivity test change portrait 7 tmpfile not exist");
            //return null;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(path));
            NooieLog.d("-->> MyProfileActivity test change portrait 8 uri=" + (uri != null ? uri.getPath() : ""));
        } else {
            uri = Uri.fromFile(new File(path));
            NooieLog.d("-->> MyProfileActivity test change portrait 9 uri=" + (uri != null ? uri.getPath() : ""));
        }
        return uri;
    }

    public void clipWithUcrop(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(300, 300)
                .start(this);
    }

    @Override
    public void notifySetNickNameState(String result) {
        if (isPause()) {
            return;
        }

        if (result.equals(ConstantValue.SUCCESS)) {
            updateInfoView();
            // guard use information
            //UserInfoGuardian.getInstance().setNickName(mAccount, mNickName);
        } else {
            ErrorHandleUtil.toastTuyaError(this, result);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterDownloadFileListener();
    }
}

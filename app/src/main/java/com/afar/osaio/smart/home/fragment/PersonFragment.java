package com.afar.osaio.smart.home.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.account.activity.SignInActivity;
import com.afar.osaio.application.activity.AboutActivity;
import com.afar.osaio.application.activity.FAQActivity;
import com.afar.osaio.bean.LabelItemBean;
import com.afar.osaio.message.activity.MessageActivity;
import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.electrician.activity.MyProfileActivity;
import com.afar.osaio.smart.event.MsgCountUpdateEvent;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.adapter.NormalLabelItemAdapter;
import com.afar.osaio.widget.listener.LabelItemListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.afar.osaio.base.NooieBaseSupportActivity;
import com.afar.osaio.smart.event.TabSelectedEvent;
import com.afar.osaio.smart.lpipc.activity.GatewaySettingsActivity;
import com.afar.osaio.smart.media.activity.MediaActivity;
import com.afar.osaio.util.CommonUtil;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.graphics.BitmapUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.afar.osaio.R;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.application.activity.FeedbackActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieBaseMainFragment;
import com.afar.osaio.bean.UserInfo;
import com.afar.osaio.notification.NotificationManager;
import com.afar.osaio.smart.setting.activity.AppSettingsActivity;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.event.HomeActionEvent;
import com.afar.osaio.smart.event.SelectPortraitEvent;
import com.afar.osaio.smart.home.contract.PersonContract;
import com.afar.osaio.smart.home.presenter.PersonPresenter;
import com.afar.osaio.smart.setting.activity.CustomNameActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.widget.RoundedImageView.RoundedImageView;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DetectionThumbnailCache;
import com.nooie.sdk.processor.cloud.listener.DetectionThumbnailCacheListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import pub.devrel.easypermissions.EasyPermissions;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PersonFragment extends NooieBaseMainFragment implements PersonContract.View {

    private static final int PERSON_SETTING_ITEM_PHOTO = 1;
    private static final int PERSON_SETTING_ITEM_GATEWAY = 2;
    private static final int PERSON_SETTING_ITEM_ABOUT = 3;
    private static final int PERSON_SETTING_ITEM_APP_SETTING = 4;

    private PersonContract.Presenter mPresenter;
    private boolean isFirstRefreshPortrait = true;

    public static PersonFragment newInstance() {
        Bundle args = new Bundle();
        PersonFragment fragment = new PersonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.ivPersonPortrait)
    RoundedImageView ivPersonPortrait;
    @BindView(R.id.tvPersonName)
    TextView tvPersonName;
    @BindView(R.id.rvPersonSetting)
    RecyclerView rvPersonSetting;
    @BindView(R.id.ivPersonMsgPoint)
    ImageView ivPersonMsgPoint;

    private AlertDialog mLogoutDialog;
    private boolean mIsUploadingPortrait = false;
    private NormalLabelItemAdapter mPersonSettingAdapter;
    private DetectionThumbnailCacheListener mDownloadFileListener = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PersonPresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView();
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        NooieLog.d("-->> PersonFragment onResume");
        resumeData();
        //checkMsgUnread();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
        unRegisterDownloadFileListener();
        if (mLogoutDialog != null) {
            mLogoutDialog.dismiss();
            mLogoutDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
    }

    @OnClick({R.id.containerPersonInfo, R.id.ivPersonPortrait, R.id.containerPersonFeedback, R.id.containerFAQ, R.id.ivPersonMsg})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivPersonPortrait:
                //EventBus.getDefault().post(new HomeActionEvent(HomeActionEvent.HOME_ACTION_SHOW_PHOTO_PICKER));
                //break;
            case R.id.containerPersonInfo:
                MyProfileActivity.toMyProfileActivity(_mActivity, 0, "");
                break;
            case R.id.containerPersonFeedback:
                FeedbackActivity.toFeedbackActivity(_mActivity);
                break;
            case R.id.containerFAQ:
                //HelpActivity.toHelpActivity(_mActivity);
                FAQActivity.toFAQActivity(_mActivity);
                break;
            case R.id.ivPersonMsg: {
                if (ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
                    break;
                }
                MessageActivity.toMessageActivity(_mActivity);
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_CUSTOM_NAME:
                    String name = data != null ? data.getStringExtra(ConstantValue.INTENT_KEY_NICK_NAME) : "";
                    if (!TextUtils.isEmpty(name)) {
                        tvPersonName.setText(name);
                        if (mPresenter != null) {
                            mPresenter.changeUserName(name);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void setPresenter(PersonContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        NooieLog.d("-->> Receive TabSelectedEvent PersonFragment onTabSelectedEvent  position=" + event.position);
        if (event == null || event.position != HomeFragment.THIRD) {
            return;
        }
        resumeData();
    }

    /**
     * 接收消息未读数
     *
     * @param event
     */
    @Subscribe
    public void onMsgCountUpdate(MsgCountUpdateEvent event) {
        if (checkActivityIsDestroy() || checkNull(event)) {
            return;
        }
        //displayMsgUnreadPoint(event.count > 0);
    }

    // 无用的
    @Override
    public void notifyLogoutResult(String result) {
        ((NooieBaseSupportActivity) _mActivity).hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            resetPersonInfoView();
            MyAccountHelper.getInstance().logout();
            NotificationManager.getInstance().cancelAllNotifications();

            SignInActivity.toSignInActivity(getActivity(), mUserAccount, "", true);
            _mActivity.finish();
        } else {
        }
    }

    @Override
    public void notifyGetUserInfoResult(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            refreshAccountInfo();
        }
    }

    @Override
    public void notifyChangeUserNameResult(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            ToastUtil.showToast(_mActivity, R.string.success);
            refreshAccountInfo();
        }
    }

    @Override
    public void notifyRefreshUserPortrait(String result, boolean isUploadPortrait) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            refreshPortrait(FileUtil.getPortraitPhotoPathInPrivate(NooieApplication.mCtx, mUserAccount, mUid));
        }
        if (isUploadPortrait) {
            ToastUtil.showToast(_mActivity, ConstantValue.SUCCESS.equals(result) ? R.string.account_set_portrait : R.string.get_fail);
        }
        mIsUploadingPortrait = false;
    }

    @Override
    public void onLoadGatewayDevicesResult(String result, List<GatewayDevice> gatewayDevices) {
        refreshPersonSettingView(CollectionUtil.isNotEmpty(gatewayDevices));
    }

    @Override
    public void onGetUnreadMsgSuccess(int state, MsgUnreadInfo info) {
        if (checkActivityIsDestroy() || checkNull(info) || state == SDKConstant.ERROR) {
            return;
        }
        int unreadCount = info.getSystemUnreadCount();
        for (int i = 0; i < info.getDevMsgUnreadInfos().size(); i++) {
            unreadCount += info.getDevMsgUnreadInfos().get(i).getUnreadCount();
        }
        displayMsgUnreadPoint(unreadCount > 0);
    }

    @Subscribe
    public void onSelectPortraitEvent(SelectPortraitEvent event) {
        if (event == null) {
            return;
        }

       /* if (mPresenter != null) {
            mIsUploadingPortrait = true;
            mPresenter.uploadPictures(mUid, mUserAccount, event.photoPath);
        }*/
        // updatePortrait();
        isFirstRefreshPortrait = true;
        //refreshPortrait(FileUtil.getPortraitPhotoPathInPrivate(NooieApplication.mCtx, mUserAccount, mUid));
        if (UserInfoCache.getInstance().getUserInfo() != null && !TextUtils.isEmpty(UserInfoCache.getInstance().getUserInfo().getPhoto())) {
            refreshPortrait(UserInfoCache.getInstance().getUserInfo().getPhoto());
        }
    }

    private void updatePortrait() {
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
        RequestOptions requestOptions = new RequestOptions();
        //requestOptions.dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 15))))
        requestOptions.dontTransform().transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.ic_account_avatar)
                .error(R.drawable.ic_account_avatar);
        //.diskCacheStrategy(DiskCacheStrategy.NONE)
        //.signature(new ObjectKey(System.currentTimeMillis()));
        Log.d("PersonFragment", "*********************: " + GlobalPrefs.getPreferences(NooieApplication.mCtx).getTuyaPhoto());
        Glide.with(NooieApplication.mCtx)
                .load(GlobalPrefs.getPreferences(NooieApplication.mCtx).getTuyaPhoto())
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                .into(ivPersonPortrait);
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);
        EventBusActivityScope.getDefault(_mActivity).register(this);
        EventBus.getDefault().register(this);
        registerDownloadFileListener();
    }

    private void setupView() {
        setupPersonSettingView();
        refreshAccountInfo();
    }

    private void resumeData() {
        if (!mIsUploadingPortrait && mPresenter != null) {
            String portraitPath = FileUtil.getPersonPortraitInPrivate(NooieApplication.mCtx, mUserAccount).getPath();
            mPresenter.getUserInfo(mUid, mUserAccount, portraitPath);
        }
        if (mPresenter != null) {
            mPresenter.loadGatewayDevices();
        }
        //checkMsgUnread();
    }

    private void refreshAccountInfo() {
        UserInfo userInfo = UserInfoCache.getInstance().getUserInfo();

        if (userInfo != null) {
            tvPersonName.setText(userInfo.getNickname());
            if (userInfo.getPhoto() != null && userInfo.getPhoto().contains("https")) {
                NooieLog.e("-----------PersonFragment updatePhoto https");
                refreshPortrait(userInfo.getPhoto());
            } else {
                String photoUrl = FileUtil.getPortraitPhotoPathInPrivate(NooieApplication.mCtx, mUserAccount, mUid);
                File portraitFile = new File(photoUrl);
                if (portraitFile != null && portraitFile.exists()) {
                    NooieLog.e("-----------PersonFragment updatePhoto portraitFile");
                    refreshPortrait(photoUrl);
                } else {
                    refreshPortrait("");
                }
                //updatePortrait();
            }
        }
    }

    private void refreshPortrait(String portraitPath) {
        if (TextUtils.isEmpty(portraitPath)) {
            ivPersonPortrait.setImageResource(R.drawable.ic_account_avatar);
            updatePortrait();
            return;
        }
        Drawable placeHolderDrawable = null;
        try {
            int reqWidth = DisplayUtil.dpToPx(NooieApplication.mCtx, DisplayUtil.dpToPx(NooieApplication.mCtx, 80));
            int reqHeight = DisplayUtil.dpToPx(NooieApplication.mCtx, DisplayUtil.dpToPx(NooieApplication.mCtx, 80));
            placeHolderDrawable = BitmapUtil.getBitmapWithOption(portraitPath, reqWidth, reqHeight);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        /*RequestOptions requestOptions = new RequestOptions();
        if (placeHolderDrawable != null) {
            requestOptions.placeholder(placeHolderDrawable);
        } else {
            requestOptions.placeholder(R.drawable.ic_account_avatar);
        }
        NooieLog.e("-----------PersonFragment updatePhoto refreshPortrait isFirstRefreshPortrait "+isFirstRefreshPortrait);
        requestOptions.dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 15))))
                .format(DecodeFormat.PREFER_RGB_565)
                //.placeholder(R.drawable.user)
                .error(R.drawable.ic_account_avatar)
                .skipMemoryCache(isFirstRefreshPortrait ? true : false)
                .diskCacheStrategy(isFirstRefreshPortrait ? DiskCacheStrategy.NONE : DiskCacheStrategy.ALL)
                .signature(new ObjectKey(System.currentTimeMillis()));
        Glide.with(NooieApplication.mCtx)
                .load(portraitPath)
                .apply(requestOptions)
                .transition(withCrossFade())
                .into(ivPersonPortrait);*/
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
        Glide.with(NooieApplication.mCtx)
                .load(portraitPath)
                .apply(new RequestOptions().circleCrop().placeholder(R.drawable.ic_account_avatar).error(R.drawable.ic_account_avatar).skipMemoryCache(isFirstRefreshPortrait)
                        .diskCacheStrategy(isFirstRefreshPortrait ? DiskCacheStrategy.NONE : DiskCacheStrategy.ALL).signature(new ObjectKey(System.currentTimeMillis())))
                .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                .into(ivPersonPortrait);
        isFirstRefreshPortrait = false;
    }

    private void setupPersonSettingView() {
        mPersonSettingAdapter = new NormalLabelItemAdapter();
        mPersonSettingAdapter.setListener(new LabelItemListener() {
            @Override
            public void onItemClick(int id, Bundle param) {
                dealOnPersonSettingItemClick(id, param);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(_mActivity);
        rvPersonSetting.setLayoutManager(layoutManager);
        refreshPersonSettingView(false);
    }

    private void refreshPersonSettingView(boolean isShowGateway) {
        if (checkActivityIsDestroy() || checkNull(rvPersonSetting, mPersonSettingAdapter)) {
            return;
        }
        mPersonSettingAdapter.setData(getPersonSettingList(isShowGateway));
        rvPersonSetting.setAdapter(mPersonSettingAdapter);
    }

    private List<LabelItemBean> getPersonSettingList(boolean isShowGateway) {
        List<LabelItemBean> result = new ArrayList<>();
        result.add(new LabelItemBean(PERSON_SETTING_ITEM_PHOTO, getString(R.string.nooie_play_photo_title), R.drawable.ic_account_album_off));
        if (isShowGateway) {
            result.add(new LabelItemBean(PERSON_SETTING_ITEM_GATEWAY, getString(R.string.home_gateway), R.drawable.ic_account_gateway_off));
        }
        result.add(new LabelItemBean(PERSON_SETTING_ITEM_ABOUT, getString(R.string.app_settings_about), R.drawable.ic_account_about_off));
        result.add(new LabelItemBean(PERSON_SETTING_ITEM_APP_SETTING, getString(R.string.home_app_settings), R.drawable.ic_account_set_off));
        return result;
    }

    private void dealOnPersonSettingItemClick(int id, Bundle param) {
        switch (id) {
            case PERSON_SETTING_ITEM_PHOTO: {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !EasyPermissions.hasPermissions(NooieApplication.mCtx, CommonUtil.getStoragePermGroup())) {
                    EventBus.getDefault().post(new HomeActionEvent(HomeActionEvent.HOME_ACTION_ALBUM_STORAGE_PERMISSION));
                    break;
                }
                MediaActivity.toMediaActivity(_mActivity);
                break;
            }
            case PERSON_SETTING_ITEM_GATEWAY: {
                GatewaySettingsActivity.toGatewaySettingsActivity(_mActivity);
                break;
            }
            case PERSON_SETTING_ITEM_ABOUT: {
                AboutActivity.toAboutActivity(_mActivity);
                break;
            }
            case PERSON_SETTING_ITEM_APP_SETTING: {
                AppSettingsActivity.toAppSettingsActivity(_mActivity);
                break;
            }
        }
    }

    private void resetPersonInfoView() {
        refreshPortrait("");
        refreshPersonSettingView(false);
        tvPersonName.setText("");
    }

    private void displayMsgUnreadPoint(boolean show) {
        if (checkActivityIsDestroy() || checkNull(ivPersonMsgPoint)) {
            return;
        }
        ivPersonMsgPoint.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void checkMsgUnread() {
        if (ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            return;
        }
        List<String> idList = new ArrayList<>();
        List<DeviceInfo> devices = NooieDeviceHelper.getAllDeviceInfo();
        for (DeviceInfo deviceInfo : CollectionUtil.safeFor(devices)) {
            idList.add(deviceInfo.getNooieDevice().getUuid());
        }
        if (mPresenter != null) {
            mPresenter.loadMsgUnread(idList);
        }
    }

    private void registerDownloadFileListener() {
        if (mDownloadFileListener == null) {
            mDownloadFileListener = new DetectionThumbnailCacheListener() {
                @Override
                public void onUpdatePortrait(String portraitPath) {
                    NooieLog.d("-->> PersonFragment registerDownloadFileListener onUpdatePortrait portraitPath=" + portraitPath);
                    if (mPresenter != null) {
                        mPresenter.setDownloadPortraitState(false);
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

    private void showLogoutDialog() {
        hideLogoutDialog();
        mLogoutDialog = DialogUtils.showConfirmWithSubMsgDialog(_mActivity, R.string.account_logout, R.string.account_logout_confirm, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mPresenter != null) {
                    ((NooieBaseSupportActivity) _mActivity).showLoading(true);
                    mPresenter.logout();
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideLogoutDialog() {
        if (checkActivityIsDestroy()) {
            return;
        }
        if (mLogoutDialog != null) {
            mLogoutDialog.dismiss();
            mLogoutDialog = null;
        }
    }

    private void startRenameActivity() {
        Intent intent = new Intent(_mActivity, CustomNameActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_CUSTOM_NAME_TYPE_USER);
        intent.putExtra(ConstantValue.INTENT_KEY_TITLE, getString(R.string.account_rename_title));
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, getString(R.string.account_nick_name));
        intent.putExtra(ConstantValue.INTENT_KEY_EVENT_ID, EventDictionary.EVENT_ID_ACCESS_RENAME_NICKNAME);
        startActivityForResult(intent, ConstantValue.REQUEST_CODE_CUSTOM_NAME);
    }

}

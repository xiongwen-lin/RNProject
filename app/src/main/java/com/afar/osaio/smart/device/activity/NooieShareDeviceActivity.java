package com.afar.osaio.smart.device.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DeviceSharedUserInfo;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.device.adapter.AddPersonAdapter;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.device.presenter.INooieShareDevicePresenter;
import com.afar.osaio.smart.device.presenter.NooieShareDevicePresenter;
import com.afar.osaio.smart.device.view.INooieShareDeviceView;
import com.afar.osaio.smart.setting.activity.CloudMoreActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceRelationResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NooieShareDeviceActivity
 *
 * @author Administrator
 * @date 2019/4/19
 */
public class NooieShareDeviceActivity extends BaseActivity implements INooieShareDeviceView, OnRefreshListener, OnLoadMoreListener, AddPersonAdapter.OnClickUserItemListener {

    public static final int PER_PAGE = 100;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.swipe_target)
    RecyclerView swipeTarget;
    @BindView(R.id.swipeToLoadLayout)
    SwipeToLoadLayout swipeToLoadLayout;
    @BindView(R.id.tvCameraAlias)
    TextView tvCameraAlias;
    @BindView(R.id.ipvShareDeviceAccount)
    InputFrameView ipvShareDeviceAccount;
    @BindView(R.id.btnDone)
    FButton btnDone;

    private String mDeviceId;
    private INooieShareDevicePresenter mAddPersonPresenter;

    private AddPersonAdapter mAddPersonAdapter;
    private List<DeviceSharedUserInfo> mUserInfos = new ArrayList<>();

    public static void toNooieShareDeviceActivity(Context from, String deviceId) {
        Intent intent = new Intent(from, NooieShareDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_sharing);
        ButterKnife.bind(this);

        initData();
        initView();
        registerUpdateCameraReceiver();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mAddPersonPresenter = new NooieShareDevicePresenter(this);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setVisibility(View.GONE);
        tvTitle.setText(R.string.camera_share_title);
        setupLearnMoreTv();

        swipeToLoadLayout.setOnRefreshListener(this);
        swipeTarget.setLayoutManager(new LinearLayoutManager(this));
        mAddPersonAdapter = new AddPersonAdapter(mUserInfos);
        swipeTarget.setAdapter(mAddPersonAdapter);
        mAddPersonAdapter.setOnClickUserItemListener(this);

        swipeToLoadLayout.setRefreshEnabled(false);
        swipeToLoadLayout.setLoadMoreEnabled(false);
        setupInputFrameView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAddPersonPresenter != null) {
            showLoading();
            mAddPersonPresenter.getDeviceSharedUserList(mDeviceId, 1, PER_PAGE);
        }
        registerShortLinkKeepListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRefresh();
        unRegisterShortLinkKeepListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideRemoveShareDialog();
        hideShareDialog();
        unRegisterUpdateCameraReceiver();
        if (mAddPersonPresenter != null) {
            mAddPersonPresenter.destroy();
        }
        release();
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        if (swipeTarget != null) {
            swipeTarget.setAdapter(null);
        }
        if (swipeToLoadLayout != null) {
            swipeToLoadLayout.setOnRefreshListener(null);
            swipeToLoadLayout.setOnLoadMoreListener(null);
        }
        tvCameraAlias = null;
        if (ipvShareDeviceAccount != null) {
            ipvShareDeviceAccount.release();
        }
        btnDone = null;

        if (mUserInfos != null) {
            mUserInfos.clear();
            mUserInfos = null;
        }

        mAddPersonAdapter = null;
    }

    private void setupInputFrameView() {
        ipvShareDeviceAccount.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.phone_number_email))
                .setInputBtn(R.drawable.close_icon_state_list)
                .setEtInputMaxLength(ConstantValue.DEVICE_ACCOUNT_MAX_LENGTH)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvShareDeviceAccount.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {

                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
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
        if (!TextUtils.isEmpty(ipvShareDeviceAccount.getInputText())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @Override
    public void onClickRemoveItem(View view, DeviceSharedUserInfo info) {
        showRemoveShareDialog(info);
    }

    @Override
    public void onLongClickItem(View view, DeviceSharedUserInfo info) {
        showRemoveShareDialog(info);
    }

    private Dialog mRemoveShareDialog;
    private void showRemoveShareDialog(DeviceSharedUserInfo info) {
        hideRemoveShareDialog();
        mRemoveShareDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.camera_share_remove_confirm), String.format(getString(R.string.camera_share_remove_info), info.getUserAccount()),
                R.string.camera_settings_no_remove, R.string.camera_settings_remove, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        deleteShareDevice(info);
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
    }

    private void hideRemoveShareDialog() {
        if (mRemoveShareDialog != null) {
            mRemoveShareDialog.dismiss();
            mRemoveShareDialog = null;
        }
    }

    private void deleteShareDevice(DeviceSharedUserInfo info) {
        if (info == null) {
            return;
        }
        int sharedId = TextUtils.isEmpty(info.getUserId()) ? 0 : Integer.parseInt(info.getUserId());
        Iterator sharedDeviceIterator = mUserInfos.iterator();
        while (sharedDeviceIterator.hasNext()) {
            DeviceSharedUserInfo deviceSharedUserInfo = (DeviceSharedUserInfo)sharedDeviceIterator.next();
            if (deviceSharedUserInfo != null && deviceSharedUserInfo.getUserAccount() != null && deviceSharedUserInfo.getUserAccount().equalsIgnoreCase(info.getUserAccount())) {
                sharedDeviceIterator.remove();
                if (mAddPersonPresenter != null) {
                    mAddPersonPresenter.deleteDeviceShared(sharedId);
                }
                break;
            }
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                setIsGotoOtherPage(true);
                finish();
                break;
            case R.id.btnDone:
                hideInputMethod();
                if (TextUtils.isEmpty(ipvShareDeviceAccount.getInputText())) {
                    ToastUtil.showToast(this, R.string.camera_share_send_enter_user_account);
                    return;
                }

                ArrayList<String> accounts = new ArrayList<>();
                for (DeviceSharedUserInfo info : CollectionUtil.safeFor(mUserInfos)) {
                    accounts.add(info.getUserAccount());
                }
                if (!mAddPersonPresenter.checkUser(mUserAccount, ipvShareDeviceAccount.getInputText(), accounts)) {
                    return;
                }

                String deviceName = NooieDeviceHelper.getDeviceInfoById(mDeviceId) != null && NooieDeviceHelper.getDeviceInfoById(mDeviceId).getNooieDevice() != null ? NooieDeviceHelper.getDeviceInfoById(mDeviceId).getNooieDevice().getName() : "";
                showShareDialog(deviceName);
                break;
        }
    }

    private Dialog mShowShareDialog;
    private void showShareDialog(String deviceName) {
        hideShareDialog();
        mShowShareDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.camera_settings_share_camera), String.format(getString(R.string.camera_share_to), deviceName, ipvShareDeviceAccount.getInputText()), R.string.camera_share_no_share, R.string.camera_share_share, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mAddPersonPresenter != null && ipvShareDeviceAccount != null) {
                    showLoading();
                    mAddPersonPresenter.shareDevice(mDeviceId, ipvShareDeviceAccount.getInputText());
                }
            }

            @Override
            public void onClickLeft() {
                if (ipvShareDeviceAccount != null) {
                    ipvShareDeviceAccount.setEtInputText("");
                }
            }
        });
    }

    private void hideShareDialog() {
        if (mShowShareDialog != null) {
            mShowShareDialog.dismiss();
            mShowShareDialog = null;
        }
    }

    @Override
    public void onRefresh() {
        mUserInfos.clear();
        mAddPersonPresenter.getDeviceSharedUserList(mDeviceId, 1, PER_PAGE);
    }

    private void startRefresh() {
        swipeToLoadLayout.setRefreshing(true);
    }

    private void stopRefresh() {
        if (swipeToLoadLayout.isRefreshing()) {
            swipeToLoadLayout.setRefreshing(false);
        }
    }

    private int mNextPage = 2;
    @Override
    public void onLoadMore() {
        mAddPersonPresenter.getDeviceSharedUserList(mDeviceId, mNextPage, PER_PAGE);
    }

    @Override
    public void onShowShareToUsers(DeviceRelationResult result) {
        if (isDestroyed() || checkNull(swipeToLoadLayout, mAddPersonAdapter)) {
            return;
        }

        hideLoading();
        if (result == null) {
            return;
        }

        int currentPage = result.getPage_info() != null ? result.getPage_info().getCurrent_page() : 0;
        int totalPage = result.getPage_info() != null ? result.getPage_info().getTotal_page() : 0;
        if (currentPage == 0 || totalPage == 0) {
            stopRefresh();
            swipeToLoadLayout.setLoadMoreEnabled(false);
            return;
        }

        if (mUserInfos == null) {
            mUserInfos = new ArrayList<>();
        }

        // add owner
        if (currentPage == 1) {
            mUserInfos.clear();
            DeviceSharedUserInfo info = new DeviceSharedUserInfo();
            info.setHeadIconUrl("");
            info.setUserAccount(mUserAccount);
            info.setUserAlias(mUserAccount);
            info.setUserId(mUid);
            info.setDeviceIdList(new ArrayList<String>());
            mUserInfos.add(info);
        }

        for (DeviceRelationResult.DeviceRelation deviceRelation : CollectionUtil.safeFor(result.getData())) {
            if (deviceRelation.getType() == ApiConstant.BIND_TYPE_SHARE) {
                DeviceSharedUserInfo info = new DeviceSharedUserInfo();
                info.setHeadIconUrl("");
                info.setUserAccount(deviceRelation.getAccount());
                info.setUserAlias(deviceRelation.getAccount());
                info.setUserId(String.valueOf(deviceRelation.getId()));
                info.setDeviceIdList(new ArrayList<String>());
                mUserInfos.add(info);
            }
        }

        mAddPersonAdapter.notifyDataSetChanged();
        swipeToLoadLayout.setLoadMoreEnabled(currentPage < totalPage);
        mNextPage = currentPage + 1;
        stopRefresh();
    }

    @Override
    public void onGetShareToUsersError(String message) {
        if (isDestroyed()) {
            return;
        }

        if (!TextUtils.isEmpty(message)) {
            ToastUtil.showToast(this, message);
        }
        hideLoading();
        stopRefresh();
    }

    @Override
    public void onShowUserIsYourself(String userAccount) {
        if (isDestroyed()) {
            return;
        }
        ToastUtil.showLongToast(this, R.string.shared_send_not_share_for_yourself);
    }

    @Override
    public void onUserIsYourSharer(String userAccount) {
        if (isDestroyed()) {
            return;
        }
        ToastUtil.showLongToast(this, R.string.share_send_invitation_wait);
    }

    @Override
    public void onShareDevSuccess(String message) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (!TextUtils.isEmpty(message)) {
            ToastUtil.showToast(this, message);
        }
    }

    @Override
    public void onShareDevFailed(int code, String shareAccount, int num) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (code == StateCode.ACCOUNT_NOT_EXIST.code) {
            ToastUtil.showToast(this, String.format(getString(R.string.shared_send_user_not_exist), shareAccount));
        } else if (code == StateCode.SHARE_ACCOUNT_IN_DIFFERENT_COUNTRY.code) {
            ToastUtil.showToast(this, getString(R.string.share_to_other_area));
        } else if (code == StateCode.SHARE_ACCOUNT_BOND_BY_DEVICE.code) {
            ToastUtil.showLongToast(this, R.string.share_send_invitation_wait);
        } else if (code == StateCode.SHARE_DEVICE_COUNT_OVER.code) {
            int shareMaxNum = num < 1 ? ConstantValue.SHARE_DEVICE_MAX_COUNT : num;
            ToastUtil.showToast(this, String.format(getString(R.string.share_device_count_over), String.valueOf(shareMaxNum)));
        } else if (code == StateCode.ACCOUNT_FORMAT_ERROR.code) {
            ToastUtil.showToast(this, R.string.camera_share_account_invalid);
        } else {
            ToastUtil.showToast(this, NooieApplication.get().getString(R.string.get_fail));
        }
    }

    @Override
    public void notifyDeleteSharedResult(String result) {
        if (isDestroyed()) {
            return;
        }

        if (ConstantValue.SUCCESS.equals(result)) {
            ToastUtil.showLongToast(this, R.string.success);
        } else if (!TextUtils.isEmpty(result)) {
            ToastUtil.showToast(this, result);
        }

        if (mAddPersonPresenter != null) {
            mAddPersonPresenter.getDeviceSharedUserList(mDeviceId, 1, PER_PAGE);
        }
    }

    private void setupLearnMoreTv() {
        BindDevice device = NooieDeviceHelper.getDeviceInfoById(mDeviceId) != null ? NooieDeviceHelper.getDeviceInfoById(mDeviceId).getNooieDevice() : null;
        String deviceName = device != null ? device.getName() : "";
        String learMoreTxt = getString(R.string.camera_share_info_learn_more);
        String text = String.format(getString(R.string.camera_share_info), deviceName, learMoreTxt);
        SpannableStringBuilder style = new SpannableStringBuilder();

        text.length();

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                CloudMoreActivity.toCloudMoreActivity(NooieShareDeviceActivity.this);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        style.setSpan(conditionClickableSpan, text.indexOf(learMoreTxt), text.indexOf(learMoreTxt) + learMoreTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvCameraAlias.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green));
        style.setSpan(conditionForegroundColorSpan, text.indexOf(learMoreTxt), text.indexOf(learMoreTxt) + learMoreTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvCameraAlias.setMovementMethod(LinkMovementMethod.getInstance());
        tvCameraAlias.setText(style);
    }

    class ShareFeedbackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAddPersonPresenter != null) {
                mAddPersonPresenter.getDeviceSharedUserList(mDeviceId, 1, PER_PAGE);
            }
        }
    }

    private ShareFeedbackBroadcastReceiver mShareFeedbackBroadcastReceiver;
    private void registerUpdateCameraReceiver() {
        if (mShareFeedbackBroadcastReceiver == null) {
            mShareFeedbackBroadcastReceiver = new ShareFeedbackBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
            intentFilter.addAction(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mShareFeedbackBroadcastReceiver, intentFilter);
        }
    }

    private void unRegisterUpdateCameraReceiver() {
        if (mShareFeedbackBroadcastReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mShareFeedbackBroadcastReceiver);
            mShareFeedbackBroadcastReceiver = null;
        }
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        BindDevice device = NooieDeviceHelper.getDeviceById(mDeviceId);
        if (device == null) {
            return null;
        }
        String model = device.getType();
        boolean isSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, model, isSubDevice, false, ConstantValue.CONNECTION_MODE_QC);
        return shortLinkDeviceParam;
    }

}

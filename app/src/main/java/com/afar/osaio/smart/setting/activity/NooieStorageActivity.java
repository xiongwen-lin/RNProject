package com.afar.osaio.smart.setting.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseConstant;
import com.afar.osaio.widget.FButton;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.hybrid.webview.HybridWebViewActivity;
import com.afar.osaio.smart.setting.presenter.INooieStoragePresenter;
import com.afar.osaio.smart.setting.presenter.NooieStoragePresenter;
import com.afar.osaio.smart.setting.view.INooieStorageView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceOfOrderResult;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.suke.widget.SwitchButton;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NooieStorageActivity
 *
 * @author Administrator
 * @date 2019/4/18
 */
public class NooieStorageActivity extends BaseActivity implements INooieStorageView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.textView6)
    TextView textView6;
    @BindView(R.id.tvCapacity)
    TextView tvCapacity;
    @BindView(R.id.textView7)
    TextView textView7;
    @BindView(R.id.textView8)
    TextView textView8;
    @BindView(R.id.switchLoopRecording)
    SwitchButton switchLoopRecording;
    @BindView(R.id.tvExpireDate)
    TextView tvExpireDate;
    @BindView(R.id.tvContactUs)
    TextView tvContactUs;
    @BindView(R.id.tvContactUsTime)
    TextView tvContactUsTime;
    @BindView(R.id.textView4)
    TextView tvSubscribe;
    @BindView(R.id.tvCloudUnsubscribe)
    TextView tvCloudUnsubscribe;
    @BindView(R.id.tvCloudStatus)
    TextView tvCloudStatus;
    @BindView(R.id.ivBuyCloud)
    ImageView ivBuyCloud;
    @BindView(R.id.textView3)
    View tvCloudTitle;
    @BindView(R.id.divideLine)
    View divideLine;
    @BindView(R.id.btnFormatSDCard)
    FButton btnFormatSDCard;

    private String mDeviceId;
    private boolean mHaveSDCard;
    private boolean mOpenCloud;
    private boolean mIsSubDevice = false;
    private boolean mIsLpDevice = false;
    private int mConnectionMode;
    private INooieStoragePresenter storagePresenter;

    public static void toNooieStorageActivity(Context from, String deviceId, boolean isSubDevice, int connectionMode, int bindType, boolean isLpDevice, String model) {
        Intent intent = new Intent(from, NooieStorageActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, isSubDevice);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, bindType);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3, isLpDevice);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            setIsGotoOtherPage(true);
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mIsSubDevice = getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, false);
            mIsLpDevice = getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3, false);
            mConnectionMode = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
            storagePresenter = new NooieStoragePresenter(this);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.camera_settings_storage);
        ivRight.setVisibility(View.GONE);
        tvContactUs.setText(mIsLpDevice ? R.string.storage_cloud_tip_sub_device : R.string.storage_cloud_tip);

        tvCapacity.setTag(ConstantValue.NOOIE_SD_STATUS_NO_SD);
        switchLoopRecording.setEnabled(true);
        switchLoopRecording.setOnCheckedChangeListener(loopRecordingListener);
        btnFormatSDCard.setText(NooieDeviceHelper.isDeviceSDCardEnable(getModel(), mIsSubDevice) ? R.string.storage_format_card : R.string.confirm);
        displayCloudView(mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT);
        setupSDView(NooieDeviceHelper.isDeviceSDCardEnable(getModel(), mIsSubDevice), mIsSubDevice);
    }

    private void displayCloudView(boolean show) {
        tvCloudTitle.setVisibility(show ? View.VISIBLE : View.GONE);
        tvCloudStatus.setVisibility(View.GONE);
        tvExpireDate.setVisibility(View.GONE);
        tvCloudUnsubscribe.setVisibility(View.GONE);
        tvSubscribe.setVisibility(show ? View.VISIBLE : View.GONE);
        ivBuyCloud.setVisibility(show ? View.VISIBLE : View.GONE);
        tvContactUs.setVisibility(View.GONE);
        tvContactUsTime.setVisibility(View.GONE);
        divideLine.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setupSDView(boolean isDeviceSDCardEnable, boolean isSubDevice) {
        switchLoopRecording.setEnabled(false);
        textView6.setText(isSubDevice ? R.string.storage_hub_storage_tip : R.string.storage_no_sd_card);
        textView6.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvCapacity.setVisibility(View.GONE);
        textView7.setVisibility(View.GONE);
        textView8.setVisibility(View.GONE);
        switchLoopRecording.setVisibility(View.GONE);
        displaySDView(isDeviceSDCardEnable, isSubDevice);
    }

    private void displaySDView(boolean show, boolean isSubDevice) {
        textView5.setVisibility((show || isSubDevice) ? View.VISIBLE : View.GONE);
        textView6.setVisibility((show || isSubDevice) ? View.VISIBLE : View.GONE);
        tvCapacity.setVisibility(show ? View.VISIBLE : View.GONE);
        textView7.setVisibility(show ? View.VISIBLE : View.GONE);
        textView8.setVisibility(show ? View.VISIBLE : View.GONE);
        switchLoopRecording.setVisibility(show ? View.VISIBLE : View.GONE);
        btnFormatSDCard.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT && storagePresenter != null) {
            storagePresenter.getCloudState(mUserAccount, mDeviceId, getBindType());
        }
        checkDeviceSDState();
        registerShortLinkKeepListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterShortLinkKeepListener();
    }

    private void checkDeviceSDState() {
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(mDeviceId);
        boolean isOnLine = deviceInfo != null && deviceInfo.getNooieDevice() != null && deviceInfo.getNooieDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON;
        NooieLog.d("-->> NooieStorageActivity deviceInfo null " + (deviceInfo == null) + " bindevcie null " + (deviceInfo != null && deviceInfo.getNooieDevice() == null) + " isOnline=" + isOnLine + " isSubDevice=" + mIsSubDevice + " isLpDevice=" + mIsLpDevice);

        if (NooieDeviceHelper.isDeviceSDCardEnable(getModel(), mIsSubDevice) && (isOnLine || mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT)) {
            storagePresenter.getLoopRecordStatus(mDeviceId);
            storagePresenter.loadSDCardInfo(mUserAccount, mDeviceId, NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam()));
        } else {
            displaySDView(false, mIsSubDevice);
        }
    }

    private void hindSdView() {
        int[] viewIds = {R.id.divideLine, R.id.textView5, R.id.textView6, R.id.tvCapacity, R.id.textView7, R.id.switchLoopRecording};
        for (int i = 0; i < viewIds.length; i++) {
            if (findViewById(viewIds[i]) != null) {
                findViewById(viewIds[i]).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (storagePresenter != null) {
            storagePresenter.stopQuerySDCardFormatState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (storagePresenter != null) {
            storagePresenter.destroy();
        }
        hideUnsubscribePackdialog();
        hideLoading();
        release();
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        textView6 = null;
        tvCapacity = null;
        textView7 = null;
        textView8 = null;
        if (switchLoopRecording != null) {
            switchLoopRecording.setOnCheckedChangeListener(null);
        }
        tvExpireDate = null;
        tvContactUs = null;
        tvContactUsTime = null;
        tvSubscribe = null;
        tvCloudUnsubscribe = null;
        tvCloudStatus = null;
        ivBuyCloud = null;
        btnFormatSDCard = null;
        tvCloudTitle = null;
        divideLine = null;
    }

    private void setupClickableTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        final String email = getString(R.string.support_email_address);
        String text = String.format(getString(R.string.storage_contact_us), email);

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Uri uri = Uri.parse("mailto:" + email);
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(Intent.createChooser(intent, getString(R.string.about_select_email_application)));
            }
        };
        style.setSpan(clickableSpan, text.indexOf(email), text.indexOf(email) + email.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvContactUs.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_white));
        style.setSpan(foregroundColorSpan, text.indexOf(email), text.indexOf(email) + email.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvContactUs.setMovementMethod(LinkMovementMethod.getInstance());
        tvContactUs.setText(style);
    }

    private SwitchButton.OnCheckedChangeListener loopRecordingListener = new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            if (storagePresenter != null) {
                storagePresenter.setLoopRecordStatus(mDeviceId, isChecked);
            }
        }
    };

    @OnClick({R.id.ivLeft, R.id.ivBuyCloud, R.id.tvCloudUnsubscribe, R.id.btnFormatSDCard})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                setIsGotoOtherPage(true);
                finish();
                break;
            case R.id.ivBuyCloud:
                toCloudBuyPage(this, mDeviceId, NooieCloudHelper.createEnterMark(mUid), FirebaseConstant.EVENT_CLOUD_ORIGIN_FROM_STORAGE);
                break;
            case R.id.tvCloudUnsubscribe:
                if (!TextUtils.isEmpty(mDeviceId)) {
                    loadDeviceOfOrder(mDeviceId);
                }
                break;
            case R.id.btnFormatSDCard:
                if (!NooieDeviceHelper.isDeviceSDCardEnable(getModel(), mIsSubDevice)) {
                    setIsGotoOtherPage(true);
                    finish();
                    break;
                }
                //if (Util.isFormattingCard(mDeviceId)) {
                if (checkIsFormattingCard()) {
                    ToastUtil.showToast(this, R.string.storage_sd_card_is_formatting);
                } else if (!mHaveSDCard) {
                    ToastUtil.showToast(this, R.string.storage_no_sdcard_tip);
                } else {
                    DialogUtils.showConfirmWithSubMsgDialog(this, R.string.storage_format_card, R.string.storage_format_card_into,
                            R.string.cancel, R.string.confirm_upper, formatListener);
                }
                break;
        }
    }

    DialogUtils.OnClickConfirmButtonListener unsubscribeListener = new DialogUtils.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            if (storagePresenter != null) {
                showLoading();
                storagePresenter.unsubscribePack(mUserAccount, mDeviceId);
            }
        }

        @Override
        public void onClickLeft() {

        }
    };

    DialogUtils.OnClickConfirmButtonListener formatListener = new DialogUtils.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            DialogUtils.showInformationNormalDialog(NooieStorageActivity.this, getResources().getString(R.string.storage_format_card), getResources().getString(R.string.storage_format_card_into_confirm), false, confirmFormatListener);
        }

        @Override
        public void onClickLeft() {
        }
    };

    DialogUtils.OnClickInformationDialogLisenter confirmFormatListener = new DialogUtils.OnClickInformationDialogLisenter() {
        @Override
        public void onConfirmClick() {
            if (storagePresenter != null) {
                storagePresenter.formatSDCard(mDeviceId);
            }
        }
    };

    @Override
    public void notifyQuerySDStatusSuccess(int status, String freeGB, String totalGB, int progress) {
        if (isDestroyed() || checkNull(switchLoopRecording, textView6, tvCapacity, textView7, textView8)) {
            return;
        }
        tvCapacity.setTag(status);
        switch (status) {
            case ConstantValue.NOOIE_SD_STATUS_NORMAL: {
                mHaveSDCard = true;
                switchLoopRecording.setEnabled(true);
                textView6.setText(R.string.storage_use_of_space);
                textView6.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
                tvCapacity.setVisibility(View.VISIBLE);
                tvCapacity.setText(String.format("%sGB/%sGB", freeGB, totalGB));
                tvCapacity.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
                textView7.setVisibility(View.VISIBLE);
                textView8.setVisibility(View.VISIBLE);
                switchLoopRecording.setVisibility(View.VISIBLE);
                break;
            }
            case ConstantValue.NOOIE_SD_STATUS_FORMATING: {
                mHaveSDCard = true;
                switchLoopRecording.setEnabled(false);
                textView6.setText(R.string.storage_use_of_space);
                textView6.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
                tvCapacity.setVisibility(View.VISIBLE);
                tvCapacity.setText(getResources().getString(R.string.storage_formatting) + "(" + progress + "%)");
                textView7.setVisibility(View.VISIBLE);
                switchLoopRecording.setVisibility(View.VISIBLE);
                break;
            }
            case ConstantValue.NOOIE_SD_STATUS_NO_SD: {
                switchLoopRecording.setEnabled(false);
                textView6.setText(R.string.storage_no_sd_card);
                textView6.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
                textView6.setVisibility(View.VISIBLE);
                tvCapacity.setVisibility(View.GONE);
                textView7.setVisibility(View.GONE);
                textView8.setVisibility(View.GONE);
                switchLoopRecording.setVisibility(View.GONE);
                break;
            }
            case ConstantValue.NOOIE_SD_STATUS_DAMAGE: {
                mHaveSDCard = true;
                switchLoopRecording.setEnabled(false);
                textView6.setText(R.string.storage_sd_card_damaged);
                textView6.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
                textView6.setVisibility(View.VISIBLE);
                tvCapacity.setVisibility(View.GONE);
                textView7.setVisibility(View.GONE);
                textView8.setVisibility(View.GONE);
                switchLoopRecording.setVisibility(View.GONE);
                break;
            }
            default: {
            }
        }
    }

    @Override
    public void notifyQuerySDStatusFailed(String msg) {
        if (isDestroyed()) {
            return;
        }

        ToastUtil.showToast(this, msg);
    }

    @Override
    public void notifyGetLoopRecordingSuccess(boolean open) {
        if (isDestroyed() || checkNull(switchLoopRecording)) {
            return;
        }
        textView8.setText(open ? R.string.close_record_detected : R.string.on_recording_all);
        hideLoadingDialog();
        switchLoopRecording.setEnabled(true);
        if (switchLoopRecording.isChecked() != open) {
            switchLoopRecording.toggleNoCallback();
        }
    }

    @Override
    public void notifyGetLoopRecordingFailed(String message) {
        if (isDestroyed()) {
            return;
        }
        hideLoadingDialog();
    }

    @Override
    public void notifySetLoopRecordingResult(String result) {

        if (isDestroyed()) {
            return;
        }
        hideLoadingDialog();
        if (result.equalsIgnoreCase(ConstantValue.SUCCESS)) {
        } else {
        }
    }

    @Override
    public void notifyGetCloudInfoSuccess(BaseResponse<PackInfoResult> response) {
        if (isDestroyed()) {
            return;
        }

        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(mDeviceId);
        float deviceTimezone = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getZone() : CountryUtil.getCurrentTimeZone();
        PackInfoResult info = response.getData();
        mOpenCloud = response != null && response.getCode() == StateCode.SUCCESS.code && info != null ? NooieCloudHelper.isOpenCloud(info.getEnd_time(), deviceTimezone) : false;
        boolean isSubscribe = info != null && NooieCloudHelper.isSubscribeCloud(info.getStatus());
        if (mOpenCloud || isSubscribe) {
            // open
            tvCloudStatus.setVisibility(View.VISIBLE);
            tvExpireDate.setVisibility(View.VISIBLE);
            tvCloudUnsubscribe.setVisibility(View.GONE);
            tvContactUs.setVisibility(View.GONE);
            tvSubscribe.setVisibility(View.GONE);
            ivBuyCloud.setVisibility(View.GONE);

            tvCloudStatus.setText(String.format(getResources().getString(R.string.storage_status), getResources().getString(R.string.storage_status_active)));
            String time = info != null && info.getTotal_time() - 1 > 0 ? DateTimeUtil.getTimeString((info.getTotal_time() - 1) * 1000, "MM/dd/yyyy") : getString(R.string.unknown);
            tvExpireDate.setText(String.format(getString(R.string.storage_expire_date), time));
            tvCloudUnsubscribe.setText(getResources().getString(R.string.unsubscribe_up_case));

            if (info != null && NooieCloudHelper.isSubscribeCloud(info.getStatus())) {
                tvCloudUnsubscribe.setVisibility(View.VISIBLE);
            } else {
                tvCloudUnsubscribe.setVisibility(View.GONE);
                tvSubscribe.setVisibility(View.VISIBLE);
                ivBuyCloud.setVisibility(View.VISIBLE);
            }
        } else {
            // not open cloud
            tvCloudStatus.setVisibility(View.GONE);
            tvExpireDate.setVisibility(View.GONE);
            tvContactUs.setVisibility(View.VISIBLE);
            tvContactUsTime.setVisibility(View.GONE);
            tvSubscribe.setVisibility(View.VISIBLE);
            ivBuyCloud.setVisibility(View.VISIBLE);
            tvSubscribe.setText(getResources().getString(R.string.subscribe));
            tvCloudUnsubscribe.setVisibility(View.GONE);
            if (info != null && NooieCloudHelper.isSubscribeCloud(info.getStatus())) {
                //tvExpireDate.setVisibility(View.INVISIBLE);
                tvExpireDate.setVisibility(View.VISIBLE);
                tvExpireDate.setText(String.format(getString(R.string.storage_expire_date), "--"));
                tvCloudUnsubscribe.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void notifyGetCloudInfoFailed(String message) {
        if (isDestroyed()) {
            return;
        }
        mOpenCloud = false;
    }

    @Override
    public void notifyFormatCardResult(String result) {
        if (isDestroyed() || checkNull(tvCapacity, storagePresenter)) {
            return;
        }

        if (result.equalsIgnoreCase(ConstantValue.SUCCESS)) {
            storagePresenter.startQuerySDCardFormatState(mDeviceId);
            tvCapacity.setText(R.string.storage_formatting);
            tvCapacity.setTag(ConstantValue.NOOIE_SD_STATUS_FORMATING);
        } else {
            ToastUtil.showToast(this, result);
        }
    }

    @Override
    public void notifyUnsubscribePackResult(String result) {
        if (isDestroyed() || storagePresenter == null) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            ToastUtil.showToast(NooieStorageActivity.this, R.string.success);
            storagePresenter.getCloudState(mUserAccount, mDeviceId, getBindType());
        } else {
            ToastUtil.showToast(NooieStorageActivity.this, R.string.get_fail);
        }
    }

    @Override
    public void notifyNoStorage() {
        if (isDestroyed() || (mOpenCloud || mHaveSDCard)) {
            return;
        }

        DialogUtils.showConfirmWithSubMsgDialog(this, R.string.message_no_storage, R.string.message_please_subscribe_cloud, R.string.ignore, R.string.subscribe_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                toCloudBuyPage(NooieStorageActivity.this, mDeviceId, NooieCloudHelper.createEnterMark(mUid), FirebaseConstant.EVENT_CLOUD_ORIGIN_FROM_STORAGE);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    @Override
    public void onLoadDeviceOfOrder(String result, String deviceId, List<DeviceOfOrderResult> orderResult) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            String content = getString(R.string.storage_unsubscribe_info);
            if (CollectionUtil.size(orderResult) > 1) {
                StringBuilder contentSb = new StringBuilder();
                Iterator<DeviceOfOrderResult> deviceOrderIterator = orderResult.iterator();
                while (deviceOrderIterator.hasNext()) {
                    DeviceOfOrderResult deviceOfOrder = deviceOrderIterator.next();
                    if (TextUtils.isEmpty(deviceOfOrder.getUuid()) || deviceOfOrder.getUuid().equalsIgnoreCase(deviceId)) {
                        deviceOrderIterator.remove();
                    }
                }

                for (int i = 0; i < CollectionUtil.size(orderResult); i++) {
                    DeviceOfOrderResult deviceOfOrder = orderResult.get(i);
                    if (deviceOfOrder != null && deviceOfOrder.getUuid() != null) {
                        contentSb.append(deviceOfOrder.getUuid());
                        if (i < CollectionUtil.size(orderResult) - 1) {
                            contentSb.append(CConstant.COMMA);
                        }
                    }
                }

                if (!TextUtils.isEmpty(contentSb.toString())) {
                    content = String.format(getString(R.string.storage_unsubscribe_info_for_relation), contentSb.toString());
                }
            }
            NooieLog.d("-->> NooieStorageActivity onLoadDeviceOfOrder deviceId=" + deviceId + " content=" + content);
            showUnsubscribePackDialog(content);
        } else {
            ToastUtil.showToast(this, R.string.network_error0);
        }
    }

    @Override
    public boolean checkIsAddDeviceApHelperListener() {
        return true;
    }

    private void loadDeviceOfOrder(String deviceId) {
        if (storagePresenter != null) {
            showLoading();
            storagePresenter.getDeviceOfOrder(deviceId);
        }
    }

    private Dialog mShowUnsubscribePackDialog = null;

    private void showUnsubscribePackDialog(String content) {
        hideUnsubscribePackdialog();
        mShowUnsubscribePackDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.unsubscribe), content, R.string.cancel, R.string.confirm_upper, unsubscribeListener);
    }

    private void hideUnsubscribePackdialog() {
        if (mShowUnsubscribePackDialog != null) {
            mShowUnsubscribePackDialog.dismiss();
            mShowUnsubscribePackDialog = null;
        }
    }

    private int getBindType() {
        int bindType = ApiConstant.BIND_TYPE_OWNER;
        if (getCurrentIntent() != null) {
            bindType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, ApiConstant.BIND_TYPE_OWNER);
        }
        return bindType;
    }

    private boolean checkIsFormattingCard() {
        return tvCapacity != null && tvCapacity.getTag() != null && ((Integer) tvCapacity.getTag()) == ConstantValue.NOOIE_SD_STATUS_FORMATING;
    }

    private boolean checkIsShowLoopRecording() {
        return mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT;
    }

    public static void toCloudBuyPage(Context from, String deviceId, String enterMark, String origin) {
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
        if (deviceInfo != null && deviceInfo.getNooieDevice() != null) {
            //String url = "http://172.16.21.17:805/v2/pack/list-v2?model= IPC300-CAM&uuid=7087f82b0365882025f48323269e910d&uid=aac98c8c5a7bab98&appid=da7c2ef9dac8535d" + "&enter_mark=" + enterMark + "&origin=" + origin;
            String url = NooieCloudHelper.createCloudPackUrl(deviceInfo.getNooieDevice().getUuid(), deviceInfo.getNooieDevice().getType(), enterMark, origin);
            Bundle param = new Bundle();
            param.putString(ConstantValue.INTENT_KEY_URL, url);
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, true);
            HybridWebViewActivity.toHybridWebViewActivity(from, param);
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
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, model, isSubDevice, false, mConnectionMode);
        return shortLinkDeviceParam;
    }

    @Override
    public CurrentDeviceParam getCurrentDeviceParam() {
        if (TextUtils.isEmpty(mDeviceId)) {
            return null;
        }
        CurrentDeviceParam currentDeviceParam = null;
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            currentDeviceParam = new CurrentDeviceParam();
            currentDeviceParam.setDeviceId(mDeviceId);
            currentDeviceParam.setConnectionMode(mConnectionMode);
            currentDeviceParam.setModel(getModel());
        } else {
        }
        return currentDeviceParam;
    }

    private String getModel() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
    }
}

package com.afar.osaio.smart.electrician.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.presenter.IMemberPresenter;
import com.afar.osaio.smart.electrician.presenter.MemberPresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IMemberView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nooie.common.widget.RoundedImageView.RoundedImageView;
import com.tuya.smart.home.sdk.bean.ShareSentUserDetailBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * MemberActivity
 * 设备分享者
 *
 * @author Administrator
 * @date 2019/3/15
 */
public class MemberActivity extends BaseActivity implements IMemberView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivPortrait)
    RoundedImageView ivPortrait;
    @BindView(R.id.tvNickName)
    TextView tvNickName;
    @BindView(R.id.tvAccount)
    TextView tvAccount;
    @BindView(R.id.tvMemberAccess)
    TextView tvMemberAccess;
    @BindView(R.id.tvMemberSharedDevice)
    TextView tvMemberSharedDevice;
    @BindView(R.id.containerSharedDevice)
    LinearLayout containerSharedDevice;
    @BindView(R.id.ivNameArrow)
    ImageView ivNameArrow;
    @BindView(R.id.ivSharedArrow)
    ImageView ivSharedArrow;
    @BindView(R.id.btnMemberRemove)
    FButton btnMemberRemove;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private IMemberPresenter mMemberPresenter;

    private String account;
    private long memberId;
    private String name = "";
    private final static int MIN_PLURAL = 2;

    private boolean isAdmin;
    private String mDeviceId;
    private String mMemberOperate;
    private String mNickName;
    private String access;
    private String mUrl;
    private String mDeviceName;

    public static void toMemberActivity(Activity from, int requestCode, long memberId, String account, boolean isAdmin, String deviceId, String memberOperate, String nickName, String url) {
        Intent intent = new Intent(from, MemberActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_ID, memberId);
        intent.putExtra(ConstantValue.INTENT_KEY_ACCOUNT, account);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_HOME_ADMIN, isAdmin);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_OPERATE, memberOperate);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, nickName);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_URL, url);
        from.startActivityForResult(intent, requestCode);
    }

    public static void toMemberActivity(Activity from, int requestCode, long memberId, String account, boolean isAdmin, String deviceId, String memberOperate, String nickName, String url, String deviceName) {
        Intent intent = new Intent(from, MemberActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_ID, memberId);
        intent.putExtra(ConstantValue.INTENT_KEY_ACCOUNT, account);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_HOME_ADMIN, isAdmin);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_OPERATE, memberOperate);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, nickName);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_URL, url);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.profile);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mMemberPresenter = new MemberPresenter(this);
            memberId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_MEMBER_ID, 0);
            account = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT);
            isAdmin = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_HOME_ADMIN, false);
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mMemberOperate = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_MEMBER_OPERATE);
            mNickName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_NICK_NAME);
            mUrl = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_NICK_URL);
            mDeviceName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
            initUi();
            if (!TextUtils.isEmpty(account)) {
                mMemberPresenter.getAccount(account);
            }
            mMemberPresenter.loadUserShareInfo(memberId);

            containerSharedDevice.setVisibility(View.VISIBLE);
            tvNickName.setText(mNickName);
        }
    }

    private void initUi() {
        access = getResources().getString(R.string.guest);
        ivNameArrow.setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
        ivSharedArrow.setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
        tvMemberAccess.setText(getResources().getString(R.string.guest));
        if (!TextUtils.isEmpty(mUrl)) {
            Glide.with(NooieApplication.mCtx)
                    .load(mUrl)
                    .apply(new RequestOptions().circleCrop().placeholder(R.drawable.user).error(R.drawable.user))
                    .into(ivPortrait);
        }
    }

    @SuppressLint("StringFormatInvalid")
    @OnClick({R.id.containerNickname, R.id.containerSharedDevice, R.id.btnMemberRemove, R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.containerNickname: {
                if (isAdmin) {
                    SetNameActivity.toSetNameActivity(this, ConstantValue.REQUEST_CODE_MEMBER_RENAME, tvNickName.getText().toString(), memberId);
                }
                break;
            }
            case R.id.containerSharedDevice: {
                MemberAccessActivity.toMemberAccessActivity(MemberActivity.this, ConstantValue.REQUEST_CODE_DEVICE_REMOVE, memberId,
                        tvAccount.getText().toString(), mUrl, mDeviceId, mDeviceName);
                break;
            }
            case R.id.btnMemberRemove: {
                DialogUtil.showConfirmWithSubMsgDialog(this, getString(R.string.remove_access),
                        String.format(getResources().getString(R.string.remove_access_tip), name, access.toLowerCase()), R.string.cancel, R.string.confirm_upper, new DialogUtil.OnClickConfirmButtonListener() {
                            @Override
                            public void onClickRight() {
                                long memberId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_MEMBER_ID, 0);
                                if (memberId != 0) {
                                    showLoadingDialog();
                                    if (mMemberOperate.equalsIgnoreCase(ConstantValue.REMOVE_HOME_GUEST)) {
                                        mMemberPresenter.removeUserShare(memberId);
                                    } else if (mMemberOperate.equalsIgnoreCase(ConstantValue.REMOVE_SINGLE_DEVICE_SHARE)) {
                                        mMemberPresenter.removeMemberForSingleDevice(memberId, mDeviceId);
                                    }
                                }
                            }

                            @Override
                            public void onClickLeft() {
                            }
                        });
                break;
            }
            case R.id.ivLeft: {
                setResult(RESULT_OK, new Intent());
                finish();
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent());
        finish();
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_DEVICE_REMOVE: {
                    mMemberPresenter.loadUserShareInfo(memberId);
                    break;
                }
                case ConstantValue.REQUEST_CODE_MEMBER_RENAME: {
                    mMemberPresenter.loadUserShareInfo(memberId);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void notifyRemoveMemberState(String msg) {
        hideLoadingDialog();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } else {
        }
    }

    @Override
    public void notifyLoadUserShareInfoSuccess(ShareSentUserDetailBean detailBean) {
        if (detailBean != null && detailBean.getDevices() != null) {
            if (detailBean.getDevices().size() < MIN_PLURAL) {
                tvMemberSharedDevice.setText(String.format(getResources().getString(R.string.device_singular), detailBean.getDevices().size()));
            } else {
                tvMemberSharedDevice.setText(String.format(getResources().getString(R.string.device_plural), detailBean.getDevices().size()));
            }
            if (detailBean.getRemarkName() != null && detailBean.getRemarkName().length() > 0) {
                name = detailBean.getRemarkName();
                tvNickName.setText(detailBean.getRemarkName());
            } else if (detailBean.getNameWithoutRemark() != null) {
                name = detailBean.getNameWithoutRemark();
                tvNickName.setText(detailBean.getNameWithoutRemark());
            }
        }
    }

    @Override
    public void notifyLoadUserShareInfoFailed(String msg) {
    }

    @Override
    public void notifyGetAccountSuccess(String account) {
        tvAccount.setText(account);
    }

    @Override
    public void notifyGetAccountFailed(String msg) {
        ErrorHandleUtil.toastTuyaError(this, msg);
    }

    @Override
    public void notifyRemoveUserShareSuccess(String msg) {
        hideLoadingDialog();
        if (msg.equals(ConstantValue.SUCCESS)) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void notifyRemoveUserShareFail(String error) {
        hideLoadingDialog();
    }
}

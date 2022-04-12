package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.adapter.MemberAccessDeviceAdapter;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.IMemberAccessPresenter;
import com.afar.osaio.smart.electrician.presenter.MemberAccessPresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IMemberAccessView;
import com.afar.osaio.util.ConstantValue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tuya.smart.home.sdk.bean.DeviceShareBean;
import com.tuya.smart.home.sdk.bean.ShareSentUserDetailBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * MemberAccessActivity
 *
 * @author jiangzt
 * @date 2019/4/25
 */

public class MemberAccessActivity extends BaseActivity implements IMemberAccessView {

    public static final int OPTION_EDIT = 0;
    public static final int OPTION_DONE = 1;

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvOption)
    TextView tvOption;
    @BindView(R.id.ivPortrait)
    ImageView ivPortrait;
    @BindView(R.id.ivDelete)
    ImageView ixvDelete;
    @BindView(R.id.rcvDevices)
    RecyclerView rcvDevices;

    private IMemberAccessPresenter mMemberAccessPresenter;
    private MemberAccessDeviceAdapter mDevicesAdapter;
    private long memberId;
    private String mEmail;
    private String mUrl;
    private String mDeviceId;
    private String mDeviceName;

    public static void toMemberAccessActivity(Activity from, int requestCode, long memberId, String email, String url, String deviceId, String deviceName) {
        Intent intent = new Intent(from, MemberAccessActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_ID, memberId);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_EMAIL, email);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_URL, url);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_access);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvOption.setTag(OPTION_EDIT);
        tvTitle.setText(R.string.shared_access);
        ivRight.setImageResource(R.drawable.add_icon);
        setupDevicesView();
    }

    private void initData() {

        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mMemberAccessPresenter = new MemberAccessPresenter(this);
            memberId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_MEMBER_ID, 0);
            mEmail = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_MEMBER_EMAIL);
            mUrl = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_NICK_URL);
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mDeviceName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
            setUpView();
            mMemberAccessPresenter.loadUserShareInfo(memberId);
        }
    }

    private void setUpView() {
        tvEmail.setText(mEmail);
        Glide.with(NooieApplication.mCtx)
                .load(mUrl)
                .apply(new RequestOptions().circleCrop().placeholder(R.drawable.user).error(R.drawable.user))
                .into(ivPortrait);
    }

    @OnClick({R.id.tvOption, R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tvOption:
                toggleOption();
                break;
            case R.id.ivLeft:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.ivRight:
                //name可以不要 , 类做修改 ，传空，那边不显示
                SelectShareDeviceActivity.toSelectShareDeviceActivity(MemberAccessActivity.this, ConstantValue.REQUEST_CODE_DEVICE_ADD,
                        FamilyManager.getInstance().getCurrentHomeId(), null, mDevicesAdapter.getDeicesIds(), memberId);
                break;
        }
    }

    private void setupDevicesView() {
        mDevicesAdapter = new MemberAccessDeviceAdapter();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);

        rcvDevices.setLayoutManager(layoutManager);
        rcvDevices.setAdapter(mDevicesAdapter);
        ((SimpleItemAnimator) rcvDevices.getItemAnimator()).setSupportsChangeAnimations(false);

//        int leftRight = DisplayUtil.dpToPx(this, 16);
//        int topBottom = DisplayUtil.dpToPx(this, 16);
//        rcvDevices.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));

        mDevicesAdapter.setListener(new MemberAccessDeviceAdapter.MemberAccessDeviceListener() {
            @Override
            public void onDeleteClick(DeviceShareBean device, int position) {
                showDeleteDialog(position);
            }

            @Override
            public void onItemClick(DeviceShareBean device, int position) {

            }
        });
    }

    /**
     * 删除
     */
    private void showDeleteDialog(final int position) {
        DialogUtil.showConfirmWithSubMsgDialog(MemberAccessActivity.this, R.string.remove_access, String.format(getResources().getString(R.string.member_device_remove_tip), tvName.getText().toString(), mDevicesAdapter.getDeviceByPosition(position).getDeviceName()),
                R.string.cancel, R.string.confirm_upper, new DialogUtil.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        mMemberAccessPresenter.removeDevice(memberId, mDevicesAdapter.getDeviceByPosition(position).getDevId());
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
    }

    private void toggleOption() {
        if ((int) tvOption.getTag() == OPTION_DONE) {
            tvOption.setTag(OPTION_EDIT);
            tvOption.setText(R.string.delete_uppercase);
            mDevicesAdapter.setShowDelete(false);
        } else {
            tvOption.setTag(OPTION_DONE);
            tvOption.setText(R.string.DONE);
            mDevicesAdapter.setShowDelete(true);
        }
    }

    private void initOption() {
        tvOption.setTag(OPTION_EDIT);
        tvOption.setText(R.string.delete_uppercase);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_DEVICE_ADD: {
                    mMemberAccessPresenter.loadUserShareInfo(memberId);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void notifyRemoveDeviceState(String msg) {
        if (msg.equalsIgnoreCase(ConstantValue.SUCCESS)) {
            mMemberAccessPresenter.loadUserShareInfo(memberId);
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void notifyLoadUserShareInfoSuccess(ShareSentUserDetailBean detailBean) {
        if (detailBean != null) {
            if (detailBean.getRemarkName() != null && detailBean.getRemarkName().length() > 0) {
                tvName.setText(detailBean.getRemarkName());
            } else if (detailBean.getNameWithoutRemark() != null) {
                tvName.setText(detailBean.getNameWithoutRemark());
            }
            mDevicesAdapter.setData(detailBean.getDevices(), false);
            initOption();
        }
    }

    @Override
    public void notifyLoadUserShareInfoFailed(String msg) {
        if (msg.equals("PERMISSION_DENIED")) {
            DeviceShareUsersActivity.toDeviceShareUsersActivity(MemberAccessActivity.this, mDeviceId, FamilyManager.getInstance().getCurrentHomeId(), mDeviceName, 0);
        }
        //ErrorHandleUtil.toastTuyaError(this, msg);
    }
}

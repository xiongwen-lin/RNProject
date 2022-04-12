package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.DeviceShareUsersAdapter;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.DeviceShareUsersPresenter;
import com.afar.osaio.smart.electrician.presenter.IDeviceShareUsersPresenter;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.IDeviceShareUsersView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * DeviceShareUsersActivity
 * <p>
 * 查询指定设备的分享用户列表
 *
 * @author Administrator
 * @date 2019/3/27
 */
public class DeviceShareUsersActivity extends BaseActivity implements IDeviceShareUsersView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @BindView(R.id.rcvShareUsers)
    RecyclerView rcvShareUsers;

    @BindView(R.id.btnAddDeviceShare)
    Button btnAddDeviceShare;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private DeviceShareUsersAdapter mDeviceShareUsersAdapter;

    private IDeviceShareUsersPresenter mPresenter;

    private String mDeviceId;
    private String mDeviceName;

    private boolean isAdmin = true;

    public static void toDeviceShareUsersActivity(Activity from, String deviceId, long homeId, String deviceName, int requestCode) {
        Intent intent = new Intent(from, DeviceShareUsersActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_ID, homeId);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_share_users);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.dev_sharing);
        setupSahreUsersView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mDeviceName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
            mPresenter = new DeviceShareUsersPresenter(this);
            mPresenter.getHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
            mPresenter.queryDevShareUserList(mDeviceId);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mDeviceName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
            mPresenter = new DeviceShareUsersPresenter(this);
            mPresenter.getHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
            if (mPresenter != null) {
                mPresenter.queryDevShareUserList(mDeviceId);
            }
        }
    }

    @OnClick({R.id.btnAddDeviceShare, R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddDeviceShare: {
                SingleDeviceShareActivity.toSingleDeviceShareActivity(DeviceShareUsersActivity.this, mDeviceId, FamilyManager.getInstance().getCurrentHomeId(), mDeviceName, ConstantValue.REQUEST_CODE_ADD_MEMBER_SINGLE);
                break;
            }
            case R.id.ivLeft: {
                finish();
                break;
            }
        }
    }

    public void setupSahreUsersView() {
        mDeviceShareUsersAdapter = new DeviceShareUsersAdapter();
        mDeviceShareUsersAdapter.setListener(new DeviceShareUsersAdapter.DeviceShareUserListListener() {
            @Override
            public void onItemClick(SharedUserInfoBean sharedUserInfoBean) {
                MemberActivity.toMemberActivity(DeviceShareUsersActivity.this, ConstantValue.REQUEST_CODE_MEMBER_INFO, sharedUserInfoBean.getMemeberId(),
                        sharedUserInfoBean.getUserName(), isAdmin, mDeviceId, ConstantValue.REMOVE_SINGLE_DEVICE_SHARE, sharedUserInfoBean.getRemarkName(), sharedUserInfoBean.getIconUrl(), mDeviceName);
            }

        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rcvShareUsers.setLayoutManager(layoutManager);
        rcvShareUsers.setAdapter(mDeviceShareUsersAdapter);


        int leftRight = DisplayUtil.dpToPx(this, 16);
        int topBottom = DisplayUtil.dpToPx(this, 16);
        rcvShareUsers.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_ADD_MEMBER_SINGLE:
                case ConstantValue.REQUEST_CODE_MEMBER_INFO: {
                    mPresenter.queryDevShareUserList(mDeviceId);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void notifyGetHomeDetailSuccess(HomeBean homeBean) {
        isAdmin = homeBean.isAdmin();
    }

    @Override
    public void notifyGetHomeDetailFailed(String msg) {

    }

    @Override
    public void queryDevShareUserListSuccess(List<SharedUserInfoBean> sharedUserInfoBeanList) {
        mDeviceShareUsersAdapter.setData(sharedUserInfoBeanList);
    }

    @Override
    public void queryDevShareUserListError(String error) {
        ToastUtil.showToast(this, error);
    }
}

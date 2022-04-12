package com.afar.osaio.smart.electrician.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.GroupTypeAdapter;
import com.afar.osaio.smart.electrician.fragment.LampGroupFragment;
import com.afar.osaio.smart.electrician.fragment.PlugGroupFragment;
import com.afar.osaio.smart.electrician.presenter.CreateGroupPresenter;
import com.afar.osaio.smart.electrician.presenter.ICreateGroupPresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.ICreateGroupView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.google.android.material.tabs.TabLayout;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * CreateGroupActivity
 *
 * @author Administrator
 * @date 2019/3/20
 */
public class CreateGroupActivity extends BaseActivity implements ICreateGroupView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tlSwitchType)
    TabLayout tlGroupType;
    @BindView(R.id.vpSwitchType)
    ViewPager vpSwitchType;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private ICreateGroupPresenter mCreateCroupPresenter;
    private List<String> mGroupType;
    private List<Fragment> mGroupTypeFragment;
    private GroupTypeAdapter mGroupTypeAdapter;

    private PlugGroupFragment mPlugGroupFragment;
    private LampGroupFragment mLampGroupFragment;

    public static void toCreateCroupActivity(Context from) {
        Intent intent = new Intent(from, CreateGroupActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
            mCreateCroupPresenter = new CreateGroupPresenter(this);
            mGroupType = new ArrayList<>();
            mGroupTypeFragment = new ArrayList<>();
            mGroupType.add(getResources().getString(R.string.plug));
            mGroupType.add(getResources().getString(R.string.light));
            mGroupTypeFragment.add(mPlugGroupFragment = new PlugGroupFragment());
            mGroupTypeFragment.add(mLampGroupFragment = new LampGroupFragment());

            mGroupTypeAdapter = new GroupTypeAdapter(getSupportFragmentManager(), mGroupType, mGroupTypeFragment);

            vpSwitchType.setAdapter(mGroupTypeAdapter);
            vpSwitchType.setOffscreenPageLimit(mGroupTypeFragment.size());
            tlGroupType.setupWithViewPager(vpSwitchType);
            tvTitle.setText(R.string.create_group);
            ivRight.setImageResource(R.drawable.define_black);
        }
    }

    @Override
    public void notifyLoadDevicesSuccess(List<GroupDeviceBean> devices) {
    }

    @Override
    public void notifyDevicesFailed(String msg) {
        ErrorHandleUtil.toastTuyaError(this,msg);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivRight: {
                if (vpSwitchType.getCurrentItem() == 0) {
                    NooieLog.e("------plugGroup");
                    if (mPlugGroupFragment.getPlugAdapter() != null) {
                        ArrayList<String> selectedDeviceIds = mPlugGroupFragment.getPlugAdapter().getSelectedDevice();
                        if (CollectionUtil.isNotEmpty(selectedDeviceIds)) {
                            NameGroupActivity.toNameGroupActivity(CreateGroupActivity.this, ConstantValue.SMART_PLUG_PRODUCTID, selectedDeviceIds, ConstantValue.GROUP_CREATE);
                        } else {
                            ToastUtil.showToast(this, R.string.select_least_one_device);
                        }
                    }
                }
                if (vpSwitchType.getCurrentItem() == 1) {
                    NooieLog.e("------lightGroup");
                    if (mLampGroupFragment.getLampAdapter() != null) {
                        ArrayList<String> selectedDeviceIds = mLampGroupFragment.getLampAdapter().getSelectedDevice();
                        if (CollectionUtil.isNotEmpty(selectedDeviceIds)) {
                            //NameGroupActivity.toNameGroupActivity(CreateGroupActivity.this, ConstantValue.SMART_LAMP_PRODUCTID, selectedDeviceIds, ConstantValue.GROUP_CREATE);
                            NameGroupActivity.toNameGroupActivity(CreateGroupActivity.this, ConstantValue.SMART_LAMP_PRODUCTID_FOUR, selectedDeviceIds, ConstantValue.GROUP_CREATE);
                        } else {
                            ToastUtil.showToast(this, R.string.select_least_one_device);
                        }
                    }
                }
                break;
            }
        }
    }
}

package com.afar.osaio.smart.electrician.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.smart.electrician.adapter.SelectedDeviceAdapter;
import com.afar.osaio.smart.electrician.bean.DeviceHelper;
import com.afar.osaio.smart.electrician.presenter.CreateGroupPresenter;
import com.afar.osaio.smart.electrician.presenter.ICreateGroupPresenter;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.ICreateGroupView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LampGroupFragment extends Fragment implements ICreateGroupView {

    @BindView(R.id.rcvCreateGroupDevice)
    RecyclerView rcvCreateGroupDevice;

    private Unbinder unbinder;
    private SelectedDeviceAdapter mDeviceAdapter;
    private ICreateGroupPresenter mCreateCroupPresenter;
    private List<GroupDeviceBean> mGroups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lamp_group_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        initData();
        return view;
    }

    private void initData() {
        mCreateCroupPresenter = new CreateGroupPresenter(this);

        //mCreateCroupPresenter.loadDevices(ConstantValue.SMART_LAMP_PRODUCTID);
        mCreateCroupPresenter.loadDevices(ConstantValue.SMART_LAMP_PRODUCTID_FOUR);

    }

    public SelectedDeviceAdapter getLampAdapter() {
        return mDeviceAdapter;
    }

    private void initView() {
        setupDeviceList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setupDeviceList() {
        mDeviceAdapter = new SelectedDeviceAdapter();
        mDeviceAdapter.setListener(new SelectedDeviceAdapter.DeviceItemListener() {
            @Override
            public void onItemClick(DeviceBean device) {
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (CollectionUtil.isEmpty(DeviceHelper.covertDeviceBean(mGroups))) {
                    return 2;
                }
                return 1;
            }
        });
        rcvCreateGroupDevice.setLayoutManager(layoutManager);
        rcvCreateGroupDevice.setAdapter(mDeviceAdapter);

        int leftRight = DisplayUtil.dpToPx(getActivity(), 16);
        int topBottom = DisplayUtil.dpToPx(getActivity(), 16);
        rcvCreateGroupDevice.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));
    }

    @Override
    public void notifyLoadDevicesSuccess(List<GroupDeviceBean> devices) {
        if (devices != null && devices.size() > 0) {
            mGroups = devices;
            NooieLog.e("-------------lampgroup notifyLoadDevicesSuccess    " + devices.size());
            mDeviceAdapter.setData(DeviceHelper.covertDeviceBean(devices));
        }
    }

    @Override
    public void notifyDevicesFailed(String msg) {

    }

    @Override
    public void showLoadingDialog() {

    }

    @Override
    public void hideLoadingDialog() {

    }
}

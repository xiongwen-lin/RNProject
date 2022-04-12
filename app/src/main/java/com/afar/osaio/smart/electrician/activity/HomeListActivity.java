package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.HomeListAdapter;
import com.afar.osaio.smart.electrician.eventbus.HomeChangeEvent;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.HomeListPresenter;
import com.afar.osaio.smart.electrician.presenter.IHomeListPresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IHomeListView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.api.service.MicroServiceManager;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.home.sdk.bean.HomeBean;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeListActivity extends BaseActivity implements IHomeListView {

    @BindView(R.id.rvcList)
    RecyclerView rvcList;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    private HomeListAdapter mAdapter;
    private IHomeListPresenter mPresenter;

    public static void toHomeListActivity(Activity from) {
        Intent intent = new Intent(from, HomeListActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_list);
        ButterKnife.bind(this);
        initView();
        initData();
    }


    public void initView() {
        tvTitle.setText(R.string.home_manager);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.menu_icon_add_state_list);
    }

    public void initData() {
        mPresenter = new HomeListPresenter(this);
        setupHomeListView();
    }

    private void setupHomeListView() {
        mAdapter = new HomeListAdapter();
        mAdapter.setListener(new HomeListAdapter.HomeListListener() {
            @Override
            public void onItemClick(HomeBean homeBean) {
                mPresenter.changeCurrentHome(homeBean.getHomeId());
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvcList.setLayoutManager(layoutManager);
        rvcList.setAdapter(mAdapter);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivRight: {
                AddHomeActivity.toAddHomeActivity(this);
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadHomes();
    }

    @Override
    public void notifyLoadHomesSuccess(List<HomeBean> homes) {
        mAdapter.setData(homes);
    }

    @Override
    public void notifyLoadHomesFailed(String code, String msg) {
        ErrorHandleUtil.toastTuyaError(this, msg);
    }

    @Override
    public void notifyChangeHomeState(String code) {
        if (code.equals(ConstantValue.SUCCESS)) {
            AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
            //设置为当前家庭的homeId
            service.setCurrentHomeId(FamilyManager.getInstance().getCurrentHomeId());
            EventBus.getDefault().post(new HomeChangeEvent(FamilyManager.getInstance().getCurrentHomeId()));
            HomeManagerActivity.toHomeManagerActivity(HomeListActivity.this, 0);
        } else {
            ErrorHandleUtil.toastTuyaError(this, code);
        }
    }

}


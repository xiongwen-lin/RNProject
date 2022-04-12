package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.smartScene.adapter.SmartSceneTypeAdapter;
import com.afar.osaio.smart.electrician.smartScene.fragment.AutomationFragment;
import com.afar.osaio.smart.electrician.smartScene.fragment.TapToRunFragment;
import com.afar.osaio.smart.electrician.smartScene.manager.SceneManager;
import com.afar.osaio.smart.electrician.smartScene.presenter.IRunAndAutoPresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.RunAndAutoPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.IRunAndAutoView;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.sdk.api.ISmartUpdateListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TeckinSmartSceneActivity extends BaseActivity implements IRunAndAutoView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tlSwitchType)
    TabLayout tlSceneType;
    @BindView(R.id.vpSwitchType)
    ViewPager vpSwitchType;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private IRunAndAutoPresenter mPresenter;
    private List<String> mSceneType;
    private List<Fragment> mSceneTypeFragment;
    private SmartSceneTypeAdapter mSmartSceneTypeAdapter;

    private TapToRunFragment mTapToRunFragment;
    private AutomationFragment mAutomationFragment;
    private List<SceneBean> launchList;
    private List<SceneBean> autoList;

    public static void toTeckinSmartSceneActivity(Activity from) {
        Intent intent = new Intent(from, TeckinSmartSceneActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_smart_scene);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getSceneList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TuyaHomeSdk.getSceneManagerInstance().onDestroy();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mPresenter = new RunAndAutoPresenter(this);
            mSceneType = new ArrayList<>();
            mSceneTypeFragment = new ArrayList<>();
            mSceneType.add(getResources().getString(R.string.tap_to_run));
            mSceneType.add(getResources().getString(R.string.automation));
            mSceneTypeFragment.add(mTapToRunFragment = new TapToRunFragment());
            mSceneTypeFragment.add(mAutomationFragment = new AutomationFragment());

            mSmartSceneTypeAdapter = new SmartSceneTypeAdapter(getSupportFragmentManager(), mSceneType, mSceneTypeFragment);

            vpSwitchType.setAdapter(mSmartSceneTypeAdapter);
            vpSwitchType.setOffscreenPageLimit(mSceneTypeFragment.size());
            tlSceneType.setupWithViewPager(vpSwitchType);

            TuyaHomeSdk.getSceneManagerInstance().registerSmartUpdateListener(new ISmartUpdateListener() {
                @Override
                public void onSmartUpdateListener() {
                    NooieLog.e("---------------sceneBean change");
                    mPresenter.getSceneList();
                }

                @Override
                public void onCollectionsUpdateListener() {

                }
            });
        }
    }

    private void initView() {
        tvTitle.setText(R.string.smart);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.add_black);
        autoList = new ArrayList<>();
        launchList = new ArrayList<>();
    }

    @OnClick({R.id.ivSort, R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivSort: {
                if (vpSwitchType.getCurrentItem() == 0) {
                    SceneSortActivity.toSceneSortActivity(this, new Gson().toJson(launchList));
                } else if (vpSwitchType.getCurrentItem() == 1) {
                    SceneSortActivity.toSceneSortActivity(this, new Gson().toJson(autoList));
                }
                break;
            }
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivRight: {
                CreateSceneActivity.toCreateSceneActivity(this, false, "");
                break;
            }
        }
    }

    @Override
    public void notifyGetSceneListSuccess(List<SceneBean> sceneBeanList) {
        SceneManager.getInstance().syncSceneList(sceneBeanList);
        autoList.clear();
        launchList.clear();
        for (SceneBean sceneBean : sceneBeanList) {
            if (sceneBean != null && sceneBean.getConditions() != null) {
                autoList.add(sceneBean);
            }
        }
        for (SceneBean sceneBean : sceneBeanList) {
            if (sceneBean != null && sceneBean.getConditions() == null) {
                launchList.add(sceneBean);
            }
        }
    }

    @Override
    public void notifyGetSceneListFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyExecuteSceneSuccess() {

    }

    @Override
    public void notifyExecuteSceneFail(String errorMsg) {

    }

    @Override
    public void showLoadingDialog() {

    }

    @Override
    public void hideLoadingDialog() {

    }
}

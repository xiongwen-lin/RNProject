package com.afar.osaio.smart.electrician.smartScene.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.smartScene.activity.CreateNewSmartActivity;
import com.afar.osaio.smart.electrician.smartScene.adapter.SceneAutomationAdapter;
import com.afar.osaio.smart.electrician.smartScene.manager.SceneManager;
import com.afar.osaio.smart.electrician.smartScene.presenter.IRunAndAutoPresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.RunAndAutoPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.IRunAndAutoView;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AutomationFragment extends Fragment implements IRunAndAutoView, SceneManager.IAutoSceneBeanCallBack {

    @BindView(R.id.rcvAutomation)
    RecyclerView rcvAutomation;

    private IRunAndAutoPresenter mPresenter;
    private SceneAutomationAdapter mAdapter;
    private Unbinder unbinder;
    private List<SceneBean> automationList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.automation_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        initData();
        initView();
        return view;
    }

    private void initData() {
        mPresenter = new RunAndAutoPresenter(this);
        SceneManager.getInstance().setAutoSceneBeanCallback(this);
    }

    private void initView() {
        automationList = new ArrayList<>();
        mAdapter = new SceneAutomationAdapter();
        mAdapter.setListener(new SceneAutomationAdapter.AutomationItemListener() {
            @Override
            public void onModifyClick(int position, SceneBean sceneBean) {
                CreateNewSmartActivity.toCreateNewSmartActivity(getActivity(), sceneBean);
            }

            @Override
            public void onSwitchClick(SceneBean sceneBean, boolean isChecked) {
                if (isChecked) {
                    mPresenter.enableScene(sceneBean.getId());
                } else {
                    mPresenter.disableScene(sceneBean.getId());
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(NooieApplication.mCtx);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcvAutomation.setLayoutManager(layoutManager);
        rcvAutomation.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void notifyGetSceneListSuccess(List<SceneBean> sceneBeanList) {

    }

    @Override
    public void notifyGetSceneListFail(String errorMsg) {

    }

    @Override
    public void notifyExecuteSceneSuccess() {

    }

    @Override
    public void notifyExecuteSceneFail(String errorMsg) {

    }

    @Override
    public void callAutoBackSceneBean(List<SceneBean> sceneBeanList) {
        mAdapter.setData(sceneBeanList);
    }

    @Override
    public void showLoadingDialog() {

    }

    @Override
    public void hideLoadingDialog() {

    }
}

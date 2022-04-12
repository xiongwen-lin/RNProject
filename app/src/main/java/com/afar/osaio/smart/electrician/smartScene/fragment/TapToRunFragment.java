package com.afar.osaio.smart.electrician.smartScene.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.smartScene.activity.CreateNewSmartActivity;
import com.afar.osaio.smart.electrician.smartScene.adapter.TaptoRunAdapter;
import com.afar.osaio.smart.electrician.smartScene.manager.SceneManager;
import com.afar.osaio.smart.electrician.smartScene.presenter.IRunAndAutoPresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.RunAndAutoPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.IRunAndAutoView;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TapToRunFragment extends Fragment implements SceneManager.ILaunchSceneBeanCallBack, IRunAndAutoView {

    @BindView(R.id.rcvTapToRun)
    RecyclerView rcvTapToRun;

    private IRunAndAutoPresenter mPresenter;
    private TaptoRunAdapter mAdapter;
    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tap_to_run_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        initData();
        initView();
        return view;
    }

    private void initData() {
        mPresenter = new RunAndAutoPresenter(this);
        SceneManager.getInstance().setLaunchSceneBeanCallback(this);
    }

    private void initView() {
        mAdapter = new TaptoRunAdapter();
        mAdapter.setListener(new TaptoRunAdapter.LaunchItemListener() {
            @Override
            public void onItemClick(int position, SceneBean sceneBean) {
                mPresenter.executeScene(sceneBean.getId());
            }

            @Override
            public void onModifyClick(SceneBean sceneBean) {
                CreateNewSmartActivity.toCreateNewSmartActivity(getActivity(), sceneBean);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(NooieApplication.mCtx, 2);
        rcvTapToRun.setLayoutManager(layoutManager);
        rcvTapToRun.setAdapter(mAdapter);
        int leftRight = DisplayUtil.dpToPx(Objects.requireNonNull(getActivity()), 16);
        int topBottom = DisplayUtil.dpToPx(Objects.requireNonNull(getActivity()), 16);
        rcvTapToRun.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));
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
        ToastUtil.showToast(getActivity(), R.string.success);
    }

    @Override
    public void notifyExecuteSceneFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(getActivity(), errorMsg);
    }


    @Override
    public void callLaunchBackSceneBean(List<SceneBean> sceneBeanList) {
        mAdapter.setData(sceneBeanList);
    }

    @Override
    public void showLoadingDialog() {

    }

    @Override
    public void hideLoadingDialog() {

    }
}

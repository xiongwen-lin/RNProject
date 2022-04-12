package com.afar.osaio.smart.electrician.smartScene.activity;

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
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.smartScene.adapter.SortAdapter;
import com.afar.osaio.smart.electrician.smartScene.component.SceneSortComponent;
import com.afar.osaio.smart.electrician.smartScene.presenter.SortPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.ISortSceneView;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SceneSortActivity extends BaseActivity implements ISortSceneView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.rcvSort)
    RecyclerView rcvSort;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private SortAdapter mAdapter;
    private List<SceneBean> sceneBeanList;
    private List<String> sceneIds;
    private SortPresenter mPresenter;
    private SceneSortComponent mSortComponent;

    public static void toSceneSortActivity(Activity from, String sceneBean) {
        Intent intent = new Intent(from, SceneSortActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_sort_smart_scene);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.management);
        ivRight.setImageResource(R.drawable.define_black);
        sceneBeanList = new ArrayList<>();
        sceneIds = new ArrayList<>();
        setupSortView();
    }

    private void setupSortView() {
        mAdapter = new SortAdapter();
        mAdapter.setListener(new SortAdapter.SortItemListener() {
            @Override
            public void onItemDelete(int position, SceneBean sceneBean) {
                showDeleteSceneDialog(sceneBean);
            }

            @Override
            public void onStartDragItem(RecyclerView.ViewHolder holder) {
                if (mSortComponent != null) {
                    mSortComponent.startDrag(holder);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(NooieApplication.mCtx);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcvSort.setLayoutManager(layoutManager);
        rcvSort.setAdapter(mAdapter);

        mSortComponent = new SceneSortComponent();
        mSortComponent.setDeviceRv(rcvSort);
        mSortComponent.setDeviceAdapter(mAdapter);
        mSortComponent.setupItemTouchHelper();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mPresenter = new SortPresenter(this);
            String sceneBean = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            sceneBeanList.clear();
            sceneBeanList = new Gson().fromJson(sceneBean, new TypeToken<List<SceneBean>>() {
            }.getType());
            mAdapter.setData(sceneBeanList);
            mSortComponent.setSortList();
        }
    }

    @OnClick({R.id.ivLeft,R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivRight: {
                if (mSortComponent != null) {
                    sceneBeanList.clear();
                    sceneBeanList.addAll(mSortComponent.getSceneBeanList());
                    for (SceneBean sceneBean : sceneBeanList) {
                        sceneIds.add(sceneBean.getId());
                    }
                    if (CollectionUtil.isEmpty(mAdapter.getData())){
                        TeckinSmartSceneActivity.toTeckinSmartSceneActivity(this);
                    }else {
                        mPresenter.sortSceneList(FamilyManager.getInstance().getCurrentHomeId(), sceneIds);
                    }
                }
                break;
            }
        }
    }

    private void showDeleteSceneDialog(final SceneBean sceneBean) {
        DialogUtil.showConfirmDialog(this, NooieApplication.mCtx.getResources().getString(R.string.remove_scene) + sceneBean.getName(), R.string.cancel, R.string.confirm_upper, true, new DialogUtil.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mPresenter.deleteScene(sceneBean);
            }

            @Override
            public void onClickLeft() {

            }
        });
    }

    @Override
    public void notifyDeleteSceneSuccess(SceneBean sceneBean) {
        ToastUtil.showToast(this, "Delete Success");
        mAdapter.removeItem(sceneBean);
        mSortComponent.setSortList();
    }

    @Override
    public void notifyDeleteSceneFailed(String errorCode, String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifySortSceneListSuccess() {
        TeckinSmartSceneActivity.toTeckinSmartSceneActivity(this);
    }

    @Override
    public void notifySortSceneListFailed(String errorCode, String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }
}

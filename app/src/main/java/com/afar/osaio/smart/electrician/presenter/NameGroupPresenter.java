package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.INameGroupView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.List;

/**
 * NameGroupPresenter
 *
 * @author Administrator
 * @date 2019/3/20
 */
public class NameGroupPresenter implements INameGroupPresenter {

    private INameGroupView mNameGroupView;

    public NameGroupPresenter(INameGroupView view) {
        mNameGroupView = view;
    }

    @Override
    public void createGroup(String productId, String name, List<String> deviceIds) {
        TuyaHomeSdk.newHomeInstance(FamilyManager.getInstance().getCurrentHomeId()).createGroup(productId, name, deviceIds, new ITuyaResultCallback<Long>() {
            @Override
            public void onSuccess(Long groupId) {
                if (mNameGroupView != null) {
                    mNameGroupView.notifyNameGroupState(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mNameGroupView != null) {
                    mNameGroupView.notifyNameGroupState(code);
                }
            }
        });
    }

    @Override
    public void renameGroup(long groupId, String name) {
        TuyaHomeSdk.newGroupInstance(groupId).renameGroup(name, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mNameGroupView != null) {
                    mNameGroupView.notifyNameGroupState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mNameGroupView != null) {
                    mNameGroupView.notifyNameGroupState(ConstantValue.SUCCESS);
                }
            }
        });
    }
}

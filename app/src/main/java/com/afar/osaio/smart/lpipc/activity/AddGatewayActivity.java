package com.afar.osaio.smart.lpipc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class AddGatewayActivity extends BaseActivity implements TplContract.View {

    private static final int REQUEST_CODE_FOR_CAMERA = 1;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    private TplContract.Presenter mPresenter;

    public static void toAddGatewayActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, AddGatewayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gateway);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_gateway_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRes();
        release();
    }

    private void release() {
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                checkPerm();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void permissionsGranted(int requestCode) {
        if (requestCode == REQUEST_CODE_FOR_CAMERA) {
            DeviceScanCodeActivity.toDeviceScanCodeActivity(this, getStartParam());
        }
    }

    public void checkPerm() {
        if (EasyPermissions.hasPermissions(this, ConstantValue.PERM_GROUP_CAMERA)) {
            DeviceScanCodeActivity.toDeviceScanCodeActivity(this, getStartParam());
        } else {
            requestPermission(ConstantValue.PERM_GROUP_CAMERA, REQUEST_CODE_FOR_CAMERA);
        }
    }
}

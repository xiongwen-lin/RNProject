package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.tool.PatternMatchUtil;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.scan.bean.NooieScanDeviceCache;
import com.afar.osaio.util.ConstantValue;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by victor on 2018/6/30
 * Email is victor.qiao.0604@gmail.com
 */
public class ScanCameraResultActivity extends BaseActivity {
    private static final String TAG = ScanCameraResultActivity.class.getName();

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvBack)
    TextView tvBack;
    @BindView(R.id.cbDevices)
    ConvenientBanner cbDevices;

    private IpcType mDeviceType;

    public static void toScanCameraResultActivity(Context from, String model, int connectionMode) {
        Intent intent = new Intent(from, ScanCameraResultActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_camera_result);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        mDeviceType = getCurrentIntent() != null ? IpcType.getIpcType(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL)) : IpcType.getIpcType(getIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL));
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_connected_failed);
        ivRight.setVisibility(View.GONE);
        setupClickableTv();
        setupDevicesCb(mDeviceType.getType());
    }

    private void setupClickableTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        String text = getString(R.string.add_camera_back_home);

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                HomeActivity.toHomeActivity(ScanCameraResultActivity.this);
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(clickableSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvBack.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_gray));
        style.setSpan(foregroundColorSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvBack.setMovementMethod(LinkMovementMethod.getInstance());
        tvBack.setText(style);
    }

    private void setupDevicesCb(String model) {
        List<BindDevice> availableDevice = NooieScanDeviceCache.getInstance().getBindByOtherDeviceInfoEntityList();
        if (availableDevice.size() == 0) {
            return;
        }
        cbDevices.setPages(new CBViewHolderCreator() {
            @Override
            public Holder createHolder(View itemView) {
                return new DeviceHolderView(itemView, getApplicationContext());
            }

            @Override
            public int getLayoutId() {
                return R.layout.item_device_display;
            }
        }, availableDevice)
                .setPageIndicator(new int[]{R.drawable.point_gray, R.drawable.point_black})
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL);
        if (availableDevice.size() > 1) {
            cbDevices.startTurning(3000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cbDevices != null) {
            cbDevices.stopTurning();
        }
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        tvBack = null;
        if (cbDevices != null) {
            cbDevices.setOnPageChangeListener(null);
            cbDevices = null;
        }
    }

    @OnClick({R.id.btnRemove, R.id.ivLeft})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.btnRemove:
                AddACameraActivity.toAddACameraActivity(this, mDeviceType.getType(), getConnectionMode());
                break;
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    private static class DeviceHolderView extends Holder<BindDevice> {
        private Context mContext;
        private ImageView tvDeviceIcon;
        private TextView tvDeviceOwnerName;
        private TextView tvDeviceOwnerMode;

        public DeviceHolderView(View view, Context context) {
            super(view);
            mContext = context;
        }

        @Override
        protected void initView(View itemView) {
            tvDeviceIcon = (ImageView) itemView.findViewById(R.id.tvDeviceIcon);
            tvDeviceOwnerName = (TextView) itemView.findViewById(R.id.tvDeviceOwnerName);
            tvDeviceOwnerMode = (TextView) itemView.findViewById(R.id.tvDeviceOwnerMode);
        }

        @Override
        public void updateUI(BindDevice data) {
            if (data != null) {
                IpcType ipcType = IpcType.getIpcType(data.getType());
                if (NooieDeviceHelper.mergeIpcType(ipcType) ==  IpcType.PC420 || NooieDeviceHelper.mergeIpcType(ipcType) == IpcType.MC120) {
                    Glide.with(mContext).load(R.drawable.nooie_cam_big).apply(new RequestOptions().error(R.drawable.nooie_cam_big)).transition(withCrossFade()).into(tvDeviceIcon);
                } else if (NooieDeviceHelper.mergeIpcType(ipcType) == IpcType.PC530) {
                    Glide.with(mContext).load(R.drawable.nooie360_cam_big).apply(new RequestOptions().error(R.drawable.nooie360_cam_big)).transition(withCrossFade()).into(tvDeviceIcon);
                } else if (NooieDeviceHelper.mergeIpcType(ipcType) == IpcType.PC730) {
                    Glide.with(mContext).load(R.drawable.nooie_outdoor_cam_big).apply(new RequestOptions().error(R.drawable.nooie_outdoor_cam_big)).transition(withCrossFade()).into(tvDeviceIcon);
                } else if (NooieDeviceHelper.mergeIpcType(ipcType) == IpcType.EC810PRO) {
                    Glide.with(mContext).load(R.drawable.device_icon_lp_810).apply(new RequestOptions().error(R.drawable.device_icon_lp_810)).transition(withCrossFade()).into(tvDeviceIcon);
                } else {
                    Glide.with(mContext).load(R.drawable.nooie_cam_big).apply(new RequestOptions().error(R.drawable.nooie_cam_big)).transition(withCrossFade()).into(tvDeviceIcon);
                }
                //Glide.with(mContext).load(R.drawable.nooie_cam_big).apply(new RequestOptions().error(R.drawable.nooie_cam_big)).transition(withCrossFade()).into(tvDeviceIcon);

                StringBuilder ownerBuilder = new StringBuilder();
                /*
                ownerBuilder.append(mContext.getResources().getString(R.string.scan_owner));
                ownerBuilder.append("\n");
                */
                ownerBuilder.append(PatternMatchUtil.encryptEmailWithStar(data.getAccount(), 3));
                tvDeviceOwnerName.setText(ownerBuilder);
                StringBuilder modelBuilder = new StringBuilder();
                modelBuilder.append(mContext.getResources().getString(R.string.scan_device_model));
                modelBuilder.append(": ");
                modelBuilder.append(data.getType());
                tvDeviceOwnerMode.setText(modelBuilder);
            }
        }
    }

    private int getConnectionMode() {
        int connectionMode = getCurrentIntent() != null ? getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) : ConstantValue.CONNECTION_MODE_QC;
        return connectionMode;
    }
}

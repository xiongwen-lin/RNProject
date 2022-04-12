package com.afar.osaio.smart.home.tuyable;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.mixipc.adapter.ScanBluetoothDeviceAdapter;
import com.afar.osaio.smart.mixipc.adapter.listener.ScanBluetoothDeviceListener;
import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.NEventFButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.tuya.smart.android.ble.api.LeScanSetting;
import com.tuya.smart.android.ble.api.ScanDeviceBean;
import com.tuya.smart.android.ble.api.ScanType;
import com.tuya.smart.android.ble.api.TyBleScanResponse;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.ConfigProductInfoBean;
import com.tuya.smart.sdk.api.IBleActivatorListener;
import com.tuya.smart.sdk.api.IMultiModeActivatorListener;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.BleActivatorBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.MultiModeActivatorBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 涂鸦配网弹窗
 * 1、开始扫描              startLeScan()
 * 2、获取蓝牙设备信息       getActivatorDeviceInfo
 * 3、分配网模式
 *    开始连接，纯蓝牙配网   startActivator(selectScanDeviceBean)
 *    蓝牙+wifi配网        mListener.onSelectSmartClick（外部进行配网）
 */

public class AddTuYaBlePopupWindows extends PopupWindow {
    public static final String TAG = "AddTuYaBlePopupWindows";
    private View mMenuView;
    private Context context;
    private AddTuYaBleListener mListener;
    private NEventFButton btnDone;
    private ObjectAnimator btnSignInAnimator = null;
    private ImageView ivLoading;
    private TextView tvDeviceMore;
    private TextView tv_device_ip ;
    private TextView tv_device_name ;
    private ImageView  device_icon ;
    private TextView tv_device_tip;
    private TextView tv_popup_title;
    private TextView tv_ble_search_empty;
    private Button tv_device_manually_select;


    /**
     * 选中的蓝牙设备
     *
     */
    DeviceBleInfoBean selectScanDeviceBean;
    private TuyaBluetoothDeviceAdapter mAdapter;
    RecyclerView rvBluetoothScanList;
    private List<DeviceBleInfoBean> scanDeviceBeanList; //多个设备
    private static   int  TUYA_BLE_CONFIG_TYPE_SINGLE1  = 200; //蓝牙模式直接连接
    private static   int  TUYA_BLE_CONFIG_TYPE_SINGLE2  = 300;
    private static   int  TUYA_BLE_CONFIG_TYPE_SINGLE3  = 400;

    private  BlueScanView blueScanView;

    private static   int  TUYA_BLE_CONFIG_TIME_OUT  = 300000;
    private static   int  TUYA_BLE_CONFIG_TIME_OUT_EMPTY  = 10000;
    private boolean isShowBlueMore = false;
    private boolean isClickShowBlueMore = false;
    /**
     *
     *
     * @param context
     */
    public AddTuYaBlePopupWindows(Activity context,boolean isShowBlueScan,boolean isShowBlueMores) {
        super(context);
        this.context = context;
        isShowBlueMore = isShowBlueMores;
        initPopWindow();
        startLeScan();
    }

    private void initPopWindow() {
        // PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.layout_pop_tuya_add_ble, null);
        initPopStyle();
        initView();

        initBluetoothList();
     //   showBtnSignInLoading();
        // 导入布局
        this.setContentView(mMenuView);
        Log.d(TAG,"TuYaHomeSdk.getBleOperator().startActivator---initPopWindow()");
    }


    // 1、开始不断扫描
    private void  startLeScan(){
        Log.d(TAG,"TuYaHomeSdk.getBleOperator().startLeScan ");

        scanDeviceBeanList = new ArrayList<>();
        scanDeviceBeanList.clear();

        LeScanSetting scanSetting = new LeScanSetting.Builder()
                .setTimeout(TUYA_BLE_CONFIG_TIME_OUT) // 扫描的超时时间：ms
                .addScanType(ScanType.SINGLE) // 若需要扫描蓝牙设备，则只需要添加 ScanType.SINGLE
                // .addScanType(ScanType.SIG_MESH) 可同时添加其他类型设备
                .build();
        TuyaHomeSdk.getBleOperator().startLeScan(scanSetting, new TyBleScanResponse() {
            @Override
            public void onResult(ScanDeviceBean bean) {
                Log.d(TAG,"TuYaHomeSdk.getBleOperator().startLeScan has = "+bean.toString());
                mListener.startLeScanSuccess(bean);
                setDeviceData(bean);
                getActivatorDeviceInfo(bean);

            }
        });


    }



    /**
     * 2、获取蓝牙设备信息
     * @param scanDeviceBean
     */
 private void getActivatorDeviceInfo(ScanDeviceBean scanDeviceBean){
     if (scanDeviceBean == null){
         return;
     }
     TuyaHomeSdk.getActivatorInstance().getActivatorDeviceInfo(
             scanDeviceBean.getProductId(),
             scanDeviceBean.getUuid(),
             scanDeviceBean.getMac(),
             new ITuyaDataCallback<ConfigProductInfoBean>() {
                 @Override
                 public void onSuccess(ConfigProductInfoBean result) {
                     Log.e(TAG,"TuYaHomeSdk.getActivatorDeviceInfo onSuccess = "+result.getName()+",icon = "+result.getIcon());
                     DeviceBleInfoBean deviceBleInfoBean = new DeviceBleInfoBean(scanDeviceBean,result);
                     updateDeviceInfo(deviceBleInfoBean);
                 }

                 @Override
                 public void onError(String errorCode, String errorMessage) {
                     Log.e(TAG,"TuYaHomeSdk.getActivatorDeviceInfo onError = "+errorCode +",errorMessage ="+errorMessage);
                 }
             });
     }

    /**
     * 展示扫描到的蓝牙设备UI
     * @param result 需要展示的UI内容
     */
    private  void  setDeviceData(ScanDeviceBean result){
        if (result == null){
            return;
        }

        tv_device_ip.setText(result.getAddress());
    }

    /**
     *更新蓝牙设备的名称、图标
     */
    private void  updateDeviceInfo(DeviceBleInfoBean result){
        if (result == null){
            return;
        }
        selectScanDeviceBean  = result;
        hideBtnSignInLoading();
        if (result.getScanDeviceBean() != null){
            tv_device_ip.setText(result.getScanDeviceBean().getAddress());
        }


        if (result.getConfigProductInfoBean() !=null){
            tv_device_name.setText(result.getConfigProductInfoBean().getName());
            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
            Glide.with(NooieApplication.mCtx)
                    .load(result.getConfigProductInfoBean().getIcon())
                    .apply(new RequestOptions().placeholder(R.drawable.home_plug_icon).error(R.drawable.home_plug_icon))
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(device_icon);
        }
        scanDeviceBeanList.add(result);
        mAdapter.setData(scanDeviceBeanList);
        if (!isClickShowBlueMore && scanDeviceBeanList !=null && scanDeviceBeanList.size() > 1){
            tvDeviceMore.setVisibility(View.VISIBLE);
        }
    }



    private  void initView(){
        tv_popup_title= mMenuView.findViewById(R.id.tv_popup_title);
        tv_device_ip = mMenuView.findViewById(R.id.tv_device_ip);
        tv_device_name = mMenuView.findViewById(R.id.tv_device_name);
        device_icon = mMenuView.findViewById(R.id.device_icon);
        btnDone = mMenuView.findViewById(R.id.btnDone);
        ivLoading = mMenuView.findViewById(R.id.ivLoading);
        rvBluetoothScanList = mMenuView.findViewById(R.id.rvBluetoothScanList);
        tvDeviceMore = mMenuView.findViewById(R.id.tv_device_more);
        tv_device_tip = mMenuView.findViewById(R.id.tv_device_tip);
        tv_device_manually_select = mMenuView.findViewById(R.id.tv_device_manually_select);


        tv_ble_search_empty = mMenuView.findViewById(R.id.tv_ble_search_empty);
        mMenuView.findViewById(R.id.ivLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mListener.closePopView();
            }
        });
        tv_device_manually_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tv_ble_search_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startLeScanEmpty();
                dismiss();
            }
        });


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null && selectScanDeviceBean != null && selectScanDeviceBean.getScanDeviceBean()!=null) {

                    mListener.onSelectSmartClick(selectScanDeviceBean); //前往wifi输入页面配网
                    dismiss();
                }
            }
        });

        tvDeviceMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvBluetoothScanList.setVisibility(View.VISIBLE);
                device_icon.setVisibility(View.GONE);
                tv_device_name.setVisibility(View.GONE);
                tv_device_ip.setVisibility(View.GONE);
                isClickShowBlueMore = true;
                tvDeviceMore.setVisibility(View.GONE);
                if (isShowBlueMore){
                    tv_ble_search_empty.setVisibility(View.VISIBLE);
                }else{
                    tv_device_tip.setVisibility(View.VISIBLE);
                    tv_device_manually_select.setVisibility(View.VISIBLE);
                }
                btnDone.setVisibility(View.GONE);
            }
        });

    }

    private  void  initBluetoothList (){
        LinearLayoutManager layoutManager = new LinearLayoutManager(NooieApplication.mCtx);
        rvBluetoothScanList.setLayoutManager(layoutManager);
        rvBluetoothScanList.addItemDecoration(getItemDecoration());
        mAdapter = new TuyaBluetoothDeviceAdapter();
        mAdapter.setListener(new TuyaBluetoothDeviceAdapter.TuyaBluetoothDeviceListener() {
            @Override
            public void onItemClick(DeviceBleInfoBean deviceBleInfoBean) {
                mListener.onSelectSmartClick(deviceBleInfoBean); //前往wifi输入页面配网
                dismiss();
            }
        });
        mAdapter.setData(scanDeviceBeanList);
        rvBluetoothScanList.setAdapter(mAdapter);
    }
    private RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = DisplayUtil.dpToPx(NooieApplication.mCtx, 5);//这里增加了20的上边距
            }
        };
    }

    public void dismiss(){
        super.dismiss();
        TuyaHomeSdk.getBleOperator().stopLeScan();
    }

    public void setListener(AddTuYaBleListener listener) {
        this.mListener = listener;
    }

    public interface AddTuYaBleListener {
        /**
         * 用户点击"未找到设备"提示
         */
        void startLeScanEmpty();
        /**
         * 扫描到蓝牙设备
         * @param bean
         */
        void startLeScanSuccess(ScanDeviceBean bean);
        /**
         * 用户选择某个蓝牙设备
         */
        void onSelectSmartClick(DeviceBleInfoBean scanDeviceBean );
        void closePopView();//关闭弹窗
    }

    private void showBtnSignInLoading() {
        setBtnSignInAnimator();
        btnDone.setText("");
        btnSignInAnimator.start();
        ivLoading.setVisibility(View.VISIBLE);
        btnDone.setEnabled(false);
    }

    private void hideBtnSignInLoading() {
        setBtnSignInAnimator();
        btnDone.setText(R.string.pop_tuya_ble_sure_add);
        btnSignInAnimator.pause();
        ivLoading.setVisibility(View.GONE);
        btnDone.setEnabled(true);
    }
    private void setBtnSignInAnimator(){
        btnSignInAnimator = ObjectAnimator.ofFloat(ivLoading, "Rotation", 0, 360);
        btnSignInAnimator.setDuration(2000);
        btnSignInAnimator.setRepeatCount(-1);
    }

    private  void initPopStyle(){
        // 设置动画效果
        this.setAnimationStyle(R.style.from_bottom_anim);
        this.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(dw);
        // 单击弹出窗以外处 关闭弹出窗
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.containerBtn).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
        Log.d(TAG,"TuYaHomeSdk.getBleOperator().startActivator---initPopStyle()");
    }

    /**
     * 开始连接，纯蓝牙配网
     */
    private  void startActivator(ScanDeviceBean mScanDeviceBean){
        BleActivatorBean bleActivatorBean = new BleActivatorBean();

// mScanDeviceBean 来自于扫描回调的 ScanDeviceBean
        bleActivatorBean.homeId =  FamilyManager.getInstance().getCurrentHomeId(); // homeId
        bleActivatorBean.address = mScanDeviceBean.getAddress(); // 设备地址
        bleActivatorBean.deviceType = mScanDeviceBean.getDeviceType(); // 设备类型
        bleActivatorBean.uuid = mScanDeviceBean.getUuid(); // UUID
        bleActivatorBean.productId = mScanDeviceBean.getProductId(); // 产品 ID

        TuyaHomeSdk.getActivator().newBleActivator().startActivator(bleActivatorBean, new IBleActivatorListener() {
            @Override
            public void onSuccess(DeviceBean deviceBean) {
                Log.d(TAG,"TuYaHomeSdk.getBleOperator().startActivator().onSuccess()"+deviceBean.toString());
                // 配网成功
                //     mListener.onSelectSmartSuccess(deviceBean);
                dismiss();
            }

            @Override
            public void onFailure(int code, String msg, Object handle) {
                Toast.makeText(NooieApplication.mCtx, msg, Toast.LENGTH_SHORT).show();
                Log.e(TAG,"TuYaHomeSdk.getBleOperator().startActivator---"+msg);
                dismiss();
                // 配网失败
            }
        });
    }

}

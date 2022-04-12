package com.afar.osaio.widget;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.widget.base.BasePopupWindows;

import java.util.HashMap;
import java.util.Map;

/**
 * CustomResetStatePw
 *
 * @author Administrator
 * @date 2019/3/18
 */
public class CustomNightVisionPw extends BasePopupWindows {

    TextView tvPowerTitle;
    TextView btnPowerOff;
    TextView btnPowerOn;
    TextView btnPowerRemember;
    TextView btnResetStateCancel;
    TextView btnResetStateConfirm;

    private CustomNightVisionListener mListener;
    private Map<Integer, String> mItemMap;
    private int mSelected = 0;

    public CustomNightVisionPw(Activity context, CustomNightVisionListener listener) {
        super(context);
        mListener = listener;
        mItemMap = new HashMap<>();
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_pop_custom_night_vision;
    }

    @Override
    public void bindView(View view) {

        tvPowerTitle = (TextView)view.findViewById(R.id.tvPowerTitle);
        btnPowerOff = (TextView)view.findViewById(R.id.btnPowerOff);
        btnPowerOn = (TextView)view.findViewById(R.id.btnPowerOn);
        btnPowerRemember = (TextView)view.findViewById(R.id.btnPowerRemember);
        btnResetStateCancel = (TextView)view.findViewById(R.id.btnResetStateCancel);
        btnResetStateConfirm = (TextView)view.findViewById(R.id.btnResetStateConfirm);

        btnPowerOff.setVisibility(View.GONE);
        btnPowerOn.setVisibility(View.GONE);
        btnPowerRemember.setVisibility(View.GONE);

        btnPowerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelected = 0;
                updateUI();
                if (mListener != null) {
                    mListener.onConfirmClick(mSelected);
                }
                dismiss();
            }
        });

        btnPowerOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelected = 1;
                updateUI();
                if (mListener != null) {
                    mListener.onConfirmClick(mSelected);
                }
                dismiss();
            }
        });

        btnPowerRemember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelected = 2;
                updateUI();
                if (mListener != null) {
                    mListener.onConfirmClick(mSelected);
                }
                dismiss();
            }
        });

        btnResetStateCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancelClick();
                }
                dismiss();
            }
        });

        btnResetStateConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConfirmClick(mSelected);
                }
                dismiss();
            }
        });

    }

    public void updateUI() {
        btnPowerOff.setBackgroundColor(Color.TRANSPARENT);
        btnPowerOn.setBackgroundColor(Color.TRANSPARENT);
        btnPowerRemember.setBackgroundColor(Color.TRANSPARENT);
        switch (mSelected) {
            case 0: {
                btnPowerOff.setBackgroundColor(NooieApplication.mCtx.getResources().getColor(R.color.theme_green));
                break;
            }
            case 1: {
                btnPowerOn.setBackgroundColor(NooieApplication.mCtx.getResources().getColor(R.color.theme_green));
                break;
            }
            case 2: {
                btnPowerRemember.setBackgroundColor(NooieApplication.mCtx.getResources().getColor(R.color.theme_green));
                break;
            }
        }
    }

    public void setItemMap(Map<Integer,String> itemMap) {
        mItemMap = itemMap;

        if (mItemMap.containsKey(0) && !TextUtils.isEmpty(mItemMap.get(0))) {
            btnPowerOff.setVisibility(View.VISIBLE);
            btnPowerOff.setText(mItemMap.get(0));
        }

        if (mItemMap.containsKey(1) && !TextUtils.isEmpty(mItemMap.get(1))) {
            btnPowerOn.setVisibility(View.VISIBLE);
            btnPowerOn.setText(mItemMap.get(1));
        }

        if (mItemMap.containsKey(2) && !TextUtils.isEmpty(mItemMap.get(2))) {
            btnPowerRemember.setVisibility(View.VISIBLE);
            btnPowerRemember.setText(mItemMap.get(2));
        }
    }

    public void setTitle(String title) {
        if (tvPowerTitle != null) {
            tvPowerTitle.setText(title);
        }
    }

    public interface CustomNightVisionListener {
        void onConfirmClick(int selected);
        void onCancelClick();
    }
}

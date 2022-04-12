package com.afar.osaio.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

public class SwitchImageButton extends AppCompatImageView {

    private boolean mIsOn = false;
    private int mOnStateResId = 0;
    private int mOffStateResId = 0;
    private Drawable mOnStateDrawable = null;
    private Drawable mOffStateDrawable = null;
    private OnStateChangeListener mListener;
    private OnClickListener mClickListener;

    public SwitchImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    public void initBtn(int onResId, int offResId) {
        setOnStateRes(onResId);
        setOffStateRes(offResId);
        refreshOnState();
    }

    public void setOnStateRes(int resId) {
        mOnStateResId = resId;
    }

    public void setOffStateRes(int resId) {
        mOffStateResId = resId;
    }

    public void setOnStateRes(Drawable drawable) {
        mOnStateDrawable = drawable;
    }

    public void setOffStateRes(Drawable drawable) {
        mOffStateDrawable = drawable;
    }

    public void toggleCallback() {
        toggle();
        refreshOnState();
        notifyOnStateChange(mIsOn);
    }

    public void toggleNoCallback() {
        toggle();
        refreshOnState();
    }

    public void setListener(OnStateChangeListener listener) {
        mListener = listener;
    }

    public boolean isOn() {
        return mIsOn;
    }

    private void init() {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCallback();
                if (mClickListener != null) {
                    mClickListener.onClick(v);
                }
            }
        });
    }

    private void toggle() {
        mIsOn = !mIsOn;
    }

    private void refreshOnState() {
        if (mIsOn) {
            if (mOnStateDrawable != null) {
                setImageDrawable(mOnStateDrawable);
            } else {
                setImageResource(mOnStateResId);
            }
        } else {
            if (mOffStateDrawable != null) {
                setImageDrawable(mOffStateDrawable);
            } else {
                setImageResource(mOffStateResId);
            }
        }
    }

    private void notifyOnStateChange(boolean on) {
        if (mListener != null) {
            mListener.onStateChange(on);
        }
    }

    public interface OnStateChangeListener {

        void onStateChange(boolean on);

    }
}

package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.nooie.widget.INEventViewProcessor;
import com.nooie.widget.NEventViewProcessor;

/**
 * NEventButton
 *
 * @author Administrator
 * @date 2020/9/3
 */
public class NEventNormalTextAnimIconView extends NormalTextAnimIconView {

    private INEventViewProcessor mViewProcessor;
    private OnClickListener listener;

    public NEventNormalTextAnimIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mViewProcessor = new NEventViewProcessor(context, attrs, 0);
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewProcessor != null) {
                    mViewProcessor.sendEvent();
                }
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
    }


    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void setEventId(String eventId) {
        if (mViewProcessor != null) {
            mViewProcessor.setEventId(eventId);
        }
    }

    public void setExternal(String external) {
        if (mViewProcessor != null) {
            mViewProcessor.setExternal(external);
        }
    }
}
package com.afar.osaio.widget.component;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blog.www.guideview.Component;
import com.afar.osaio.R;


/**
 * Created by binIoter on 16/6/17.
 */
public class PanelComponent implements Component {

    private int mWidth;
    private PanelComponentListener mListener;

    public PanelComponent(int width, PanelComponentListener listener) {
        mWidth = width;
        mListener = listener;
    }

    @Override
    public View getView(LayoutInflater inflater) {
        RelativeLayout container = (RelativeLayout) inflater.inflate(R.layout.layout_panel_component, null);
        TextView btnNext = (TextView)container.findViewById(R.id.btnNext);
        View btnNextContainer = container.findViewById(R.id.btnNextContainer);
        if (btnNextContainer != null) {
            btnNextContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onNextClick();
                    }
                }
            });
        }
        return container;
    }

    @Override
    public int getAnchor() {
        return Component.ANCHOR_BOTTOM;
    }

    @Override
    public int getFitPosition() {
        return Component.FIT_CENTER;
    }

    @Override
    public int getXOffset() {
        return 0;
    }

    @Override
    public int getYOffset() {
        return 0;
    }

    public interface PanelComponentListener {
        void onNextClick();
    }
}

package com.afar.osaio.widget.component;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.blog.www.guideview.Component;
import com.afar.osaio.R;

/**
 * Created by binIoter on 16/6/17.
 */
public class CtrlComponent implements Component {

    private int mWidth;
    private CtrlComponentListener mListener;

    public CtrlComponent(int width, CtrlComponentListener listener) {
        mWidth = width;
        mListener = listener;
    }

    @Override
    public View getView(LayoutInflater inflater) {
        ConstraintLayout container = (ConstraintLayout) inflater.inflate(R.layout.layout_ctrl_component, null, true);
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
        return Component.ANCHOR_TOP;
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
        return -26;
    }

    public interface CtrlComponentListener {
        void onNextClick();
    }
}

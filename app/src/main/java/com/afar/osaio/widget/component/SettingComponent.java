package com.afar.osaio.widget.component;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.blog.www.guideview.Component;
import com.afar.osaio.R;
import com.nooie.sdk.bean.IpcType;

/**
 * Created by binIoter on 16/6/17.
 */
public class SettingComponent implements Component {

    private SettingComponentListener mListener;
    private IpcType mDeviceType;

    public SettingComponent(IpcType type, SettingComponentListener listener) {
        mDeviceType = type;
        mListener = listener;
    }

    @Override
    public View getView(LayoutInflater inflater) {
        ConstraintLayout container = (ConstraintLayout) inflater.inflate(R.layout.layout_setting_component, null);
        TextView tvGuideDes = (TextView)container.findViewById(R.id.tvGuideDes);
        if (tvGuideDes != null) {
            tvGuideDes.setText(IpcType.IPC_100 == mDeviceType ? R.string.device_guide_setting_motion : R.string.device_guide_setting);
        }
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
        return Component.FIT_END;
    }

    @Override
    public int getXOffset() {
        return 0;
    }

    @Override
    public int getYOffset() {
        return 0;
    }

    public interface SettingComponentListener {
        void onNextClick();
    }
}

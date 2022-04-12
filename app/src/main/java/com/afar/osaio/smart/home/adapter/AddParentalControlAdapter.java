package com.afar.osaio.smart.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.ParentalControlDeviceInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddParentalControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int TITLE_ITEM_TYPE = 0;
    private static int DEVICE_ITEM_TYPE = 1;

    List<ParentalControlDeviceInfo> mDeviceList = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(NooieApplication.mCtx);
        View view;
        if (DEVICE_ITEM_TYPE == viewType) {
            view = layoutInflater.inflate(R.layout.layout_parental_control_item, parent, false);
            AddParentalControlViewHolder addParentalControlViewHolder = new AddParentalControlViewHolder(view);
            return addParentalControlViewHolder;
        } else {
            view = layoutInflater.inflate(R.layout.layout_title_item, parent, false);
            TitleParentalControlViewHolder titleParentalControlViewHolder = new TitleParentalControlViewHolder(view);
            return titleParentalControlViewHolder;
        }
    }

    public void setData(List<ParentalControlDeviceInfo> list) {
        if (list == null || list.size() <= 0) {
            return;
        }

        mDeviceList.clear();
        mDeviceList.addAll(list);
        notifyDataSetChanged();
    }

    private OnSetAddParentalControlListener listener;
    public void setAddParentalControlListener(OnSetAddParentalControlListener listener) {
        this.listener = listener;
    }

    public interface OnSetAddParentalControlListener {
        public void onClickAddParentalControlItem(String device);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof AddParentalControlViewHolder) {
            AddParentalControlViewHolder addParentalControlViewHolder = (AddParentalControlViewHolder)holder;
            ParentalControlDeviceInfo parentalControlDeviceInfo = mDeviceList.get(position);
            addParentalControlViewHolder.container.setTag(position);

            addParentalControlViewHolder.saveTime.setText(parentalControlDeviceInfo.getDeviceName());

            if (parentalControlDeviceInfo.isOnlineState()) {
                addParentalControlViewHolder.imgIcon.setImageResource(R.drawable.device_add_icon_lp_device_with_router);
            } else {
                addParentalControlViewHolder.imgIcon.setImageResource(R.drawable.device_add_icon_lp_device_with_router);
            }

            addParentalControlViewHolder.tvDeviceState.setText(parentalControlDeviceInfo.isOnlineState()
                                                                ? "Protected" : "");

            addParentalControlViewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int cnt = (int)view.getTag();
                        listener.onClickAddParentalControlItem("" + position);
                    }
                }
            });
        } else {
            TitleParentalControlViewHolder titleParentalControlViewHolder = (TitleParentalControlViewHolder)holder;
            ParentalControlDeviceInfo parentalControlDeviceInfo = mDeviceList.get(position);
            titleParentalControlViewHolder.container.setTag(position);

            titleParentalControlViewHolder.tvTitle.setText(parentalControlDeviceInfo.getTitleInfo());
        }
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size() > 1 ? mDeviceList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (!mDeviceList.get(position).getTitleInfo().equals("")) {
            return TITLE_ITEM_TYPE;
        } else {
            return DEVICE_ITEM_TYPE;
        }
        //return super.getItemViewType(position);
    }

    public class AddParentalControlViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgIcon)
        ImageView imgIcon;
        @BindView(R.id.saveTime)
        TextView saveTime;
        @BindView(R.id.tvDeviceState)
        TextView tvDeviceState;

        View container;
        public AddParentalControlViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    public class TitleParentalControlViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle)
        TextView tvTitle;

        View container;
        public TitleParentalControlViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }
}

package com.afar.osaio.smart.scan.adapter;

import android.graphics.Paint;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.bean.RouterConfigureLink;
import com.afar.osaio.smart.scan.adapter.listener.RouterConfigureLinkListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouterConfigureLinkAdapter extends BaseAdapter<RouterConfigureLink, RouterConfigureLinkListener,RouterConfigureLinkAdapter.RouterConfigureLinkVH> {

    @Override
    public RouterConfigureLinkVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RouterConfigureLinkVH(createVHView(R.layout.item_router_configure_link, parent));
    }

    @Override
    public void onBindViewHolder(RouterConfigureLinkVH holder, int position) {
        RouterConfigureLink routerConfigureLink = mDatas.get(position);
        holder.tvRouterLink.setText(routerConfigureLink.getTitle());
        holder.tvRouterLink.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        holder.tvRouterLink.getPaint().setAntiAlias(true);
        holder.vRouterLinkContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(routerConfigureLink);
                }
            }
        });
    }

    public static class RouterConfigureLinkVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vRouterLinkContainer)
        View vRouterLinkContainer;
        @BindView(R.id.tvRouterLink)
        TextView tvRouterLink;

        public RouterConfigureLinkVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}

package com.afar.osaio.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.widget.SitePopupWindows;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SitePopupAdapter extends RecyclerView.Adapter<SitePopupAdapter.SitePopupViewHolder> {

    List<String> regionList = new ArrayList<>();
    SitePopupWindows.SiteListener mListener;

    @NonNull
    @Override
    public SitePopupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_popup_site_item, parent, false);
        return new SitePopupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SitePopupViewHolder holder, int position) {
        String region = regionList.get(position);
        holder.tvPopupSiteItem.setText(region);
        holder.vPopupSiteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSiteItemClick(position, region);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return regionList.size();
    }

    public void setRegions(List<String> regions) {
        regionList.clear();
        regionList.addAll(CollectionUtil.safeFor(regions));
        notifyDataSetChanged();
    }

    public void setListener(SitePopupWindows.SiteListener listener) {
        mListener = listener;
    }

    public static class SitePopupViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vPopupSiteItem)
        View vPopupSiteItem;
        @BindView(R.id.tvPopupSiteItem)
        TextView tvPopupSiteItem;

        public SitePopupViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}

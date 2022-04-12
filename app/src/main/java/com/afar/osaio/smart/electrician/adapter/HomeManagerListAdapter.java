package com.afar.osaio.smart.electrician.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.tuya.smart.home.sdk.bean.HomeBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * HomeListAdapter
 *
 * @author Administrator
 * @date 2019/3/22
 */
public class HomeManagerListAdapter extends RecyclerView.Adapter<HomeManagerListAdapter.HomeHomeListViewHolder> {

    private List<HomeBean> mHomes = new ArrayList<>();
    private HomeListListener mListener;

    @Override
    public HomeHomeListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_home_list, parent, false);
        return new HomeHomeListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeHomeListViewHolder holder, int position) {
        final HomeBean home = mHomes.get(position);
        holder.tvHomeName.setText(home.getName());
        holder.tvHomeName.setTextColor(NooieApplication.mCtx.getResources().getColor(R.color.theme_text_color));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(home);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mHomes.size();
    }

    public void itemRemoved(int position) {
        mHomes.remove(position);
        notifyDataSetChanged();
    }

    public List<HomeBean> getData() {
        return this.mHomes;
    }

    public void setData(List<HomeBean> homes) {
        mHomes.clear();
        mHomes.addAll(homes);
        notifyDataSetChanged();
    }

    public void setListener(HomeListListener listener) {
        mListener = listener;
    }

    public interface HomeListListener {
        void onItemClick(HomeBean homeBean);
    }

    public static class HomeHomeListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvHomeName)
        TextView tvHomeName;
        View container;

        public HomeHomeListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }

    }

}

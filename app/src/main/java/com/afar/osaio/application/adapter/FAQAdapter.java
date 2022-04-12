package com.afar.osaio.application.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.FAQBean;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private List<FAQBean> mData = new ArrayList<>();
    private OnFAQItemClickListener mListener;

    public FAQAdapter() {
    }

    @Override
    public FAQViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FAQViewHolder holder, int position) {
        final int id = position;
        final FAQBean faqBean = mData.get(id);
        holder.tvQueTitle.setText(faqBean.getTitle());
        holder.tvQueContent.setText(faqBean.getContent());
        holder.tvQueContent.setVisibility(faqBean.isExpand() ? View.VISIBLE : View.GONE);
        holder.vh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CollectionUtil.isNotEmpty(faqBean.getChildren())) {
                    if (mListener != null) {
                        mListener.onItemClick(id, faqBean);
                    }
                } else {
                    mData.get(holder.getAdapterPosition()).toggleExpand();
                    holder.tvQueContent.setVisibility(mData.get(holder.getAdapterPosition()).isExpand() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public void setData(List<FAQBean> data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }

        if (data == null) {
            data = new ArrayList<>();
        }
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setListener(OnFAQItemClickListener listener) {
        this.mListener = listener;
    }

    public void release() {
        if (mData != null) {
            mData.clear();
            mData = null;
        }
    }

    public static class FAQViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvQueTitle)
        public TextView tvQueTitle;
        @BindView(R.id.tvQueContent)
        public TextView tvQueContent;
        public View vh;
        public FAQViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            vh = view;
        }
    }

    public interface OnFAQItemClickListener {
        void onItemClick(int position, FAQBean data);
    }
}

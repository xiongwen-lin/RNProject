package com.afar.osaio.application.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<String> mData = new ArrayList<>();
    private Context mContext;
    private OnFeedbackItemClickListener mListener;

    public FeedbackAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public FeedbackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_feedback, null, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeedbackViewHolder holder, int position) {
        final int id = position;
        final String issueContent = mData.get(id);
        holder.tvIssueContent.setText(issueContent);
        holder.vh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(id, issueContent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public void setData(List<String> data) {
        if (mData == null) {
            return;
        }
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setListener(OnFeedbackItemClickListener listener) {
        this.mListener = listener;
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvIssueContent)
        public TextView tvIssueContent;
        public View vh;
        public FeedbackViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            vh = view;
        }
    }

    public interface OnFeedbackItemClickListener {
        void onItemClick(int position, String data);
    }
}

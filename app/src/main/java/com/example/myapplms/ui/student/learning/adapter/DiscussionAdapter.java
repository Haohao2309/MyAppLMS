package com.example.myapplms.ui.student.learning.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.DiscussionResponse;
import java.util.ArrayList;
import java.util.List;

public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.DiscussionViewHolder> {

    private List<DiscussionResponse> discussionList = new ArrayList<>();

    // 1. KHAI BÁO BIẾN LẮNG NGHE SỰ KIỆN
    private OnReplyClickListener replyListener;

    // 2. TẠO INTERFACE ĐỂ ACTIVITY BẮT ĐƯỢC SỰ KIỆN
    public interface OnReplyClickListener {
        void onReplyClick(String authorName);
    }

    // 3. HÀM MÀ LEARNING ACTIVITY ĐANG GỌI TỚI (FIX LỖI CHÍNH LÀ ĐÂY)
    public void setReplyListener(OnReplyClickListener listener) {
        this.replyListener = listener;
    }

    public void setDiscussions(List<DiscussionResponse> list) {
        if (list != null) {
            this.discussionList = list;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public DiscussionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discussion, parent, false);
        return new DiscussionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscussionViewHolder holder, int position) {
        DiscussionResponse discussion = discussionList.get(position);

        String authorName = discussion.authorName != null ? discussion.authorName : "Học viên";
        holder.tvName.setText(authorName);
        holder.tvTitle.setText(discussion.title);
        holder.tvContent.setText(discussion.content);
        holder.tvReply.setText(discussion.replyCount + " phản hồi");

        // 4. GẮN SỰ KIỆN CLICK CHO NÚT "TRẢ LỜI"
        if (holder.btnReplyAction != null) {
            holder.btnReplyAction.setOnClickListener(v -> {
                if (replyListener != null) {
                    replyListener.onReplyClick(authorName);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return discussionList.size();
    }

    static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTitle, tvContent, tvReply, btnReplyAction;

        public DiscussionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_author_name);
            tvTitle = itemView.findViewById(R.id.tv_discussion_title);
            tvContent = itemView.findViewById(R.id.tv_discussion_content);
            tvReply = itemView.findViewById(R.id.tv_reply_count);

            // 5. ÁNH XẠ NÚT TRẢ LỜI (Đảm bảo layout item_discussion.xml của bạn có ID này)
            btnReplyAction = itemView.findViewById(R.id.btn_reply_action);
        }
    }
}
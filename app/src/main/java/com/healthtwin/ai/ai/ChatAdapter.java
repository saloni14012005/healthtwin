package com.healthtwin.ai.ai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.healthtwin.ai.R;

import java.util.List;

public class ChatAdapter
        extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatMessage> messageList;

    public ChatAdapter(
            Context context,
            List<ChatMessage> messageList
    ) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {

        if (messageList
                .get(position)
                .isUserMessage()) {

            return 1;

        } else {

            return 0;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view;

        if (viewType == 1) {

            // USER → RIGHT
            view = LayoutInflater.from(context)
                    .inflate(
                            R.layout.item_chat_user,
                            parent,
                            false
                    );

        } else {

            // AI → LEFT
            view = LayoutInflater.from(context)
                    .inflate(
                            R.layout.item_chat_bot,
                            parent,
                            false
                    );
        }

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChatViewHolder holder,
            int position
    ) {

        ChatMessage message =
                messageList.get(position);

        holder.tvMessage.setText(
                message.getMessage()
        );
    }

    @Override
    public int getItemCount() {

        return messageList.size();
    }

    static class ChatViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvMessage;

        public ChatViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvMessage =
                    itemView.findViewById(
                            R.id.tvMessage
                    );
        }
    }
}
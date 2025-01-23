package com.elias.swapify.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elias.swapify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<MessageModel> messages;
    private LayoutInflater inflater;
    private String currentUserId;

    // Define view types
    private static final int MESSAGE_SENT_BY_ME = 0;
    private static final int MESSAGE_SENT_BY_OTHER = 1;

    public MessageAdapter(Context context, List<MessageModel> messages) {
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MESSAGE_SENT_BY_ME) {
            view = inflater.inflate(R.layout.message_layout_right, parent, false);
        } else {
            view = inflater.inflate(R.layout.message_layout_left, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return MESSAGE_SENT_BY_ME;
        } else {
            return MESSAGE_SENT_BY_OTHER;
        }
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView, messageContentTextView, messageTimestampTextView;
        ImageView senderAvatarImageView;

        MessageViewHolder(View itemView) {
            super(itemView);
            senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
            messageContentTextView = itemView.findViewById(R.id.messageContentTextView);
            messageTimestampTextView = itemView.findViewById(R.id.messageTimestampTextView);
            senderAvatarImageView = itemView.findViewById(R.id.smallProfileImage);
        }

        void bind(MessageModel message) {
            messageContentTextView.setText(message.getContent());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            messageTimestampTextView.setText(message.getDatetime() != null ? dateFormat.format(message.getDatetime()) : "No timestamp available");
            fetchSenderAndAvatar(message.getSenderId(), senderNameTextView, senderAvatarImageView);
        }

        private void fetchSenderAndAvatar(String userId, final TextView senderNameTextView, final ImageView senderAvatarImageView) {
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("USERS").document(userId);
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userName = document.getString("name");
                        String userAvatarUrl = document.getString("profilepicture");
                        senderNameTextView.setText(userName);
                        Glide.with(senderAvatarImageView.getContext())
                                .load(userAvatarUrl)
                                .placeholder(R.drawable.ic_profile) // default avatar placeholder
                                .into(senderAvatarImageView);
                    }
                }
            });
        }
    }
}

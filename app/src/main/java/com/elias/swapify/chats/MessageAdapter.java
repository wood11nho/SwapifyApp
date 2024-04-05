package com.elias.swapify.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.elias.swapify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class MessageAdapter extends ArrayAdapter<MessageModel> {
    private Context context;
    private int resource;
    private static final int MESSAGE_SENT_BY_ME = 0;
    private static final int MESSAGE_SENT_BY_OTHER = 1;

    public MessageAdapter(Context context, int resource, List<MessageModel> messages) {
        super(context, resource, messages);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public int getViewTypeCount() {
        // The number of different layouts that will be used in the list
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = getItem(position);

        if (message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            // If the current user sent the message, return the sent by me layout
            return MESSAGE_SENT_BY_ME;
        } else {
            // If some other user sent the message, return the sent by other layout
            return MESSAGE_SENT_BY_OTHER;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        MessageModel message = getItem(position);
        int viewType = getItemViewType(position);

        if (convertView == null) {
            if (viewType == MESSAGE_SENT_BY_ME) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_layout_right, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_layout_left, parent, false);
            }

            viewHolder = new ViewHolder();
            viewHolder.senderNameTextView = convertView.findViewById(R.id.senderNameTextView);
            viewHolder.messageContentTextView = convertView.findViewById(R.id.messageContentTextView);
            viewHolder.messageTimestampTextView = convertView.findViewById(R.id.messageTimestampTextView);
            viewHolder.senderAvatarImageView = convertView.findViewById(R.id.smallProfileImage);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (message != null) {
            // Fetch the sender's name and avatar
            fetchSenderAndAvatar(message.getSenderId(), viewHolder.senderNameTextView, viewHolder.senderAvatarImageView);
            viewHolder.messageContentTextView.setText(message.getContent());

            // Check if the datetime property is not null before converting and displaying it
            if (message.getDatetime() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                viewHolder.messageTimestampTextView.setText(dateFormat.format(message.getDatetime()));
            } else {
                viewHolder.messageTimestampTextView.setText("No timestamp available");
            }
        }

        return convertView;
    }

    private void fetchSenderAndAvatar(String userId, final TextView senderNameTextView, final ImageView senderAvatarImageView) {
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("USERS").document(userId);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userName = document.getString("name");
                        String userAvatarUrl = document.getString("profilepicture");
                        senderNameTextView.setText(userName);
                        // Now load the avatar using Glide or Picasso
                        Glide.with(context)
                                .load(userAvatarUrl)
                                .placeholder(R.drawable.ic_profile) // default avatar placeholder
                                .into(senderAvatarImageView);
                    }
                }
            }
        });
    }


    private static class ViewHolder {

        TextView senderNameTextView;
        TextView messageContentTextView;
        TextView messageTimestampTextView;
        ImageView senderAvatarImageView;
    }
}

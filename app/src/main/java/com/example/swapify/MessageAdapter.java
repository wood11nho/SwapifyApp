package com.example.swapify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<MessageModel> {
    private Context context;
    private int resource;

    public MessageAdapter(Context context, int resource, List<MessageModel> messages) {
        super(context, resource, messages);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.senderNameTextView = convertView.findViewById(R.id.senderNameTextView);
            viewHolder.messageContentTextView = convertView.findViewById(R.id.messageContentTextView);
            viewHolder.messageTimestampTextView = convertView.findViewById(R.id.messageTimestampTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MessageModel message = getItem(position);

        if (message != null) {
            fetchSenderName(message.getCurrentUserId(), viewHolder.senderNameTextView);
            viewHolder.messageContentTextView.setText(message.getContent());

            // Check if the datetime property is not null before converting and displaying it
            if (message.getDatetime() != null) {
                viewHolder.messageTimestampTextView.setText(message.getDatetime().toString());
            } else {
                viewHolder.messageTimestampTextView.setText("No timestamp available");
            }
        }

        return convertView;
    }

    private void fetchSenderName(String userId, final TextView senderNameTextView) {
        // Assuming you have a 'USERS' collection in Firestore
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("USERS").document(userId);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get the user's name from the document
                        String userName = document.getString("name");
                        senderNameTextView.setText(userName);
                    }
                }
            }
        });
    }

    private static class ViewHolder {
        TextView senderNameTextView;
        TextView messageContentTextView;
        TextView messageTimestampTextView;
    }
}

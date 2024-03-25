package com.elias.swapify.chats;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elias.swapify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatModel>{
    private Context context;
    private int resource;
    private FirebaseAuth firebaseAuth;

    public ChatAdapter(Context context, int resource, List<ChatModel> chats) {
        super(context, resource, chats);
        this.context = context;
        this.resource = resource;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            // Inflate the layout
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);

            // Initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.otherUserTextView = convertView.findViewById(R.id.otherPersonNameTextView);
            viewHolder.chatLayout = convertView.findViewById(R.id.otherPersonLayout);

            // Set the tag
            convertView.setTag(viewHolder);
        } else {
            // Recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set an OnClickListener for the chatLayout
        viewHolder.chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel chat = getItem(position);
                if (chat != null) {
                    // Start the ChatActivity with the selected chat user ID
                    String receiverId = chat.getUser1().equals(firebaseAuth.getCurrentUser().getUid()) ? chat.getUser2() : chat.getUser1();
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("receiverId", receiverId);
                    intent.putExtra("otherPersonName", viewHolder.otherUserTextView.getText().toString());
                    context.startActivity(intent);
                }
            }
        });

        // Get the current item
        ChatModel chat = getItem(position);

        // Set the username
        if (chat != null){
            fetchOtherUser(chat, viewHolder.otherUserTextView);
        }

        return convertView;
    }


    private void fetchOtherUser(ChatModel chat, TextView otherUserTextView) {
        String senderId = firebaseAuth.getCurrentUser().getUid();
        String receiverId = (senderId.equals(chat.getUser1())) ? chat.getUser2() : chat.getUser1();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("USERS").document(receiverId);

        // Add a tag to the TextView to identify it later
        otherUserTextView.setTag(receiverId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String retrievedUserId = otherUserTextView.getTag().toString();
                    if (retrievedUserId.equals(receiverId)) {
                        // Only update the UI if the tags match
                        otherUserTextView.setText("Chat with " + document.getString("username"));
                    }
                } else {
                    String retrievedUserId = otherUserTextView.getTag().toString();
                    if (retrievedUserId.equals(receiverId)) {
                        otherUserTextView.setText("No user found");
                    }
                }
            } else {
                String retrievedUserId = otherUserTextView.getTag().toString();
                if (retrievedUserId.equals(receiverId)) {
                    otherUserTextView.setText("No user found");
                }
            }
        });
    }


    private static class ViewHolder {
        TextView otherUserTextView;
        LinearLayout chatLayout;
    }
}

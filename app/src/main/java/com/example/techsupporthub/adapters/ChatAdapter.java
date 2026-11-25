package com.example.techsupporthub.adapters;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.R;
import com.example.techsupporthub.models.ChatModel;
import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.utils.Constants;
import com.example.techsupporthub.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final ArrayList<ChatModel> chatList;
    private final Context context;

    public ChatAdapter(Context context, ArrayList<ChatModel> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatModel chat = chatList.get(position);
        String userId = FirebaseAuth.getInstance().getUid();
        String senderId = chat.getSenderId();

//        Toast.makeText(context, "id:" + userId, Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "senderId:" + senderId, Toast.LENGTH_SHORT).show();

        holder.dateTextView.setText(Constants.getTimeAgo(chat.getCurrentDate()));

        if (senderId != null && senderId.equals(userId)) {
            holder.messageTextView.setBackgroundResource(R.drawable.bg_patient_message);
            holder.messageTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            // Change gravity to end for the current user's messages
            holder.linearLayout.setGravity(Gravity.END);
            holder.senderProfile.setVisibility(View.VISIBLE);
            holder.receiverProfile.setVisibility(View.GONE);
            getImage(chat.getSenderId(), holder.senderProfile);
        } else {
            holder.messageTextView.setBackgroundResource(R.drawable.bg_doctor_message);
            holder.messageTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            // Change gravity to start for other user's messages
            holder.linearLayout.setGravity(Gravity.START);
            holder.senderProfile.setVisibility(View.GONE);
            holder.receiverProfile.setVisibility(View.VISIBLE);
            getImage(chat.getSenderId(), holder.receiverProfile);
        }

        holder.messageTextView.setText(chat.getMessage());
    }

    private void getImage(String userId, CircleImageView profileImage) {
        Utils.getDatabaseReference(Constants.USERS_DB).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel model = snapshot.getValue(UserModel.class);
                    if (model != null) {
                        if (model.getProfile() != null && !model.getProfile().isEmpty()) {
                            Glide.with(context)
                                    .load(model.getProfile())
                                    .placeholder(R.drawable.profile)
                                    .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.profile);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, dateTextView;
        LinearLayout linearLayout;
        CircleImageView receiverProfile, senderProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            receiverProfile = itemView.findViewById(R.id.receiver_profile);
            senderProfile = itemView.findViewById(R.id.sender_profile);
        }
    }
}

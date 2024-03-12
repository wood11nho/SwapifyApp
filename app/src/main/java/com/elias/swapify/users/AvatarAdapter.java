package com.elias.swapify.users;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.elias.swapify.R;

import java.util.List;

public class AvatarAdapter extends ArrayAdapter<String> {

    private List<String> avatars;
    private Context context;

    public AvatarAdapter(Context context, List<String> avatars) {
        super(context, 0, avatars);
        this.context = context;
        this.avatars = avatars;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.avatar_dialog_item, parent, false); //
        }

        ImageView imageView = convertView.findViewById(R.id.avatarImageView);
        String avatarUrl = avatars.get(position);

        Glide.with(context)
                .load(avatarUrl)
                .placeholder(R.mipmap.default_profile_picture)
                .error(R.mipmap.default_profile_picture)
                .into(imageView);

        return convertView;
    }
}


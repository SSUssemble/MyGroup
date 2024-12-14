package com.example.ssussemble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserProfileAdapter extends RecyclerView.Adapter<UserProfileAdapter.UserProfileViewHolder> {

    private List<UserProfile> userProfiles;

    public UserProfileAdapter(List<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }

    @Override
    public UserProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.teamate_profile, parent, false);
        return new UserProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserProfileViewHolder holder, int position) {
        UserProfile userProfile = userProfiles.get(position);
        holder.bind(userProfile);
    }

    @Override
    public int getItemCount() {
        return userProfiles.size();
    }

    class UserProfileViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private ImageView userProfileImage;

        UserProfileViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userProfileImage = itemView.findViewById(R.id.cr_profile_image);
        }

        void bind(UserProfile userProfile) {
            userName.setText(userProfile.getName());
            Glide.with(userProfileImage.getContext())
                    .load(userProfile.getProfileImageUrl())
                    .into(userProfileImage);
        }
    }
}

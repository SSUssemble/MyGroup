package com.example.tmp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

public class MyGroupsAdapter extends RecyclerView.Adapter<MyGroupsAdapter.GroupViewHolder> {
    private List<Room> groupList;
    private final WeakReference<Context> contextRef;

    public MyGroupsAdapter(List<Room> groupList, Context context) {
        this.groupList = groupList;
        this.contextRef = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Room room = groupList.get(position);
        holder.roomNameText.setText(room.getName());
        holder.descriptionText.setText(room.getDescription());
        holder.headerText.setText("방장: " + room.getHeader());

        holder.itemView.setOnClickListener(v -> {
            Fragment roomDetailFragment = RoomDetailFragment.newInstance(
                    room.getId(),
                    room.getName(),
                    room.getDescription(),
                    room.getComment()
            );

            ((FragmentActivity) contextRef.get()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, roomDetailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView roomNameText;
        TextView descriptionText;
        TextView headerText;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameText = itemView.findViewById(R.id.roomNameText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            headerText = itemView.findViewById(R.id.headerText);
        }
    }
}
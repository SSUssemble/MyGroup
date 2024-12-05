//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.fragment.app.FragmentActivity;
//import androidx.fragment.app.FragmentManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.ssussemble.Room;
//import com.example.ssussemble.RoomDetailFragment;
//
//import java.util.List;
//
//public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
//
//    private List<Room> roomList;
//    private OnRoomListChangedListener onRoomListChangedListener;
//
//    public RoomAdapter(List<Room> roomList) {
//        this.roomList = roomList;
//    }
//    public void setOnRoomListChangedListener(OnRoomListChangedListener listener){
//        this.onRoomListChangedListener = listener;
//    }
//
//    @NonNull
//    @Override
//    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_layout, parent, false);
//        return new RoomViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
//        Room room = roomList.get(position);
//        holder.roomName.setText(room.getName());
//        holder.roomDescription.setText(room.getDescription());
//    }
//
//    @Override
//    public int getItemCount() {
//        return roomList.size();
//    }
//    public void addRoom(Room room){
//        roomList.add(room);
//        notifyItemInserted(roomList.size()-1);
//        if(onRoomListChangedListener)
//    }
//
//    public static class RoomViewHolder extends RecyclerView.ViewHolder {
//        TextView roomName;
//        TextView roomDescription;
//
//        public RoomViewHolder(@NonNull View itemView) {
//            super(itemView);
//            roomName = itemView.findViewById(R.id.buttonName);
//            roomDescription = itemView.findViewById(R.id.textView);
//        }
//    }
//}
//

package com.example.ssussemble;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;

    public RoomAdapter(List<Room> roomList) {
        this.roomList = roomList;
    }



    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_layout, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.roomName.setText(room.getName());
        holder.roomDescription.setText(room.getDescription());
        holder.roomHeader.setText(room.getHeader());
        holder.roomUserNumMax.setText(room.getUserNumMax());

        CardView cardView = (CardView) holder.itemView;
        cardView.setCardBackgroundColor(getCategoryColor(room.getDescription()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = (FragmentActivity) v.getContext();
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                RoomDetailFragment roomDetailFragment =
                        RoomDetailFragment.newInstance(room.getId(), room.getName(), room.getDescription(), room.getComment());

                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, roomDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "팀플":
                return Color.parseColor("#81BFDA");
            case "대회":
                return Color.parseColor("#FADA7A");
            case "스터디":
                return Color.parseColor("#90EE90");
            case "미팅":
                return Color.parseColor("#FFB6C1");
            case "기타":
                return Color.parseColor("#FCF6F5");
            default:
                return Color.parseColor("#FFFFFF");
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        TextView roomDescription;
        TextView roomHeader;
        TextView roomUserNumMax;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.buttonName);
            roomDescription = itemView.findViewById(R.id.textView);
            roomHeader = itemView.findViewById(R.id.header_name);
            roomUserNumMax = itemView.findViewById(R.id.max_user_num);
        }
    }
}


package com.example.group_w01_07_3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OpenedCapsuleAdapter extends RecyclerView.Adapter<OpenedCapsuleAdapter.capsuleCardViewHolder> {

    Context mcontext;
    List<OpenedCapsule> mData;

    public OpenedCapsuleAdapter(Context mcontext, List<OpenedCapsule> mData) {
        this.mcontext = mcontext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public capsuleCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewgroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mcontext);
        View layout = inflater.inflate(R.layout.capsule_material_card, viewgroup, false);

        return new capsuleCardViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull capsuleCardViewHolder holder, int position) {

        // animation for capsule image
        holder.capsule_image.setAnimation(AnimationUtils.loadAnimation(mcontext,R.anim.fade_transition_animation));

        // animation for the whole card
        holder.megaCardLayout.setAnimation(AnimationUtils.loadAnimation(mcontext,R.anim.fade_scale_animation));

        holder.capsule_image.setImageResource(mData.get(position).getCapsule_image());
        holder.original_user_avatar.setImageResource(mData.get(position).getAvatar());
        holder.capsule_title.setText(mData.get(position).getCapsule_title());
        holder.opened_date.setText(mData.get(position).getOpened_date());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class capsuleCardViewHolder extends RecyclerView.ViewHolder {
        ImageView capsule_image, original_user_avatar;
        TextView capsule_title, opened_date;
        ConstraintLayout megaCardLayout;


        public capsuleCardViewHolder(View itemView){
            super(itemView);
            megaCardLayout = itemView.findViewById(R.id.history_capsule_card_layout);
            capsule_image = itemView.findViewById(R.id.history_capsule_card_background);
            original_user_avatar = itemView.findViewById(R.id.history_capsule_original_user_avatar);
            capsule_title = itemView.findViewById(R.id.history_opened_capsule_title);
            opened_date = itemView.findViewById(R.id.history_opened_capsule_openDate);
        }
    }
}

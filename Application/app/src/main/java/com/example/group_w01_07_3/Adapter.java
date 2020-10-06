package com.example.group_w01_07_3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.myViewHolder> {

    Context mcontext;

    public Adapter(Context mcontext, List<Item> mData) {
        this.mcontext = mcontext;
        this.mData = mData;
    }

    List<Item> mData;

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mcontext);
        View v = inflater.inflate(R.layout.capsule_cardview, parent, false);

        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        holder.profile_photo.setImageResource(mData.get(position).getProfilePhito());
        holder.background_img.setImageResource(mData.get(position).getBackground());
        holder.tv_title.setText(mData.get(position).getProfileName());
        holder.tv_nbFollowers.setText(mData.get(position).getNbFollowers() + "Followers");
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder{

        ImageView profile_photo, background_img;
        TextView tv_title, tv_nbFollowers;


        public myViewHolder(View itemView){
            super(itemView);
            profile_photo = itemView.findViewById(R.id.profile_img);
            background_img = itemView.findViewById(R.id.card_background);
            tv_title = itemView.findViewById(R.id.card_title);
            tv_nbFollowers = itemView.findViewById(R.id.card_nb_follower);
        }
    }

}

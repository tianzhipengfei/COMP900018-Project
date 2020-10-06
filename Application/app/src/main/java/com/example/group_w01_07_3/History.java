package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //set status bar background to transparent
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //setup recycleView with adapter
        RecyclerView recyclerView = findViewById(R.id.rv_list);
        List<Item> mlist = new ArrayList<>();
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        mlist.add(new Item(R.drawable.email, "Cities", R.drawable.avatar_sample, 2500));
        Adapter adapter = new Adapter(this, mlist);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
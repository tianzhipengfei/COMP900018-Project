package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //setup recycleView with adapter
        //这里我是手动添加的几个样本数据供测试layout用,写代码时请删除
        RecyclerView recyclerView = findViewById(R.id.history_opened_capsule_list);
        List<OpenedCapsule> testingList = new ArrayList<>();
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule_welcome));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule_welcome));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule_welcome));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule_welcome));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule_welcome));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule_welcome));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule_welcome));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule_welcome));

        OpenedCapsuleAdapter openedCapsuleAdapter = new OpenedCapsuleAdapter(this, testingList);
        recyclerView.setAdapter(openedCapsuleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        mToolbar = findViewById(R.id.capsule_history_back_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //navigate back to account page. 请自己根据activity life cycle来写返回功能
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
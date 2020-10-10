package com.example.group_w01_07_3.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_w01_07_3.R;

import java.util.ArrayList;
import java.util.List;

public class OpenedCapsuleHistoryFragment extends Fragment {

    public OpenedCapsuleHistoryFragment() {
    }

    public static OpenedCapsuleHistoryFragment newInstance() {

        Bundle args = new Bundle();

        OpenedCapsuleHistoryFragment fragment = new OpenedCapsuleHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_opened_capsule_history, container, false);

        //手动添加愚蠢的数据
        RecyclerView recyclerView = root.findViewById(R.id.history_opened_capsule_list);
        List<OpenedCapsule> testingList = new ArrayList<>();
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));

        OpenedCapsuleAdapter openedCapsuleAdapter = new OpenedCapsuleAdapter(getContext(), testingList);
        recyclerView.setAdapter(openedCapsuleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return root;


    }

}
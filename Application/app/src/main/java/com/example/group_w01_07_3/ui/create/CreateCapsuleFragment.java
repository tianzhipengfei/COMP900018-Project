package com.example.group_w01_07_3.ui.create;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.group_w01_07_3.EditProfile;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.ui.account.AccountFragment;

public class CreateCapsuleFragment extends Fragment {
    private int count = 0;
    private TextView text;
    public CreateCapsuleFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_create_capsule, container, false);



        //testing if only one fragment copy is in app(count should remain)
        Button add = (Button) root.findViewById(R.id.create_capsule_test_btn);
        text = root.findViewById(R.id.create_capsule_test_count);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count += 1;
                text.setText(String.valueOf(count));
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
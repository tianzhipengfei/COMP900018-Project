package com.example.group_w01_07_3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.group_w01_07_3.ChangePassword;
import com.example.group_w01_07_3.EditProfile;
import com.example.group_w01_07_3.R;

public class EditProfileFragment extends Fragment {

    public static EditProfileFragment newInstance() {
        
        Bundle args = new Bundle();
        
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        Button changePasswordBtn = (Button) root.findViewById(R.id.edit_profile_btn_change_password);
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChangePassword.class);
                startActivity(intent);
            }
        });

        return root;

    }

}
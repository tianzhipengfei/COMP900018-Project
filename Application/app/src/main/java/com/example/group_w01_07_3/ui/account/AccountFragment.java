package com.example.group_w01_07_3.ui.account;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.group_w01_07_3.EditProfile;
import com.example.group_w01_07_3.History;
import com.example.group_w01_07_3.HomeActivity;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.SignIn;

public class AccountFragment extends Fragment {

    public AccountFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        Button signOutButton = (Button) root.findViewById(R.id.button_acct_sign_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountFragment.this.getContext());
                builder.setIcon(R.drawable.warning);
                builder.setTitle("Warning");
                builder.setMessage("Do you want to sign out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AccountFragment.this.getContext(), "Sign out successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        Button editProfileButton = (Button) root.findViewById(R.id.button_acct_edit_profile);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountFragment.this.getContext(), EditProfile.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(AccountFragment.this.getActivity()).toBundle());
            }
        });

        Button openedCapsuleHistory = (Button) root.findViewById(R.id.button_acct_view_opened_capsule_history);
        openedCapsuleHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountFragment.this.getContext(), History.class);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavController navController = Navigation.findNavController(view);

        Button testSwitchFragment = view.findViewById(R.id.testing_switch_fragment);
        testSwitchFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_accountFragment_to_editProfileFragment);
            }
        });

    }
}
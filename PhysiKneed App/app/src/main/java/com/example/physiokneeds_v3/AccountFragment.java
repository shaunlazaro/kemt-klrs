package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;


public class AccountFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView email = view.findViewById(R.id.username_account);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView name = view.findViewById(R.id.name_account);

        email.setText("Email: " + MainMenu.email);
        name.setText("Name: " + MainMenu.name);

        Button signOut = view.findViewById(R.id.sign_out_button);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainMenu.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}
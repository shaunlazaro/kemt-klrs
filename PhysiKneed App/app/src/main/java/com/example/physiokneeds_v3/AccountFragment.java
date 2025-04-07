package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;


public class AccountFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView username = view.findViewById(R.id.username_account);

        username.setText("Username: " + HomeScreen.loginUsername);

        // Inflate the layout for this fragment
        return view;
    }
}
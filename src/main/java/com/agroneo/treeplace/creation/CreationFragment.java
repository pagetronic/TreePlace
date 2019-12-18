package com.agroneo.treeplace.creation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.agroneo.treeplace.R;

public class CreationFragment extends Fragment {

    private CreationViewModel creationViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        creationViewModel = ViewModelProviders.of(this).get(CreationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_creation, container, false);
        final TextView textView = root.findViewById(R.id.text_creation);
        creationViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
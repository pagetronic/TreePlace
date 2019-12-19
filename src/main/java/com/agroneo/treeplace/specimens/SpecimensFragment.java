package com.agroneo.treeplace.specimens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.agroneo.treeplace.R;

public class SpecimensFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.specimens_list, container, false);

        final ListView listSpecimens = root.findViewById(R.id.specimens);
        final SpecimensAdapter specimensAdapter = new SpecimensAdapter(getActivity());
        listSpecimens.setAdapter(specimensAdapter);


        return root;
    }
}
package com.agroneo.droid.specimens;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agroneo.droid.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SpecimensFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.specimens_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SpecimensAdapter specimensAdapter = new SpecimensAdapter(getActivity(), R.layout.specimen_view);
        specimensAdapter.get("/gaia/specimens");
        ((ListView) view.findViewById(R.id.specimens)).setAdapter(specimensAdapter);
        specimensAdapter.notifyDataSetChanged();
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SpecimenCreator.class);
                startActivity(intent);
            }
        });
    }

}
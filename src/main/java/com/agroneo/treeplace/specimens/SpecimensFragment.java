package com.agroneo.treeplace.specimens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agroneo.treeplace.R;

public class SpecimensFragment extends Fragment {

    private SpecimensAdapter specimensAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        specimensAdapter = new SpecimensAdapter(getActivity(), R.layout.specimen_view);
        specimensAdapter.get("/gaia/specimens");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.specimens_list, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        specimensAdapter.setActivity(getActivity());
        ((ListView) view.findViewById(R.id.specimens)).setAdapter(specimensAdapter);
    }

}
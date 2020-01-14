package com.agroneo.droid.threads;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agroneo.droid.R;

public class ForumsFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.threads_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ForumAdapter forumAdapter = new ForumAdapter(getActivity(), R.layout.threads_view);
        forumAdapter.get("/questions?lng=fr");
        ((ListView) view.findViewById(R.id.threads)).setAdapter(forumAdapter);
        forumAdapter.notifyDataSetChanged();
    }

}

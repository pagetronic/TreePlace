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


    private ForumAdapter forumAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forumAdapter = new ForumAdapter(getActivity(), R.layout.threads_view);
        forumAdapter.get("/questions?lng=fr");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.threads_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        forumAdapter.setContext(getContext());
        ((ListView) view.findViewById(R.id.threads)).setAdapter(forumAdapter);
        forumAdapter.notifyDataSetChanged();
    }

}

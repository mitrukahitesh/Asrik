package com.mitrukahitesh.asrik.fragments.homefragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.adapters.FeedRequests;

public class Feed extends Fragment {

    private RecyclerView recyclerView;
    private FeedRequests adapter;
    private FloatingActionButton fab;
    private NavController controller;

    public Feed() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (recyclerView == null) {
            adapter = new FeedRequests(requireContext());
        } else {
            adapter = (FeedRequests) recyclerView.getAdapter();
        }
        controller = Navigation.findNavController(view);
        recyclerView = view.findViewById(R.id.recycler);
        fab = view.findViewById(R.id.fab);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigate(R.id.action_feed_to_raiseRequest);
            }
        });
    }

}
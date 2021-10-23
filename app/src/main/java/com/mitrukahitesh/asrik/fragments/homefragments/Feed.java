package com.mitrukahitesh.asrik.fragments.homefragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.adapters.CampAdapter;
import com.mitrukahitesh.asrik.adapters.EmergencyRequests;
import com.mitrukahitesh.asrik.adapters.FeedRequests;
import com.mitrukahitesh.asrik.models.BloodCamp;
import com.mitrukahitesh.asrik.utility.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Feed extends Fragment {

    private RecyclerView campRecycler, emergencyRecyclerView, recyclerView;
    private FeedRequests adapter;
    private EmergencyRequests emergencyAdapter;
    private CampAdapter campAdapter;
    private NavController controller;
    private TextView noCamps;
    private final List<BloodCamp> campList = new ArrayList<>();

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
        if (emergencyRecyclerView == null) {
            emergencyAdapter = new EmergencyRequests(requireContext());
        } else {
            emergencyAdapter = (EmergencyRequests) emergencyRecyclerView.getAdapter();
        }
        if (campRecycler == null) {
            campAdapter = new CampAdapter(requireContext(), campList);
            fetchCamps();
        } else {
            campAdapter = (CampAdapter) campRecycler.getAdapter();
        }
        controller = Navigation.findNavController(view);
        noCamps = view.findViewById(R.id.no_camps);
        campRecycler = view.findViewById(R.id.camp_recycler);
        if (campList.size() != 0) {
            campRecycler.setVisibility(View.VISIBLE);
            noCamps.setVisibility(View.GONE);
        }
        emergencyRecyclerView = view.findViewById(R.id.emergency_recycler);
        recyclerView = view.findViewById(R.id.recycler);
        campRecycler.setNestedScrollingEnabled(false);
        emergencyRecyclerView.setNestedScrollingEnabled(false);
        recyclerView.setNestedScrollingEnabled(false);
        campRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        emergencyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        campRecycler.setAdapter(campAdapter);
        emergencyRecyclerView.setAdapter(emergencyAdapter);
        recyclerView.setAdapter(adapter);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigate(R.id.action_feed_to_raiseRequest);
            }
        });
    }

    private void fetchCamps() {
        SharedPreferences preferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        FirebaseFirestore.getInstance()
                .collection(Constants.CAMPS)
                .whereEqualTo(Constants.PINCODE.toLowerCase(Locale.ROOT), preferences.getString(Constants.PIN_CODE, ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : task.getResult()) {
                                BloodCamp camp = querySnapshot.toObject(BloodCamp.class);
                                Calendar c1 = Calendar.getInstance();
                                c1.set(camp.getYear(), camp.getMonth(), camp.getDay(), camp.getStartHour(), camp.getStartMin());
                                if (Calendar.getInstance().getTimeInMillis() < c1.getTimeInMillis())
                                    campList.add(camp);
                            }
                            campList.sort(new Comparator<BloodCamp>() {
                                @Override
                                public int compare(BloodCamp o1, BloodCamp o2) {
                                    Calendar c1 = Calendar.getInstance();
                                    Calendar c2 = Calendar.getInstance();
                                    c1.set(o1.getYear(), o1.getMonth(), o1.getDay(), o1.getStartHour(), o1.getStartMin());
                                    c2.set(o2.getYear(), o2.getMonth(), o2.getDay(), o2.getStartHour(), o2.getStartMin());
                                    return (c1.getTimeInMillis() < c2.getTimeInMillis() ? -1 : 0);
                                }
                            });
                            campAdapter.notifyDataSetChanged();
                            campRecycler.setVisibility(View.VISIBLE);
                            noCamps.setVisibility(View.GONE);
                        }
                        Log.i("Asrik: Camps", "fetched " + campList.size());
                    }
                });
    }

}
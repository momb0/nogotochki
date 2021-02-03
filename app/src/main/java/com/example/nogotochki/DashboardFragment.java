package com.example.nogotochki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardFragment extends Fragment {

    private RecyclerView mastersRecyclerView;
    private RecyclerAdapter recyclerAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mastersRecyclerView = view.findViewById(R.id.mastersRecyclerView);
        mastersRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //mastersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));
        recyclerAdapter = new RecyclerAdapter();
        mastersRecyclerView.setAdapter(recyclerAdapter);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
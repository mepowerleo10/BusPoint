package com.gorillagang.buspoint;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gorillagang.buspoint.adapters.JourneyAdapter;
import com.gorillagang.buspoint.data.Journey;

import java.util.List;

public class JourneyActivity extends AppCompatActivity {

    List<Journey> journeyList;
    public final String TAG = JourneyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        RecyclerView jourRecyclerView = findViewById(R.id.journey_list);
        Intent fromIntent = getIntent();
        Bundle bundle = fromIntent.getExtras();
        journeyList = (List<Journey>) bundle.getSerializable("journeyList");

        JourneyAdapter adapter = new JourneyAdapter(journeyList);
        jourRecyclerView.setAdapter(adapter);
        jourRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
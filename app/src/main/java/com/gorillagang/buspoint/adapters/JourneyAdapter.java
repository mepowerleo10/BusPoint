package com.gorillagang.buspoint.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gorillagang.buspoint.R;
import com.gorillagang.buspoint.data.Journey;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.ViewHolder> {
    private final List<Journey> journeyList;

    public JourneyAdapter(List<Journey> journeyList) {
        this.journeyList = journeyList;
    }

    @NonNull
    @NotNull
    @Override
    public JourneyAdapter.ViewHolder onCreateViewHolder(
            @NonNull @NotNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View journeyView = inflater.inflate(R.layout.journey_item, parent, false);
        return new ViewHolder(journeyView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull @NotNull JourneyAdapter.ViewHolder holder, int position) {

        if (journeyList.size() > 0) {
            Journey journey = journeyList.get(position);
            DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyy hh:mm");
            String date = dateFormat.format(journey.getDateTime());
            holder.dateTextView.setText(date);
            holder.descTextView.setText(
                    String.format("%s to %s", journey.getFromDescription(), journey.getToDescription()));
            holder.costTextView.setText(String.format("%s Tshs.", journey.getCost()));
            holder.openJourneyBtn.setEnabled(true);
        } else {
            holder.dateTextView.setText("Oops! You haven't travelled yet!");
            holder.descTextView.setText("");
            holder.costTextView.setText("");
            holder.openJourneyBtn.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return journeyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView descTextView;
        public TextView costTextView;
        public Button openJourneyBtn;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.journey_date);
            descTextView = itemView.findViewById(R.id.journey_place_desc);
            costTextView = itemView.findViewById(R.id.journey_cost);
            openJourneyBtn = itemView.findViewById(R.id.journey_open);
        }
    }
}

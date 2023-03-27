package com.vocalix.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vocalix.app.R;
import com.vocalix.app.activity.DetailsActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> implements Filterable {

    private final List<Map<String, Object>> places;
    private final List<Map<String, Object>> filteredPlaces;
    private final Context context;

    public PlacesAdapter(Context context, List<Map<String, Object>> places) {
        this.context = context;
        this.places = places;
        this.filteredPlaces = new ArrayList<>(places);
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vertical_place, parent, false);
        return new PlaceViewHolder(view, context, places);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Map<String, Object> place = filteredPlaces.get(position);

        holder.placeName.setText((String) place.get("name"));
        holder.placeLocation.setText((String) place.get("location"));
        holder.placePrice.setText((String) place.get("price"));

        // Load the image using a library like Glide or Picasso
        Glide.with(context).load((String) place.get("img")).into(holder.placeImage);
    }

    @Override
    public int getItemCount() {
        return filteredPlaces.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeName, placeLocation, placePrice;
        Context context;
        List<Map<String, Object>> places;

        public PlaceViewHolder(@NonNull View itemView, Context context, List<Map<String, Object>> places) {
            super(itemView);
            this.context = context;
            this.places = places;

            placeImage = itemView.findViewById(R.id.place_image);
            placeName = itemView.findViewById(R.id.place_name);
            placeLocation = itemView.findViewById(R.id.place_location);
            placePrice = itemView.findViewById(R.id.place_price);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Map<String, Object> place = places.get(position);
                    // Handle the item click event here, and navigate to the Details activity
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("place", (Serializable) place);
                    context.startActivity(intent);
                }
            });
        }
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Map<String, Object>> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(places);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Map<String, Object> place : places) {
                        String name = Objects.requireNonNull(place.get("name")).toString().toLowerCase();
                        String location = Objects.requireNonNull(place.get("location")).toString().toLowerCase();

                        if (name.contains(filterPattern) || location.contains(filterPattern)) {
                            filteredList.add(place);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredPlaces.clear();
                filteredPlaces.addAll((List<Map<String, Object>>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}


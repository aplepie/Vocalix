package com.vocalix.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vocalix.app.adapter.PlacesAdapter;
import com.vocalix.app.resources.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private PlacesAdapter placesAdapter;
    private List<Map<String, Object>> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup your RecyclerView here
        populatePlaces();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesAdapter = new PlacesAdapter(this, places);
        recyclerView.setAdapter(placesAdapter);

        // Add the SpaceItemDecoration with the desired space between items
        int spaceBetweenItems = 20; // Adjust this value for more or less space
        SpaceItemDecoration itemDecoration = new SpaceItemDecoration(spaceBetweenItems);
        recyclerView.addItemDecoration(itemDecoration);

        EditText searchEditText = findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        // Handle Home navigation
                        return true;
                    case R.id.navigation_favorite:
                        // Handle Favorites navigation
                        return true;
                    case R.id.navigation_comment:
                        // Handle Comments navigation
                        return true;
                    case R.id.navigation_person:
                        // Handle Profile navigation
                        return true;
                }
                return false;
            }
        });
        */
    }

    private void populatePlaces() {
        places = new ArrayList<>();

        Map<String, Object> place1 = new HashMap<>();
        place1.put("name", "Place 1");
        place1.put("location", "Location 1");
        place1.put("price", "$100");
        place1.put("img", "https://lp-cms-production.imgix.net/2021-02/shutterstock_557009335.jpg");

        Map<String, Object> place2 = new HashMap<>();
        place2.put("name", "Place 2");
        place2.put("location", "Location 2");
        place2.put("price", "$200");
        place2.put("img", "https://media.timeout.com/images/105809001/image.jpg");

        Map<String, Object> place3 = new HashMap<>();
        place3.put("name", "Place 3");
        place3.put("location", "Location 3");
        place3.put("price", "$300");
        place3.put("img", "https://assets.traveltriangle.com/blog/wp-content/uploads/2016/07/limestone-rock-phang-nga-1-Beautiful-limestone-rock-in-the-ocean.jpg");

        Map<String, Object> place4 = new HashMap<>();
        place4.put("name", "Place 4");
        place4.put("location", "Location 4");
        place4.put("price", "$400");
        place4.put("img", "https://img.money.com/2019/02/190304-best-in-travel-2019-international-nanjing.jpg?quality=60");

        Map<String, Object> place5 = new HashMap<>();
        place5.put("name", "Place 5");
        place5.put("location", "Location 5");
        place5.put("price", "$200");
        place5.put("img", "https://d2rdhxfof4qmbb.cloudfront.net/wp-content/uploads/20180301194244/Rome-Tile.jpg");

        Map<String, Object> place6 = new HashMap<>();
        place6.put("name", "Place 6");
        place6.put("location", "Location 6");
        place6.put("price", "$200");
        place6.put("img", "https://img.theculturetrip.com/1440x807/smart/wp-content/uploads/2022/07/h96yg9-1.jpg");

        // Add more places if necessary
        // ...

        places.add(place1);
        places.add(place2);
        places.add(place3);
        places.add(place4);
        places.add(place5);
        places.add(place6);
    }

    public void goToProfilePage(View view) {
        /*
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        */
        // Perform your desired action here
        Toast.makeText(this, "Profile icon clicked", Toast.LENGTH_SHORT).show();
    }

    public void goToMoreClicked(View view) {
        // Perform your desired action here
        Toast.makeText(this, "Three dots icon clicked", Toast.LENGTH_SHORT).show();
    }
}

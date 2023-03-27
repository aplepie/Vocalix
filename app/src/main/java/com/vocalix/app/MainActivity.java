package com.vocalix.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vocalix.app.adapter.PlacesAdapter;

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

        // Add more places if necessary
        // ...

        places.add(place1);
        places.add(place2);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            // Handle the notification icon click event here
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.poutividad.act11;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";

    private Spinner spinnerMapType;
    private GoogleMap mMap;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerMapType = findViewById(R.id.spinnerMapType);

        // **IMPORTANTE**: Reemplaza "YOUR_API_KEY" con tu clave de API real
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "YOUR_API_KEY");
        }

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupAutocomplete();
        setupSpinner();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(19.4326, -99.1332); // Ciudad de México
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
    }

    private void setupAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
            autocompleteFragment.setHint("Ingrese una dirección");
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Log.i(TAG, "Place: " + place.getName() + ", " + place.getAddress());
                    addMarker(place.getLatLng(), place.getName());
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.e(TAG, "An error occurred: " + status);
                    Toast.makeText(MainActivity.this, "Error en la búsqueda de lugares", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.map_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMapType.setAdapter(adapter);

        spinnerMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mMap == null) return;
                String type = parent.getItemAtPosition(position).toString();
                switch (type) {
                    case "Satélite":
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case "Relieve":
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    default:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                }
                updateMarkerColor();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void addMarker(LatLng location, String title) {
        if (mMap == null) return;
        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = mMap.addMarker(new MarkerOptions().position(location).title(title));
        updateMarkerColor();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private void updateMarkerColor() {
        if (currentMarker == null) return;

        float color = BitmapDescriptorFactory.HUE_BLUE;
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            color = BitmapDescriptorFactory.HUE_RED;
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN) {
            color = BitmapDescriptorFactory.HUE_GREEN;
        }
        currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(color));
    }
}

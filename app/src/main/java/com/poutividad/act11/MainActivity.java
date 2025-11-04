package com.poutividad.act11;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText etDireccion;
    private Button btnBuscar;
    private Spinner spinnerMapType;
    private GoogleMap mMap;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDireccion = findViewById(R.id.etDireccion);
        btnBuscar = findViewById(R.id.btnBuscar);
        spinnerMapType = findViewById(R.id.spinnerMapType);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupSpinner();
        setupSearchButton();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Default location (e.g., Mexico City)
        LatLng defaultLocation = new LatLng(19.4326, -99.1332);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
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
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupSearchButton() {
        btnBuscar.setOnClickListener(v -> {
            String address = etDireccion.getText().toString();
            if (!address.isEmpty()) {
                searchAddress(address);
            } else {
                Toast.makeText(this, "Por favor, ingrese una dirección", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAddress(String addressString) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                
                if (currentMarker != null) {
                    currentMarker.remove();
                }
                
                currentMarker = mMap.addMarker(new MarkerOptions().position(location).title(addressString));
                updateMarkerColor(); // Set color for the new marker

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            } else {
                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al buscar la dirección", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarkerColor() {
        if (currentMarker == null) return;

        float color = BitmapDescriptorFactory.HUE_BLUE; // Default (Normal)
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            color = BitmapDescriptorFactory.HUE_RED;
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN) {
            color = BitmapDescriptorFactory.HUE_GREEN;
        }
        
        currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(color));
    }
}

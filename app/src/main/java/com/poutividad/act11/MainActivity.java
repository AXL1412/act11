package com.poutividad.act11;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";

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

        // --- CRASH-PROOF LOGIC: Disable all UI until the map is fully loaded ---
        etDireccion.setEnabled(false);
        btnBuscar.setEnabled(false);
        spinnerMapType.setEnabled(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error fatal: No se pudo cargar el fragmento del mapa.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Mapa listo y cargado.");

        // --- UI is enabled here, ONLY after the map is guaranteed to be ready ---
        etDireccion.setEnabled(true);
        btnBuscar.setEnabled(true);
        spinnerMapType.setEnabled(true);

        setupSearchButton();
        setupSpinner();

        // Set a default location (e.g., Mexico City)
        LatLng defaultLocation = new LatLng(19.4326, -99.1332);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
    }

    private void setupSearchButton() {
        btnBuscar.setOnClickListener(v -> {
            String addressString = etDireccion.getText().toString();
            hideKeyboard();
            if (!addressString.isEmpty()) {
                searchAddress(addressString);
            } else {
                Toast.makeText(this, "Por favor, ingrese una dirección", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAddress(String addressString) {
        Log.d(TAG, "Buscando dirección: " + addressString);
        // Use Geocoder for a simple and stable search
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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
            Log.e(TAG, "Error al buscar la dirección", e);
            Toast.makeText(this, "Error de red o geocodificación. Verifique su conexión o la API Key.", Toast.LENGTH_LONG).show();
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
                if (mMap == null) return; // This check is now redundant, but safe

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

    private void updateMarkerColor() {
        if (currentMarker == null || mMap == null) return;

        float color = BitmapDescriptorFactory.HUE_AZURE; // Elegant blue for Normal map
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            color = BitmapDescriptorFactory.HUE_CYAN; // Accent color for Satellite
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN) {
            color = BitmapDescriptorFactory.HUE_VIOLET; // Primary theme color for Terrain
        }

        currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(color));
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

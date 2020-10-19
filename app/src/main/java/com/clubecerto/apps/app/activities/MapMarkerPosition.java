package com.clubecerto.apps.app.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.clubecerto.apps.app.GPS.GPStracker;
import com.clubecerto.apps.app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;


public class MapMarkerPosition extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private GPStracker mGps;
    private double lat = 0, lng = 0;
    private String address = null;
    private Button confirm;
    private LatLng myposition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_marker_position);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        confirm = findViewById(R.id.confirmBtn);
        confirm.setOnClickListener(this);
        mGps = new GPStracker(this);


        if (mGps.canGetLocation()) {

            lat = mGps.getLatitude();
            lng = mGps.getLongitude();
            myposition = new LatLng(lat, lng);
        } else {
            myposition = new LatLng(-34, 151);
        }


        lat = myposition.latitude;
        lng = myposition.longitude;

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near myposition, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in myposition and move the camera
        mMap.addMarker(new MarkerOptions().position(myposition).title("Marker in myposition").draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myposition));

        mMap.setOnMarkerDragListener(this);
    }

   /* public void onMapSearch(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.searchBar);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address description = addressList.get(0);
            LatLng latLng = new LatLng(description.getLatitude(), description.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }*/


    public void getAddressFromMap(Marker marker) {
        try {
            LatLng newPosition = marker.getPosition();
            Geocoder geo = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(newPosition.latitude, newPosition.longitude, 1);
            if (addresses.isEmpty()) {
                address = "Waiting for Location";
            } else {
                if (addresses.size() > 0) {
                    address = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                    Toast.makeText(this, address, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {


        getAddressFromMap(marker);
    }
}



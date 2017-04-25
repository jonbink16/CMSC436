package com.example.oscar.cmsc436.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.OutdoorWalkTest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class OutdoorWalkActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {


    private long startTime, endTime;
    private LocationManager manager;
    private Location previousLoc;
    private LatLng startLatLng, endLatLng;
    private Set<Polyline> lineSet;
    private String provider;
    private float dist;
    private boolean testStart;
    private final int REQUEST = 1;
    private GoogleMap map;
    private UiSettings uiSettings;
    private Marker startMarker, mCurrLocationMarker;
    private ArrayList<Marker> markers = new ArrayList<>();
    private final long MIN_TIME = 10;
    private final float MIN_DIST = 10;
    private float mps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdoor_walk);

        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        lineSet = new HashSet<>();
        dist = 0;
        mps = 0;
        previousLoc = null;
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        final Criteria criteria = new Criteria();

        provider = manager.getBestProvider(criteria, false);
        testStart = false;
        mCurrLocationMarker = null;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},  REQUEST);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},  REQUEST);
        }

        findViewById(R.id.endWalk).setEnabled(false);
        findViewById(R.id.startWalk).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                testStart = true;
                findViewById(R.id.startWalk).setEnabled(false);
                findViewById(R.id.endWalk).setEnabled(true);
                mps = 0;
                dist = 0;
                startTime = SystemClock.elapsedRealtime();
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location loc = manager.getLastKnownLocation(provider);
                if (loc != null) {
                    onLocationChanged(loc);
                }
                for(Polyline p : lineSet){
                    p.setVisible(false);
                }
                manager.requestLocationUpdates(provider,MIN_TIME,MIN_DIST,OutdoorWalkActivity.this);
            }
        });

        findViewById(R.id.endWalk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testStart = false;
                findViewById(R.id.startWalk).setEnabled(true);
                findViewById(R.id.endWalk).setEnabled(false);
                endTime = SystemClock.elapsedRealtime();
                long elapsedMilli = endTime-startTime;
                double realTime = elapsedMilli/1000.0;
                mps = dist/(float)realTime;
                Database.getInstance().addOutdoorWalkTest(new OutdoorWalkTest(elapsedMilli/1000, dist, mps));
                ((TextView)findViewById(R.id.outdoorWalkText)).setText(String.valueOf(realTime) + " seconds elapsed." + "\nDistance: " + dist + " meters.\n" + mps + " m/s.");
                manager.removeUpdates(OutdoorWalkActivity.this);

                // Center Camera between the two points
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                System.out.println(bounds.toString());
                int padding = 300; // Padding between marker and edges of the map
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.moveCamera(cu);
                mCurrLocationMarker.setIcon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng prevLatLng;
        if(previousLoc == null){
            previousLoc = location;
            return;
        }else{
            dist += previousLoc.distanceTo(location);
            prevLatLng = new LatLng(previousLoc.getLatitude(), previousLoc.getLongitude());
            previousLoc = location;
        }

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        endLatLng = latLng;
        Polyline line = map.addPolyline(new PolylineOptions()
                .add(prevLatLng, latLng)
                .width(5)
                .color(Color.RED));
        lineSet.add(line);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = map.addMarker(markerOptions);

        markers.add(mCurrLocationMarker);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        map.moveCamera(cameraUpdate);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(testStart) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location loc = manager.getLastKnownLocation(provider);

            if (loc != null) {
                onLocationChanged(loc);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = manager.getLastKnownLocation(provider);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        startMarker = map.addMarker(new MarkerOptions().position(latLng).title("Start"));
        startMarker.setIcon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markers.add(startMarker);

        mCurrLocationMarker = map.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        startLatLng = latLng;
        map.moveCamera(cameraUpdate);
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        uiSettings = map.getUiSettings();
        uiSettings.setAllGesturesEnabled(false);
    }

}

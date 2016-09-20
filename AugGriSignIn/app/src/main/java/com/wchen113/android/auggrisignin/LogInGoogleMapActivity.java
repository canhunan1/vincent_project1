package com.wchen113.android.auggrisignin;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.Manifest;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LogInGoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    GoogleMap mGoogleMapActivity;
    GoogleApiClient mGoogleApiClient;

    private double currentLatitude;
    private double currentLongitude;

    String personEmail=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServicesAvailable()) {
            Toast.makeText(this, "Perfect!!!", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_log_in_google_map);
            initMap();
        } else {
            //No google maps
        }
        Intent intent1=getIntent();
        personEmail=intent1.getStringExtra(MainActivity.EXTRA_MESSAGE);

    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void initMap() {
        MapFragment mmapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFrag);
        mmapFragment.getMapAsync(this);
        Toast.makeText(this, "Awesome!!!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMapActivity = googleMap;
        Toast.makeText(this, "here you go!!!", Toast.LENGTH_LONG).show();
//        goToLocation(33.420491, -111.931011, 16);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling

                return;
            }
        }
        mGoogleMapActivity.setMyLocationEnabled(true);
        mGoogleApiClient=new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

    }

/*    private void goToLocation(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMapActivity.moveCamera(update);
    }*/

    LocationRequest mLocationRequest;
    Marker placeMarker;
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "here we go!!!", Toast.LENGTH_LONG).show();
        mLocationRequest=LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling

                return;
            }
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    private List<Tag> tagList = null;

    @Override
    public void onLocationChanged(Location location) {
        if(location==null){
            Toast.makeText(this,"Can't get current location", Toast.LENGTH_LONG).show();
        }else{

            Double lat = location.getLatitude();
            Double lng = location.getLongitude();

            if(Math.abs(this.currentLatitude-lat)>0.000001&&Math.abs(this.currentLongitude-lng)>0.000001) {
                this.currentLatitude = lat;
                this.currentLongitude = lng;

                LatLng ll=new LatLng(lat,lng);
                CameraUpdate update=CameraUpdateFactory.newLatLngZoom(ll,16);
                mGoogleMapActivity.animateCamera(update);
                setPlaceMarker(lat,lng);

                RequestQueue queue= Volley.newRequestQueue(this);
                String url="http://roblkw.com/msa/neartags.php";
                final Map<String, String> params = new HashMap<String, String>();

                if(personEmail==null){
                    Intent intent=getIntent();
                    personEmail=intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
                }
                params.put("email", personEmail);
                params.put("loc_long", lng.toString());
                params.put("loc_lat", lat.toString());
                // Request a string response from the provided URL.
                StringRequest stringRequest = postTagNearByRequest(params, url);
                queue.add(stringRequest);

            }


        }
    }
    private Marker setCollectMarker(LatLng ll){
        MarkerOptions optionsCollect = new MarkerOptions()
                .title("Collect")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.c))
                .position(ll)
                .snippet("Clicking me to collect a tag");
        return mGoogleMapActivity.addMarker(optionsCollect);
    }

    private StringRequest postTagNearByRequest(final Map<String, String> params, String url) {

        return new StringRequest(Request.Method.POST, url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response){
                //The format of the response is "tagId,Latitude,Longitude"
                String[] tagLoc = response.trim().split("[,]+");
                //First we assume the format of the response never changed and the response we get is always correct
                //then we can get the numb int numTag = 0;
                int numTag = tagLoc.length%3==0?tagLoc.length/3:-1;
                if(tagList == null){
                    tagList = new LinkedList<Tag>();
                }
                int i = numTag;
                while(--i >=0) {//we have tags near by
                    tagList.add(new Tag(Integer.valueOf(tagLoc[i * 3]), new LatLng(Double.valueOf(tagLoc[i * 3 + 2]), Double.valueOf(tagLoc[i * 3 + 1]))));
                    setCollectMarker(tagList.get(numTag - i- 1).ll);
                }

                if(response.equals("0")) {
                    Intent intent = new Intent(getApplicationContext(), LogInGoogleMapActivity.class);
                    startActivity(intent);
                }
            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

    }

    private void setPlaceMarker(double lat, double lng) {
        if(placeMarker != null){
            placeMarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .position(new LatLng(lat,lng))
                .snippet("I am here");
        placeMarker = mGoogleMapActivity.addMarker(options);
    }
}
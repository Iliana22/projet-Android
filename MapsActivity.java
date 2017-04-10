package android.projet.meteo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import android.Manifest;
import android.Manifest.permission;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {
    private Marker marker;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//
        super.onCreate(savedInstanceState);//
        setContentView(R.layout.activity_maps);//
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);//
        mapFragment.getMapAsync(this);//
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {//
        mMap = googleMap;//
        miUbicacion();
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                GeneralWeather myMeteo = (GeneralWeather) marker.getTag();
                //Toast.makeText(getApplicationContext(),"OK ¡¡¡¡", Toast.LENGTH_SHORT).show();
                Intent secondActivity = new Intent(MapsActivity.this,MainActivityD.class);

                // On rajoute un extra
                secondActivity.putExtra("name",myMeteo.getName());
               

                // Puis on lance l'intent !
                startActivity(secondActivity);
            }
        });
    }

    LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //agregarMarcador(location.getLatitude(), location.getLongitude());
            DownloadWebPageTask task = new DownloadWebPageTask(location);
            task.execute(new String[] { "http://api.openweathermap.org/data/2.5/weather?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&units=metric&lang=fr&appid=5bdfb081811a28abc515bc673fc0d20f" });
            Log.d("DEBUG", String.valueOf(location.getLatitude()));
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
    };

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        Location location;

        public DownloadWebPageTask(Location location){
            this.location = location;
        }

        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            Request request =
                    new Request.Builder()
                            .url(urls[0])
                            .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Download failed";
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("DEBUG", result);
            //Creamos la estancia que utiliza el gson
            Gson gson = new GsonBuilder().create();
            //Entramos
            GeneralWeather myweather = gson.fromJson(result, GeneralWeather.class);
            Log.d("DEBUG", myweather.getName());

            if(marker == null) {
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))); //Se crea el marcador
            }else{
                marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }
            marker.setTag(myweather);
        }
    }

    public void miUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //  ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location !=  null){
            DownloadWebPageTask task = new DownloadWebPageTask(location);
            task.execute(new String[] { "http://api.openweathermap.org/data/2.5/weather?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&units=metric&lang=fr&appid=5bdfb081811a28abc515bc673fc0d20f" });
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,15000,0,locListener);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return prepareInfoView(marker);
    }

    private View prepareInfoView(Marker marker){
        View v = getLayoutInflater().inflate(R.layout.mapinformation, null);
        GeneralWeather myMeteo = (GeneralWeather) marker.getTag();

        TextView title = (TextView) v.findViewById(R.id.txtTitle);
        title.setText(myMeteo.getName());


        TextView temp = (TextView) v.findViewById(R.id.tempe);
        temp.setText("Temperature  " + myMeteo.getMain().getTemp() + "°C");

        TextView temp_min = (TextView) v.findViewById(R.id.min);
        temp_min.setText("Min  " + myMeteo.getMain().getTemp_min() + "°C");

        TextView temp_max = (TextView) v.findViewById(R.id.max);
        temp_max.setText("Max  " + myMeteo.getMain().getTemp_min() + "°C");

        TextView desc = (TextView) v.findViewById(R.id.desc);
        desc.setText("" + myMeteo.getWeather().get(0).getDescription());

        ImageView image = (ImageView) v.findViewById(R.id.imageView);

        image.setImageDrawable(getDrawable(getResources().getIdentifier("_" + myMeteo.getWeather().get(0).getIcon(),"drawable",getPackageName()) ));

        return v;
    }


}

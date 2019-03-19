package com.example.hikerswatch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    LocationListener locationListener;
    TextView lattitudeTextView;
    TextView longitudeTextView;
    TextView cityTextView;
    TextView cuacaTextView;
    TextView deskripsiCuacaTextView;
    String city = "Malang";



    //AYSNC TASK
    public class DownloadTask extends AsyncTask<String, Void, String> {
        //parameter 1 ialah apa yang akan dilakukan asynctask
        //parameter 2 ialah
        //parameter 3 ialah kembalian dari asynctask


        @Override
        protected String doInBackground(String... params) {
            //protected String ini dapat di akses di package ini
            String result = "";
            URL url ;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;
            }
            catch (Exception e){
                e.printStackTrace();
                return "Failed";
            }






        }

        protected  void onPostExecute(String result){
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                Log.i("result", result);
                String weatherInfo =  jsonObject.getString("weather");
                Log.i("weather", weatherInfo);
                JSONArray arr =  new JSONArray(weatherInfo);
                for(int i = 0; i<arr.length();i++){
                    JSONObject jsonPart = arr.getJSONObject(i);
                    String main = jsonPart.getString("main");
                    String description = jsonPart.getString("description");
                    Log.i("main", main);
                    Log.i("descripion", description);
                    cuacaTextView.setText("Cuaca : "+ main);
                    deskripsiCuacaTextView.setText("Deskripsi Cuaca "+String.valueOf(description));


                }



            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }







    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lattitudeTextView = (TextView) findViewById(R.id.lattitudeTextView);
        longitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        cityTextView = (TextView) findViewById(R.id.cityTextView);
        cuacaTextView = (TextView) findViewById(R.id.cuacaTextView);
        deskripsiCuacaTextView = (TextView) findViewById(R.id.deskripsiCuacaTextView);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //mendengar bila ada perubahan lokasi
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Location", location.toString());


                //geocoder
                lattitudeTextView.setText("Lattitude : "+String.valueOf(location.getLatitude()));
                longitudeTextView.setText("Longitude : "+String.valueOf(location.getLongitude()));

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                    if(listAddress != null && listAddress.size() > 0){
                        Log.i("Place Info", listAddress.get(0).getLocality());
                        cityTextView.setText("Kota : "+String.valueOf(listAddress.get(0).getLocality()));
                        city = String.valueOf(listAddress.get(0).getLocality());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

        //jika device dibawah marshmallow
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            }
        }
    }

    public  void getCuaca(View view){
        DownloadTask task = new DownloadTask();
        //api key
        //545ca74824f667a6e84b845f47938c27
        String link = "http://api.openweathermap.org/data/2.5/weather?q="+city+"&APPID=545ca74824f667a6e84b845f47938c27";
        Log.i("kota", city);
        Log.i("link", link);
        task.execute(link);

    }
}

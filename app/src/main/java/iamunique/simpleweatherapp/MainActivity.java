package iamunique.simpleweatherapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // Textview to show temperature and description
    TextView temperature, description, cityName;
    EditText city;
    String enteredCity;
    Button search, location;
    private double lat, lon;

    private FusedLocationProviderClient mFusedLocationClient;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;

    //String city= "Darmstadt";
    //String country= "de";

    // we"ll make HTTP request to this URL to retrieve weather conditions
    //String weatherWebserviceURL = "http://api.openweathermap.org/data/2.5/weather?q="+enteredCity+"&appid=61eb25f1560a6de656f3a3346bf2a956&units=metric";

    //the loading Dialog
    ProgressDialog pDialog;

    // background image
    ImageView weatherBackground;

    // JSON object that contains weather information
    JSONObject jsonObj;

    private void callService(final String webService){

        weatherBackground = (ImageView) findViewById(R.id.weatherbackground);

        Log.v("SERVICE", webService.toString());
        //Log.v("EnteredCity", enteredCity.toString());

        // prepare and show the loading Dialog
        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Please wait while retrieving weather conditions ..");
        pDialog.setCancelable(false);
        pDialog.show();


        // make HTTP request to retrieve the weather
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                webService, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Parsing json object response
                    // response will be a json object

                    jsonObj = (JSONObject) response.getJSONArray("weather").get(0);
                    // display weather description into the "description textview"
                    description.setText(jsonObj.getString("description"));
                    // display the temperature
                    cityName.setText(response.getString("name") + ", ");
                    temperature.setText(response.getJSONObject("main").getString("temp") + " °C");

                    String backgroundImage = "";

                    //choose the image to set as background according to weather condition
                    if (jsonObj.getString("main").equals("Clouds")) {
                        backgroundImage = "https://marwendoukh.files.wordpress.com/2017/01/clouds-wallpaper2.jpg";
                    } else if (jsonObj.getString("main").equals("Rain")) {
                        backgroundImage = "https://marwendoukh.files.wordpress.com/2017/01/rainy-wallpaper1.jpg";
                    } else if (jsonObj.getString("main").equals("Snow")) {
                        backgroundImage = "https://marwendoukh.files.wordpress.com/2017/01/snow-wallpaper1.jpg";
                    }

                    // load image from link and display it on background
                    // We'll use the Glide library
                    Glide
                            .with(getApplicationContext())
                            .load(backgroundImage)
                            .centerCrop()
                            .crossFade()
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target target, boolean isFirstResource) {
                                    System.out.println(e.toString());
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(weatherBackground);

                    // hide the loading Dialog
                    pDialog.dismiss();


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error , try again ! ", Toast.LENGTH_LONG).show();
                    pDialog.dismiss();

                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("tag", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error while loading ... ", Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                pDialog.dismiss();
            }
        });
        // Adding request to request queue
        AppController.getInstance(MainActivity.this).addToRequestQueue(jsonObjReq);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //search city temperature

                //link graphical items to variables
                temperature = (TextView) findViewById(R.id.temperature);
                description = (TextView) findViewById(R.id.description);
                city = (EditText) findViewById(R.id.editText);
                cityName= (TextView) findViewById(R.id.textView3);
                enteredCity = city.getText().toString();
                final String weatherWebserviceURL = "http://api.openweathermap.org/data/2.5/weather?q=" + enteredCity + "&appid=61eb25f1560a6de656f3a3346bf2a956&units=metric";
                callService(weatherWebserviceURL);
            }
        });

        //Get my location
        location = (Button) findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSION_ACCESS_COARSE_LOCATION);
                }

                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    temperature = (TextView) findViewById(R.id.temperature);
                                    description = (TextView) findViewById(R.id.description);
                                    cityName= (TextView) findViewById(R.id.textView3);
                                    lat= location.getLatitude();
                                    lon= location.getLongitude();
                                    Log.v("LATLONG", lat+" "+lon);

                                    final String weatherWebserviceURL1 = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon="+ lon +"&appid=61eb25f1560a6de656f3a3346bf2a956&units=metric";
                                    callService(weatherWebserviceURL1);
                                }
                            }
                        });
            }
        });
    }
}

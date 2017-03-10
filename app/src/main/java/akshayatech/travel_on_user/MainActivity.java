package akshayatech.travel_on_user;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import static android.R.attr.type;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    GoogleApiClient mGoogleApiClient;

    Location mLastLocation, mCurrentLocation;
    LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;

    Double latInDouble, lonInDouble;

    String mLastUpdateTime;

    EditText dest;
    Button details;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                    .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();

        dest = (EditText) findViewById(R.id.destination);
        details = (Button) findViewById(R.id.send_details);

        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mCurrentLocation != null)
                {
//                    Toast.makeText(MainActivity.this, "mCurrentLocation", Toast.LENGTH_SHORT).show();
                    String destination = dest.getText().toString();
                    Double lat = mCurrentLocation.getLatitude();
                    Double lon = mCurrentLocation.getLongitude();
//                    Toast.makeText(MainActivity.this, lat + "" + lon, Toast.LENGTH_LONG).show();
                    String type = "phone";
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
//                    Intent intent = new Intent(MainActivity.this, BusDetails.class);
                    intent.putExtra("dest", destination);
                    intent.putExtra("lon", lon + "");
                    intent.putExtra("lat", lat + "");
                    intent.putExtra("type", type);
                    startActivity(intent);
//                    startActivity(intent);
                }
                else if (mLastLocation != null)
                {
//                    Toast.makeText(MainActivity.this, "mLastLocation", Toast.LENGTH_LONG).show();
                    String destination = dest.getText().toString();
                    Double lat = mLastLocation.getLatitude();
                    Double lon = mLastLocation.getLongitude();
                    String type = "phone";
                    Intent intent = new Intent(MainActivity.this, BusDetails.class);
                    intent.putExtra("dest", destination);
                    intent.putExtra("lon", lon);
                    intent.putExtra("lat", lat);
                    intent.putExtra("type", type);
                    startActivity(intent);

                }
                else
                {
                    Toast.makeText(MainActivity.this, "Retrieving location Pls wait", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    protected void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.e("Reached Here", "2");
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener) MainActivity.this);
        Log.e("Reached Here", "4");
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    11);

        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                builder.build());

        Log.e("Reached Here", "3");
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates temp = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    0x1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;


                }


            }
        });
        startLocationUpdates();

        if (mLastLocation != null) {
            latInDouble = mLastLocation.getLatitude();
            lonInDouble = mLastLocation.getLongitude();
            Log.e("Check location", " "+latInDouble + " " + lonInDouble);
//            lat.setText(String.valueOf(mLastLocation.getLatitude()));
//            lon.setText(String.valueOf(mLastLocation.getLongitude()));
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        lonInDouble = mCurrentLocation.getLongitude();
        latInDouble = mCurrentLocation.getLatitude();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//        updateLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        Log.e("Reached Here", "1");
//        updateUI();

    }

}

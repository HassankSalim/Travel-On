package akshayatech.travel_on_user;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String strLat, strLon, destination;
    private Polyline mPolyline;
    private TextView duration, distance;

    private Marker mMarkerA, mMarkerB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        destination = getIntent().getStringExtra("dest");
        duration = (TextView) findViewById(R.id.dur);
        distance = (TextView) findViewById(R.id.dist);
        strLat = getIntent().getStringExtra("lat");
        strLon = getIntent().getStringExtra("lon");


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {

        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        Double lat = Double.parseDouble(strLat);
        Double lon = Double.parseDouble(strLon);
//        mMap.setMinZoomPreference(12.0f);
//
//        LatLng user = new LatLng(lat, lon);
//        mMap.addMarker(new MarkerOptions().position(user).title("Marker for user"));
        testFunc(lat, lon);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(user));

        LatLng latLngA = new LatLng(lat, lon);
//        LatLng latLngB = new LatLng(9.9894, 76.5790);
        mMarkerA = mMap.addMarker(new MarkerOptions().position(latLngA).draggable(true).title("MarkerA"));
//        mMarkerB = mMap.addMarker(new MarkerOptions().position(latLngB).draggable(true).title("MarkerA"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngA));
        mMap.animateCamera( CameraUpdateFactory.zoomTo(12.0f));
    }

    private void testFunc(final Double lat, final Double lon)
    {

//        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+lat+","+lon+"&destinations=9.9894,76.5790&key=AIzaSyCkZpBkMhxeeARYdMcqyDjzKL2XqtSH1bQ ";
//        String temp_url = "https://maps.googleapis.com/maps/api/directions/json?units=metric&origins="+lat+","+lon+"&destinations=9.9894,76.5790&key=AIzaSyDQxqarK-1wdkb_7R4ulsKx1YDjhoVHiL0 ";
        String temp_url = "https://maps.googleapis.com/maps/api/directions/json?origin="+lat+","+lon+"&destination="+destination.trim()+"&sensor=false&mode=driving&key=AIzaSyDQxqarK-1wdkb_7R4ulsKx1YDjhoVHiL0 ";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, temp_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("Response", ""+response);
                    JSONArray routeArray = response.getJSONArray("routes");
                    JSONObject routes = routeArray.getJSONObject(0);
                    JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                    JSONArray legs = routes.getJSONArray("legs");
                    JSONObject details = legs.getJSONObject(0);
                    JSONObject duration_details = details.getJSONObject("duration");
                    JSONObject location_details = details.getJSONObject("end_location");
                    JSONObject distance_details = details.getJSONObject("distance");
                    Log.e("Details", " " + duration_details + " " + location_details);
                    distance.setText(distance_details.getString("text"));
                    duration.setText(duration_details.getString("text"));
                    LatLng latLngB = new LatLng(location_details.getDouble("lat"), location_details.getDouble("lng"));
                    mMarkerB = mMap.addMarker(new MarkerOptions().position(latLngB).draggable(true).title("MarkerB"));
                    String encodedString = overviewPolylines.getString("points");
                    List<LatLng> list = decodePoly(encodedString);
                    Polyline line = mMap.addPolyline(new PolylineOptions().addAll(list).width(20).geodesic(true).color(Color.BLUE));

                } catch (JSONException e) {
                    Toast.makeText(MapsActivity.this, "Exxxxxxx", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                Log.e("Success", "Success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        AppController.getInstance().addToRequestQueue(request);
    }


    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }


}

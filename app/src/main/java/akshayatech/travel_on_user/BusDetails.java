package akshayatech.travel_on_user;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BusDetails extends AppCompatActivity {


    JSONObject obj;
    String url = "http://192.168.0.20:8000/test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_details);

        String dest = getIntent().getStringExtra("dest");
        String strLat = getIntent().getStringExtra("lat");
        String strLon = getIntent().getStringExtra("lon");
        String type = getIntent().getStringExtra("type");

        Toast.makeText(BusDetails.this, dest + "\n" + strLat + "\n" + strLon + "\n" + type + "\n", Toast.LENGTH_SHORT).show();

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        Double lat = Double.parseDouble(strLat);
        Double lon = Double.parseDouble(strLon);

        obj = new JSONObject();

        while(true)
        {
            try
            {
                addresses = geocoder.getFromLocation(lat, lon, 1);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String subAdminArea = addresses.get(0).getSubLocality();
                Toast.makeText(BusDetails.this, city + "\n" + state + "\n" + country + "\n" + postalCode + "\n" + subAdminArea, Toast.LENGTH_LONG).show();
                try {
                    obj.put("dest", dest);
                    obj.put("lon", lon);
                    obj.put("lat", lat);
                    obj.put("type", type);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendData();
                break;
            }
            catch (IOException e)
            {
                Log.e("Final Pdt", "Nothing");
                e.printStackTrace();
                Log.e("Final Pdt", "Nothing2");
            }
        }
    }

    private void sendData() {

        Log.e("Final Product", obj + "\n");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    Toast.makeText(BusDetails.this, response.getString("result"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }


}

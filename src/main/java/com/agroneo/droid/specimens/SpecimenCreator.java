package com.agroneo.droid.specimens;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agroneo.droid.R;

import live.page.android.sys.LocationUtils;

public class SpecimenCreator extends AppCompatActivity implements LocationListener {

    private int ACCESS_LOCATION = 105;
    private LocationManager lm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specimen_creator);
        //Selectable species = findViewById(R.id.species);

        findViewById(R.id.localize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
                    Toast.makeText(SpecimenCreator.this, R.string.location_disable, Toast.LENGTH_LONG).show();
                    return;
                }

                if (lm != null) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    lm.removeUpdates(SpecimenCreator.this);
                    lm = null;
                    return;
                }

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


                lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0L, SpecimenCreator.this);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView localization = findViewById(R.id.localization);
        localization.setText(LocationUtils.convertDMS(location));
        if (!location.hasAccuracy()) {
            return;
        }
        if (location.getAccuracy() < 1) {
            lm.removeUpdates(SpecimenCreator.this);
            localization.setTextColor(Color.GREEN);
        } else if (location.getAccuracy() < 5) {
            localization.setTextColor(Color.YELLOW);
        } else {
            localization.setTextColor(Color.RED);
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
        lm.removeUpdates(SpecimenCreator.this);
    }
}

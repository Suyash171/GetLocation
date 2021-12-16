package com.example.forest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
;
import com.google.android.gms.common.api.ResolvableApiException;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;


/**
 * Created by naresh on 17-Jul-2019.
 */
public abstract class BaseLocationProviderActivity extends BaseActivity {
    private static final String TAG = "BaseLocationProviderAct";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location mCurrentLocation;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 512;
    private static final int REQUEST_CHECK_SETTINGS = 513;

    protected abstract void onLocationUpdated(Location location);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

     void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();

                if (mCurrentLocation != null) {
                    Log.d(TAG, "got location : " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
                }
                onLocationUpdated(mCurrentLocation);
            }
        };
    }

     void export() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Write external storage permission is not granted",Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncTaskExportDB(this).execute();
       // progressBar.setVisibility(View.VISIBLE);
    }

    //location request
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000/2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(locationSettingsRequest);
        task.addOnSuccessListener(BaseLocationProviderActivity.this, locationSettingsResponse -> {

            //request location permission if not provided
            if (ActivityCompat.checkSelfPermission(BaseLocationProviderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                /*&& ActivityCompat.checkSelfPermission(BaseLocationProviderActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
                showLocationPermissionRationaleDialog();
                return;
            }
            //requestingLocationUpdates = true;
            Log.d(TAG, "starting location updates");
            Task<Void> taskReqLocUpdates = fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            taskReqLocUpdates.addOnSuccessListener(command -> {
                Log.d(TAG, "taskReqLocUpdates success");
            });

            taskReqLocUpdates.addOnFailureListener(command -> {
                Log.d(TAG, "taskReqLocUpdates failed");
                if (!BaseLocationProviderActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    return;
                }
                //ShowMessageUtils.showToast(BaseLocationProviderActivity.this, "unable to get location", MessageType.ERROR);
            });

            taskReqLocUpdates.addOnCanceledListener(() -> {
                Log.d(TAG, "taskReqLocUpdates cancelled");
                if (!BaseLocationProviderActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    return;
                }
               // ShowMessageUtils.showToast(BaseLocationProviderActivity.this, "unable to get location", MessageType.ERROR);
            });
        });

        task.addOnFailureListener(BaseLocationProviderActivity.this, e -> {
            //requestingLocationUpdates = false;
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(BaseLocationProviderActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });

    }

     void stopLocationUpdates() {
        /*if (!requestingLocationUpdates){
            return;
        }*/
        Log.d(TAG, "stopping location updates");
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void showLocationPermissionRationaleDialog() {
        DialogInterface.OnClickListener okClickListener = (dialog, which) -> {
            //requestLocationPermission();
            //openAppSettings();
            dialog.dismiss();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.ACCESS_COARSE_LOCATION*/},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        };

        DialogInterface.OnClickListener exitClickListener = (dialog, which) -> dialog.dismiss();
         /* ActivityUtils.createAlertDialog(this
                , getString(R.string.alert_title_location_permission_rationale)
                , getString(R.string.alert_message_location_permission_rationale)
                , getString(R.string.ok)
                , okClickListener
                , getString(R.string.cancel)
                , exitClickListener
                , true).show();*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                //showOpenSettingsDialog();
              //  ShowMessageUtils.showToast(this, getString(R.string.alert_message_location_permission_rationale), MessageType.WARNING);
            }
        }
    }

    @Nullable
    public Location getmCurrentLocation() {
        return mCurrentLocation;
    }
}

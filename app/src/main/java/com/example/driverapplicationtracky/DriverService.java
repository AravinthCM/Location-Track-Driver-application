package com.example.driverapplicationtracky;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverService extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final long LOCATION_UPDATE_INTERVAL = 1000;
    private FusedLocationProviderClient locationProviderClient;
    private Switch locationSwitch;
    private Button updateLocationButton;
    private Handler locationUpdateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_service);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationSwitch = findViewById(R.id.locationSwitch);
        updateLocationButton = findViewById(R.id.updateLocationButton);
        locationUpdateHandler = new Handler();

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Start periodic location updates
                    startPeriodicLocationUpdates();
                } else {
                    // Stop periodic location updates
                    stopPeriodicLocationUpdates();
                }
            }
        });

        updateLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Manually update location
                updateLocation();
            }
        });
    }

    private void startPeriodicLocationUpdates() {
        locationUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update location and post again after the interval
                updateLocation();
                locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);

        Toast.makeText(this, "Location sharing enabled", Toast.LENGTH_SHORT).show();
    }

    private void stopPeriodicLocationUpdates() {
        // Remove callbacks to stop periodic updates
        locationUpdateHandler.removeCallbacksAndMessages(null);

        Toast.makeText(this, "Location sharing disabled", Toast.LENGTH_SHORT).show();
    }

    private void updateLocation() {
        // Request the last known location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Push the current location to Firebase
                        pushLocationToFirebase(location);
                        Toast.makeText(DriverService.this, "Location updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DriverService.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void pushLocationToFirebase(Location location) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();

            // Encode the email address to create a valid Firebase Database path
            String encodedEmail = userEmail.replace(".", "_").replace("@", "_");

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference busesRef = database.getReference("Buses");

            busesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot busSnapshot : dataSnapshot.getChildren()) {
                        DataSnapshot driverSnapshot = busSnapshot.child("Driver");
                        String storedEmail = driverSnapshot.child("email").getValue(String.class);

                        if (storedEmail != null && storedEmail.equals(userEmail)) {
                            // Email matches, update the location for the corresponding bus
                            DatabaseReference driverLocationRef = busSnapshot.getRef()
                                    .child("Location")
                                    .child(encodedEmail); // Use encodedEmail as the node name

                            driverLocationRef.child("latitude").setValue(location.getLatitude());
                            driverLocationRef.child("longitude").setValue(location.getLongitude());

                            Toast.makeText(DriverService.this, "Location updated", Toast.LENGTH_SHORT).show();
                            return; // Break out of the loop once a match is found
                        }
                    }

                    Toast.makeText(DriverService.this, "Invalid driver email", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors that may occur
                    Toast.makeText(DriverService.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform the location-related operation
                updateLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

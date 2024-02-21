package com.example.driverapplicationtracky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegistration extends AppCompatActivity {

    TextInputLayout name,email,latitude,longitude,password;
    Button registerButton;
    FirebaseDatabase rootnode;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_registration);

        name=findViewById(R.id.getName);
        email=findViewById(R.id.getEmail);
        latitude=findViewById(R.id.getLatitude);
        longitude=findViewById(R.id.getLongitude);
        password=findViewById(R.id.getPassword);
        registerButton=findViewById(R.id.submit);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference driverRef = database.getReference("drivers");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser(){

        String driverName = name.getEditText().getText().toString().trim();
        String driverEmail = email.getEditText().getText().toString().trim();
        String driverLatitude = latitude.getEditText().getText().toString().trim();
        String driverLongitude = longitude.getEditText().getText().toString().trim();
        String driverPassword = password.getEditText().getText().toString().trim();

        rootnode = FirebaseDatabase.getInstance();
        reference = rootnode.getReference("drivers");

            // Implement Firebase registration logic
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getEditText().getText().toString(), password.getEditText().getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Registration success
                                saveUserData(driverName, email.getEditText().getText().toString(), driverLatitude, driverLongitude, driverPassword);
                                Toast.makeText(DriverRegistration.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(DriverRegistration.this, MainActivity.class));
                                finish();
                            } else {
                                // Registration failed
                                Toast.makeText(DriverRegistration.this, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

    private void saveUserData(String name, String email, String latitude, String longitude, String password) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("drivers");
        DriverModel driver = new DriverModel(name, email, latitude, longitude, password);
        usersRef.child(userId).setValue(driver);
        //driverRef.push().setValue(driver);
    }


    private Boolean validateName() {
        String val = name.getEditText().getText().toString();

        if (val.isEmpty()) {
            name.setError("Field cannot be Empty");
            return false;
        } else {
            name.setError(null);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = email.getEditText().getText().toString();
       // String emailPattern = "^[a-zA-Z]+\\.(\\d+)@srec\\.ac\\.in$";   //
        if (val.isEmpty()) {
            email.setError("Field cannot be empty");
            return false;
        } /*else if (!val.matches(emailPattern)) {
        email.setError("Invalid email address");
            return false;
        }*/
        else {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateLat() {
        String val = latitude.getEditText().getText().toString();
        if (val.isEmpty()) {
            latitude.setError("Field cannot be empty");
            return false;
        } else {
            latitude.setError(null);
            latitude.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatelong() {
        String val = longitude.getEditText().getText().toString();
        if (val.isEmpty()) {
            longitude.setError("Field cannot be empty");
            return false;
        } else {
            longitude.setError(null);
            longitude.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = password.getEditText().getText().toString();
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                //"(?=.*[a-zA-Z])" +      //any letter
                //"(?=.*[@#$%^&+=])" +    //at least 1 special character
             //   "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";
        if (val.isEmpty()) {
            password.setError("Field cannot be empty");
            return false;
        }/* else if (!val.matches(passwordVal)) {
            password.setError("Password is too weak");
            return false;
        }*/ else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }
}
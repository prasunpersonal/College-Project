package com.jisce.collegeproject.Activities;

import static com.jisce.collegeproject.App.CURRENT_BUSINESS;
import static com.jisce.collegeproject.App.ME;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jisce.collegeproject.Adapters.BusinessAdapter;
import com.jisce.collegeproject.Models.Business;
import com.jisce.collegeproject.R;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class BusinessActivity extends AppCompatActivity {
    RecyclerView allBusinesses;
    CardView addBusiness;
    Toolbar toolbar;
    ImageView profileImg;
    ArrayList<Business> businesses;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);

        toolbar = findViewById(R.id.profileToolbar);
        ((TextView)toolbar.findViewById(R.id.toolbarTitle)).setText(ME.getName());
        ((TextView)toolbar.findViewById(R.id.toolbarSubTitle)).setText(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        allBusinesses = findViewById(R.id.allBusinesses);
        addBusiness = findViewById(R.id.addBusiness);
        profileImg = findViewById(R.id.profileImg);
        businesses = new ArrayList<>();

        Glide.with(this).load(ME.getDp()).placeholder(R.drawable.ic_person).into(profileImg);
        profileImg.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        allBusinesses.setLayoutManager(new LinearLayoutManager(this));
        allBusinesses.setAdapter(new BusinessAdapter(businesses, business -> {
            CURRENT_BUSINESS = business;
            startActivity(new Intent(this, HomeActivity.class));
            this.finish();
        }));

        db.collection("Users").document(ME.getId()).collection("Businesses").addSnapshotListener((value, error) -> {
            if (error != null){
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null && allBusinesses.getAdapter() != null) {
                for (DocumentChange dc : value.getDocumentChanges()){
                    if (dc.getType() == DocumentChange.Type.ADDED){
                        businesses.add(dc.getDocument().toObject(Business.class));
                        allBusinesses.getAdapter().notifyItemInserted(businesses.size()-1);
                    }else if (dc.getType() == DocumentChange.Type.REMOVED){
                        int index = IntStream.range(0, businesses.size()).filter(i -> businesses.get(i).getId().equals(dc.getDocument().toObject(Business.class).getId())).findFirst().orElse(-1);
                        if (index != -1){
                            businesses.remove(index);
                            allBusinesses.getAdapter().notifyItemRemoved(index);
                        }
                    }else {
                        int index = IntStream.range(0, businesses.size()).filter(i -> businesses.get(i).getId().equals(dc.getDocument().toObject(Business.class).getId())).findFirst().orElse(-1);
                        if (index != -1){
                            businesses.set(index, dc.getDocument().toObject(Business.class));
                            allBusinesses.getAdapter().notifyItemChanged(index);
                        }
                    }
                }
            }
        });

        addBusiness.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setView(LayoutInflater.from(this).inflate(R.layout.register_business, null))
                    .setCancelable(false);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(ActivityCompat.getDrawable(this, R.drawable.dialog_bg));
            dialog.show();

            EditText businessName, ownerName, businessEmail, businessPhone, pinCode, building, area, city, state, country;
            Button register;
            ImageView close;

            businessName = dialog.findViewById(R.id.shopName);
            ownerName = dialog.findViewById(R.id.ownerName);
            businessEmail = dialog.findViewById(R.id.businessEmail);
            businessPhone = dialog.findViewById(R.id.businessPhone);
            pinCode = dialog.findViewById(R.id.address);
            building = dialog.findViewById(R.id.building);
            area = dialog.findViewById(R.id.area);
            city = dialog.findViewById(R.id.city);
            state = dialog.findViewById(R.id.state);
            country = dialog.findViewById(R.id.country);
            register = dialog.findViewById(R.id.register);
            close = dialog.findViewById(R.id.cancel);

            ownerName.setText(ME.getName());
            businessEmail.setText(ME.getEmail());
            businessPhone.setText(ME.getPhone());

            close.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            register.setOnClickListener(v1 -> {
                if (businessName.getText().toString().trim().isEmpty()){
                    businessName.setError("Business name can't be empty!");
                    return;
                }
                if (ownerName.getText().toString().trim().isEmpty()){
                    ownerName.setError("Owner's name can't be empty!");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(businessEmail.getText().toString().trim()).matches()){
                    businessEmail.setError("Please enter a valid email!");
                    return;
                }
                if (businessPhone.getText().toString().trim().isEmpty()){
                    businessPhone.setError("Business phone can't be empty!");
                    return;
                }
                if (pinCode.getText().toString().trim().isEmpty()){
                    pinCode.setError("Pincode can't be empty!");
                    return;
                }
                if (building.getText().toString().trim().isEmpty()){
                    building.setError("Flat, House no., Building name, Apartment name can't be empty!");
                    return;
                }
                if (area.getText().toString().trim().isEmpty()){
                    area.setError("Area, Colony, Street, Sector, Village can't be empty!");
                    return;
                }
                if (city.getText().toString().trim().isEmpty()){
                    city.setError("Town/City name can't be empty!");
                    return;
                }
                if (state.getText().toString().trim().isEmpty()){
                    state.setError("State name can't be empty!");
                    return;
                }
                if (country.getText().toString().trim().isEmpty()){
                    country.setError("Country name can't be empty!");
                    return;
                }

                String businessID = ME.getId() + "_" + businessName.getText().toString().trim().replace(' ', '_');
                Business business = new Business(businessID, businessName.getText().toString().trim(), ownerName.getText().toString().trim(), businessEmail.getText().toString().trim(),
                        businessPhone.getText().toString().trim(), pinCode.getText().toString().trim(), building.getText().toString().trim(), area.getText().toString().trim(),
                        city.getText().toString().trim(), state.getText().toString().trim(), country.getText().toString().trim());

                db.collection("Users").document(ME.getId()).collection("Businesses").document(businessID).set(business).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(this, "Successfully created your new business "+business.getName(), Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.dismiss();
            });
        });
    }
}
package com.jisce.collegeproject.Activities;

import static com.jisce.collegeproject.App.CURRENT_BUSINESS;
import static com.jisce.collegeproject.App.ME;
import static com.jisce.collegeproject.App.PROGRESS_NOTIFICATION_ID;
import static com.jisce.collegeproject.App.getFileSize;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jisce.collegeproject.Adapters.BusinessAdapter;
import com.jisce.collegeproject.Models.Business;
import com.jisce.collegeproject.Models.Product;
import com.jisce.collegeproject.Models.User;
import com.jisce.collegeproject.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_PHOTO = 501;
    TextView name, phone, email, dob, gender;
    ImageView dp;
    CardView addBusiness;
    RecyclerView allBusinesses;
    Toolbar toolbar;
    ArrayList<Business> businesses;
    FirebaseFirestore db;
    int dd, mm, yyyy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.normalToolbar);
        toolbar.setTitle(ME.getName());
        setSupportActionBar(toolbar);


        name = findViewById(R.id.profileName);
        phone = findViewById(R.id.profilePhone);
        email = findViewById(R.id.profileEmail);
        dob = findViewById(R.id.profileDob);
        gender = findViewById(R.id.profileGender);
        dp = findViewById(R.id.profileDp);
        addBusiness = findViewById(R.id.addBusiness);
        allBusinesses = findViewById(R.id.allBusinessesProfile);

        businesses = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        toolbar.setNavigationOnClickListener(v -> finish());

        db.collection("Users").document(ME.getId()).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null && value.exists()) {
                ME = value.toObject(User.class);
                assert ME != null;
                name.setText(ME.getName());
                phone.setText(ME.getPhone());
                email.setText(ME.getEmail());
                dob.setText(ME.getDob());
                gender.setText(ME.getGender());
                Glide.with(getApplicationContext()).load(ME.getDp()).placeholder(ContextCompat.getDrawable(this, R.drawable.ic_person)).into(dp);
            } else {
                Toast.makeText(this, "Something went wrong! Please log in again.", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        allBusinesses.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        allBusinesses.setAdapter(new BusinessAdapter(businesses, business -> {
            CURRENT_BUSINESS = business;
            startActivity(new Intent(this, HomeActivity.class));
            finishAffinity();
        }));
        db.collection("Users").document(ME.getId()).collection("Businesses").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null && allBusinesses.getAdapter() != null) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        businesses.add(dc.getDocument().toObject(Business.class));
                        allBusinesses.getAdapter().notifyItemInserted(businesses.size() - 1);
                    } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                        int index = IntStream.range(0, businesses.size()).filter(i -> businesses.get(i).getId().equals(dc.getDocument().toObject(Business.class).getId())).findFirst().orElse(-1);
                        if (index != -1) {
                            businesses.remove(index);
                            allBusinesses.getAdapter().notifyItemRemoved(index);
                        }
                    } else {
                        int index = IntStream.range(0, businesses.size()).filter(i -> businesses.get(i).getId().equals(dc.getDocument().toObject(Business.class).getId())).findFirst().orElse(-1);
                        if (index != -1) {
                            businesses.set(index, dc.getDocument().toObject(Business.class));
                            allBusinesses.getAdapter().notifyItemChanged(index);
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Something went wrong! Please log in again.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                FirebaseAuth.getInstance().signOut();
                finish();
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
                if (businessName.getText().toString().trim().isEmpty()) {
                    businessName.setError("Business name can't be empty!");
                    return;
                }
                if (ownerName.getText().toString().trim().isEmpty()) {
                    ownerName.setError("Owner's name can't be empty!");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(businessEmail.getText().toString().trim()).matches()) {
                    businessEmail.setError("Please enter a valid email!");
                    return;
                }
                if (businessPhone.getText().toString().trim().isEmpty()) {
                    businessPhone.setError("Business phone can't be empty!");
                    return;
                }
                if (pinCode.getText().toString().trim().isEmpty()) {
                    pinCode.setError("Pincode can't be empty!");
                    return;
                }
                if (building.getText().toString().trim().isEmpty()) {
                    building.setError("Flat, House no., Building name, Apartment name can't be empty!");
                    return;
                }
                if (area.getText().toString().trim().isEmpty()) {
                    area.setError("Area, Colony, Street, Sector, Village can't be empty!");
                    return;
                }
                if (city.getText().toString().trim().isEmpty()) {
                    city.setError("Town/City name can't be empty!");
                    return;
                }
                if (state.getText().toString().trim().isEmpty()) {
                    state.setError("State name can't be empty!");
                    return;
                }
                if (country.getText().toString().trim().isEmpty()) {
                    country.setError("Country name can't be empty!");
                    return;
                }

                String businessID = businessName.getText().toString().trim().replace(' ', '_');
                Business business = new Business(businessID, businessName.getText().toString().trim(), ownerName.getText().toString().trim(), businessEmail.getText().toString().trim(),
                        businessPhone.getText().toString().trim(), pinCode.getText().toString().trim(), building.getText().toString().trim(), area.getText().toString().trim(),
                        city.getText().toString().trim(), state.getText().toString().trim(), country.getText().toString().trim());

                db.collection("Users").document(ME.getId()).collection("Businesses").document(businessID).set(business).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Successfully created your new business " + business.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.dismiss();
            });
        });
        dp.setOnClickListener(v -> {
            BottomSheetDialog dialog = new BottomSheetDialog(v.getContext());
            dialog.setContentView(R.layout.img_options);

            TextView editImg, deleteImg, viewImg;
            editImg = dialog.findViewById(R.id.editImg);
            deleteImg = dialog.findViewById(R.id.deleteImg);
            viewImg = dialog.findViewById(R.id.viewImg);

            if (ME.getDp() == null){
                deleteImg.setVisibility(View.GONE);
                viewImg.setVisibility(View.GONE);
            }else {
                deleteImg.setVisibility(View.VISIBLE);
                viewImg.setVisibility(View.VISIBLE);
            }

            editImg.setOnClickListener(v1 -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                editImageResponse.launch(intent);
                dialog.dismiss();
            });

            viewImg.setOnClickListener(v1 -> {
                showImg(ME.getDp(), true, false);
                dialog.dismiss();
            });

            deleteImg.setOnClickListener(v1 -> {
                dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Delete profile photo?")
                        .setMessage("Are you sure about this process?")
                        .setPositiveButton("Delete", (dialog1, which) -> {
                            dialog1.dismiss();
                            db.collection("Users").document(ME.getId()).update("dp", null).addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Toast.makeText(this, "Profile photo deleted successfully.", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(this, "Can't delete profile photo: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss());
                AlertDialog deleteAlert = builder.create();
                deleteAlert.setOnShowListener(dialog13 -> {
                    deleteAlert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                });
                deleteAlert.show();
            });
            dialog.show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit_profile) {
            editProfile();
        } else if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        }
        return super.onOptionsItemSelected(item);
    }

    ActivityResultLauncher<Intent> editImageResponse = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            showImg(result.getData().getData().toString(), false, true);
        }
    });

    private void editProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(LayoutInflater.from(this).inflate(R.layout.activity_signup, null))
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(ActivityCompat.getDrawable(this, R.drawable.dialog_bg));
        dialog.show();

        EditText userName, userEmail, userPhone, userDOB;
        TextView signupTitle;
        RadioGroup userGender;
        Button create;
        ImageView datePicker, close;
        FirebaseFirestore db;

        userName = dialog.findViewById(R.id.userName);
        userEmail = dialog.findViewById(R.id.userEmail);
        userPhone = dialog.findViewById(R.id.userPhone);
        signupTitle = dialog.findViewById(R.id.signupTitle);
        userDOB = dialog.findViewById(R.id.userDOB);
        userGender = dialog.findViewById(R.id.userGender);
        create = dialog.findViewById(R.id.createBtn);
        datePicker = dialog.findViewById(R.id.datePicker);
        close = dialog.findViewById(R.id.cancel);

        db = FirebaseFirestore.getInstance();

        String[] dob = ME.getDob().split("-");
        dd = Integer.parseInt(dob[0]);
        mm = Integer.parseInt(dob[1])-1;
        yyyy = Integer.parseInt(dob[2]);

        userName.setText(ME.getName());
        userEmail.setText(ME.getEmail());
        userPhone.setText(ME.getPhone());
        userDOB.setText(ME.getDob());
        if ((ME.getGender().equals("Male"))) {
            ((RadioButton) userGender.findViewById(R.id.maleRG)).setChecked(true);
        } else if ((ME.getGender().equals("Female"))) {
            ((RadioButton) userGender.findViewById(R.id.femaleRG)).setChecked(true);
        } else {
            ((RadioButton) userGender.findViewById(R.id.othersRG)).setChecked(true);
        }
        create.setText("Update");
        signupTitle.setText("Update Profile");

        close.setOnClickListener(v2 -> dialog.dismiss());

        datePicker.setOnClickListener(view1 -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
                dd = day; mm = month; yyyy = year;
                userDOB.setText(String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year));
            }, yyyy, mm, dd);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        create.setOnClickListener(v2 -> {
            if (userName.getText().toString().trim().length() == 0) {
                userName.setError("Name can't be empty.");
                return;
            }
            if (userEmail.getText().toString().trim().length() == 0) {
                userEmail.setError("Email can't be empty.");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail.getText().toString().trim()).matches()) {
                userEmail.setError("Enter a valid email address.");
                return;
            }
            if (!Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$").matcher(userDOB.getText().toString().trim()).matches()) {
                userDOB.setError("Please enter a valid Date of Birth.");
                return;
            }
            if (userGender.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select your gender at first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!userName.getText().toString().trim().equals(ME.getName()) || !userEmail.getText().toString().trim().equals(ME.getEmail())
                    || !userPhone.getText().toString().trim().equals(ME.getPhone()) || !userDOB.getText().toString().trim().equals(ME.getDob())
                    || !((RadioButton) dialog.findViewById(userGender.getCheckedRadioButtonId())).getText().toString().equals(ME.getGender())) {

                User user = new User(ME.getId(), userName.getText().toString(), userEmail.getText().toString(), userPhone.getText().toString(), userDOB.getText().toString(), ((RadioButton) dialog.findViewById(userGender.getCheckedRadioButtonId())).getText().toString());
                db.collection("Users").document(user.getId()).set(user).addOnCompleteListener(task12 -> {
                    if (task12.isSuccessful()) {
                        Toast.makeText(this, "Successfully updated your profile.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, task12.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
        });
    }

    private void showImg(String uri, boolean cancelable, boolean editable){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(LayoutInflater.from(this).inflate(R.layout.show_image, null))
                .setCancelable(cancelable);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(ActivityCompat.getDrawable(this, R.drawable.dialog_bg));
        dialog.show();

        ImageView img;
        Button cancel, upload;
        LinearLayout editableArea;

        img = dialog.findViewById(R.id.showImg);
        cancel = dialog.findViewById(R.id.btnCancel);
        upload = dialog.findViewById(R.id.btnUpload);
        editableArea = dialog.findViewById(R.id.editableArea);

        if (editable){
            editableArea.setVisibility(View.VISIBLE);
        }else {
            editableArea.setVisibility(View.GONE);
        }

        Glide.with(getApplicationContext()).load(uri).placeholder(R.drawable.ic_person).into(img);
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        upload.setOnClickListener(v -> {
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, PROGRESS_NOTIFICATION_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Uploading image")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true);

            NotificationManagerCompat notificationManager;
            notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, notificationBuilder.build());

            StorageReference storageRef = FirebaseStorage.getInstance().getReference(getResources().getString(R.string.app_name)).child(ME.getId()).child("Profile").child("profile_image");
            storageRef.putFile(Uri.parse(uri)).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            storageRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                db.collection("Users").document(ME.getId()).update("dp", uri1.toString()).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()){
                                        Toast.makeText(this, "Profile photo updated successfully.", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(this, "Cant update profile photo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        }
                        notificationManager.cancel(1);
                    }).addOnProgressListener(snapshot -> {
                notificationBuilder.setContentText(getFileSize(snapshot.getBytesTransferred()) + "/" + getFileSize(snapshot.getTotalByteCount()))
                        .setProgress((int) snapshot.getTotalByteCount(), (int) snapshot.getBytesTransferred(), false);
                notificationManager.notify(1, notificationBuilder.build());
            });
            dialog.dismiss();
        });
    }
}
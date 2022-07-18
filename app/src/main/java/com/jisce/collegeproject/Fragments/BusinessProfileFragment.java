package com.jisce.collegeproject.Fragments;

import static com.jisce.collegeproject.App.CURRENT_BUSINESS;
import static com.jisce.collegeproject.App.ME;
import static com.jisce.collegeproject.App.PROGRESS_NOTIFICATION_ID;
import static com.jisce.collegeproject.App.getFileSize;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jisce.collegeproject.Activities.BusinessActivity;
import com.jisce.collegeproject.Models.Business;
import com.jisce.collegeproject.R;

import java.util.Locale;
import java.util.Objects;

public class BusinessProfileFragment extends Fragment {
    private static final int PICK_PHOTO = 501;
    TextView name, phone, email, owner, address;
    ImageView dp;
    FirebaseFirestore db;

    public BusinessProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        name = v.findViewById(R.id.bpfName);
        phone = v.findViewById(R.id.bpfPhone);
        email = v.findViewById(R.id.bpfEmail);
        owner = v.findViewById(R.id.bpfOwnerName);
        address = v.findViewById(R.id.bpfAddress);
        dp = v.findViewById(R.id.bpfDp);

        db = FirebaseFirestore.getInstance();

        db.collection("Users").document(ME.getId()).collection("Businesses").document(CURRENT_BUSINESS.getId()).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null && value.exists()) {
                CURRENT_BUSINESS = value.toObject(Business.class);

                name.setText(CURRENT_BUSINESS.getName());
                owner.setText(CURRENT_BUSINESS.getOwnerName());
                phone.setText(CURRENT_BUSINESS.getPhone());
                email.setText(CURRENT_BUSINESS.getEmail());
                address.setText(String.format(Locale.getDefault(), "%s, %s\n%s, %s, %s", CURRENT_BUSINESS.getBuilding(), CURRENT_BUSINESS.getArea(), CURRENT_BUSINESS.getCity(), CURRENT_BUSINESS.getState(), CURRENT_BUSINESS.getPinCode()));
                Glide.with(this).load(CURRENT_BUSINESS.getShopImg()).placeholder(ContextCompat.getDrawable(getContext(), R.drawable.ic_store)).into(dp);
            } else {
                Toast.makeText(getContext(), "Something went wrong! Please select business again.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), BusinessActivity.class));
                getActivity().finish();
            }
        });

        dp.setOnClickListener(v11 -> {
            BottomSheetDialog dialog = new BottomSheetDialog(v11.getContext());
            dialog.setContentView(R.layout.img_options);

            TextView editImg, deleteImg, viewImg;
            editImg = dialog.findViewById(R.id.editImg);
            deleteImg = dialog.findViewById(R.id.deleteImg);
            viewImg = dialog.findViewById(R.id.viewImg);

            if (CURRENT_BUSINESS.getShopImg() == null){
                deleteImg.setVisibility(View.GONE);
                viewImg.setVisibility(View.GONE);
            }else {
                deleteImg.setVisibility(View.VISIBLE);
                viewImg.setVisibility(View.VISIBLE);
            }

            editImg.setOnClickListener(v1 -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_PHOTO);
                dialog.dismiss();
            });

            viewImg.setOnClickListener(v1 -> {
                showImg(CURRENT_BUSINESS.getShopImg(), true, false);
                dialog.dismiss();
            });

            deleteImg.setOnClickListener(v1 -> {
                dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Delete business photo?")
                        .setMessage("Are you sure about this process?")
                        .setPositiveButton("Delete", (dialog1, which) -> {
                            dialog1.dismiss();
                            db.collection("Users").document(ME.getId()).collection("Businesses").document(CURRENT_BUSINESS.getId()).update("shopImg", null).addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Toast.makeText(getContext(), "Business photo deleted successfully.", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getContext(), "Can't delete business photo: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss());
                AlertDialog deleteAlert = builder.create();
                deleteAlert.setOnShowListener(dialog13 -> {
                    deleteAlert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                });
                deleteAlert.show();
            });
            dialog.show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO && resultCode == Activity.RESULT_OK && data != null){
            showImg(data.getData().toString(), false, true);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_business_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit_business_profile) {
            editBusinessProfile();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImg(String uri, boolean cancelable, boolean editable){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(LayoutInflater.from(getContext()).inflate(R.layout.show_image, null))
                .setCancelable(cancelable);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(ActivityCompat.getDrawable(getContext(), R.drawable.dialog_bg));
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

        Glide.with(getContext()).load(uri).placeholder(R.drawable.ic_person).into(img);
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        upload.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getContext(), PROGRESS_NOTIFICATION_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Uploading image")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true);

            NotificationManagerCompat notificationManager;
            notificationManager = NotificationManagerCompat.from(getContext());
            notificationManager.notify(1, notificationBuilder.build());

            StorageReference storageRef = FirebaseStorage.getInstance().getReference(getResources().getString(R.string.app_name)).child(ME.getId()).child("Businesses").child(CURRENT_BUSINESS.getId()).child("Business Profile").child("business_profile_image");
            storageRef.putFile(Uri.parse(uri)).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri1 -> db.collection("Users").document(ME.getId()).collection("Businesses").document(CURRENT_BUSINESS.getId()).update("shopImg", uri1.toString()).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()){
                            Toast.makeText(getContext(), "Business photo updated successfully.", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getContext(), "Cant update business photo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
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

    private void editBusinessProfile(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(LayoutInflater.from(getContext()).inflate(R.layout.register_business, null))
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(ActivityCompat.getDrawable(getContext(), R.drawable.dialog_bg));
        dialog.show();

        EditText businessName, ownerName, businessEmail, businessPhone, pinCode, building, area, city, state, country;
        Button register;
        ImageView close;
        TextView registerTitle;

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
        registerTitle = dialog.findViewById(R.id.registerTitle);

        businessName.setText(CURRENT_BUSINESS.getName());
        ownerName.setText(CURRENT_BUSINESS.getOwnerName());
        businessEmail.setText(CURRENT_BUSINESS.getEmail());
        businessPhone.setText(CURRENT_BUSINESS.getPhone());
        pinCode.setText(CURRENT_BUSINESS.getPinCode());
        building.setText(CURRENT_BUSINESS.getBuilding());
        area.setText(CURRENT_BUSINESS.getArea());
        city.setText(CURRENT_BUSINESS.getCity());
        state.setText(CURRENT_BUSINESS.getState());
        country.setText(CURRENT_BUSINESS.getCountry());
        register.setText("Update");
        registerTitle.setText("Update Business");

        close.setOnClickListener(v2 -> dialog.dismiss());

        register.setOnClickListener(v2 -> {
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

            if (!businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName()) || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName())
                    || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName()) || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName())
                    || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName()) || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName())
                    || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName()) || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName())
                    || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName()) || !businessName.getText().toString().trim().equals(CURRENT_BUSINESS.getName())) {

                Business business = new Business(CURRENT_BUSINESS.getId(), businessName.getText().toString().trim(), ownerName.getText().toString().trim(), businessEmail.getText().toString().trim(),
                        businessPhone.getText().toString().trim(), pinCode.getText().toString().trim(), building.getText().toString().trim(), area.getText().toString().trim(),
                        city.getText().toString().trim(), state.getText().toString().trim(), country.getText().toString().trim());

                FirebaseFirestore.getInstance().collection("Users").document(ME.getId()).collection("Businesses").document(CURRENT_BUSINESS.getId()).set(business).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Successfully updated your new business " + business.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
        });
    }
}
package com.jisce.collegeproject.Activities;

import static com.jisce.collegeproject.App.ME;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jisce.collegeproject.Models.User;
import com.jisce.collegeproject.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    AlertDialog alertDialog;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progress = findViewById(R.id.progress);

        checkAndRequestPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndRequestPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (requestCode == 100 && resultCode == RESULT_OK) {
                if (Environment.isExternalStorageManager()) {
                    loadData();
                }
            } else {
                showSettingsDialog();
            }
        }
    }

    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    ME = task.getResult().toObject(User.class);
                    startActivity(new Intent(this, BusinessActivity.class));
                } else {
                    Toast.makeText(this, "Something went wrong! Please try again.", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "loadData: ", task.getException());
                }
                finish();
            });
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void showSettingsDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_folder);
            builder.setTitle("Storage Permission");
            builder.setMessage("This app needs all files access permission. You can enable it in app settings.");
            builder.setCancelable(false);
            builder.setPositiveButton("Settings", (dialog, which) -> {
                dialog.cancel();
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityForResult(intent, 100);
                } catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, 100);
                }
            });
            builder.setNegativeButton("Exit", (dialog, which) -> {
                dialog.cancel();
                finish();
            });

            alertDialog = builder.create();
            alertDialog.setOnShowListener(dialog -> alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.grey)));
            alertDialog.show();
        }
    }

    private void checkAndRequestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (Environment.isExternalStorageManager()){
                loadData();
            } else {
                showSettingsDialog();
            }
        } else {
            Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    loadData();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                }
            }).onSameThread().check();
        }
    }
}
package com.jisce.collegeproject.Activities;

import static com.jisce.collegeproject.App.ME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;
import com.jisce.collegeproject.Models.User;
import com.jisce.collegeproject.R;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    int dd, mm, yyyy;
    private Button sendOtp, verify;
    private EditText phoneNo, otp;
    private ImageView editNumber;
    private LinearLayout verificationArea;
    private CountryCodePicker ccp;
    private ProgressDialog pd;
    private CountDownTimer timer;
    private FirebaseAuth auth;
    private String verificationId;
    private boolean codeSendOnce;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sendOtp = findViewById(R.id.sendOtp);
        verify = findViewById(R.id.verify);
        phoneNo = findViewById(R.id.phoneNo);
        editNumber = findViewById(R.id.editNumber);
        otp = findViewById(R.id.otp);
        verificationArea = findViewById(R.id.verificationArea);
        ccp = findViewById(R.id.ccp);

        auth = FirebaseAuth.getInstance();
        codeSendOnce = false;
        ME = null;

        sendOtp.setOnClickListener(v -> {
            if (phoneNo.getText().toString().trim().length() < 10 || phoneNo.getText().toString().contains(" ")) {
                phoneNo.setError("Enter a valid mobile number.");
                return;
            }

            phoneNo.setEnabled(false);
            sendOtp.setEnabled(false);

            if (codeSendOnce){
                resendOTP(ccp.getSelectedCountryCodeWithPlus() + phoneNo.getText().toString().trim(), resendToken);
            }else {
                sendOTP(ccp.getSelectedCountryCodeWithPlus() + phoneNo.getText().toString().trim());
                codeSendOnce = true;
            }
            verificationArea.setVisibility(View.VISIBLE);
            editNumber.setVisibility(View.GONE);
            startCountdown();
        });
        verify.setOnClickListener(v -> {
            if (otp.getText().toString().trim().length() < 6 || otp.getText().toString().contains(" ")) {
                otp.setError("Enter a valid otp");
                return;
            }
            verifyCode(otp.getText().toString());
        });
        editNumber.setOnClickListener(v -> {
            phoneNo.setEnabled(true);
            codeSendOnce = false;
            sendOtp.setEnabled(true);
            sendOtp.setText("Send OTP");
            verificationArea.setVisibility(View.GONE);
            editNumber.setVisibility(View.GONE);
        });
    }

    private void startCountdown(){
        timer = new CountDownTimer(120000, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                sendOtp.setText(String.format(Locale.getDefault(),"Resend OTP %d", (millisUntilFinished/1000)));
            }
            @Override
            public void onFinish() {
                sendOtp.setText("Resend OTP");
                sendOtp.setEnabled(true);
                editNumber.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void sendOTP(String phoneNo) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNo)
                        .setTimeout(120L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendOTP(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(120L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            resendToken = forceResendingToken;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                otp.setText(code);
                verify.setEnabled(false);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            timer.cancel();
            phoneNo.setEnabled(true);
            verify.setEnabled(true);
            verificationArea.setVisibility(View.GONE);
            codeSendOnce = false;
            sendOtp.setText("Send OTP");
            sendOtp.setEnabled(true);
        }
    };

    private void verifyCode(String smsCode) {
        pd = new ProgressDialog(this);
        pd.setMessage("Verifying...");
        pd.setCancelable(false);
        pd.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, smsCode);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(task1 -> {
                    pd.dismiss();
                    if (task1.isSuccessful() && task1.getResult() != null && task1.getResult().exists()){
                        ME = task1.getResult().toObject(User.class);
                        startActivity(new Intent(this, BusinessActivity.class));
                    }else {
                        createProfile();
                    }
                    finish();
                });
            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                verify.setEnabled(true);
                pd.dismiss();
                otp.setText("");
                Log.w("TAG", "signInWithCredential:failure", task.getException());
            }
        });
    }

    private void createProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(LayoutInflater.from(this).inflate(R.layout.activity_signup, null))
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(ActivityCompat.getDrawable(this, R.drawable.dialog_bg));
        dialog.show();

        EditText userName, userEmail, userPhone, userDOB;
        RadioGroup userGender;
        Button create;
        ImageView datePicker, close;
        FirebaseFirestore db;
        ProgressDialog progressDialog;

        userName = dialog.findViewById(R.id.userName);
        userEmail = dialog.findViewById(R.id.userEmail);
        userPhone = dialog.findViewById(R.id.userPhone);
        userDOB = dialog.findViewById(R.id.userDOB);
        userGender = dialog.findViewById(R.id.userGender);
        create = dialog.findViewById(R.id.createBtn);
        datePicker = dialog.findViewById(R.id.datePicker);
        close = dialog.findViewById(R.id.cancel);

        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("Please wait while we create your account.");
        progressDialog.setCancelable(false);

        dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        mm = Calendar.getInstance().get(Calendar.MONTH);
        yyyy = Calendar.getInstance().get(Calendar.YEAR);

        close.setOnClickListener(v2 -> {
            dialog.dismiss();
            FirebaseAuth.getInstance().signOut();
            finish();
        });

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

            progressDialog.show();

            User user = new User(Objects.requireNonNull(auth.getCurrentUser()).getUid(), userName.getText().toString(), userEmail.getText().toString(), userPhone.getText().toString(), userDOB.getText().toString(), ((RadioButton) findViewById(userGender.getCheckedRadioButtonId())).getText().toString());
            db.collection("Users").document(user.getId()).set(user).addOnCompleteListener(task12 -> {
                if (task12.isSuccessful()) {
                    ME = user;
                    startActivity(new Intent(this, BusinessActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, task12.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            });
        });
        dialog.dismiss();
    }
}
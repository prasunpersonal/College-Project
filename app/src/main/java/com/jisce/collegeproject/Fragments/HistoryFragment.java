package com.jisce.collegeproject.Fragments;

import static com.jisce.collegeproject.Activities.HomeActivity.ALL_INVOICES;
import static com.jisce.collegeproject.Activities.HomeActivity.allInvoicesLoaded;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.jisce.collegeproject.Activities.PDFViewActivity;
import com.jisce.collegeproject.Adapters.InvoiceAdapter;
import com.jisce.collegeproject.Models.Invoice;
import com.jisce.collegeproject.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressLint("NotifyDataSetChanged")
public class HistoryFragment extends Fragment {
    RecyclerView allInvoices;
    EditText srcTxt;
    TextView srcCount;
    ImageView clear;
    SwipeRefreshLayout refresh;
    public static ArrayList<Invoice> invoices;
    FirebaseFirestore db;

    public HistoryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        invoices = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        allInvoices = v.findViewById(R.id.allInvoices);
        srcTxt = v.findViewById(R.id.searchText);
        clear = v.findViewById(R.id.searchClear);
        srcCount = v.findViewById(R.id.srcCount);
        refresh = v.findViewById(R.id.swipeRefresh);
        clear.setOnClickListener(v1 -> srcTxt.setText(null));

        allInvoices.setLayoutManager(new LinearLayoutManager(getContext()));
        allInvoices.setAdapter(new InvoiceAdapter(invoices, invoice -> startActivity(new Intent(getContext(), PDFViewActivity.class).putExtra("PDF_VIEW_TYPE", PDFViewActivity.FROM_URL).putExtra("PDF_PATH", invoice.getUrl()).putExtra("PDF_NAME", invoice.getName()))));

        invoices.addAll(ALL_INVOICES);
        Objects.requireNonNull(allInvoices.getAdapter()).notifyDataSetChanged();

        refresh.setOnRefreshListener(() -> {
            invoices.clear();
            if (srcTxt.getText().toString().trim().isEmpty()) {
                invoices.addAll(ALL_INVOICES);
            } else {
                invoices.addAll(ALL_INVOICES.stream().filter(invoice -> invoice.getCustomerPhone().toLowerCase(Locale.ROOT).contains(srcTxt.getText().toString().trim().toLowerCase(Locale.ROOT))).collect(Collectors.toCollection(ArrayList::new)));
                if (invoices.size() > 1) {
                    srcCount.setText(String.format(Locale.getDefault(), "%d invoices found", invoices.size()));
                } else {
                    srcCount.setText(String.format(Locale.getDefault(), "%d invoice found", invoices.size()));
                }
            }
            if (allInvoices.getAdapter() != null) {
                allInvoices.getAdapter().notifyDataSetChanged();
            }
            refresh.setRefreshing(false);
        });

        srcTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                invoices.clear();
                if (s.length() > 0) {
                    clear.setVisibility(View.VISIBLE);
                    invoices.addAll(ALL_INVOICES.stream().filter(invoice -> invoice.getCustomerPhone().toLowerCase(Locale.ROOT).contains(s.toString().toLowerCase(Locale.ROOT))).collect(Collectors.toCollection(ArrayList::new)));
                    srcCount.setVisibility(View.VISIBLE);
                    if (invoices.size() > 1) {
                        srcCount.setText(String.format(Locale.getDefault(), "%d invoices found", invoices.size()));
                    } else {
                        srcCount.setText(String.format(Locale.getDefault(), "%d invoice found", invoices.size()));
                    }
                } else {
                    clear.setVisibility(View.GONE);
                    srcCount.setVisibility(View.GONE);
                    invoices.addAll(ALL_INVOICES);
                }
                if (allInvoices.getAdapter() != null){
                    allInvoices.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TAG", "onResume: ");
        if (allInvoices.getAdapter() != null) {
            invoices.clear();
            invoices.addAll(ALL_INVOICES);
            allInvoices.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_history, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.scanner){
            Dexter.withContext(getContext()).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                    scanQrCode();
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    showSettingsDialog();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                }
            }).onSameThread().check();
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanQrCode(){
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(R.layout.scanner_dialog);

        CodeScannerView scannerView;
        scannerView = dialog.findViewById(R.id.scannerView);
        assert scannerView != null;
        CodeScanner scanner;
        scanner = new CodeScanner(requireContext(), scannerView);
        scanner.setCamera(CodeScanner.CAMERA_BACK);
        scanner.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));
        scanner.setAutoFocusMode(AutoFocusMode.SAFE);
        scanner.setScanMode(ScanMode.SINGLE);
        scanner.setAutoFocusEnabled(true);
        scanner.setFlashEnabled(false);
        scanner.setDecodeCallback(result -> requireActivity().runOnUiThread(()-> {
            Invoice invoice = ALL_INVOICES.stream().filter(invoice1 -> invoice1.getName().equals(result.getText())).findFirst().orElse(null);
            if (invoice == null){
                Toast.makeText(getContext(), "This invoice can not be found!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(getContext(), PDFViewActivity.class).putExtra("PDF_VIEW_TYPE", PDFViewActivity.FROM_URL).putExtra("PDF_PATH", invoice.getUrl()).putExtra("PDF_NAME", invoice.getName()));
            }
            dialog.dismiss();
        }));
        scanner.setErrorCallback(error -> Log.e("TAG", "Scanner Error: ", error));

        scanner.startPreview();
        dialog.show();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setIcon(R.drawable.ic_camera);
        builder.setTitle("Camera Permissions");
        builder.setMessage("This app needs permission to use camera feature. You can grant this in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog13 -> {
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        });
        alertDialog.show();
    }
}
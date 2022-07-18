package com.jisce.collegeproject.Fragments;

import static com.jisce.collegeproject.App.CURRENT_BUSINESS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jisce.collegeproject.Activities.CustomerDetails;
import com.jisce.collegeproject.Adapters.ItemAdapter;
import com.jisce.collegeproject.Models.Product;
import com.jisce.collegeproject.Models.SelectionArgs;
import com.jisce.collegeproject.R;
import com.otaliastudios.nestedscrollcoordinatorlayout.NestedScrollCoordinatorLayout;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressLint("NotifyDataSetChanged")
public class HomeFragment extends Fragment {
    private static final int PICK_XLSX = 502;
    EditText srcTxt;
    ImageView clear;
    TextView srcCount;
    RecyclerView allProducts;
    public ArrayList<Product> ALL_PRODUCTS;
    public static ArrayList<Product> products;
    public static Map<Product, SelectionArgs> selected;
    ProgressDialog dialog;

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        selected = new HashMap<>();
        products = new ArrayList<>();
        ALL_PRODUCTS = new ArrayList<>();

        srcTxt = v.findViewById(R.id.searchText);
        clear = v.findViewById(R.id.searchClear);
        srcCount = v.findViewById(R.id.srcCount);

        allProducts = v.findViewById(R.id.allProducts);
        allProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        allProducts.setAdapter(new ItemAdapter(products, selected, () -> {

        }));

        clear.setOnClickListener(v1 -> srcTxt.setText(null));

        srcTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                products.clear();
                if (s.length() > 0) {
                    clear.setVisibility(View.VISIBLE);
                    products.addAll(ALL_PRODUCTS.stream().filter(product -> product.getName().toLowerCase(Locale.ROOT).contains(s.toString().toLowerCase(Locale.ROOT))).collect(Collectors.toCollection(ArrayList::new)));
                    srcCount.setVisibility(View.VISIBLE);
                    if (products.size() > 1) {
                        srcCount.setText(String.format(Locale.getDefault(), "%d products found", products.size()));
                    } else {
                        srcCount.setText(String.format(Locale.getDefault(), "%d product found", products.size()));
                    }
                } else {
                    clear.setVisibility(View.GONE);
                    srcCount.setVisibility(View.GONE);
                    products.addAll(ALL_PRODUCTS);
                }
                if (allProducts.getAdapter() != null){
                    allProducts.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        File file = new File(getContext().getDataDir(), CURRENT_BUSINESS.getId()+".xlsx");
        if (file.exists()) {
            ProductLoader loader = new ProductLoader();
            loader.execute(file);
        } else {
            Toast.makeText(getContext(), "No files found containing products!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (allProducts.getAdapter() != null){
            allProducts.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addFile) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            startActivityForResult(intent, PICK_XLSX);
        } if (item.getItemId() == R.id.done){
            if (selected.isEmpty()){
                Toast.makeText(getContext(), "Please select items first!", Toast.LENGTH_SHORT).show();
            }else {
                startActivity(new Intent(getActivity(), CustomerDetails.class));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_XLSX && resultCode == Activity.RESULT_OK && data != null){
            uploadFile(data.getData());
        }
    }

    private void uploadFile(Uri uri){
        try {
            FileUtils.copyInputStreamToFile(getContext().getContentResolver().openInputStream(uri), new File(getContext().getDataDir(), CURRENT_BUSINESS.getId() + ".xlsx"));
            ProductLoader loader = new ProductLoader();
            loader.execute(new File(getContext().getDataDir(), CURRENT_BUSINESS.getId() + ".xlsx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ProductLoader extends AsyncTask<File, Integer, Void> {
        public ProductLoader() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ALL_PRODUCTS.clear();
            products.clear();
            dialog = new ProgressDialog(getContext());
            dialog.setTitle("Loading Products...");
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.show();
        }

        @Override
        protected Void doInBackground(File... files) {
            File file = files[0];
            try {
                XSSFWorkbook workbook = new XSSFWorkbook(file);
                XSSFSheet sheet = workbook.getSheetAt(0);
                Log.d("TAG", "doInBackground: "+sheet.getPhysicalNumberOfRows());
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    if (sheet.getRow(i) != null && sheet.getRow(i).getCell(0).getRawValue() != null && sheet.getRow(i).getCell(1).getRawValue() != null && sheet.getRow(i).getCell(2).getRawValue() != null) {
                        Product product = new Product(sheet.getRow(i).getCell(0).getStringCellValue(), Integer.parseInt(sheet.getRow(i).getCell(1).getRawValue()), Double.parseDouble(sheet.getRow(i).getCell(2).getRawValue()), (sheet.getRow(i).getCell(3) == null) ? null : sheet.getRow(i).getCell(3).getStringCellValue());
                        ALL_PRODUCTS.add(product);
                    }
                    publishProgress(i*100/sheet.getPhysicalNumberOfRows());
                }
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void unused) {
            ALL_PRODUCTS.sort(Comparator.comparing(Product::getName));
            dialog.dismiss();
            if (allProducts.getAdapter() != null){
                products.addAll(ALL_PRODUCTS);
                allProducts.getAdapter().notifyDataSetChanged();
            }
        }
    }
}
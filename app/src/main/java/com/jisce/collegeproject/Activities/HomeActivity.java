package com.jisce.collegeproject.Activities;

import static com.jisce.collegeproject.App.CURRENT_BUSINESS;
import static com.jisce.collegeproject.App.ME;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jisce.collegeproject.Adapters.FragmentAdapter;
import com.jisce.collegeproject.Fragments.BusinessProfileFragment;
import com.jisce.collegeproject.Fragments.HistoryFragment;
import com.jisce.collegeproject.Fragments.HomeFragment;
import com.jisce.collegeproject.Models.Customer;
import com.jisce.collegeproject.Models.Invoice;
import com.jisce.collegeproject.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    public static ArrayList<Invoice> ALL_INVOICES;
    public static Set<Customer> customers;
    public static boolean allInvoicesLoaded;
    ArrayList<Fragment> fragments;
    ArrayList<String> fragmentNames;
    ArrayList<Integer> icons;
    ImageView profileImg;
    ViewPager2 mainPager;
    TabLayout mainTabs;
    Toolbar toolbar;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.profileToolbar);
        ((TextView)toolbar.findViewById(R.id.toolbarTitle)).setText(CURRENT_BUSINESS.getName());
        ((TextView)toolbar.findViewById(R.id.toolbarSubTitle)).setText(ME.getName());
        setSupportActionBar(toolbar);

        mainPager = findViewById(R.id.mainPager);
        mainTabs = findViewById(R.id.mainTabs);
        profileImg = findViewById(R.id.profileImg);

        db = FirebaseFirestore.getInstance();
        ALL_INVOICES = new ArrayList<>();
        customers = new HashSet<>();
        allInvoicesLoaded = false;

        db.collection("Users").document(ME.getId()).collection("Businesses")
                .document(CURRENT_BUSINESS.getId()).collection("Invoices")
                    .addSnapshotListener((value, error) -> {
                        if (error != null){
                            Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (value != null) {
                            for (DocumentChange dc : value.getDocumentChanges()){
                                Invoice invoice = dc.getDocument().toObject(Invoice.class);
                                Customer customer = new Customer(invoice.getCustomerPhone(), invoice.getCustomerName(), invoice.getCustomerEmail(), invoice.getCustomerAddress(), invoice.getCustomerPin());
                                if (dc.getType() == DocumentChange.Type.ADDED){
                                    ALL_INVOICES.add(invoice);
                                    customers.add(customer);
                                }else if (dc.getType() == DocumentChange.Type.REMOVED){
                                    ALL_INVOICES.remove(invoice);
                                    customers.remove(customer);
                                }else {
                                    customers.add(customer);
                                    int index = ALL_INVOICES.indexOf(invoice);
                                    if (index != -1){
                                        ALL_INVOICES.set(index, invoice);
                                    }
                                }
                                Log.d("TAG", "onCreate: "+invoice.getName());
                            }
                            allInvoicesLoaded = true;
                        }
                    });

        Glide.with(this).load(ME.getDp()).placeholder(R.drawable.ic_person).into(profileImg);
        profileImg.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new HistoryFragment());
        fragments.add(new BusinessProfileFragment());

        fragmentNames = new ArrayList<>();
        fragmentNames.add("Home");
        fragmentNames.add("History");
        fragmentNames.add("Business");

        icons = new ArrayList<>();
        icons.add(R.drawable.ic_home);
        icons.add(R.drawable.ic_history);
        icons.add(R.drawable.ic_store);

        mainPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragments));

        mainPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mainTabs.selectTab(mainTabs.getTabAt(position));
                super.onPageSelected(position);
            }
        });

        mainTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mainPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
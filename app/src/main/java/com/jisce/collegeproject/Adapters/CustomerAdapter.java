package com.jisce.collegeproject.Adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jisce.collegeproject.Models.Customer;
import com.jisce.collegeproject.Models.Invoice;
import com.jisce.collegeproject.R;

import java.util.ArrayList;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.MyCustomerViewHolder> {
    private Context context;
    private ArrayList<Customer> customers;
    private final setOnClickCustomerListener setOnClickCustomerListener;

    public CustomerAdapter(ArrayList<Customer> customers, setOnClickCustomerListener setOnClickCustomerListener) {
        this.customers = customers;
        this.setOnClickCustomerListener = setOnClickCustomerListener;
    }

    @NonNull
    @Override
    public MyCustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer, parent, false);
        return new MyCustomerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCustomerViewHolder holder, int position) {
        holder.name.setText(customers.get(position).getCustomerName());
        holder.phone.setText(customers.get(position).getCustomerPhone());
        holder.email.setText(customers.get(position).getCustomerEmail());
        holder.address.setText(customers.get(position).getCustomerAddress());
        holder.pin.setText(customers.get(position).getCustomerPin());
        holder.itemView.setOnClickListener(v -> {
            setOnClickCustomerListener.OnClickInvoiceListener(customers.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    public interface setOnClickCustomerListener {
        void OnClickInvoiceListener(Customer customer);
    }

    public class MyCustomerViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone, email, address, pin;;

        public MyCustomerViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.cLayoutName);
            phone = v.findViewById(R.id.cLayoutPhone);
            email = v.findViewById(R.id.cLayoutEmail);
            address = v.findViewById(R.id.cLayoutAddress);
            pin = v.findViewById(R.id.cLayoutPin);
        }
    }
}

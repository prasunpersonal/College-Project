package com.jisce.collegeproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jisce.collegeproject.Models.Business;
import com.jisce.collegeproject.R;

import java.util.ArrayList;
import java.util.Locale;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.MyBusinessViewHolder> {
    private Context context;
    private ArrayList<Business> businesses;
    private setOnSelectBusinessListener setOnSelectBusinessListener;

    public BusinessAdapter(ArrayList<Business> businesses, setOnSelectBusinessListener setOnSelectBusinessListener) {
        this.businesses = businesses;
        this.setOnSelectBusinessListener = setOnSelectBusinessListener;
    }

    @NonNull
    @Override
    public MyBusinessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.business, parent, false);
        return new MyBusinessViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBusinessViewHolder holder, int position) {
        Glide.with(context).load(businesses.get(position).getShopImg()).placeholder(ContextCompat.getDrawable(context, R.drawable.ic_store)).into(holder.img);
        holder.name.setText(businesses.get(position).getName());
        holder.building.setText(businesses.get(position).getBuilding());
        holder.area.setText(businesses.get(position).getArea());
        holder.city.setText(String.format(Locale.getDefault(), "%s, %s, %s", businesses.get(position).getCity(), businesses.get(position).getState(), businesses.get(position).getPinCode()));

        holder.itemView.setOnClickListener(v -> {
            setOnSelectBusinessListener.OnSelectBusinessListener(businesses.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return businesses.size();
    }

    public interface setOnSelectBusinessListener {
        void OnSelectBusinessListener(Business business);
    }

    public class MyBusinessViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, building, area, city;

        public MyBusinessViewHolder(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.businessImg);
            name = v.findViewById(R.id.businessName);
            building = v.findViewById(R.id.businessAddressBuilding);
            area = v.findViewById(R.id.businessAddressArea);
            city = v.findViewById(R.id.businessAddressCity);
        }
    }
}

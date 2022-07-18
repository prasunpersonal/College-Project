package com.jisce.collegeproject.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jisce.collegeproject.Models.Product;
import com.jisce.collegeproject.Models.SelectionArgs;
import com.jisce.collegeproject.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyItemViewHolder> implements Filterable {
    private Context context;
    private final ArrayList<Product> products;
    private final Map<Product, SelectionArgs> selected;
    private setOnSelectionChangeListener listener;

    public ItemAdapter(ArrayList<Product> products, Map<Product, SelectionArgs> selected, setOnSelectionChangeListener listener) {
        this.products = products;
        this.selected = selected;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MyItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyItemViewHolder holder, int position) {
        Product product = products.get(position);

        holder.title.setText(product.getName());
        Glide.with(context).load(product.getImg()).placeholder(R.drawable.ic_image).into(holder.img);
        holder.price.setText(String.format(Locale.getDefault(), "%.2f", product.getPrice()));
        holder.itemUnit.setText(String.format(Locale.getDefault(), "/%s", new ArrayList<>(product.getUnitMap().keySet()).get(0)));

        if (selected.containsKey(product)){
            holder.selectionDetails.setVisibility(View.VISIBLE);
            holder.quantity.setText(String.format(Locale.getDefault(), "%s", (product.getType() == Product.SINGLE) ? (int) selected.get(product).getQuantity() : selected.get(product).getQuantity()));
            holder.selectionUnit.setText(selected.get(product).getUnit());
            holder.total.setText(String.format(Locale.getDefault(), "%.2f", product.getPrice() * selected.get(product).getQuantity() * product.getUnitMap().get(selected.get(product).getUnit())));
            holder.select.setChecked(true);
            holder.itemCard.setStrokeWidth(5);
        } else {
            holder.selectionDetails.setVisibility(View.GONE);
            holder.quantity.setText(null);
            holder.selectionUnit.setText(null);
            holder.total.setText(null);
            holder.select.setChecked(false);
            holder.itemCard.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selected.containsKey(product)){
                selected.remove(product);
                notifyItemChanged(position);
                listener.OnSelectionChangeListener();
            } else {
                showSelectionDialog(product, position);
            }
        });
    }

    public interface setOnSelectionChangeListener{
        void OnSelectionChangeListener();
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }


    public static class MyItemViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title, price, total, quantity, itemUnit, selectionUnit;
        CheckBox select;
        LinearLayout selectionDetails;
        MaterialCardView itemCard;

        public MyItemViewHolder(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.productImg);
            title = v.findViewById(R.id.productName);
            price = v.findViewById(R.id.pricePerItem);
            itemUnit = v.findViewById(R.id.itemUnit);
            selectionUnit = v.findViewById(R.id.selectionUnit);
            select = v.findViewById(R.id.select);
            selectionDetails = v.findViewById(R.id.selectionDetails);
            total = v.findViewById(R.id.selectionTotal);
            quantity = v.findViewById(R.id.selectionQuantity);
            itemCard = v.findViewById(R.id.itemCard);
        }
    }

    private void showSelectionDialog(Product product, int position){
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.item_selection_dialog);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ImageView img;
        TextView title, price, total;
        EditText quantity;
        Spinner unit;
        Button select;

        img = dialog.findViewById(R.id.productImg);
        title = dialog.findViewById(R.id.productName);
        price = dialog.findViewById(R.id.pricePerItem);
        total = dialog.findViewById(R.id.priceTotal);
        quantity = dialog.findViewById(R.id.quantity);
        select = dialog.findViewById(R.id.selectBtn);
        unit = dialog.findViewById(R.id.unit);

        assert img != null; assert title != null; assert price != null; assert total != null; assert quantity != null; assert select != null; assert unit != null;

        title.setText(product.getName());
        Glide.with(context).load(product.getImg()).placeholder(R.drawable.ic_image).into(img);
        price.setText(String.format(Locale.getDefault(), "%.2f / %s", product.getPrice(), new ArrayList<>(product.getUnitMap().keySet()).get(0)));

        String allowCharacterSet;
        InputFilter filter;
        if (product.getType() == Product.GRAIN_CROPS || product.getType() == Product.LIQUID){
            allowCharacterSet = "0123456789.";
            filter = (source, start, end, dest, dStart, dEnd) -> {
                if (source != null && !allowCharacterSet.contains(source)) {
                    return "";
                }
                return null;
            };
        } else {
            allowCharacterSet = "0123456789";
            filter = (source, start, end, dest, dStart, dEnd) -> {
                if (source != null && !allowCharacterSet.contains(source)) {
                    return "";
                }
                return null;
            };
        }
        quantity.setFilters(new InputFilter[] {filter});

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, new ArrayList<>(product.getUnitMap().keySet()));
        unit.setAdapter(unitAdapter);

        quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    total.setText(String.format(Locale.getDefault(), "%.2f", product.getPrice() * Double.parseDouble(s.toString().trim()) * product.getUnitMap().get(unit.getSelectedItem().toString())));
                    select.setEnabled(true);
                }catch (Exception e) {
                    if (!quantity.getText().toString().trim().isEmpty()) {
                        quantity.setError("Please enter a valid quantity!");
                    }
                    total.setText(null);
                    select.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!quantity.getText().toString().trim().isEmpty()) {
                    total.setText(String.format(Locale.getDefault(), "%.2f", product.getPrice() * Double.parseDouble(quantity.getText().toString().trim()) * product.getUnitMap().get(unit.getSelectedItem().toString())));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        select.setOnClickListener(v -> {
            selected.put(product, new SelectionArgs(Double.parseDouble(quantity.getText().toString().trim()), unit.getSelectedItem().toString()));
            notifyItemChanged(position);
            listener.OnSelectionChangeListener();
            dialog.dismiss();
        });

        quantity.requestFocus();
        dialog.show();
    }
}

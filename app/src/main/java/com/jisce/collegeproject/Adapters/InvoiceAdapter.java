package com.jisce.collegeproject.Adapters;

import static com.jisce.collegeproject.App.CURRENT_BUSINESS;
import static com.jisce.collegeproject.App.ME;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jisce.collegeproject.Models.Invoice;
import com.jisce.collegeproject.R;

import java.io.File;
import java.util.ArrayList;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.MyInvoicesViewHolder> {
    private Context context;
    private ArrayList<Invoice> invoices;
    private final setOnClickInvoiceListener setOnClickInvoiceListener;

    public InvoiceAdapter(ArrayList<Invoice> invoices, setOnClickInvoiceListener setOnClickInvoiceListener) {
        this.invoices = invoices;
        this.setOnClickInvoiceListener = setOnClickInvoiceListener;
    }

    @NonNull
    @Override
    public MyInvoicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.invoice, parent, false);
        return new MyInvoicesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyInvoicesViewHolder holder, int position) {
        holder.name.setText(invoices.get(position).getName());
        holder.date.setText(invoices.get(position).getDate());
        holder.customerName.setText(invoices.get(position).getCustomerName());
        holder.customerPhone.setText(invoices.get(position).getCustomerPhone());

//        if (file.exists()) {
//            holder.downloadCardView.setVisibility(View.GONE);
//        }else {
//            holder.downloadCardView.setVisibility(View.VISIBLE);
//            if (holder.downloading){
//                holder.downloadProgress.setVisibility(View.VISIBLE);
//                holder.download.setVisibility(View.GONE);
//            }else {
//                holder.downloadProgress.setVisibility(View.GONE);
//                holder.download.setVisibility(View.VISIBLE);
//            }
//        }



        holder.itemView.setOnClickListener(v -> setOnClickInvoiceListener.OnClickInvoiceListener(invoices.get(position)));
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    public interface setOnClickInvoiceListener {
        void OnClickInvoiceListener(Invoice invoice);
    }

    public class MyInvoicesViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, customerName, customerPhone;
        ImageView pdfImg;

        public MyInvoicesViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.invoiceName);
            date = v.findViewById(R.id.invoiceDate);
            pdfImg = v.findViewById(R.id.pdfImg);
            customerName = v.findViewById(R.id.customerName);
            customerPhone = v.findViewById(R.id.customerPhone);
        }
    }
}

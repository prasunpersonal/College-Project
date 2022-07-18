package com.jisce.collegeproject.Activities;

import static com.jisce.collegeproject.App.CURRENT_BUSINESS;
import static com.jisce.collegeproject.App.ME;
import static com.jisce.collegeproject.Activities.HomeActivity.ALL_INVOICES;
import static com.jisce.collegeproject.Activities.HomeActivity.customers;
import static com.jisce.collegeproject.Fragments.HomeFragment.selected;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.jisce.collegeproject.Adapters.CustomerAdapter;
import com.jisce.collegeproject.Models.Customer;
import com.jisce.collegeproject.Models.Invoice;
import com.jisce.collegeproject.Models.Product;
import com.jisce.collegeproject.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class CustomerDetails extends AppCompatActivity {
    EditText name, phone, email, pin, address;
    FirebaseFirestore db;
    String invoiceName;
    Toolbar toolbar;
    RecyclerView allCustomers;
    ArrayList<Customer> customersList;
    double total = 0;
    int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        toolbar = findViewById(R.id.normalToolbar);
        toolbar.setTitle("Customer Details");
        setSupportActionBar(toolbar);

        name = findViewById(R.id.cName);
        phone = findViewById(R.id.cPhone);
        email = findViewById(R.id.cEmail);
        address = findViewById(R.id.cAddress);
        pin = findViewById(R.id.cPinCode);
        allCustomers = findViewById(R.id.customers);

        customersList = new ArrayList<>();
        allCustomers.setLayoutManager(new LinearLayoutManager(this));
        allCustomers.setAdapter(new CustomerAdapter(customersList, this::updateInputs));

        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        invoiceName = String.format(Locale.getDefault(), "Invoice_%06d.pdf", (ALL_INVOICES.size()+1));

        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                customersList.clear();
                if (!s.toString().trim().isEmpty()){
                    customersList.addAll(customers.stream().filter(customer -> customer.getCustomerPhone().contains(s)).collect(Collectors.toCollection(ArrayList::new)));
                }
                allCustomers.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }else if (item.getItemId() == R.id.done) {
            if (phone.getText().toString().trim().isEmpty()){
                phone.setError("A valid phone number is required!");
            }else if (name.getText().toString().trim().isEmpty()){
                name.setError("Customer name can't be empty!");
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()){
                email.setError("A valid email address of customer is required!");
            }else if (address.getText().toString().trim().isEmpty()){
                address.setError("Customer address can't be empty!");
            }else if (pin.getText().toString().trim().isEmpty()){
                pin.setError("Customer pin code can't be empty!");
            }else {
                try {
                    uploadPDF(createPdf());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private File createPdf() throws FileNotFoundException {
        File file = new File(getDataDir(), "temp_invoice.pdf");
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);
        pdfDocument.setDefaultPageSize(PageSize.A4);
        document.setMargins(50,50,50,50);

        Point point = new Point();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(point);
        Log.d("TAG", "createPdf: "+point.x+"-"+point.y+"-"+Math.min(point.x, point.y) * 3/4);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        QRGEncoder encoder = new QRGEncoder(invoiceName, null, QRGContents.Type.TEXT, Math.min(point.x, point.y) * 3/4);
        encoder.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image qrCodeImg = new Image(ImageDataFactory.create(stream.toByteArray())).setHeight(80f).setWidth(80f);

        // Adding Business Details & QR Code
        float[] headerTableSize = {395f, 100f};
        Table headerTable = new Table(headerTableSize);
        headerTable.setBorder(new SolidBorder(1));
        headerTable.addCell(new Cell().add(new Paragraph(CURRENT_BUSINESS.getName()).setFontSize(30).setBold().setPaddingLeft(10f)).setBorder(Border.NO_BORDER));
        headerTable.addCell(new Cell(3, 1).add(qrCodeImg.setAutoScale(true)));
        headerTable.addCell(new Cell().add(new Paragraph(CURRENT_BUSINESS.getBuilding() + ", " + CURRENT_BUSINESS.getArea()).setFontSize(14).setPaddingLeft(10f)).setBorder(Border.NO_BORDER));
        headerTable.addCell(new Cell().add(new Paragraph(CURRENT_BUSINESS.getCity() + ", " + CURRENT_BUSINESS.getState() + ", " + CURRENT_BUSINESS.getPinCode()).setFontSize(14).setPaddingLeft(10f)).setBorder(Border.NO_BORDER));


        // Adding Top Bar
        float[] topBarSize = {247.5f, 247.5f};
        Table topBar = new Table(topBarSize);
        topBar.setBorder(new SolidBorder(1));
        topBar.addCell(new Cell().add(new Paragraph("Invoice No.: " + String.format(Locale.getDefault(), "%06d", (ALL_INVOICES.size()+1))).setFontSize(12).setTextAlignment(TextAlignment.LEFT).setPaddingLeft(10)).setBorder(Border.NO_BORDER));
        topBar.addCell(new Cell().add(new Paragraph("Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).setFontSize(12).setTextAlignment(TextAlignment.RIGHT).setPaddingRight(10)).setBorder(Border.NO_BORDER));


        // Adding Customer Details
        float[] detailsChartSize = {90f, 405f};
        Table detailsChart = new Table(detailsChartSize);
        detailsChart.addCell(new Cell().add(new Paragraph("Name:").setBold().setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph(name.getText().toString()).setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph("Phone No.:").setBold().setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph(phone.getText().toString()).setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph("Email:").setBold().setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph(email.getText().toString()).setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph("Full Address:").setBold().setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph(address.getText().toString()).setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph("Pin-code:").setBold().setFontSize(12)).setBorder(Border.NO_BORDER));
        detailsChart.addCell(new Cell().add(new Paragraph(pin.getText().toString()).setFontSize(12)).setBorder(Border.NO_BORDER));

        // Adding Price Chart
        float[] priceChartSize = {55f, 170f, 100f, 100f, 70f};
        Table priceChart = new Table(priceChartSize);
        priceChart.setHorizontalAlignment(HorizontalAlignment.CENTER);
        priceChart.addCell(new Cell().add(new Paragraph("Sl. No.").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)));
        priceChart.addCell(new Cell().add(new Paragraph("Product Name").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)));
        priceChart.addCell(new Cell().add(new Paragraph("MRP").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)));
        priceChart.addCell(new Cell().add(new Paragraph("Quantity").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)));
        priceChart.addCell(new Cell().add(new Paragraph("Total").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)));
        for (Product p : selected.keySet()){
            double tmp = selected.get(p).getQuantity() * p.getPrice();
            priceChart.addCell(new Cell().add(new Paragraph(String.valueOf(i)).setFontSize(12).setTextAlignment(TextAlignment.CENTER)).setVerticalAlignment(VerticalAlignment.MIDDLE));
            priceChart.addCell(new Cell().add(new Paragraph(p.getName()).setFontSize(12)).setVerticalAlignment(VerticalAlignment.MIDDLE));
            priceChart.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "%.2f / %s", p.getPrice(), selected.get(p).getUnit()))).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            priceChart.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "%s %s", p.getType() == Product.SINGLE ? String.valueOf((int) selected.get(p).getQuantity()) : String.valueOf(selected.get(p).getQuantity()), selected.get(p).getUnit()))).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            priceChart.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "%.2f", tmp)).setFontSize(12).setTextAlignment(TextAlignment.CENTER)).setVerticalAlignment(VerticalAlignment.MIDDLE));
            i++;
            total += tmp;
        }
        priceChart.addCell(new Cell(1,4).add(new Paragraph("Grand Total").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)));
        priceChart.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "%.2f", total)).setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)));


        document.add(headerTable);
        document.add(topBar);
        document.add(new Paragraph("Customer Details").setBold().setUnderline().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        document.add(detailsChart);
        document.add(new Paragraph("Price Chart").setBold().setUnderline().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        document.add(priceChart);
        document.close();
        selected.clear();
        Toast.makeText(this, "Invoice Generated Successfully.", Toast.LENGTH_SHORT).show();
        return file;
    }

    private void uploadPDF(File file){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(getResources().getString(R.string.app_name)).child(ME.getId()).child("Businesses").child(CURRENT_BUSINESS.getId()).child("Invoices").child(invoiceName);
        Invoice invoice = new Invoice(invoiceName, new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()), file.getAbsolutePath(), phone.getText().toString(), name.getText().toString(), email.getText().toString(), address.getText().toString(), pin.getText().toString(), total);
        startActivity(new Intent(this, PDFViewActivity.class).putExtra("PDF_VIEW_TYPE", PDFViewActivity.FROM_FILE).putExtra("PDF_PATH", invoice.getUrl()).putExtra("PDF_NAME", invoice.getName()));
        storageRef.putFile(Uri.fromFile(file))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storageRef.getDownloadUrl().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful() && task1.getResult() != null){
                                invoice.setUrl(task1.getResult().toString());
                                db.collection("Users").document(ME.getId()).collection("Businesses").document(CURRENT_BUSINESS.getId()).collection("Invoices").document(invoice.getName()).set(invoice);
                            }
                        });
                    }
        });
        finish();
    }

    private void updateInputs(Customer customer){
        name.setText(customer.getCustomerName());
        phone.setText(customer.getCustomerPhone());
        email.setText(customer.getCustomerEmail());
        address.setText(customer.getCustomerAddress());
        pin.setText(customer.getCustomerPin());
    }
}
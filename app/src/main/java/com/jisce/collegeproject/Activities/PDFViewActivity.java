package com.jisce.collegeproject.Activities;

import static com.jisce.collegeproject.App.CURRENT_BUSINESS;
import static com.jisce.collegeproject.App.ME;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.jisce.collegeproject.Fragments.HomeFragment;
import com.jisce.collegeproject.R;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class PDFViewActivity extends AppCompatActivity {
    public static final int FROM_URL = 0;
    public static final int FROM_FILE = 1;
    private static PDFView pdfView;
    private static ProgressBar loadingProgress;
    Toolbar toolbar;
    int type;
    String pdfPath, pdfName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);
        type = getIntent().getIntExtra("PDF_VIEW_TYPE", FROM_URL);
        pdfPath = getIntent().getStringExtra("PDF_PATH");
        pdfName = getIntent().getStringExtra("PDF_NAME");

        toolbar = findViewById(R.id.normalToolbar);
        toolbar.setTitle(pdfName);
        setSupportActionBar(toolbar);

        pdfView = findViewById(R.id.pdfView);
        loadingProgress = findViewById(R.id.loadingProgress);
        pdfView.enableAntialiasing(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (type == FROM_URL) {
            new RetrievePDFOnline().execute(pdfPath);
        } else if (type == FROM_FILE){
            loadingProgress.setVisibility(View.GONE);
            pdfView.fromFile(new File(pdfPath)).spacing(20).load();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pdfview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.download) {
            File file = new File(String.format("%s/%s/%s/%s/%s", Environment.getExternalStorageDirectory(), getString(R.string.app_name), ME.getName(), CURRENT_BUSINESS.getName(), pdfName));
            if (type == FROM_URL) {
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfPath));
                request.setTitle(pdfName);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationUri(Uri.fromFile(file));
                long reference = manager.enqueue(request);

                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (reference == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                            try {
                                Toast.makeText(context, pdfName + " downloaded successfully.", Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            } else if (type == FROM_FILE) {
                try {
                    new File(Objects.requireNonNull(file.getParent())).mkdirs();
                    FileUtils.copyFile(new File(pdfPath), file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private static class RetrievePDFOnline extends AsyncTask<String, Void, InputStream> {
        public RetrievePDFOnline() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgress.setProgress(50);
            loadingProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                Log.d("TAG", "doInBackground: "+urlConnection.getContentLength());
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            pdfView.fromStream(inputStream).load();
            loadingProgress.setVisibility(View.GONE);
        }
    }
}
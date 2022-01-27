package com.example.barcodereader;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button scan_btn;
    Button export_btn;
    ArrayList<String> codeList;
    String[][] exportList;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;

    FirebaseDatabase rootNode;
    DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        scan_btn = findViewById(R.id.scan_btn);
        export_btn = findViewById(R.id.export_btn);

        codeList = new ArrayList<>();

        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });

        export_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Code List ----> "+codeList);

                countFrequency(codeList);
            }
        });
    }

    public void countFrequency(ArrayList<String> list){
        rootNode = FirebaseDatabase.getInstance();
        dbReference = rootNode.getReference("codes");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("DATE & TIME ---> "+dtf.format(now));

        int qty;

        for (int i = 0; i < list.size(); i++){
            qty = Collections.frequency(list, list.get(i));
            dbReference.child(dtf.format(now)).child(list.get(i)).child("quantity").setValue(qty);
        }

        Set<String> st = new HashSet<String>(list);
        for (String s : st)
            System.out.println(s + ": " + Collections.frequency(list, s));

        mRecyclerView.setAdapter(null);
    }

    private void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code...");
        integrator.initiateScan();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            // execute logic for the scanned barcode here
            if (result.getContents() != null) {
                // Adding codes to ArrayList

                mRecyclerView.setHasFixedSize(true);
                mLayoutManager = new LinearLayoutManager(this);
                mAdapter = new MainAdapter(codeList);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);
                codeList.add(result.getContents());

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Scan Successful");
                builder.setMessage(result.getContents());
                builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        scanCode();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(this, "No results to show", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
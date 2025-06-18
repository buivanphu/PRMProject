package com.example.productsaleapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.productsaleapp.R;

public class ConfirmShippingInfoActivity extends AppCompatActivity {
    TextView tvSummary;
    Button btnBack, btnProceed;

    String name, phone, address, city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_shipping_info);

        tvSummary = findViewById(R.id.tvSummary);
        btnBack = findViewById(R.id.btnBack);
        btnProceed = findViewById(R.id.btnProceed);

        Intent i = getIntent();
        name = i.getStringExtra("fullName");
        phone = i.getStringExtra("phone");
        address = i.getStringExtra("address");
        city = i.getStringExtra("city");

        tvSummary.setText("Tên: " + name + "\nSĐT: " + phone + "\nĐịa chỉ: " + address + "\nThành phố: " + city);

        btnBack.setOnClickListener(v -> finish()); // quay lại ShippingInfoActivity

        btnProceed.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentMethodActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("phone", phone);
            intent.putExtra("address", address);
            intent.putExtra("city", city);
            startActivity(intent);
        });
    }
}

package com.example.productsaleapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.productsaleapp.R;

public class ConfirmShippingInfoActivity extends AppCompatActivity {
    TextView tvSummary, tvTotalAmount;

    Button btnBack, btnProceed;

    String name, phone, address, city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_shipping_info);

        tvSummary = findViewById(R.id.tvSummary);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnBack = findViewById(R.id.btnBack);
        btnProceed = findViewById(R.id.btnProceed);

        // ✅ Nhận dữ liệu từ BillingActivity
        Intent i = getIntent();
        name = i.getStringExtra("fullName");
        phone = i.getStringExtra("phone");
        address = i.getStringExtra("address");
        city = i.getStringExtra("city");
        int userId = i.getIntExtra("userId", -1);
        double totalAmount = i.getDoubleExtra("totalAmount", 0);

        // ✅ Kiểm tra dữ liệu
        if (userId == -1 || totalAmount <= 0) {
            Toast.makeText(this, "Thiếu thông tin người dùng hoặc tổng tiền", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvSummary.setText("Tên: " + name + "\nSĐT: " + phone + "\nĐịa chỉ: " + address + "\nThành phố: " + city);
        tvTotalAmount.setText("Tổng tiền: " + String.format("%,.0f", totalAmount) + "đ");


        // ✅ Quay lại
        btnBack.setOnClickListener(v -> finish());

        // ✅ Chuyển sang PaymentMethodActivity kèm dữ liệu đầy đủ
        btnProceed.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentMethodActivity.class);
            intent.putExtra("fullName", name);
            intent.putExtra("phone", phone);
            intent.putExtra("address", address);
            intent.putExtra("city", city);
            intent.putExtra("userId", userId);
            intent.putExtra("totalAmount", totalAmount);
            startActivity(intent);
        });
    }
}

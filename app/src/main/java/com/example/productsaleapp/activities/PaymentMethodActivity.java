package com.example.productsaleapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.productsaleapp.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethodActivity extends AppCompatActivity {

    Button btnCash, btnBanking;
    String name, phone, address, city;
    int totalAmount = 0; // tổng tiền của đơn hàng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        btnCash = findViewById(R.id.btnCash);
        btnBanking = findViewById(R.id.btnBanking);

        // Nhận dữ liệu từ ConfirmShippingInfoActivity
        Intent i = getIntent();
        name = i.getStringExtra("name");
        phone = i.getStringExtra("phone");
        address = i.getStringExtra("address");
        city = i.getStringExtra("city");
        totalAmount = i.getIntExtra("totalAmount", 0);

        // ⚙️ 1. Thanh toán khi nhận hàng
        btnCash.setOnClickListener(v -> {
            Intent intent = new Intent(this, BillingActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("phone", phone);
            intent.putExtra("address", address);
            intent.putExtra("city", city);
            intent.putExtra("paymentMethod", "cash");
            intent.putExtra("totalAmount", totalAmount);
            startActivity(intent);
        });

        // ⚙️ 2. Thanh toán qua PayOS
        btnBanking.setOnClickListener(v -> {
            String url = "http://10.0.2.2:8080/ProductAPI/payos".trim();
            // Thay bằng đúng IP và tên bạn

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String checkoutUrl = jsonObject.getString("checkoutUrl");

                            // Mở trình duyệt đến PayOS
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                            startActivity(browserIntent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Lỗi phản hồi từ PayOS", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Lỗi kết nối tới server", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);
                    params.put("phone", phone);
                    params.put("address", address);
                    params.put("city", city);
                    params.put("totalAmount", String.valueOf(totalAmount));
                    return params;
                }
            };

            queue.add(stringRequest);
        });
    }
}

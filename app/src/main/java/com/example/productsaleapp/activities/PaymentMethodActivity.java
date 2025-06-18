package com.example.productsaleapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.productsaleapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentMethodActivity extends AppCompatActivity {

    private String fullName, phone, address, city;
    private int userId;
    private double totalAmount;

    private Button btnBanking, btnCash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        // ✅ Lấy dữ liệu từ Intent (KHÔNG khai báo lại biến)
        Intent intent = getIntent();
        fullName = intent.getStringExtra("fullName");
        phone = intent.getStringExtra("phone");
        address = intent.getStringExtra("address");
        city = intent.getStringExtra("city");
        userId = intent.getIntExtra("userId", -1);
        totalAmount = intent.getDoubleExtra("totalAmount", 0);

        // ✅ Kiểm tra dữ liệu
        if (userId == -1 || totalAmount <= 0) {
            Toast.makeText(this, "Thiếu thông tin thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Ánh xạ nút
        btnBanking = findViewById(R.id.btnBanking);
        btnCash = findViewById(R.id.btnCash);

        // ✅ Sự kiện nút thanh toán qua PayOS
        btnBanking.setOnClickListener(v -> createOrderWithPayOS());

        // ✅ Sự kiện nút thanh toán khi nhận hàng (COD)
        btnCash.setOnClickListener(v -> {
            Toast.makeText(this, "Đặt hàng thành công (Thanh toán khi nhận hàng)", Toast.LENGTH_SHORT).show();
            // Bạn có thể chuyển sang trang xác nhận đơn hàng nếu muốn
        });
    }

    private void createOrderWithPayOS() {
        String url = "https://webhook.productfpt.id.vn/ProductAPI/payos";

        // ✅ Tạo JSON gửi đi
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("fullName", fullName);
            jsonBody.put("phone", phone);
            jsonBody.put("address", address);
            jsonBody.put("city", city);
            jsonBody.put("userId", userId);
            jsonBody.put("totalAmount", totalAmount);
            jsonBody.put("description", "Thanh toán đơn hàng của " + fullName);
            jsonBody.put("returnUrl", "productsaleapp://return"); // ✅ deep link để quay lại app

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Gửi yêu cầu POST bằng Volley
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            String checkoutUrl = response.getString("checkoutUrl");
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                            startActivity(browserIntent);
                        } else {
                            Toast.makeText(this, "Tạo đơn hàng thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Phản hồi không hợp lệ từ máy chủ", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi kết nối API: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        // ✅ Gửi request
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}

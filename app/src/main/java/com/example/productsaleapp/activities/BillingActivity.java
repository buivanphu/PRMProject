package com.example.productsaleapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.productsaleapp.R;
import com.example.productsaleapp.models.CartItem;
import com.example.productsaleapp.models.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class BillingActivity extends AppCompatActivity {

    private TextView tvTotal;
    private Button btnPlaceOrder;
    private List<CartItem> cartItems = new ArrayList<>();
    private int userId;
    private double finalTotal = 0; // Biến lớp để dùng lại khi chuyển màn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy userId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCartFromAPI();

        // Xử lý khi người dùng nhấn tiếp tục đặt hàng
        btnPlaceOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfirmShippingInfoActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("totalAmount", finalTotal);
            startActivity(intent);
        });
    }

    private void loadCartFromAPI() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/ProductAPI/cart?userId=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                JSONArray arr = new JSONArray(response.toString());
                cartItems.clear();

                double total = 0;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Product p = new Product();
                    p.id = obj.getInt("productId");
                    p.name = obj.getString("name");
                    p.price = obj.getDouble("price");
                    p.image_url = obj.getString("image_url");

                    int quantity = obj.getInt("quantity");
                    cartItems.add(new CartItem(p, quantity));
                    total += p.price * quantity;
                }

                finalTotal = total;
                runOnUiThread(() -> {
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                    tvTotal.setText("Tổng tiền: " + format.format(finalTotal));
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Hàm placeOrder có thể giữ lại nếu vẫn dùng cho xác nhận đơn hàng riêng
}

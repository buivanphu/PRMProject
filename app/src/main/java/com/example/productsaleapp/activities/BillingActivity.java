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
    private int userId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        loadCartFromAPI();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
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

                double finalTotal = total;
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

    private void placeOrder() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/ProductAPI/order");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("userId", userId);
                JSONArray items = new JSONArray();
                for (CartItem item : cartItems) {
                    JSONObject obj = new JSONObject();
                    obj.put("productId", item.product.id);
                    obj.put("quantity", item.quantity);
                    obj.put("price", item.product.price);
                    items.put(obj);
                }
                json.put("items", items);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                JSONObject responseJson = new JSONObject(result.toString());
                boolean success = responseJson.getBoolean("success");

                if (success) {
                    int orderId = responseJson.optInt("orderId", 0); // Nếu servlet trả về mã đơn
                    double total = 0;
                    StringBuilder productNames = new StringBuilder();
                    for (CartItem item : cartItems) {
                        total += item.quantity * item.product.price;
                        productNames.append(item.product.name).append(", ");
                    }

                    // Xoá dấu , cuối cùng
                    if (productNames.length() > 0) productNames.setLength(productNames.length() - 2);

                    double finalTotal = total;
                    String finalProductNames = productNames.toString();

                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, OrderConfirmationActivity.class);
                        intent.putExtra("orderId", String.valueOf(orderId));
                        intent.putExtra("productNames", finalProductNames);
                        intent.putExtra("total", finalTotal);
                        startActivity(intent);
                        finish(); // Đóng màn hình hiện tại
                    });

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Đặt hàng thất bại", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi kết nối API", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}

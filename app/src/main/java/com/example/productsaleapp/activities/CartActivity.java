// ✅ File: CartActivity.java (Android Studio)
// ➤ Phiên bản đầy đủ có reloadCart() và loadCartFromAPI()

package com.example.productsaleapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.productsaleapp.R;
import com.example.productsaleapp.adapters.CartAdapter;
import com.example.productsaleapp.models.CartItem;
import com.example.productsaleapp.models.Product;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

public class CartActivity extends AppCompatActivity {
    private RecyclerView rvCart;
    private TextView tvTotalCart;
    private List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter adapter;
    private Button btnCheckout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        tvTotalCart = findViewById(R.id.tvTotalCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Mở đúng màn hình nhập thông tin
            Intent intent = new Intent(CartActivity.this, ShippingInfoActivity.class);
            startActivity(intent);
        });



        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cartItems, this::updateTotalAmount);
        rvCart.setAdapter(adapter);

        loadCartFromAPI();
    }

    public void reloadCart() {
        loadCartFromAPI();
    }

    private void loadCartFromAPI() {
        new Thread(() -> {
            try {
                int userId = 1;
                URL url = new URL("http://10.0.2.2:8080/ProductAPI/cart?userId=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONArray arr = new JSONArray(response.toString());
                cartItems.clear();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Product p = new Product();
                    p.id = obj.getInt("productId");
                    p.name = obj.getString("name");
                    p.price = obj.getDouble("price");
                    p.image_url = obj.getString("image_url");

                    CartItem item = new CartItem(p, obj.getInt("quantity"));
                    cartItems.add(item);
                }

                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    updateTotalAmount();
                    Log.d("CartActivity", "Giỏ hàng đã reload");
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalCart.setText("Tổng: " + format.format(total));
    }
}

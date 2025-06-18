package com.example.productsaleapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.productsaleapp.R;
import com.example.productsaleapp.adapters.ProductAdapter;
import com.example.productsaleapp.models.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private final String API_URL = "http://10.0.2.2:8080/ProductAPI/products";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        rvProducts = findViewById(R.id.rvProducts);

        Button btnOpenCart = findViewById(R.id.btnOpenCart);
        btnOpenCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        // ✅ GỌI LOAD PRODUCT Ở ĐÂY!
        loadProducts();
    }

    private void loadProducts() {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                JSONArray arr = new JSONArray(result.toString());
                ArrayList<Product> productList = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Product p = new Product();
                    p.id = obj.getInt("id");
                    p.name = obj.getString("name");
                    p.description = obj.getString("description");
                    p.price = obj.getDouble("price");
                    p.image_url = obj.getString("image_url");
                    productList.add(p);
                }

                runOnUiThread(() -> {
                    rvProducts.setLayoutManager(new LinearLayoutManager(this));
                    rvProducts.setAdapter(new ProductAdapter(productList));
                });

            } catch (Exception e) {
                Log.e("API_ERROR", e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}


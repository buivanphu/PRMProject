package com.example.productsaleapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.productsaleapp.R;
import com.example.productsaleapp.adapters.OrderAdapter;
import com.example.productsaleapp.models.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class OrderListActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> orderList = new ArrayList<>();
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        recyclerOrders = findViewById(R.id.recyclerViewOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));

        orderAdapter = new OrderAdapter(orderList);
        recyclerOrders.setAdapter(orderAdapter);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        if (userId != -1) {
            fetchOrders(userId);
        } else {
            Toast.makeText(this, "Không tìm thấy userId.", Toast.LENGTH_SHORT).show();
        }

        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderListActivity.this, ProductListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void fetchOrders(int userId) {
        String url = "https://webhook.productfpt.id.vn/ProductAPI/orderlist?userId=" + userId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> parseOrderList(response),
                error -> Toast.makeText(OrderListActivity.this, "Lỗi khi tải danh sách đơn hàng", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void parseOrderList(JSONArray response) {
        try {
            orderList.clear();
            boolean hasPaidOrder = false;

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                int id = obj.getInt("id");
                String date = obj.getString("orderDate");
                double total = obj.getDouble("totalAmount");
                String status = obj.getString("status");

                orderList.add(new Order(id, status, total, date));

                if ("PAID".equals(status)) {
                    hasPaidOrder = true;
                }
            }

            orderAdapter.notifyDataSetChanged();

            if (hasPaidOrder) {
                clearCartAfterPayment(userId);
            }

        } catch (JSONException e) {
            Toast.makeText(this, "Lỗi phân tích dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearCartAfterPayment(int userId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/ProductAPI/cart?userId=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d("OrderListActivity", "✅ Đã xóa giỏ hàng sau khi thanh toán");
                } else {
                    Log.e("OrderListActivity", "❌ Xóa giỏ hàng thất bại: " + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

// ✅ File: ProductDetailActivity.java (Android Studio)
// ➤ Mục tiêu: Lấy product.id từ intent, gọi API /products/{id} và thêm vào giỏ với đúng productId

package com.example.productsaleapp.activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.productsaleapp.R;
import com.example.productsaleapp.models.Product;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProductDetailActivity extends AppCompatActivity {
    TextView tvName, tvDescription, tvPrice;
    ImageView imgProduct;
    Button btnAddToCart;
    EditText edtQuantity;
    int productId;
    Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        imgProduct = findViewById(R.id.imgProduct);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        edtQuantity = findViewById(R.id.edtQuantity);

        productId = getIntent().getIntExtra("id", -1);

        if (productId != -1) {
            loadProductDetail(productId);
        }
    }

    private void loadProductDetail(int productId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/ProductAPI/products/" + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject obj = new JSONObject(response.toString());
                product = new Product();
                product.id = obj.getInt("id");
                product.name = obj.getString("name");
                product.description = obj.getString("description");
                product.price = obj.getDouble("price");
                product.image_url = obj.getString("image_url");

                runOnUiThread(() -> {
                    tvName.setText(product.name);
                    tvDescription.setText(product.description);
                    tvPrice.setText(product.price + " VND");
                    Picasso.get().load(product.image_url).into(imgProduct);

                    btnAddToCart.setOnClickListener(v -> {
                        String qtyText = edtQuantity.getText().toString().trim();
                        if (qtyText.isEmpty()) {
                            Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int quantity = Integer.parseInt(qtyText);
                        addToCartAPI(product.id, quantity);
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void addToCartAPI(int productId, int quantity) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/ProductAPI/cart");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                int userId = 1; // ⚠️ Cố định tạm thời
                String postData = "userId=" + userId + "&productId=" + productId + "&quantity=" + quantity;

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

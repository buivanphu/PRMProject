// ✅ File: CartAdapter.java (Android Studio)
// ➤ Cập nhật để xoá item ngay và hiển thị thông báo

package com.example.productsaleapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.productsaleapp.R;
import com.example.productsaleapp.activities.CartActivity;
import com.example.productsaleapp.models.CartItem;
import com.example.productsaleapp.models.Product;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private final Runnable onUpdate;

    public CartAdapter(List<CartItem> cartItems, Runnable onUpdate) {
        this.cartItems = cartItems;
        this.onUpdate = onUpdate;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        Product product = item.product;
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        holder.tvName.setText(product.name);
        holder.tvQuantity.setText("Số lượng: " + item.quantity);
        holder.tvTotalPrice.setText("Tổng: " + vnFormat.format(item.getTotalPrice()));

        holder.btnRemove.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    int userId = 1;
                    String urlStr = "http://10.0.2.2:8080/ProductAPI/cart?userId=" + userId + "&productId=" + product.id;
                    Log.d("CartAdapter", "Gửi DELETE đến: " + urlStr);
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("DELETE");

                    int responseCode = conn.getResponseCode();
                    Log.d("CartAdapter", "Response code: " + responseCode);

                    if (responseCode == 200) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(v.getContext(), "Đã xoá sản phẩm", Toast.LENGTH_SHORT).show();

                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                cartItems.remove(pos);
                                notifyItemRemoved(pos);
                                onUpdate.run(); // cập nhật tổng tiền
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvTotalPrice;
        Button btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

package com.example.productsaleapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.productsaleapp.R;
import com.example.productsaleapp.models.Order;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;

    public OrderAdapter(List<Order> orders) {
        this.orderList = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Mã đơn hàng: #" + order.id);
        holder.tvOrderDate.setText("Ngày đặt: " + order.orderDate);
        holder.tvOrderTotal.setText("Tổng tiền: " +
                NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(order.totalAmount));
        holder.tvOrderStatus.setText("Trạng thái: " +
                (order.status.equalsIgnoreCase("PAID") ? "ĐÃ THANH TOÁN ✅" : "CHỜ THANH TOÁN ❌"));

        int statusColor = order.status.equalsIgnoreCase("PAID") ?
                android.R.color.holo_green_dark : android.R.color.holo_red_dark;
        holder.tvOrderStatus.setTextColor(holder.itemView.getContext().getResources().getColor(statusColor));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderTotal, tvOrderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}

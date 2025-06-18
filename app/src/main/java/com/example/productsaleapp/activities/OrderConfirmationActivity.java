    package com.example.productsaleapp.activities;

    import android.Manifest;
    import android.app.NotificationChannel;
    import android.app.NotificationManager;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.widget.Button;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.app.NotificationCompat;
    import androidx.core.app.NotificationManagerCompat;
    import androidx.core.content.ContextCompat;

    import com.example.productsaleapp.R;

    import java.text.NumberFormat;
    import java.util.Locale;

    public class OrderConfirmationActivity extends AppCompatActivity {

        private TextView tvOrderCode, tvTotal, tvProductNames;
        private Button btnHome, btnViewOrders;

        private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
        private String orderId;
        private double total;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_order_confirmation);

            tvOrderCode = findViewById(R.id.tvOrderCode);
            tvTotal = findViewById(R.id.tvTotal);
            tvProductNames = findViewById(R.id.tvProductNames);
            btnHome = findViewById(R.id.btnHome);
            btnViewOrders = findViewById(R.id.btnViewOrders);

            // Nhận dữ liệu từ Intent
            Intent intent = getIntent();
            orderId = intent.getStringExtra("orderId");
            String productNames = intent.getStringExtra("productNames");
            total = intent.getDoubleExtra("total", 0);

            // Gán dữ liệu
            tvOrderCode.setText("Mã đơn hàng: " + orderId);
            tvTotal.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(total));
            tvProductNames.setText("Sản phẩm: " + productNames);

            // Quay về trang chủ
            btnHome.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            });

            // Xem đơn hàng – có thể comment nếu chưa làm
            btnViewOrders.setOnClickListener(v -> {
                // startActivity(new Intent(this, OrderListActivity.class));
            });

            // Notification
            createNotificationChannel();
            requestNotificationPermissionIfNeeded();
        }

        private void showNotification() {
            String content = "Mã đơn: " + orderId + " - Tổng tiền: " +
                    NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(total);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "order_channel")
                    .setSmallIcon(R.drawable.ic_order) // Đổi icon nếu chưa có ic_order
                    .setContentTitle("Đặt hàng thành công")
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED) {

                manager.notify(1001, builder.build());
            }

        }

        private void requestNotificationPermissionIfNeeded() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
                    // ✅ Được cấp quyền -> gửi thông báo
                    showNotification();
                } else {
                    // ❌ Chưa được cấp -> xin quyền
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            1001);
                }
            } else {
                // ✅ Android < 13 -> không cần xin quyền
                showNotification();
            }
        }

            private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = "order_channel";
                CharSequence name = "Order Notifications";
                String description = "Thông báo đơn hàng";
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel = new NotificationChannel(channelId, name, importance);
                channel.setDescription(description);

                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) manager.createNotificationChannel(channel);
            }
        }

        // Xử lý kết quả xin quyền
        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            if (requestCode == 1001 &&
                    grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showNotification();
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

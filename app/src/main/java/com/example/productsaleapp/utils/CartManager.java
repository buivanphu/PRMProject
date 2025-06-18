package com.example.productsaleapp.utils;

import com.example.productsaleapp.models.CartItem;
import com.example.productsaleapp.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final List<CartItem> cartItems = new ArrayList<>();

    public static void addToCart(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.product.id == product.id) {
                item.quantity += quantity;
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
    }

    public static List<CartItem> getCartItems() {
        return cartItems;
    }

    public static void removeItem(int productId) {
        cartItems.removeIf(item -> item.product.id == productId);
    }

    public static double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public static void clearCart() {
        cartItems.clear();
    }
}

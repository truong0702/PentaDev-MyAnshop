package com.example.petshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var cartItems = listOf<CartItem>()
    private var totalPrice = 0.0
    
    private lateinit var edtCustomerName: TextInputEditText
    private lateinit var edtAddress: TextInputEditText
    private lateinit var edtPhone: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val tvOrderSummary: TextView = findViewById(R.id.tv_order_summary)
        edtCustomerName = findViewById(R.id.edt_customer_name)
        edtAddress = findViewById(R.id.edt_address)
        edtPhone = findViewById(R.id.edt_phone)
        val btnPlaceOrderCod: Button = findViewById(R.id.btn_place_order_cod)
        val btnPayOS: Button = findViewById(R.id.btn_payos)

        loadUserData()
        fetchCartItemsAndDisplaySummary(tvOrderSummary)

        btnPlaceOrderCod.setOnClickListener {
            placeOrder()
        }
        
        btnPayOS.setOnClickListener {
            Toast.makeText(this, "Chức năng thanh toán bằng PayOS đang được phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener {
            if(it != null && it.exists()){
                edtCustomerName.setText(it.getString("name"))
                edtAddress.setText(it.getString("address"))
                edtPhone.setText(it.getString("phone"))
            }
        }
    }

    private fun fetchCartItemsAndDisplaySummary(summaryView: TextView) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("carts").document(userId).collection("items").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
                cartItems = snapshot.toObjects(CartItem::class.java)
                totalPrice = cartItems.sumOf { it.price * it.quantity }
                summaryView.text = "Order Summary: ${cartItems.size} items, Total: $${String.format("%.2f", totalPrice)}"
            }
    }

    private fun placeOrder() {
        val userId = auth.currentUser?.uid ?: return
        val customerName = edtCustomerName.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val phone = edtPhone.text.toString().trim()

        if (customerName.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all shipping details", Toast.LENGTH_SHORT).show()
            return
        }

        val orderRef = db.collection("orders").document()

        val order = Order(
            orderId = orderRef.id,
            userId = userId,
            items = cartItems,
            totalPrice = totalPrice,
            customerName = customerName,
            address = address,
            phone = phone,
            status = "Pending"
        )

        orderRef.set(order).addOnSuccessListener {
            clearCart(userId)
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, OrderHistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearCart(userId: String) {
        val cartItemsRef = db.collection("carts").document(userId).collection("items")
        cartItems.forEach { item ->
            cartItemsRef.document(item.id).delete()
        }
    }
}

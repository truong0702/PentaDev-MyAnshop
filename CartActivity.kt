package com.example.petshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity(), CartAdapter.CartItemListener {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()
    private lateinit var tvTotalPrice: TextView
    private lateinit var emptyCartView: LinearLayout
    private lateinit var btnCheckout: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recycler_view_cart)
        tvTotalPrice = findViewById(R.id.tv_total_price)
        emptyCartView = findViewById(R.id.empty_cart_view)
        btnCheckout = findViewById(R.id.btn_checkout)
        progressBar = findViewById(R.id.cart_progress_bar)
        
        findViewById<Button>(R.id.btn_shop_now).setOnClickListener {
            finish() // Go back to home
        }

        setupRecyclerView()
        fetchCartItems()

        btnCheckout.setOnClickListener {
            if (cartList.isNotEmpty()) {
                startActivity(Intent(this, CheckoutActivity::class.java))
            } else {
                Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = cartAdapter
    }

    private fun fetchCartItems() {
        showLoading(true)
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showLoading(false)
            checkEmptyState()
            return
        }

        db.collection("carts").document(userId).collection("items")
            .addSnapshotListener { snapshots, e ->
                showLoading(false)
                if (e != null) {
                    checkEmptyState()
                    return@addSnapshotListener
                }

                cartList.clear()
                snapshots?.mapNotNullTo(cartList) { it.toObject(CartItem::class.java) }
                
                cartAdapter.notifyDataSetChanged()
                updateTotalPrice()
                checkEmptyState()
            }
    }

    private fun updateTotalPrice() {
        val total = cartList.sumOf { it.price * it.quantity }
        tvTotalPrice.text = "Total: $${String.format("%.2f", total)}"
    }

    private fun checkEmptyState() {
        val isEmpty = cartList.isEmpty()
        emptyCartView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        btnCheckout.isEnabled = !isEmpty
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        emptyCartView.visibility = View.GONE
    }

    // --- CartItemListener Implementation ---

    override fun onIncreaseClicked(item: CartItem) {
        updateQuantityInFirestore(item, 1)
    }

    override fun onDecreaseClicked(item: CartItem) {
        if (item.quantity > 1) {
            updateQuantityInFirestore(item, -1)
        } else {
            removeItemFromFirestore(item)
        }
    }

    private fun getCartItemRef(itemId: String) = auth.currentUser?.uid?.let {
        db.collection("carts").document(it).collection("items").document(itemId)
    }

    private fun updateQuantityInFirestore(item: CartItem, change: Long) {
        getCartItemRef(item.id)?.update("quantity", item.quantity + change)
    }

    private fun removeItemFromFirestore(item: CartItem) {
        getCartItemRef(item.id)?.delete()
    }
}

package com.example.petshop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private val cartList: List<CartItem>,
    private val listener: CartItemListener
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    interface CartItemListener {
        fun onIncreaseClicked(item: CartItem)
        fun onDecreaseClicked(item: CartItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartList[position]
        holder.bind(cartItem)
    }

    override fun getItemCount(): Int = cartList.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.tv_cart_product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.tv_cart_product_price)
        private val productImage: ImageView = itemView.findViewById(R.id.img_cart_product)
        private val quantity: TextView = itemView.findViewById(R.id.tv_quantity)
        private val increaseBtn: Button = itemView.findViewById(R.id.btn_increase_quantity)
        private val decreaseBtn: Button = itemView.findViewById(R.id.btn_decrease_quantity)

        fun bind(cartItem: CartItem) {
            productName.text = cartItem.name
            productPrice.text = "$${String.format("%.2f", cartItem.price)}"
            quantity.text = cartItem.quantity.toString()

            if (cartItem.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context).load(cartItem.imageUrl).centerCrop().into(productImage)
            } else {
                productImage.setImageResource(R.drawable.ic_launcher_background)
            }

            increaseBtn.setOnClickListener {
                listener.onIncreaseClicked(cartItem)
            }

            decreaseBtn.setOnClickListener {
                listener.onDecreaseClicked(cartItem)
            }
        }
    }
}

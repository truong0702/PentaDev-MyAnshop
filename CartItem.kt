package com.example.petshop

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    @DocumentId val id: String = "", // Renamed from productId to id to avoid conflict
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    var quantity: Long = 0
) : Parcelable

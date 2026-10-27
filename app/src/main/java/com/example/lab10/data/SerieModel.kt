package com.example.lab10.data

import com.google.gson.annotations.SerializedName

data class SerieModel(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String = "",
    @SerializedName("release_date") var release_date: String? = null,
    @SerializedName("rating") var rating: Int? = null,
    @SerializedName("category") var category: String? = null
)
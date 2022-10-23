package net.uoneweb.mapbox.uploader.mapbox.repository

import com.fasterxml.jackson.annotation.JsonProperty

data class TilesetSourceResponse(
    val id: String,
    val files: Int,
    val size: Int,
    @JsonProperty("size_nice")
    val sizeNice: String? = null
)

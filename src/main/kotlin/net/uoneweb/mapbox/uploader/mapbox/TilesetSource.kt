package net.uoneweb.mapbox.uploader.mapbox

import com.fasterxml.jackson.annotation.JsonProperty

data class TilesetSource(
    val id: TilesetSourceId,
    val files: Int,
    val size: Int,
    @JsonProperty("size_nice") val sizeNice: String? = null
)


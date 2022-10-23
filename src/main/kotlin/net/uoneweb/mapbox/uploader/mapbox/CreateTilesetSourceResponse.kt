package net.uoneweb.mapbox.uploader.mapbox

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateTilesetSourceResponse(
    @JsonProperty("file_size") val fileSize: Int,
    val files: Int,
    val id: String,
    @JsonProperty("source_size") val sourceSize: Int
)

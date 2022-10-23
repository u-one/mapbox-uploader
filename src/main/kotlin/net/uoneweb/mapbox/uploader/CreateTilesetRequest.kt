package net.uoneweb.mapbox.uploader

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateTilesetRequest(
    @JsonProperty("recipe") val recipe: Recipe,
    val name: String,
    val description: String = "",
    val attribution: List<String> = listOf()
)
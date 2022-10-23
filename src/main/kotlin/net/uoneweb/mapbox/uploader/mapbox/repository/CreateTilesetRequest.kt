package net.uoneweb.mapbox.uploader.mapbox.repository

import com.fasterxml.jackson.annotation.JsonProperty
import net.uoneweb.mapbox.uploader.mapbox.Recipe

data class CreateTilesetRequest(
    @JsonProperty("recipe") val recipe: Recipe,
    val name: String,
    val description: String = "",
    val attribution: List<String> = listOf()
)
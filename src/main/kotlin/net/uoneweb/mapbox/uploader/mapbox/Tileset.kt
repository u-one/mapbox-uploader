package net.uoneweb.mapbox.uploader.mapbox

import java.net.URI

data class Tileset(
    val type: String,
    val center: Array<Double>,
    val created: String,
    val description: String,
    val filesize: Int,
    val id: String,
    val modified: String,
    val name: String,
    val visibility: String,
    val status: String
) {

    fun getTilesetId(): String {
        val paths = URI(id).path.split("/")
        return paths?.get(2) ?: ""
    }
}

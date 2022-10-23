package net.uoneweb.mapbox.uploader.mapbox

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.nio.file.Paths

data class TilesetSource(
    val id: String,
    val files: Int,
    val size: Int,
    @JsonProperty("size_nice") val sizeNice: String? = null
) {
    fun getSimpleId(): String {
        val uri = URI(id)
        val path = Paths.get(uri.path)
        return path.fileName.toString()
    }
}

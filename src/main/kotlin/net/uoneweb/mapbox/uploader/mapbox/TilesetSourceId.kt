package net.uoneweb.mapbox.uploader.mapbox

import java.util.regex.Pattern

data class TilesetSourceId(private val id: String) {
    // limited to 32 characters.
    // allowed special characters are - and _.
    private val SIMPLE_ID_PATTERN = Pattern.compile("[\\p{Alnum}_-]{1,32}").toRegex()
    private val CANONICAL_ID_PATTERN = Pattern.compile("mapbox://tileset-source/(.+)/(.+)")

    init {
        val simpleId = getSimpleId()
        if (!simpleId.matches(SIMPLE_ID_PATTERN)) {
            throw IllegalArgumentException()
        }
    }

    fun getSimpleId(): String {
        id.split(
            ".", "/"
        ).last().let {
            return it
        }
    }

    fun getUserName(): String {
        val matcher = CANONICAL_ID_PATTERN.matcher(id)
        if (matcher.find()) {
            return matcher.group(2) ?: ""
        }
        val values = id.split(".")
        if (values.size > 1) {
            return values[0]
        }
        return ""
    }

    fun getCanonicalId(): String {
        val matcher = CANONICAL_ID_PATTERN.matcher(id)
        if (matcher.find()) {
            return id
        }
        return String.format("mapbox://tileset-source/%s/%s", getUserName(), getSimpleId())
    }
}
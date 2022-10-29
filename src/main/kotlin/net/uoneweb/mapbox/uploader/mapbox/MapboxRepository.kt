package net.uoneweb.mapbox.uploader.mapbox

interface MapboxRepository {
    fun createTileset(tilesetId: String, tilesetName: String, recipe: Recipe)

    fun updateTilesetRecipe(tilesetId: String, recipe: Recipe)

    fun listTileset(): List<Tileset>

    fun deleteTileset(tilesetId: String)

    fun publishTileset(tilesetId: String): String
}
package net.uoneweb.mapbox.uploader.mapbox.repository

import net.uoneweb.mapbox.uploader.mapbox.Recipe
import net.uoneweb.mapbox.uploader.mapbox.Tileset
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
import net.uoneweb.mapbox.uploader.mapbox.TilesetSourceId

interface MapboxRepository {

    fun listTilesetSources(): List<TilesetSource>

    fun getTilesetSource(tilesetSourceId: TilesetSourceId): TilesetSource?

    fun createTilesetSource(tilesetSourceId: TilesetSourceId, body: String): TilesetSource?

    fun updateTilesetSource(tilesetSourceId: TilesetSourceId, body: String): TilesetSource?

    fun deleteTilesetSource(tilesetSourceId: TilesetSourceId)

    fun createTileset(tilesetId: String, tilesetName: String, recipe: Recipe)

    fun updateTilesetRecipe(tilesetId: String, recipe: Recipe)

    fun listTileset(): List<Tileset>

    fun deleteTileset(tilesetId: String)

    fun publishTileset(tilesetId: String): String

    fun listJobs(tilesetId: String)

    fun getJob(tilesetId: String, jobId: String)
}
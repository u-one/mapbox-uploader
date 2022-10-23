package net.uoneweb.mapbox.uploader

import net.uoneweb.mapbox.uploader.mapbox.Tileset
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource

interface MapboxRepository {

    fun listStyles()

    fun listTilesetSources(): List<TilesetSource>

    fun getTilesetSource(tilesetSourceId: String): TilesetSource?

    fun createTilesetSource(tilesetSourceId: String, body: Any): TilesetSource?

    fun updateTilesetSource(tilesetSourceId: String, body: Any): TilesetSource?

    fun deleteTilesetSource(tilesetSourceId: String)

    fun createTileset(tilesetId: String, tilesetName: String, recipe: Recipe)

    fun listTileset(): List<Tileset>

    fun deleteTileset(tilesetId: String)

    fun publishTileset(tilesetId: String): String

    fun listJobs(tilesetId: String)

    fun getJob(tilesetId: String, jobId: String)
}